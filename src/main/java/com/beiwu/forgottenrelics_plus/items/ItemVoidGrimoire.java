package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

/**
 * 深渊魔典（Grimoire of The Abyss），注册名 {@code void_grimoire}，
 * 1.7.10 原版 {@code ItemVoidGrimoire}。
 *
 * <p>原版逻辑：
 * <ol>
 *   <li>{@code onItemRightClick}：若服务端且玩家还在共用冷却里就直接返回；客户端则另有
 *       {@code localCooldown} 计数（纯客户端防连点，冷却结束的 60 tick 内不再重新起手）。
 *       然后用 {@code EntityUtils.getPointedEntity(world, player, 0.0, 64.0, 3.0F)}
 *       沿视线找活体（射线 64 格、搜索盒外扩 3 格）：找到就记进以玩家为键的静态 map
 *       {@code targetList} 并 {@code setItemInUse(stack, 100)} 进入 {@code EnumAction.bow} 的引导；
 *       找不到就什么都不做（连引导都不开始）；</li>
 *   <li>{@code onUsingTick}（<b>两侧都跑</b>）：
 *     <ol>
 *       <li>先从背包法杖抽 Vis：<b>秩序（Ordo）9 + 混沌（Perditio）16 = 25 厘 Vis</b>，
 *           即 0.25 点/tick（5 点/秒），两项都乘 {@code voidGrimoireVisMult}；
 *           <b>抽不出来立刻 {@code stopUsingItem}</b>，且这一扣发生在目标校验之前；</li>
 *       <li>若 {@code targetList} 里没有该玩家，把该项置 null 并停止引导；</li>
 *       <li>目标存在时，<b>每 tick</b> 都做：清零坠落距离、施加 30 tick、amplifier 100 的
 *           <b>移动缓慢</b>（{@code Potion.field_76421_d}，即缓慢；amp 100 足以把目标定住）、
 *           把 {@code motionY} 直接写成 {@code 0.03}（目标缓缓上浮）、
 *           {@code noClip = true}（穿过方块上浮）；
 *           服务端每 tick 往目标周围广播 {@code PacketVoidMessage(.., false)}
 *           （8 颗随机紫色 wisp 向内收束 + 5 颗原版传送门粒子），
 *           并在引导的第一个 tick（{@code count == getMaxItemUseDuration()}）在目标中心播一次
 *           {@code forgottenrelics:sound.mdcharge}（音量 4.0、音调 0.75）；</li>
 *       <li>{@code count == 1}（100 tick 引导的最后一 tick）结算：
 *           服务端 {@code imposeBurst(.., 2.0)}（Thaumcraft 的爆裂特效）+
 *           {@code PacketVoidMessage(.., true)}（129 颗向外炸开的紫色 wisp），
 *           在目标处播 {@code thaumcraft:craftfail}（音量 4.0、音调 0.8+rand*0.2），
 *           然后 {@code overthrow(target, player)}、
 *           {@code SuperpositionHandler.setCasted(player, 30, false)}（30 tick 共用冷却）；
 *           客户端把 {@code localCooldown} 置 60；</li>
 *     </ol>
 *   </li>
 *   <li>{@code overthrow(entity, overthrower)}：把目标直接搬到<b>同一个维度</b>的
 *       {@code x/z = ±10001 随机、y = -100000 ± 10001 随机}处（世界的虚空）。
 *       非玩家目标搬完立刻 {@code setDead()}；玩家目标留在原地等虚空伤害慢慢杀死，
 *       并向全服广播 {@code OverthrowChatMessage(type 1)}
 *       （{@code <施法者> has overthrown <目标> into the Void.}）。
 *       注意这里<b>不换维度</b>，也不像永恒放逐之诫那样在下界找落脚点；</li>
 *   <li>物品堆叠上限 1、稀有度 EPIC，{@code EnumAction.bow}、可用时长 100，
 *       {@code getWarp} 返回 <b>3</b>。</li>
 * </ol>
 *
 * <p><b>{@code voidGrimoireEnabled} 这个总开关</b>（原版 {@code RelicsConfigHandler}）只在一个地方用到：
 * {@code RelicsResearchRegistry} 用它包住整个「VoidGrimoire 研究词条」的注册，
 * 也就是说关掉之后<b>只是让这件遗物无法合法制造</b>（研究不存在 → 无法解锁灌注配方），
 * 既不会删除世界里已有的成品，也不阻止创造模式刷出。本项目<b>没有移植这个开关</b>，
 * 理由与做法见提交说明：1.21.1 的研究只能写数据包 JSON、没有任何 Java 注册/注销 API
 * （{@code docs/reference/thaumaturge-1.21.1-api.md} §4.1 已实测确认），
 * 想禁用它只能删/改 {@code research_entry/void_grimoire.json}；前一版的「虚伪审判」遇到同名的
 * {@code falseJusticeEnabled} 也是同样处理。</p>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code getMaxItemUseDuration} → {@code Item#getUseDuration}；{@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见
 *       {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。按本模组统一约定改成
 *       {@link FRRechargable} 的<b>物品自身充能</b>，用 {@link RechargeAccess#consumeCharge} 扣除：</li>
 *   <li>原版以玩家为键的静态 map {@code targetList} → 物品数据组件
 *       {@link FRDataComponents#VOID_GRIMOIRE_TARGET}（与永恒放逐之诫的 {@code EDICT_TARGET} 同源）；</li>
 *   <li>原版 {@code SuperpositionHandler} 的共用冷却 → {@link CooldownHelper}（同样是全体共用）；</li>
 *   <li><b>不写任何自定义网络包</b>：{@code PacketVoidMessage} / {@code BurstMessage} /
 *       {@code EntityMotionMessage} 全部改由服务端直接生成粒子与改速度，
 *       靠原版同步送达客户端（服务端改 {@code motion} 后置 {@code hurtMarked} 即会同步）；</li>
 *   <li>音效：{@code forgottenrelics:sound.mdcharge} → 原版 {@link SoundEvents#RESPAWN_ANCHOR_CHARGE}
 *       （同为「蓄力」音，且都有音调参数）；{@code thaumcraft:craftfail} → 原版
 *       {@link SoundEvents#FIRE_EXTINGUISH}（同为失败时的「嗤」声）。本模组对自定义/Thaumcraft
 *       音效一律换原版等价物，且都过 {@link SoundHelper#play} 统一压低音量；</li>
 *   <li>粒子：Botania 紫色 wisp → 原版 {@link ParticleTypes#ENTITY_EFFECT} 上色（与永恒放逐之诫同一方案），
 *       Thaumcraft 的 {@code portalstuff} 就是原版 {@code EntityPortalFX} → {@link ParticleTypes#PORTAL}。</li>
 * </ul>
 *
 * <p><b>Vis 折算</b>：原版每 tick 抽 25 厘 Vis = 0.25 点/tick，即 <b>5 点/秒</b>；充能是整数，
 * 故在原版数值上直接取 5，并在每一秒的第一 tick 扣一次（与永恒放逐之诫、核子之怒同一扣费节奏）。
 * 一次完整引导（100 tick）合计扣 5 次 = 25 点。
 *
 * <p><b>与 1.7.10 的偏差</b>：
 * <ol>
 *   <li>原版 {@code onUsingTick} 在<b>两侧都执行</b>（客户端也自己改目标速度、加缓慢）。
 *       这里整套只在服务端跑，客户端完全依赖服务端同步 —— 可见结果一致，但少掉客户端那一份
 *       冗余计算（与永恒放逐之诫同一写法）；</li>
 *   <li>原版的 {@code localCooldown = 60} 是纯客户端防连点计数，与 {@code setCasted} 的 30 tick
 *       共用冷却是两回事。这里只保留服务端权威的 30 tick 冷却，不实现客户端计数；</li>
 *   <li>原版把 {@code noClip = true}（1.21.1 里该字段名为 {@code noPhysics}）写在目标身上后<b>从不复位</b>：引导若被提前打断
 *       （松手、Vis 耗尽、目标消失），目标会永久保持 {@code noClip}。这是 1.7.10 自身的缺陷，
 *       按「以 1.7.10 为准」的原则<b>逐字保留</b>，未做修正（见提交说明「不确定点」）；</li>
 *   <li>原版非玩家目标用 {@code setPosition} 后 {@code setDead()}；1.21.1 对玩家目标走
 *       {@code ServerPlayer#teleportTo(ServerLevel, ..)}（保证客户端被同步），其它实体走
 *       {@code Entity#teleportTo(x, y, z)} 后 {@code discard()}；</li>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerTick.lore} + 各要素成本）依赖
 *       {@code GuiScreen.isCtrlKeyDown}，而共享基类 {@link FRItem} 只实现 Shift 展开，
 *       近几件施法物品同样没有该行，这里保持一致。</li>
 * </ol>
 */
public class ItemVoidGrimoire extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getPointedEntity(world, player, 0.0, 64.0, 3.0F)} 的射线长度与搜索盒外扩量。 */
    private static final double SEARCH_RANGE = 64.0D;
    private static final double SEARCH_TOLERANCE = 3.0D;

    /** 原版 {@code Entity#getCollisionBorderSize()} 的默认值：每个候选实体的碰撞箱再外扩 0.1。 */
    private static final double COLLISION_BORDER = 0.1D;

    /** 折算后的扣费节奏：每 1 秒（20 tick）扣一次，且扣在这一秒的第一 tick。 */
    private static final int VIS_INTERVAL = 20;

    /** 原版每 tick 给目标施加的缓慢：{@code new PotionEffect(Potion.field_76421_d.id, 30, 100, true)}。 */
    private static final int SLOWNESS_DURATION = 30;
    private static final int SLOWNESS_AMPLIFIER = 100;

    /** 原版每 tick 写死的竖直速度 {@code target.motionY = 0.03}。 */
    private static final double ASCEND_MOTION = 0.03D;

    /** 原版虚空落点：{@code y = -100000 + (random - 0.5) * 20002}，X/Z 各 ±10001。 */
    private static final double ABYSS_Y = -100000.0D;
    private static final double VOID_SPREAD = 20002.0D;

    /** 原版 wisp 颜色范围（r 0.2~0.5、g 0、b 0.5~0.7）取一个代表色。 */
    private static final int WISP_COLOR = 0x590099;

    public ItemVoidGrimoire(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 每秒的 Vis 消耗：原版每 tick 秩序（Ordo）9 + 混沌（Perditio）16 厘 Vis = 0.25 点/tick，
     * 即 5 点/秒。
     */
    public int getVisCostPerSecond() {
        return (int) (FRConfig.VOID_GRIMOIRE_VIS_COST.get() * FRConfig.VOID_GRIMOIRE_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.VOID_GRIMOIRE_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.VOID_GRIMOIRE_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // 原版 getMaxItemUseDuration(stack) 返回 100。
        return FRConfig.VOID_GRIMOIRE_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版：服务端先查共用冷却，冷却中连引导都不起手。
        if (!level.isClientSide() && CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }
        // 原版 getPointedEntity(world, player, 0.0, 64.0, 3.0F)：锁定准星指向的活体。
        LivingEntity target = findPointedEntity(level, player);
        if (target == null) {
            // 原版把 targetList 里该玩家置为 null，且不进入引导。
            if (!level.isClientSide()) {
                stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
            }
            return InteractionResultHolder.fail(stack);
        }
        // 原版把目标写进以玩家为键的静态 map；1.21.1 改用物品数据组件存目标实体 id。
        if (!level.isClientSide()) {
            stack.set(FRDataComponents.VOID_GRIMOIRE_TARGET.get(), target.getId());
        }
        // 原版 setItemInUse(stack, 100) 进入拉弓姿态；客户端也一起进入，弓的拉扯动画才会出现
        //（与其它法术典籍同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 原版 onUsingTick 两侧都会跑；这里整套只在服务端跑，客户端交给服务端同步。
        if (level.isClientSide()) {
            return;
        }
        ServerLevel server = (ServerLevel) level;

        // 原版 onUsingTick 第一步就是抽 Vis（秩序 9 + 混沌 16 = 25 厘 = 0.25 点/tick），
        // 抽不出来立刻中断；这一步发生在目标校验之前。
        // 这里折算成「每 20 tick 扣一次 5 点」，扣在每一秒的第一 tick。
        int duration = getUseDuration(stack, player);
        int elapsed = duration - remainingUseDuration;
        if (elapsed % VIS_INTERVAL == 0
                && !RechargeAccess.consumeCharge(stack, player, getVisCostPerSecond())) {
            stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
            player.stopUsingItem();
            return;
        }

        Integer targetId = stack.get(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
        if (targetId == null) {
            // 对应原版 !targetList.containsKey(player) -> stopUsingItem。
            player.stopUsingItem();
            return;
        }
        Entity targetEntity = server.getEntity(targetId);
        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
            // 原版目标死亡/消失时把 map 项清空并停止引导。
            stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
            player.stopUsingItem();
            return;
        }

        // ---- 每 tick：定身 + 上浮 ----
        // target.fallDistance = 0.0f
        target.resetFallDistance();
        // new PotionEffect(Potion.field_76421_d.id, 30, 100, true)：30 tick、amplifier 100 的移动缓慢。
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, true, true));
        // target.motionY = 0.03；X/Z 分量原版没动，这里一起保留。
        Vec3 motion = target.getDeltaMovement();
        target.setDeltaMovement(motion.x, ASCEND_MOTION, motion.z);
        // 原版改完速度后发 EntityMotionMessage 通知客户端；1.21.1 等价做法是标记速度已变，
        // 原版的服务端实体会在下一 tick 把速度同步下去。
        target.hurtMarked = true;
        // target.noClip = true（1.21.1 里这个字段叫 noPhysics；原版从不复位，见类注释「偏差」第 3 条）。
        target.noPhysics = true;

        // 原版 Vector3.fromEntityCenter(target)，再 y += 0.03 作为粒子与爆裂的基准点。
        Vec3 center = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D);
        Vec3 thisPos = center.add(0.0D, 0.03D, 0.0D);

        // 原版只在引导的第一个 tick（count == getMaxItemUseDuration()）播一次蓄力音。
        if (remainingUseDuration == duration) {
            SoundHelper.play(level, thisPos.x, thisPos.y, thisPos.z,
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 4.0F, 0.75F);
        }

        // 原版每 tick 广播 PacketVoidMessage(.., false)：紫色 wisp 向内收束 + 传送门粒子。
        voidParticles(server, thisPos, false);

        if (remainingUseDuration != 1) {
            return;
        }

        // ---- 引导的最后一 tick：结算 ----
        // 原版 SuperpositionHandler.imposeBurst(.., 2.0f)：Thaumcraft 的爆裂特效。
        voidBurst(server, thisPos);
        // 原版 PacketVoidMessage(.., true)：129 颗 wisp 向外炸开。
        voidParticles(server, thisPos, true);
        // 原版 thaumcraft:craftfail（音量 4.0、音调 0.8 + random * 0.2），播在目标处。
        SoundHelper.play(level, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 4.0F,
                0.8F + server.random.nextFloat() * 0.2F);

        overthrow(server, target, player);
        stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
        // 原版 SuperpositionHandler.setCasted(player, 30, false)。
        CooldownHelper.setCooldown(player, FRConfig.VOID_GRIMOIRE_COOLDOWN.get());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        // 提前松手时清掉锁定的目标（原版的静态 map 项会留到下次右键，这里顺手清干净）。
        if (!level.isClientSide()) {
            stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
        }
    }

    /**
     * 对应原版 {@code overthrow(entity, overthrower)}：把目标丢进当前维度的虚空。
     *
     * <p>与永恒放逐之诫不同，这里<b>不换维度</b>、也不搜索落脚点：X/Z 各取 ±10001 之间的随机数，
     * Y 取 {@code -100000 ± 10001}。非玩家目标搬完立即抹除；玩家目标留在虚空里被虚空伤害杀死，
     * 并向全服广播 type 1 的消息。
     */
    private static void overthrow(ServerLevel level, LivingEntity target, Player caster) {
        double x = (level.random.nextDouble() - 0.5D) * VOID_SPREAD;
        double z = (level.random.nextDouble() - 0.5D) * VOID_SPREAD;
        double y = ABYSS_Y + (level.random.nextDouble() - 0.5D) * VOID_SPREAD;

        if (target instanceof ServerPlayer serverPlayer) {
            // 原版对玩家只做 setPosition：留在原地等死；1.21.1 走 ServerPlayer 的传送以同步客户端。
            serverPlayer.teleportTo(level, x, y, z, serverPlayer.getYRot(), serverPlayer.getXRot());
            broadcastOverthrow(caster, serverPlayer);
            return;
        }
        // 原版非玩家目标：setPosition 后立刻 setDead。
        target.teleportTo(x, y, z);
        target.discard();
    }

    /**
     * 对应原版 {@code OverthrowChatMessage(type 1)} 的全服广播：
     * {@code <施法者> has overthrown <目标> into the Void.}
     *
     * <p>本项目<b>不写自定义网络包</b>，改用原版 {@link Component} + {@code PlayerList#broadcastSystemMessage}
     *（等价于原版的 {@code sendToAll}）。注意 type 1 用的是 {@code message.overthrown3}
     *（"into the Void."），与永恒放逐之诫用的 {@code message.overthrown2}（"into the Nether."）不同。
     */
    private static void broadcastOverthrow(Player caster, ServerPlayer victim) {
        MinecraftServer server = caster.getServer();
        if (server == null) {
            return;
        }
        Component message = Component.literal(caster.getDisplayName().getString() + " ")
                .append(Component.translatable("message.overthrown1"))
                .append(Component.literal(" " + victim.getDisplayName().getString() + " "))
                .append(Component.translatable("message.overthrown3"));
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    /**
     * 对应原版 {@code PacketVoidMessage} 的客户端渲染（Botania wispFX + EntityPortalFX）。
     *
     * <p>{@code finish == false}（每 tick）：在中心 ±6 内随机取 8 个点，初速 = (中心 - 取点) × 0.08，
     * 让紫色 wisp 向内收束；再在中心撒 5 颗随机初速的原版传送门粒子。
     * {@code finish == true}（引导结束）：{@code i <= 128}，即 129 颗 wisp 从中心向外炸开
     * （初速 ±0.25，即 speed 参数取 0.5）。
     */
    private static void voidParticles(ServerLevel level, Vec3 center, boolean finish) {
        if (finish) {
            level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                    center.x, center.y, center.z, 129, 0.0D, 0.0D, 0.0D, 0.5D);
            return;
        }
        for (int i = 0; i < 8; i++) {
            double px = center.x + (level.random.nextDouble() - 0.5D) * 12.0D;
            double py = center.y + (level.random.nextDouble() - 0.5D) * 12.0D;
            double pz = center.z + (level.random.nextDouble() - 0.5D) * 12.0D;
            level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                    px, py, pz, 0,
                    (center.x - px) * 0.08D, (center.y - py) * 0.08D, (center.z - pz) * 0.08D, 1.0D);
        }
        for (int i = 0; i < 5; i++) {
            level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 0,
                    (level.random.nextDouble() - 0.5D) * 8.0D,
                    (level.random.nextDouble() - 0.5D) * 8.0D,
                    (level.random.nextDouble() - 0.5D) * 8.0D, 1.0D);
        }
    }

    /**
     * 对应原版 {@code SuperpositionHandler.imposeBurst(world, dim, x, y, z, 2.0f)}
     *（Thaumcraft 的 {@code proxy.burst}）。1.21.1 没有等价特效，这里用「1 颗闪光 +
     * 一簇向外炸开的紫色 effect 粒子」近似，向外速度随 size 放大。
     */
    private static void voidBurst(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                center.x, center.y, center.z, 32, 0.5D, 0.5D, 0.5D, 0.1D);
    }

    /**
     * 对应原版 {@code EntityUtils.getPointedEntity(world, player, 0.0, 64.0, 3.0F)}：
     * 先按「视线线段外扩 3 格」的包围盒粗筛，再对每个候选实体的碰撞箱（外扩 0.1，即原版
     * {@code getCollisionBorderSize()}）做线段求交，取交点上离眼睛最近的活体。
     *
     * <p>与永恒放逐之诫、错位之典里那份同名实现完全一致（项目按「一件物品一份自足实现」的
     * 惯例各留一份，暂未抽公共工具）。
     */
    private static LivingEntity findPointedEntity(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(SEARCH_RANGE));
        AABB search = new AABB(eye, end).inflate(SEARCH_TOLERANCE);
        LivingEntity best = null;
        double bestDistance = SEARCH_RANGE;
        for (Entity candidate : level.getEntities(player, search,
                entity -> entity instanceof LivingEntity && entity.isPickable())) {
            AABB box = candidate.getBoundingBox().inflate(COLLISION_BORDER);
            Optional<Vec3> hit = box.clip(eye, end);
            double distance;
            if (hit.isPresent()) {
                distance = eye.distanceTo(hit.get());
            } else if (box.contains(eye)) {
                distance = 0.0D;
            } else {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (LivingEntity) candidate;
            }
        }
        return best;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：1~3、空行、4~5、空行、6（末尾空行由 FRItem 统一补）。
        tooltip.add(Component.translatable("item.ItemVoidGrimoire1.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire2.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire4.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire6.lore"));
    }
}
