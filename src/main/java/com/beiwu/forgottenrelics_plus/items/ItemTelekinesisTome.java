package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData;
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
 * 预言之典（Tome of Predestiny），注册名 {@code tome_of_predestiny}，1.7.10 原版
 * {@code ItemTelekinesisTome}（同目录的 {@code ItemTelekinesisTomeLegacy} 没有被任何地方引用，
 * 不移植；依据见 {@code Main.java} 第 257 / 293 行）。堆叠上限 1、Warp 4。
 *
 * <p>行为：拉弓姿态（可用时长 72000 tick），按住右键进入念力引导——沿视线逐格搜索最近的活体，
 * 清零其坠落距离、补缓慢 IV（2 tick / amplifier 3），再把它往「玩家中心 + 视线 × 7.5，再抬高 0.5」
 * 拉（潜行时保持上次距离不拉近）；方向长度 &gt; 1 时归一化，倍率距离 &lt; 1.5 取 0.333、否则 0.667、
 * 距离 ≥ 8 时再按 {@code dist / 8} 放大。左键对已锁定目标打闪电：≤ 16 格且充能足够时连画 4 道折线
 * 闪电、播一次 {@code zap}、造成 {@code 16 + 24 × 随机} 的真实闪电伤害；潜行 + 左键则把目标沿视线以
 * {@code (3.0, 1.5, 3.0)} 抛开。Thaumcraft Boss 与 Botania 盖亚守护者不吃念力（Vis 照扣）。
 *
 * <p>1.21.1 对应：{@code use} / {@code onUseTick} / {@link UseAnim#BOW} 一一对应；
 * <b>「从背包法杖抽 Vis」没有对应 API</b>（见 {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1），
 * 改成 {@link FRRechargable} 的物品自身充能；原版以玩家为键的静态 map {@code globalTomeMap} → 本类的
 * {@code Map<UUID, TomeState>}（Player 实例会在换维度 / 重登时被替换，UUID 更稳，与
 * {@link CooldownHelper} 同一考虑）；{@code SuperpositionHandler} 的共用施法冷却 → {@link CooldownHelper}。
 * <b>不写任何自定义网络包</b>：紫色 wisp 走服务端 {@link FRParticles}（原版就是 Botania wispFX），
 * portalstuff 继续用 {@link ParticleTypes#PORTAL}，玩家目标的移动改为改完速度后置 {@code hurtMarked}
 * 靠原版同步。左键闪电用 {@link FRBoltParticleData#broadcast} 把两端送到客户端、由
 * {@code client/FRBolts} 交给 Botania 的 {@code BoltRenderer} 画折线闪电，宽度沿用原版
 * {@code 0.225 + distSq / 2000}（比霹雳咒书的 0.075 粗得多，是 1.7.10 自己的取值）；
 * {@code thaumcraft:zap} → {@link SoundEvents#FIREWORK_ROCKET_BLAST} 并过 {@link SoundHelper#play}。
 *
 * <p><b>使用姿态</b>：原版默认（{@code altTelekinesisAlgorithm = false}）右键不进入
 * {@code setItemInUse}，而由客户端每 tick 发包驱动服务端；1.21.1 不允许自定义包，这里统一按该选项为
 * {@code true} 的路径实现：右键进入拉弓姿态、{@code onUseTick} 里做念力控制，可见效果与打开该配置的
 * 原版一致。
 *
 * <p><b>Vis 折算</b>：原版念力 0.14 点/tick、闪电 3.3 点、抛开 2.3 点，充能是整数。念力改为每秒扣
 * 一次（本模组引导型遗物的既有节奏），默认 3（2.8 向上取整）；闪电取 3、抛开取 2（四舍五入）。
 *
 * <p><b>与 1.7.10 的偏差</b>：
 * <ol>
 *   <li>原版搜索目标时把 {@code target} 既累加 {@code look × distance} 又累加 {@code y += 0.5}，
 *       搜索点会越来越偏、越来越高——这是原版自身的实现，这里<b>逐字保留</b>，未做「修正」；</li>
 *   <li>念力的 Vis 从「每 tick 抽一次、抽不出来当 tick 不动」改成「每秒扣一次、扣不出来才中断引导」，
 *       节奏与其它引导型遗物统一；</li>
 *   <li>原版 {@code onUsingTickAlt} 在客户端也跑一份（本地预测），这里整套只在服务端跑，客户端完全
 *       依赖服务端同步 —— 可见结果一致；</li>
 *   <li>左键触发：原版是「按下攻击键的边沿」，与左键有没有点到实体无关。1.21.1 拆成两条互补入口
 *       （点实体走 {@link WeaponAttackBehaviour} / 点空气或方块走客户端补发的空载荷
 *       {@code TelekinesisLeftClickPayload}），都汇到 {@link #leftClick(Player, ItemStack)} 这一份实现，
 *       <b>1.6.2 修正</b>：上一版只有点实体一条路，点空气完全没反应，与 1.7.10 不一致；</li>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerTick.lore} + 各要素成本）依赖
 *       {@code GuiScreen.isCtrlKeyDown}，共享基类 {@link FRItem} 只实现 Shift 展开，这里保持一致。</li>
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
     * <p>{@code modifier = 1.0} 时：{@code wisps = 1}，循环 {@code i <= 1} 即 2 颗紫色 wisp，
     * 每颗 {@code wispFX(中心, r=0.2+rand*0.3, g=0, b=0.5+rand*0.2, size=0.2+rand*0.1,
     * xm/ym/zm=(rand-0.5)*0.15, maxAgeMul=1.0)}；{@code supers = 3}，循环 {@code i <= 3}
     * 即 4 颗传送门粒子（原版就是 {@code EntityPortalFX}，初速 ±1.5）。
     */
    private static void telekinesisParticles(ServerLevel level, Vec3 center) {
        for (int i = 0; i <= 1; i++) {
            FRParticles.serverWisp(level, center.x, center.y, center.z,
                    0.2F + level.random.nextFloat() * 0.3F,
                    0.0F,
                    0.5F + level.random.nextFloat() * 0.2F,
                    0.2F + level.random.nextFloat() * 0.1F,
                    (level.random.nextDouble() - 0.5D) * 0.15D,
                    (level.random.nextDouble() - 0.5D) * 0.15D,
                    (level.random.nextDouble() - 0.5D) * 0.15D);
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
                // 原版 for (counter = 0; counter <= 3; ++counter) 连画 4 道 imposeLightning：
                // 起点 (player.x, player.y + 1.0, player.z)、终点目标身体中心、
                // 宽度 (float)(0.225 + player.getDistanceSq(target) / 2000.0)。
                double width = 0.225D + player.distanceToSqr(target) / 2000.0D;
                FRBoltParticleData.broadcast(server, player.position().add(0.0D, 1.0D, 0.0D), targetCenter,
                        (float) width, 4,
                        FRBoltParticleData.ARC_RED, FRBoltParticleData.ARC_GREEN, FRBoltParticleData.ARC_BLUE);
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
