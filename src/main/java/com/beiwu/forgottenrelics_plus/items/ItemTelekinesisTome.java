package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityThaumaturgeBoss;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import vazkii.botania.common.entity.GaiaGuardianEntity;

/**
 * 预言之典（Tome of Predestiny），注册名 {@code tome_of_predestiny}，
 * 1.7.10 原版 {@code ItemTelekinesisTome}。
 *
 * <p><b>选用依据</b>：同目录下还有一份 {@code ItemTelekinesisTomeLegacy}，但
 * 1.7.10 的 {@code Main} 里注册的是 {@code new ItemTelekinesisTome()} 并赋给
 * {@code Main.itemTelekinesisTome}（{@code Main.java} 第 257 / 293 行），
 * {@code ItemTelekinesisTomeLegacy} <b>没有被任何地方引用</b>；研究词条
 * {@code TelekinesisTome} 的图标、配方产物用的也都是 {@code Main.itemTelekinesisTome}。
 * 所以这里以「Final 版」的 {@code ItemTelekinesisTome} 为准，Legacy 不移植。
 *
 * <p>原版逻辑（{@code ItemTelekinesisTome}）：
 * <ol>
 *   <li>物品堆叠上限 1、稀有度 EPIC，拉弓姿态（{@code EnumAction.bow}、可用时长 72000），
 *       {@code getWarp} 返回 <b>4</b>；</li>
 *   <li>每个玩家一份静态状态 {@code globalTomeMap}：{@code ticksTillExpire}（默认 0）、
 *       {@code ticksCooldown}（默认 0）、{@code target}（实体 id，默认 -1）、
 *       {@code dist}、{@code reDist}（默认 -1.0）。{@code onUpdate} 每 tick 递减前两者，
 *       并在 {@code ticksTillExpire == 0} 时清空目标；</li>
 *   <li><b>念力抓取</b>（{@code onUsingTickAlt}，按住右键时每 tick 执行，受
 *       {@code ticksCooldown} 门控）：
 *     <ol>
 *       <li>若旧目标仍在，先 {@code getExistingTarget(.., 6.0)} 确认它还在视线前方；
 *           找不到就 {@code searchForTarget(.., 3.0)} 沿视线方向逐格找最近的活体；
 *           两处搜索的点位都是<b>累加</b>的（每轮 {@code target += look * distance} 且
 *           {@code y += 0.5}），这是 1.7.10 自身的行为，逐字保留；</li>
 *       <li>找到目标后从背包法杖抽 <b>风 6 + 秩序 8 厘 Vis</b>（= 0.14 点，乘
 *           {@code telekinesisTomeVisMult}）；抽不出来就这一 tick 什么都不做；</li>
 *       <li>目标清零坠落距离、施加 {@code 缓慢IV}（时长 2 tick、amplifier 3，仅当身上没有）；</li>
 *       <li>把目标往「玩家身体中心 + 视线 × 7.5，再抬高 0.5」拉（潜行时改为
 *           「玩家中心 + 视线 × 上次距离」，即保持距离不拉近）；
 *           方向向量长度 &gt; 1 时归一化，再乘 {@code vectorPower}：
 *           距离 &lt; 1.5 时 0.333，否则 0.667，距离 ≥ 8 时按 {@code dist / 8} 放大；
 *           <b>被拉目标的移动归服务端</b>，原版靠 {@code PlayerMotionUpdateMessage}
 *           同步玩家目标；</li>
 *       <li>每次成功施加念力，都往目标实体中心广播一次
 *           {@code TelekinesisParticleMessage}（2 颗紫色 wisp + 4 颗传送门粒子）；</li>
 *       <li>若目标是 {@code EntityThaumcraftBoss} / {@code EntityDoppleganger}
 *           （即 {@code isEntityBlacklistedFromTelekinesis}），Vis 已扣但直接返回，不施加任何速度；</li>
 *       <li>找到目标就把 {@code ticksTillExpire} 重置为 5。</li>
 *     </ol>
 *   </li>
 *   <li><b>闪电攻击</b>（{@code lightningAttack}，由左键触发）：
 *     <ol>
 *       <li>若 {@code SuperpositionHandler.isOnCoodown(player)}（共用施法冷却）为真，整段直接返回；</li>
 *       <li>目标 &lt;= 16 格且抽到 <b>风 80 + 秩序 50 + 火 200 厘 Vis</b>（= 3.3 点）时，
 *           连画 4 道自定义闪电（{@code imposeLightning}），在玩家处播 {@code thaumcraft:zap}，
 *           对目标造成 {@code DamageSourceTLightning} 的 {@code 16 + 24 × 随机} 伤害；</li>
 *       <li>若玩家潜行且抽到 <b>风 150 + 秩序 80 厘 Vis</b>（= 2.3 点），把目标沿玩家视线
 *           以 {@code (3.0, 1.5, 3.0)} 的倍率<b>抛开</b>（直接覆写速度），并清空锁定状态、
 *           把 {@code ticksCooldown} 设为 40；</li>
 *       <li>无论如何最后都 {@code setCasted(player, 10, true)}（10 tick 共用冷却 + 挥臂）。</li>
 *     </ol>
 *   </li>
 *   <li>左键入口 {@code leftClick} 只对<b>已锁定</b>的目标生效：目标必须仍能通过
 *       {@code getExistingTarget(.., 6.0)} 找到，否则什么都不发生。</li>
 * </ol>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code getMaxItemUseDuration} → {@code Item#getUseDuration}；{@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见
 *       {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。按本模组统一约定改成
 *       {@link FRRechargable} 的<b>物品自身充能</b>，用 {@link RechargeAccess#consumeCharge} 扣除；</li>
 *   <li>原版以玩家为键的静态 map {@code globalTomeMap} → 本类内部的
 *       {@code Map<UUID, TomeState>}（1.21.1 的 {@code Player} 实例会在维度切换/重登时被替换，
 *       用 UUID 更稳，与 {@link CooldownHelper} 同一考虑）；</li>
 *   <li>原版 {@code SuperpositionHandler} 的共用施法冷却 → {@link CooldownHelper}；</li>
 *   <li><b>不写任何自定义网络包</b>：{@code TelekinesisParticleMessage} 改为服务端
 *       {@link ServerLevel#sendParticles}；{@code LightningMessage} 改为服务端沿
 *       玩家→目标撒 {@link ParticleTypes#ELECTRIC_SPARK} 电弧（与千咒之诫同一方案）；
 *       {@code PlayerMotionUpdateMessage} 改为改完速度后置 {@code hurtMarked}，靠原版同步；</li>
 *   <li>音效 {@code thaumcraft:zap} → 原版等价物 {@link SoundEvents#FIREWORK_ROCKET_BLAST}
 *       （与千咒之诫的 {@code zap} 替代方案一致），并过 {@link SoundHelper#play} 统一压低音量；</li>
 *   <li>左键「闪电 / 抛开」在原版是客户端在 {@code onUpdate} 里检测攻击键<b>按下的边沿</b>后
 *       发 {@code TelekinesisAttackMessage}，服务端 {@code leftClick(player)} 只看「有没有锁定目标」，
 *       与这次左键有没有点到实体<b>无关</b>。1.21.1 对应成两条互补的入口：
 *     <ul>
 *       <li>左键点到<b>实体</b>：服务端 {@code AttackEntityEvent}（经 {@link WeaponAttackBehaviour} 派发）；</li>
 *       <li>左键点<b>空气或方块</b>：只有客户端的 {@code PlayerInteractEvent.LeftClickEmpty} /
 *           {@code LeftClickBlock} 会触发，所以补了一个空载荷
 *           {@code TelekinesisLeftClickPayload}，由 {@code client/FRClientEvents} 发出（见其类注释）。</li>
 *     </ul>
 *     两条路最终都汇到 {@link #leftClick(Player, ItemStack)}，冷却/锁定/充能校验只有那一个副本。
 *     <b>1.6.2 修正</b>：上一版只有第一条路，于是「点空气完全没反应」，与 1.7.10 不一致。</li>
 * </ul>
 *
 * <p><b>使用姿态的取舍</b>：原版默认（{@code altTelekinesisAlgorithm = false}）右键<b>不进入</b>
 * {@code setItemInUse}，而是由客户端每 tick 发 {@code TelekinesisUseMessage} 驱动服务端；
 * 只有把配置项打开才走 {@code onUsingTick} 的拉弓引导。1.21.1 没有「服务端驱动的手动轮询」
 * 而又不允许自定义包，所以这里<b>统一按 {@code altTelekinesisAlgorithm = true} 的路径实现</b>：
 * 右键进入拉弓姿态、{@code onUseTick} 里做念力控制。可见效果与打开该配置的原版一致。</p>
 *
 * <p><b>Vis 折算</b>：原版念力每 tick 抽 0.14 点 Vis、闪电 3.3 点、抛开 2.3 点，充能是整数。
 * 念力改为<b>每秒扣一次</b>（本模组引导型遗物的既有节奏，见核子之怒 / 永恒放逐之诫 /
 * 深渊魔典），默认值 3（0.14 × 20 = 2.8，按本项目「向上取整」的先例取 3）；
 * 闪电取 3（3.3 四舍五入）、抛开取 2（2.3 四舍五入），都是在原版数值上就近取整。</p>
 *
 * <p><b>与 1.7.10 的偏差</b>：
 * <ol>
 *   <li><s>左键触发从「按下攻击键」收紧成「左键点到实体」</s>——<b>1.6.2 已取消这条偏差</b>：
 *       现在点实体、点空气、点方块三条路都能触发，语义回到 1.7.10 的「按下左键就打锁定目标」；</li>
 *   <li>念力的 Vis 从「每 tick 抽一次、抽不出来当 tick 不动」改成「每秒扣一次、
 *       扣不出来才中断引导」，节奏与其它引导型遗物统一；</li>
 *   <li>原版搜索目标时把 {@code target} 累加（既加 {@code look × distance} 又累加 {@code y += 0.5}），
 *       点会越来越偏、越来越高。这是原版自身的实现，这里<b>逐字保留</b>，未做「修正」；</li>
 *   <li>原版 {@code onUsingTickAlt} 在客户端也跑一份（本地预测），这里整套只在服务端跑，
 *       客户端完全依赖服务端同步 —— 可见结果一致；</li>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerTick.lore} + 各要素成本）依赖
 *       {@code GuiScreen.isCtrlKeyDown}，而共享基类 {@link FRItem} 只实现 Shift 展开，
 *       近几件施法物品同样没有该行，这里保持一致。</li>
 * </ol>
 */
public class ItemTelekinesisTome extends FRItem
        implements FRRechargable, IWarpingGear, WeaponAttackBehaviour {

    /** 原版 {@code searchForTarget(player, world, 3.0f)} 的搜索盒半径。 */
    private static final double SEARCH_RANGE = 3.0D;

    /** 原版 {@code getExistingTarget(.., 6.0f)} 的搜索盒半径。 */
    private static final double EXISTING_RANGE = 6.0D;

    /** 原版念力把目标拉向「视线前方 7.5 格」。 */
    private static final double HOLD_DISTANCE = 7.5D;

    /** 原版闪电攻击的距离上限（{@code player.getDistanceToEntity(target) <= 16.0f}）。 */
    private static final double LIGHTNING_RANGE = 16.0D;

    /** 原版 {@code getMaxItemUseDuration} 返回 72000；真正的节奏靠 {@code onUseTick}。 */
    private static final int USE_DURATION = 72000;

    /** 折算后的念力扣费节奏：每 1 秒（20 tick）扣一次，且扣在这一秒的第一 tick。 */
    private static final int VIS_INTERVAL = 20;

    /** 原版 {@code PacketTelekinesisParticleMessage} 里 wisp 的颜色（r≈0.35 / g=0 / b≈0.6）。 */
    private static final int WISP_COLOR = 0x590099;

    /** 玩家 UUID -> 念力状态。对应原版 {@code globalTomeMap}（以玩家对象为键）。 */
    private static final Map<UUID, TomeState> TOME_STATES = new HashMap<>();

    public ItemTelekinesisTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 念力引导每秒的 Vis 消耗。原版 6 + 8 厘 = 0.14 点/tick，即 2.8 点/秒。 */
    public int getControlVisCost() {
        return (int) (FRConfig.TOME_OF_PREDESTINY_CONTROL_VIS_COST.get()
                * FRConfig.TOME_OF_PREDESTINY_VIS_MULT.get());
    }

    /** 闪电攻击的 Vis 消耗。原版 80 + 50 + 200 厘 = 3.3 点。 */
    public int getLightningVisCost() {
        return (int) (FRConfig.TOME_OF_PREDESTINY_LIGHTNING_VIS_COST.get()
                * FRConfig.TOME_OF_PREDESTINY_VIS_MULT.get());
    }

    /** 潜行 + 左键「抛开」的 Vis 消耗。原版 150 + 80 厘 = 2.3 点。 */
    public int getShoveVisCost() {
        return (int) (FRConfig.TOME_OF_PREDESTINY_SHOVE_VIS_COST.get()
                * FRConfig.TOME_OF_PREDESTINY_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.TOME_OF_PREDESTINY_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_PREDESTINY_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 原版 altTelekinesisAlgorithm = true 的分支：setItemInUse(stack, 72000)，进入拉弓引导。
        // 客户端也一起进入，弓的拉扯动画才会出现（与其它法术典籍同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    /**
     * 念力引导，对应原版 {@code onUsingTickAlt(stack, player, count)}。
     *
     * <p>原版默认路径由客户端每 tick 发 {@code TelekinesisUseMessage} 驱动；这里只保留服务端权威的一份。
     */
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 整套只在服务端跑，客户端交给服务端同步。
        if (level.isClientSide()) {
            return;
        }
        int elapsed = getUseDuration(stack, player) - remainingUseDuration;
        controlTick(level, player, stack, elapsed);
    }

    /**
     * 对应原版 {@code Item#onUpdate}：每 tick 递减两个计时器，并在过期时清空锁定状态。
     *
     * <p>原版对 {@code ticksTillExpire} 是<b>无条件递减</b>（归零的那一 tick 之后会继续变负），
     * 这里逐字保留同一写法。
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        TomeState state = state(player);
        if (state.ticksTillExpire == 0) {
            state.target = -1;
            state.dist = -1.0D;
            state.reDist = -1.0D;
        }
        state.ticksTillExpire--;
        if (state.ticksCooldown > 0) {
            state.ticksCooldown--;
        }
    }

    /**
     * 左键点到<b>实体</b>的入口：NeoForge 的 {@code AttackEntityEvent}（{@link WeaponAttackBehaviour} 派发）。
     *
     * <p><b>不取消事件</b>：原版的左键闪电是独立于普通攻击的一条包，普通挥击照常结算，
     * 这里保持同样行为。
     */
    @Override
    public void onAttackEntity(AttackEntityEvent event, Player attacker, ItemStack stack) {
        // 原版 leftClick 看的是 getHeldItem()（主手），副手拿书挥主手不该触发。
        if (attacker.getMainHandItem() != stack) {
            return;
        }
        leftClick(attacker, stack);
    }

    /**
     * 左键点到<b>空气 / 方块</b>的入口，由 {@code TelekinesisLeftClickPayload} 在服务端调用。
     *
     * <p>只认主手是不是预言之典，其余判定与 {@link #onAttackEntity} 完全共用。
     */
    public static void onServerLeftClick(Player player) {
        if (player.getMainHandItem().getItem() instanceof ItemTelekinesisTome tome) {
            tome.leftClick(player, player.getMainHandItem());
        }
    }

    /**
     * 对应原版 {@code leftClick(player) -> lightningAttack(player, item, ..)}：不管这次左键
     * 有没有点到东西，只要身上锁着目标、目标还在视线前方，就打一发闪电（潜行时改为抛开）。
     *
     * <p>这就是原版「按下左键就朝已锁定目标打闪电」的语义，两条入口（点实体 / 点空气或方块）
     * 共用这一份实现，冷却、锁定与充能校验因此不会出现两个副本。
     */
    private void leftClick(Player player, ItemStack stack) {
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }
        TomeState state = state(player);
        // 原版 targetID == -1 直接返回（没有锁定目标时按左键什么都不发生）。
        if (state.target == -1) {
            return;
        }
        // 原版 getEntityByID + getExistingTarget(.., 6.0) 双重确认目标还在视线前方。
        if (level.getEntity(state.target) == null) {
            return;
        }
        LivingEntity target = getExistingTarget(player, level, state.target, EXISTING_RANGE);
        if (target == null) {
            return;
        }
        lightningAttack(player, target, stack, level);
    }

    // ------------------------------------------------------------------
    // 念力引导
    // ------------------------------------------------------------------

    /** 对应原版 {@code onUsingTickAlt} 的主体（去掉客户端分支与浮点 element 消耗）。 */
    private void controlTick(Level level, Player player, ItemStack stack, int elapsed) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        TomeState state = state(player);
        // 原版 if (ticksCooldown == 0) 才做事；距离上次「抛开」不足 40 tick 时整段跳过。
        if (state.ticksCooldown != 0) {
            return;
        }

        LivingEntity target = null;
        if (state.target != -1 && server.getEntity(state.target) != null) {
            target = getExistingTarget(player, level, state.target, EXISTING_RANGE);
        }
        if (target == null) {
            target = searchForTarget(player, level, SEARCH_RANGE);
        }

        double length = HOLD_DISTANCE;
        double reDist = state.reDist;
        if (target != null && reDist == -1.0D) {
            // 原版 Vector3 用的是实体距离（getDistanceToEntity）。
            reDist = player.distanceTo(target);
        }

        if (target == null) {
            return;
        }

        // 原版每 tick 抽 6 厘风 + 8 厘秩序；充能是整数，折算成每秒扣一次。
        if (elapsed % VIS_INTERVAL == 0
                && !RechargeAccess.consumeCharge(stack, player, getControlVisCost())) {
            player.stopUsingItem();
            return;
        }

        // target.fallDistance = 0.0f
        target.resetFallDistance();
        // 原版：身上没有缓慢才补，时长 2 tick、amplifier 3（缓慢 IV）。
        if (target.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 3, true, true));
        }

        // Vector3.fromEntityCenter(player)，再按潜行与否叠加视线方向。
        Vec3 look = player.getLookAngle();
        Vec3 targetPoint = centerOf(player);
        if (player.isShiftKeyDown()) {
            targetPoint = targetPoint.add(look.scale(reDist));
        } else {
            targetPoint = targetPoint.add(look.scale(length));
            reDist = player.distanceTo(target);
        }
        // target3.y += 0.5
        targetPoint = targetPoint.add(0.0D, 0.5D, 0.0D);

        // 原版服务端广播 TelekinesisParticleMessage(实体中心, 1.0f)。
        Vec3 entityCenter = centerOf(target);
        telekinesisParticles(server, entityCenter);

        // 原版 double multiplier = item.getDistance(target3.x, target3.y, target3.z)。
        double multiplier = target.position().distanceTo(targetPoint);
        float vectorPower = 0.66666F;
        if (multiplier < 1.5D) {
            vectorPower = 0.333333F;
        } else if (multiplier >= 8.0D) {
            vectorPower *= (float) (multiplier / 8.0D);
        }

        // 黑名单（Thaumcraft Boss / Botania 盖亚守护者）：Vis 已扣，但直接返回，连 tag 都不写。
        if (isBlacklistedFromTelekinesis(target)) {
            return;
        }

        setEntityMotionFromVector(target, targetPoint, vectorPower);
        state.target = target.getId();
        state.dist = length;
        state.reDist = reDist;
        state.ticksTillExpire = 5;
    }

    /**
     * 对应原版 {@code searchForTarget(player, world, range)}。
     *
     * <p><b>注意原版的累加点位</b>：每轮循环既 {@code target += look × distance}
     * （distance 递增），又 {@code target.y += 0.5}，两者都不复位。于是搜索点会越来越偏、
     * 越来越高。这是 1.7.10 自身的行为，这里逐字保留。
     */
    private static LivingEntity searchForTarget(Player player, Level level, double range) {
        Vec3 target = centerOf(player);
        Vec3 look = player.getLookAngle();
        List<LivingEntity> entities = new ArrayList<>();
        for (int distance = 1; entities.isEmpty() && distance < 32; distance++) {
            target = target.add(look.scale(distance)).add(0.0D, 0.5D, 0.0D);
            entities = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, boxAround(target, range)));
            // 原版把玩家自己从结果里剔除。
            entities.remove(player);
        }
        return entities.isEmpty() ? null : entities.get(0);
    }

    /**
     * 对应原版 {@code getExistingTarget(player, world, targetID, range)}：沿同一条（累加的）
     * 视线路径逐格搜索，目标只要出现在任意一格的搜索盒里就算「还在」。
     */
    private static LivingEntity getExistingTarget(Player player, Level level, int targetId, double range) {
        Entity tracked = level.getEntity(targetId);
        if (!(tracked instanceof LivingEntity taritem)) {
            return null;
        }
        Vec3 target = centerOf(player);
        Vec3 look = player.getLookAngle();
        int distance = 1;
        while (distance < 32) {
            target = target.add(look.scale(distance)).add(0.0D, 0.5D, 0.0D);
            List<LivingEntity> entities =
                    new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, boxAround(target, range)));
            distance++;
            entities.remove(player);
            if (entities.contains(taritem)) {
                return taritem;
            }
        }
        return null;
    }

    /** 以 {@code point} 为中心、边长为 {@code 2 * range} 的立方体。 */
    private static AABB boxAround(Vec3 point, double range) {
        return new AABB(point.x - range, point.y - range, point.z - range,
                point.x + range, point.y + range, point.z + range);
    }

    /**
     * 对应原版 {@code setEntityMotionFromVector(entity, originalPosVector, modifier)}：
     * 由「实体中心 -> 目标点」的方向（长度 &gt; 1 时归一化）决定速度；玩家目标另发一份同步。
     */
    private static void setEntityMotionFromVector(Entity entity, Vec3 originalPos, float modifier) {
        Vec3 entityVector = centerOf(entity);
        Vec3 finalVector = originalPos.subtract(entityVector);
        if (finalVector.length() > 1.0D) {
            finalVector = finalVector.normalize();
        }
        entity.setDeltaMovement(finalVector.x * modifier, finalVector.y * modifier, finalVector.z * modifier);
        // 原版玩家目标走 PlayerMotionUpdateMessage；1.21.1 置 hurtMarked，让原版实体同步把速度发下去。
        entity.hurtMarked = true;
    }

    /** 对应 {@code Vector3.fromEntityCenter(entity)}：脚底坐标 + 碰撞箱高度的一半。 */
    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    /**
     * 对应原版 {@code SuperpositionHandler.isEntityBlacklistedFromTelekinesis}：
     * Thaumcraft 的 Boss 与 Botania 的盖亚守护者不吃念力。
     */
    private static boolean isBlacklistedFromTelekinesis(LivingEntity entity) {
        return entity instanceof EntityThaumaturgeBoss || entity instanceof GaiaGuardianEntity;
    }

    /**
     * 对应原版 {@code TelekinesisParticleMessage(x, y, z, 1.0f)} 的客户端渲染
     * （Botania wispFX + Thaumcraft portalstuff）。
     *
     * <p>{@code modifier = 1.0} 时：{@code wisps = 1}，循环 {@code i <= 1} 即 2 颗紫色 wisp
     * （位置带 ±0.075 的随机初速）；{@code supers = 3}，循环 {@code i <= 3} 即 4 颗传送门粒子
     * （初速 ±1.5）。{@code sendParticles} 在 {@code count == 0} 时会发一颗「位置不动、
     * 初速 = 给定向量 × speed」的粒子，正好复刻这个带初速的单粒子。
     */
    private static void telekinesisParticles(ServerLevel level, Vec3 center) {
        for (int i = 0; i <= 1; i++) {
            level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                    center.x, center.y, center.z, 0,
                    (level.random.nextDouble() - 0.5D) * 0.15D,
                    (level.random.nextDouble() - 0.5D) * 0.15D,
                    (level.random.nextDouble() - 0.5D) * 0.15D, 1.0D);
        }
        for (int i = 0; i <= 3; i++) {
            level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 0,
                    (level.random.nextDouble() - 0.5D) * 3.0D,
                    (level.random.nextDouble() - 0.5D) * 3.0D,
                    (level.random.nextDouble() - 0.5D) * 3.0D, 1.0D);
        }
    }

    // ------------------------------------------------------------------
    // 闪电攻击 / 抛开
    // ------------------------------------------------------------------

    /** 对应原版 {@code lightningAttack(player, target, stack, world)}。 */
    private void lightningAttack(Player player, LivingEntity target, ItemStack stack, Level level) {
        // 原版：客户端或共用冷却中直接返回（连 setCasted 都不执行）。
        if (level.isClientSide() || CooldownHelper.isOnCooldown(player)) {
            return;
        }
        Vec3 targetCenter = centerOf(target);
        // 原版 Vector3(player.getLookVec().normalize())；getLookAngle 本来就是单位向量。
        Vec3 moveVector = player.getLookAngle().normalize();

        double distance = player.distanceTo(target);
        if (distance <= LIGHTNING_RANGE
                && RechargeAccess.consumeCharge(stack, player, getLightningVisCost())) {
            if (level instanceof ServerLevel server) {
                // 原版连画 4 道 imposeLightning：起点是玩家脚下 + 1 格，终点是目标中心。
                drawLightningArc(server, player.position().add(0.0D, 1.0D, 0.0D), targetCenter);
            }
            // 原版 world.playSoundAtEntity(player, "thaumcraft:zap", 1.0F, 0.8F)。
            SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.8F);
            double min = FRConfig.TOME_OF_PREDESTINY_DAMAGE_MIN.get();
            double max = FRConfig.TOME_OF_PREDESTINY_DAMAGE_MAX.get();
            target.hurt(FRDamageTypes.source(level, FRDamageTypes.TRUE_LIGHTNING, player),
                    (float) (min + level.random.nextDouble() * (max - min)));
        }

        // 潜行 + 左键：抛开目标（原版把整块判定写成一个 if，含服务端判断）。
        if (player.isShiftKeyDown()
                && RechargeAccess.consumeCharge(stack, player, getShoveVisCost())) {
            // 原版直接覆写三个速度分量，不是叠加。
            target.setDeltaMovement(moveVector.x * 3.0D, moveVector.y * 1.5D, moveVector.z * 3.0D);
            target.hurtMarked = true;
            TomeState state = state(player);
            state.target = -1;
            state.dist = -1.0D;
            state.reDist = -1.0D;
            state.ticksTillExpire = 0;
            state.ticksCooldown = FRConfig.TOME_OF_PREDESTINY_SHOVE_COOLDOWN.get();
        }

        // 原版 SuperpositionHandler.setCasted(player, 10, true)：10 tick 共用冷却 + 挥臂。
        CooldownHelper.setCooldown(player, FRConfig.TOME_OF_PREDESTINY_COOLDOWN.get());
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    /**
     * 原版 {@code SuperpositionHandler.imposeLightning(...)} 的替身。
     *
     * <p>原版一次调用画一条带曲线的闪电、且被连调 4 次；这里沿起点→终点连线撒 4 遍
     * {@link ParticleTypes#ELECTRIC_SPARK}，每遍带一点随机抖动（与千咒之诫同一方案）。
     */
    private static void drawLightningArc(ServerLevel server, Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double length = diff.length();
        if (length < 1.0E-4D) {
            return;
        }
        int points = Math.min(16, Math.max(2, (int) (length * 6.0D)));
        Vec3 step = diff.scale(1.0D / points);
        for (int arc = 0; arc < 4; arc++) {
            Vec3 pos = from;
            for (int i = 0; i < points; i++) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x + (server.random.nextDouble() - 0.5D) * 0.15D,
                        pos.y + (server.random.nextDouble() - 0.5D) * 0.15D,
                        pos.z + (server.random.nextDouble() - 0.5D) * 0.15D,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                pos = pos.add(step);
            }
        }
    }

    // ------------------------------------------------------------------
    // 状态与 tooltip
    // ------------------------------------------------------------------

    /** 取（必要时新建）某位玩家的念力状态。 */
    private static TomeState state(Player player) {
        return TOME_STATES.computeIfAbsent(player.getUUID(), id -> new TomeState());
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：1~3、空行、4~6、空行、7~9、空行、10、空行、11（伤害行）。
        tooltip.add(Component.translatable("item.PredestinyTome1.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome2.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome4.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome5.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome7.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome8.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome9.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.PredestinyTome10.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // 原版把 16 / 40 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.PredestinyTome11_1.lore")
                .append(Component.literal(" " + (int) FRConfig.TOME_OF_PREDESTINY_DAMAGE_MIN.get().doubleValue()
                        + "-" + (int) FRConfig.TOME_OF_PREDESTINY_DAMAGE_MAX.get().doubleValue() + " "))
                .append(Component.translatable("item.PredestinyTome11_2.lore")));
    }

    /** 对应原版那张以玩家为键的 {@code HashMap}，五个字段一一对应。 */
    private static final class TomeState {
        private int ticksTillExpire;
        private int ticksCooldown;
        private int target = -1;
        private double dist = -1.0D;
        private double reDist = -1.0D;
    }
}
