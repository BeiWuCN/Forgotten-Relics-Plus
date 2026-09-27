package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.registry.FRSounds;
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
 * 1.7.10 原版 {@code ItemVoidGrimoire}。堆叠上限 1、Warp 3、引导 100 tick、{@code EnumAction.bow}。
 *
 * <p>行为：右键用 {@code EntityUtils.getPointedEntity(world, player, 0.0, 64.0, 3.0F)}（射线 64 格、
 * 搜索盒外扩 3 格）锁定准星指向的活体并记入组件，随后进入 100 tick 引导。引导期间每 tick：折算后的
 * 扣费发生在目标校验之前，抽不出来立刻停止；然后清零目标坠落距离、施加 30 tick / amplifier 100 的
 * 移动缓慢、把 {@code motionY} 写成 0.03（缓缓上浮）、置 {@code noClip = true}，并广播紫色 wisp 与
 * 传送门粒子；第一 tick 播一次蓄力音。最后一 tick 结算：爆裂特效 + 129 颗向外炸开的 wisp +
 * {@code thaumcraft:craftfail} 的替代音效，再 {@code overthrow} 把目标丢进<b>当前维度</b>的虚空
 *（X/Z 各 ±10001 随机、Y = -100000 ± 10001）：非玩家目标搬完立即抹除，玩家目标留在虚空等死并
 * 全服广播 type 1 消息（"into the Void."），最后进 30 tick 共用冷却。
 *
 * <p>1.21.1 对应：{@code onItemRightClick / onUsingTick / getMaxItemUseDuration / EnumAction.bow} →
 * {@code use / onUseTick / getUseDuration / UseAnim.BOW}；<b>「从背包法杖抽 Vis」没有对应 API</b>
 *（见 {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1），按本模组统一约定改成 {@link FRRechargable}
 * 的物品自身充能；以玩家为键的静态 map {@code targetList} → {@link FRDataComponents#VOID_GRIMOIRE_TARGET}；
 * 原版自定义网络包（{@code PacketVoidMessage / BurstMessage / EntityMotionMessage}）全部改由服务端直接
 * 生成粒子 / 改速度并靠原版同步送达；音效 {@code forgottenrelics:sound.mdcharge} → {@link FRSounds#MD_CHARGE}、
 * {@code thaumcraft:craftfail} → {@link SoundEvents#FIRE_EXTINGUISH}，都过 {@link SoundHelper#play} 压音量。
 *
 * <p>{@code voidGrimoireEnabled} 总开关<b>没有移植</b>：它原版只包住研究词条的注册，而 1.21.1 的研究
 * 只能写数据包 JSON、没有 Java 注册/注销 API（与 {@code falseJusticeEnabled} 同样处理），想禁用只能改
 * {@code research_entry/void_grimoire.json}。
 *
 * <p><b>与 1.7.10 的偏差</b>：
 * <ol>
 *   <li>原版 {@code onUsingTick} 两侧都跑（客户端也自己改速度、加缓慢），这里<b>玩法逻辑</b>只在服务端跑，
 *       可见结果一致；<b>但每 tick 的收束粒子仍照原版放在客户端本地生成</b>（{@code channelingParticles}），
 *       否则只能逐颗发包（见 {@link #channelingParticles} 的说明）；</li>
 *   <li>原版的 {@code localCooldown = 60} 是纯客户端防连点，这里只保留服务端权威的 30 tick 共用冷却；</li>
 *   <li>原版把 {@code noClip = true}（1.21.1 里字段名为 {@code noPhysics}）写在目标身上后<b>从不复位</b>：
 *       引导被提前打断（松手、Vis 耗尽、目标消失）时目标会永久穿墙。这是 1.7.10 自身的缺陷，按
 *       「以 1.7.10 为准」<b>逐字保留</b>。另外非玩家目标 {@code setPosition + setDead()} →
 *       {@code teleportTo(x, y, z) + discard()}，玩家目标走 {@code ServerPlayer#teleportTo(ServerLevel, ..)}
 *       保证客户端被同步；</li>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerTick.lore} + 各要素成本）依赖 {@code GuiScreen.isCtrlKeyDown}，
 *       而共享基类 {@link FRItem} 只实现 Shift 展开，这里保持一致。</li>
 * </ol>
 *
 * <p><b>Vis 折算</b>：原版每 tick 抽秩序（Ordo）9 + 混沌（Perditio）16 = 25 厘 Vis = 0.25 点/tick，
 * 即 <b>5 点/秒</b>；充能是整数，故直接取 5，并在每一秒的第一 tick 扣一次（与永恒放逐之诫、核子之怒
 * 同一扣费节奏），一次完整引导（100 tick）合计扣 5 次 = 25 点。
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
        Integer targetId = stack.get(FRDataComponents.VOID_GRIMOIRE_TARGET.get());

        // 客户端：只负责引导期间每 tick 的收束粒子。1.7.10 原版这段粒子本来就只在客户端生成
        //（onUsingTick 两侧都跑，而服务端的 spawnParticle 是空操作），这里照原版走本地 addParticle：
        // 零网络开销，每颗的颜色 / 尺寸 / 初速与 RE 逐字一致。
        // 旁观者客户端也会跑 onUseTick——LivingEntity#onSyncedDataUpdated 收到
        // DATA_LIVING_ENTITY_FLAGS 变化后会为观察者设好 useItem / useItemRemaining，
        // 且数据组件随装备包同步——所以其他玩家同样看得到。
        if (level.isClientSide()) {
            if (targetId != null && level.getEntity(targetId) instanceof LivingEntity clientTarget) {
                channelingParticles(level, particleAnchor(clientTarget));
            }
            return;
        }

        // 其余（抽 Vis、定身、上浮、结算）全部只在服务端跑，客户端交给服务端同步。
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

        // 粒子与爆裂的基准点：原版 Vector3.fromEntityCenter(target)，再 y += 0.03。
        Vec3 thisPos = particleAnchor(target);

        // 原版只在引导的第一个 tick（count == getMaxItemUseDuration()）播一次蓄力音。
        if (remainingUseDuration == duration) {
            SoundHelper.play(level, thisPos.x, thisPos.y, thisPos.z,
                    FRSounds.MD_CHARGE.get(), SoundSource.PLAYERS, 4.0F, 0.75F);
        }

        if (remainingUseDuration != 1) {
            return;
        }

        // ---- 引导的最后一 tick：结算 ----
        // 原版 SuperpositionHandler.imposeBurst(.., 2.0f)：Thaumcraft 的爆裂特效。
        voidBurst(server, thisPos);
        // 原版 PacketVoidMessage(.., true)：129 颗 wisp 向外炸开。
        finishBurst(server, thisPos);
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

    /** 粒子与爆裂的基准点：原版 {@code Vector3.fromEntityCenter(target)}，再 {@code y += 0.03}。 */
    private static Vec3 particleAnchor(LivingEntity target) {
        return target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D).add(0.0D, 0.03D, 0.0D);
    }

    /**
     * 引导期间每 tick 的 {@code PacketVoidMessage(.., false)} 渲染：
     * 在中心 ±6 内随机取 8 个点（{@code (rand-0.5)*12}），每颗
     * {@code wispFX(取点, r=0.2+rand*0.3, g=0, b=0.5+rand*0.2, size=0.2+rand*0.2,
     * 初速=(中心-取点)*0.08, maxAgeMul=0.45)} 让紫色 wisp 向内收束；
     * 再在中心撒 5 颗随机初速的原版传送门粒子（原版就是 {@code EntityPortalFX}）。
     *
     * <p><b>纯客户端本地粒子</b>：1.7.10 这段本来就写在客户端的 {@code onUsingTick} 里
     *（服务端的 {@code spawnParticle} 是空操作），所以这里逐颗 {@code addParticle}，
     * 与 RE 逐字一致且<b>一个包都不发</b>。此前按服务端 {@code sendParticles} 写时是每 tick
     * 13 个包（8 颗收束 wisp 各一包 + 5 颗传送门粒子各一包），即 §11 点名的带宽热点。
     *
     * <p>这类「初速方向随落点变化」的收束粒子无法用 {@code count > 0} 整簇发包表达：
     * 整簇的三个轴只能共用一个高斯初速标量（{@code ClientPacketListener#handleParticleEvent}）。
     */
    private static void channelingParticles(Level level, Vec3 center) {
        // 每颗颜色 / 尺寸 / 初速都不同，本地逐颗 addParticle 正好能精确指定初速。
        for (int i = 0; i < 8; i++) {
            double px = center.x + (level.random.nextDouble() - 0.5D) * 12.0D;
            double py = center.y + (level.random.nextDouble() - 0.5D) * 12.0D;
            double pz = center.z + (level.random.nextDouble() - 0.5D) * 12.0D;
            FRParticles.wisp(level, px, py, pz,
                    0.2F + level.random.nextFloat() * 0.3F,
                    0.0F,
                    0.5F + level.random.nextFloat() * 0.2F,
                    0.2F + level.random.nextFloat() * 0.2F,
                    (center.x - px) * 0.08D,
                    (center.y - py) * 0.08D,
                    (center.z - pz) * 0.08D,
                    0.45F);
        }
        for (int i = 0; i < 5; i++) {
            level.addParticle(ParticleTypes.PORTAL, center.x, center.y, center.z,
                    (level.random.nextDouble() - 0.5D) * 8.0D,
                    (level.random.nextDouble() - 0.5D) * 8.0D,
                    (level.random.nextDouble() - 0.5D) * 8.0D);
        }
    }

    /**
     * 引导结束的 {@code PacketVoidMessage(.., true)}：{@code i <= 128} 即 129 颗
     * {@code wispFX(中心, r=0.2+rand*0.3, g=0, b=0.5+rand*0.2, size=0.4+rand*0.4,
     * xm/ym/zm=(rand-0.5)*0.5, maxAgeMul=1.0)} 向外炸开。
     *
     * <p>颜色 / 尺寸各抽一次（原版逐颗随机），初速幅度按 {@code 0.5/√12 ≈ 0.144} 折算。
     * 这一处保留服务端广播：整簇<b>只发一个包</b>，改成客户端逐颗反而多花客户端 CPU。
     */
    private static void finishBurst(ServerLevel level, Vec3 center) {
        FRParticles.serverWispBurst(level, center.x, center.y, center.z,
                0.2F + level.random.nextFloat() * 0.3F,
                0.0F,
                0.5F + level.random.nextFloat() * 0.2F,
                0.4F + level.random.nextFloat() * 0.4F, 1.0F,
                129, 0.0D, 0.144D);
    }

    /**
     * 对应原版 {@code SuperpositionHandler.imposeBurst(world, dim, x, y, z, 2.0f)}
     *（Thaumcraft 的 {@code proxy.burst}）。1.21.1 没有等价特效，这里用「1 颗闪光 +
     * 一簇向外炸开的紫色 effect 粒子」近似，向外速度随 size 放大。
     */
    private static void voidBurst(ServerLevel level, Vec3 center) {
        // 原版 imposeBurst(.., 2.0f) → 模组自带的 FXBurst（青绿加法柔光精灵），不是白色方片。
        FRParticles.serverWispBurst(level, center.x, center.y, center.z,
                0.0F,
                (float) (0.8D + level.random.nextDouble() * 0.2D),
                (float) (0.4D + level.random.nextDouble() * 0.6D),
                2.0F, 1.0F, 1, 0.0D, 0.0D);
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
