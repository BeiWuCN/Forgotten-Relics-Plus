package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntitySoulEnergy;
import com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;

/**
 * 千咒之诫（Edict of a Thousand Damned Souls），注册名 {@code soul_tome}，
 * 1.7.10 原版 {@code ItemSoulTome}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>右键 {@code setItemInUse(stack, 72000)} 进入 {@code EnumAction.bow} 的拉弓姿态，
 *       <b>没有施法后冷却</b>，法术强度完全来自持续引导；</li>
 *   <li>{@code onUsingTick} 每 tick 无条件把玩家的 {@code motionX / motionZ} 清零，
 *       也就是引导期间锁住水平移动（对应词条 "You are greatly slowed down while casting"）；</li>
 *   <li>引导满 20 tick 后开始结算，其中<b>两件事彼此独立</b>：
 *     <ol>
 *       <li><b>近距离击退</b>（每 tick）：对 20 格内、且距玩家 &lt;= 3.0 格的每个活体，
 *           各抽一次法杖 Vis（火 150 + 混沌 120 厘 = 2.70 点），抽得出来才画 4 道自定义闪电、
 *           播 {@code thaumcraft:zap}、造成 {@code DamageSourceTLightning} 的
 *           {@code 20 + 80 × random} 伤害，并把它朝远离玩家的方向击飞（竖直方向额外 +1.0）；</li>
 *       <li><b>灵魂抽取</b>（每 4 tick）：抽一次法杖 Vis（土 25 + 风 20 + 火 35 + 混沌 50 厘 = 1.30 点），
 *           从 20 格内的活体里随机挑一个，造成 {@code 目标最大生命 / soulTomeDivisor} 的
 *           {@code DamageSourceSoulDrain} 伤害（夹在 1.0 ~ 20.0 之间），并从该目标处生成一颗
 *           追踪玩家的灵魂能量 {@code EntitySoulEnergy}——命中玩家时治疗 1 点并补 1 点饥饿；</li>
 *     </ol>
 *   </li>
 *   <li>物品堆叠上限 1，稀有度 EPIC，{@code getWarp} 返回 <b>3</b>。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code getMaxItemUseDuration} → {@code Item#getUseDuration}；{@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见
 *       {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。按本模组统一约定改成
 *       {@link FRRechargable} 的<b>物品自身充能</b>，用 {@link RechargeAccess#consumeCharge} 扣除：</li>
 *   <ul>
 *     <li>灵魂抽取每次 1 点（原版 1.30 取整）；</li>
 *     <li>击退每命中一个实体 2 点（原版 2.70，与 RE 的取值一致）。</li>
 *   </ul>
 *   <li><b>击退闪电（1.6.2 重做）</b>：原版 {@code Main.proxy.lightning(...)} 在
 *       {@code for (counterZ = 0; counterZ <= 3; ++counterZ)} 里连画 4 道（客户端是 Thaumcraft
 *       {@code FXLightningBolt}），起点 {@code (player.x, player.y + 1.0, player.z)}、
 *       终点目标身体中心、宽度 0.075。本项目改成 {@link FRBoltParticleData#broadcast}
 *       走原版粒子包把两端送到客户端，再由 {@code client/FRBolts} 交给 Botania 的
 *       {@code BoltRenderer} 画真正的折线闪电；</li>
 *   <li><b>领域结界环</b>（1.6.2 新增）：1.7.10 原版<b>没有</b>这个视觉，是 RE 补的
 *       （{@code ItemSoulTome#renderAuraBoundary}，客户端 {@code onUpdate} 里画两圈白色粒子，
 *       模仿 Botania 盖亚守护者的竞技场边界）。本项目此前完全没实现，玩家反馈「领域展开没有结界」，
 *       所以按 RE 的两圈照做：外圈半径 = 灵魂抽取搜索半径（20 格，每 8° 一颗白色 wisp），
 *       内圈半径 = 击退判定半径（每 16° 一颗淡粉白 sparkle）。RE 用的就是 Botania 的
 *       {@code wispFX / sparkleFX}，这里直接用 {@link FRParticles#wisp}（白色、尺寸 0.5、
 *       maxAgeMul 0.8）与 {@link FRParticles#sparkle}（{@code (1.0, 0.9, 0.9)}、尺寸 2.0、m=4）复刻；</li>
 *   <li>音效 {@code thaumcraft:zap} 换成原版等价物 {@link SoundEvents#FIREWORK_ROCKET_BLAST}
 *       （与霹雳咒书、腥红之咒同一替代方案）；生成灵魂能量时的 {@code botania:missile}
 *       在 Botania 里就是 {@link BotaniaSounds#MISSILE}，直接引用；命中时的 {@code random.fizz}
 *       换成 {@link SoundEvents#FIRE_EXTINGUISH}（与邪术之咒、核子之怒同一替代方案）。
 *       所有音效都过 {@link SoundHelper#play} 统一压低音量；</li>
 *   <li>原版 {@code DamageSourceSoulDrain} → {@link FRDamageTypes#SOUL_DRAIN}（仓库已有）；
 *       {@code DamageSourceTLightning} → {@link FRDamageTypes#TRUE_LIGHTNING}（霹雳咒书已有）。</li>
 * </ul>
 *
 * <p><b>与原版的两处偏差</b>：
 * <ol>
 *   <li>原版的击退与灵魂抽取都是「先抽法杖 Vis，抽得出来才生效」。这里改成物品充能后含义相同，
 *       但整数充能无法表示 1.30 / 2.70，分别取 1 与 2；</li>
 *   <li>原版 {@code getDistanceToEntity(entity) <= 3.0f} 是<b>实体中心距离</b>，而 1.7.10 自己的词条
 *       写的是 "closer than 4 blocks"。本项目以 1.7.10 代码为准，取 3.0（词条仍照 1.7.10 原文写 4）。</li>
 * </ol>
 */
public class ItemSoulTome extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000。 */
    private static final int USE_DURATION = 72000;

    /** 原版 {@code searchRange = 20}：灵魂抽取的搜索半径（格）。 */
    private static final int SEARCH_RANGE = 20;

    /**
     * 原版击退判定 {@code player.getDistanceToEntity(entity) <= 3.0f}。
     *
     * <p>这是实体中心距离；1.7.10 的词条写的是 "closer than 4 blocks"，
     * 属于原版自身的文字与代码不一致，本项目按代码复刻（见类注释「偏差」第 2 条）。
     */
    private static final double KNOCKBACK_RANGE = 3.0D;

    /** 原版灵魂能量命中时的回血与补饥饿量：{@code heal(1.0)} / {@code addStats(1, 1.0)}。 */
    private static final float SOUL_HEAL = 1.0F;
    private static final int SOUL_FOOD = 1;
    private static final float SOUL_FOOD_SATURATION = 1.0F;

    /** RE 结界外圈（wisp）每颗的水平随机初速幅度：原版 {@code m = 0.15f}。 */
    private static final double BOUNDARY_HORIZONTAL_MOTION = 0.15D;

    /** RE 结界外圈每颗的竖直随机初速幅度：原版 {@code mv = 0.35f}。 */
    private static final double BOUNDARY_VERTICAL_MOTION = 0.35D;

    /** RE 结界内圈（sparkle）的颜色 {@code (1.0, 0.9, 0.9)} 与尺寸 {@code 2.0}。 */
    private static final float BOUNDARY_INNER_RED = 1.0F;
    private static final float BOUNDARY_INNER_GREEN = 0.9F;
    private static final float BOUNDARY_INNER_BLUE = 0.9F;
    private static final float BOUNDARY_INNER_SIZE = 2.0F;

    /** 原版 {@code count % 4 == 0}：灵魂抽取的节奏（每 4 tick 一次）。 */
    private static final int DRAIN_INTERVAL = 4;

    public ItemSoulTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 单次灵魂抽取的 Vis 消耗：原版基础值 1 乘 {@code soulTomeVisMult}。 */
    public int getVisCost() {
        return (int) (FRConfig.SOUL_TOME_VIS_COST.get() * FRConfig.SOUL_TOME_VIS_MULT.get());
    }

    /** 击退每命中一个实体扣一次的 Vis 消耗：原版基础值 2 乘 {@code soulTomeVisMult}。 */
    public int getKnockbackVisCost() {
        return (int) (FRConfig.SOUL_TOME_KNOCKBACK_VIS_COST.get() * FRConfig.SOUL_TOME_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SOUL_TOME_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.SOUL_TOME_WARP.get();
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
        ItemStack stack = player.getItemInHand(hand);
        // 原版 onItemRightClick 只是 setItemInUse(stack, 72000)，真正的节奏在 onUsingTick。
        // 客户端也一起进入姿态，这样弓的拉扯动画才会出现（与原初混沌之典、核子之怒同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 原版 onUsingTick 无条件把 motionX / motionZ 清零：引导期间锁住水平移动。
        // 玩家的移动由客户端驱动，所以这一句在客户端也必须执行（服务端一并清零以保持一致）。
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(0.0D, motion.y, 0.0D);

        // 原版在客户端那份 onUpdate 里画领域结界环（Botania wisp / sparkle，纯客户端）。
        // 服务端不参与这一段，也不产生任何网络包。
        if (level.isClientSide()) {
            renderAuraBoundary(player);
            return;
        }
        // 原版条件 count < getMaxItemUseDuration() - 20：引导不足 20 tick 时不结算。
        int warmup = FRConfig.SOUL_TOME_WARMUP_TICKS.get();
        if (remainingUseDuration <= 0 || remainingUseDuration >= USE_DURATION - warmup) {
            return;
        }
        // 原版先搜一圈 20 格内的活体（再把自己剔除），这一 tick 的击退与灵魂抽取共用这个列表。
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(SEARCH_RANGE),
                entity -> entity != player && entity.isAlive());

        // 原版先做「近距离击退」，再做「灵魂抽取」。
        for (LivingEntity target : entities) {
            if (player.distanceTo(target) <= KNOCKBACK_RANGE
                    && RechargeAccess.consumeCharge(stack, player, getKnockbackVisCost())) {
                knockBack(level, player, target);
            }
        }

        // 原版 count % 4 == 0：每 4 tick 抽一次灵魂。
        if (remainingUseDuration % DRAIN_INTERVAL == 0) {
            if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
                return;
            }
            if (entities.isEmpty()) {
                return;
            }
            // 原版 entities.get((int)(entities.size() * Math.random()))。
            LivingEntity victim = entities.get((int) (entities.size() * level.random.nextDouble()));
            drainSoul(level, player, victim);
        }
    }

    /**
     * 原版那段「近距离击退」：抽到 Vis 后画闪电、播 zap、造成真雷伤害并把目标朝远离玩家的方向击飞。
     *
     * <p>扣费在调用方完成（原版是 {@code consumeVisFromInventory} 与判定写在同一个 {@code if} 里）。
     */
    private static void knockBack(Level level, Player player, LivingEntity target) {
        // 原版 Vector3.fromEntityCenter：以身体中心为基准算击退方向。
        Vec3 entityVec = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 playerVec = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        double distance = player.distanceTo(target);
        // 原版：diff = (entityVec - playerVec) * (1 / distance * 3.0)。
        Vec3 diff = entityVec.subtract(playerVec).scale(1.0D / distance * 3.0D);

        if (level instanceof ServerLevel server) {
            // 原版 for (counterZ = 0; counterZ <= 3; ++counterZ) 连画 4 道 Main.proxy.lightning：
            // 起点 (player.x, player.y + 1.0, player.z)、终点目标身体中心、宽度 0.075。
            FRBoltParticleData.broadcast(server, player.position().add(0.0D, 1.0D, 0.0D), entityVec,
                    FRBoltParticleData.WIDTH_MAIN, 4,
                    FRBoltParticleData.ARC_RED, FRBoltParticleData.ARC_GREEN, FRBoltParticleData.ARC_BLUE);
            // 原版 world.playSoundAtEntity(player, "thaumcraft:zap", 1.0F, 0.8F)。
            SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        double min = FRConfig.SOUL_TOME_KNOCKBACK_DAMAGE_MIN.get();
        double max = FRConfig.SOUL_TOME_KNOCKBACK_DAMAGE_MAX.get();
        target.hurt(FRDamageTypes.source(level, FRDamageTypes.TRUE_LIGHTNING, player),
                (float) (min + level.random.nextDouble() * (max - min)));
        // 原版把目标 motion 直接改写成 diff，竖直分量额外 +1.0。
        target.setDeltaMovement(diff.x, diff.y + 1.0D, diff.z);
    }

    /**
     * 原版那段「灵魂抽取」：按目标最大生命算伤害（夹在配置上下限之间），
     * 以夺魂伤害结算，再从该目标处生成一颗飞向施法者的灵魂能量。
     */
    private static void drainSoul(Level level, Player player, LivingEntity victim) {
        float soulDamage = victim.getMaxHealth() / (float) FRConfig.SOUL_TOME_DIVISOR.get().doubleValue();
        float min = FRConfig.SOUL_TOME_DAMAGE_MIN.get().floatValue();
        float max = FRConfig.SOUL_TOME_DAMAGE_MAX.get().floatValue();
        if (soulDamage > max) {
            soulDamage = max;
        } else if (soulDamage < min) {
            soulDamage = min;
        }
        victim.hurt(FRDamageTypes.source(level, FRDamageTypes.SOUL_DRAIN, player), soulDamage);
        spawnSoul(level, victim, player);
    }

    /**
     * 对应原版 {@code spawnSoul(world, source, target)}。
     *
     * <p><b>注意原版的参数命名有迷惑性</b>：原版调用是
     * {@code this.spawnSoul(world, randomEntity, player)}，方法内部的形参却叫 {@code player} 与 {@code target}。
     * 也就是说「出生点与初速用的是<b>受害者</b>的位置与视线」，而追踪目标是<b>施法者</b>。
     * 这里把形参改名成 {@code source} / {@code target} 以免误读，行为逐字保留。
     */
    private static void spawnSoul(Level level, LivingEntity source, LivingEntity target) {
        if (level.isClientSide()) {
            return;
        }
        // 原版：源实体身体中心 + 其视线 × 1.0，再抬高 0.5；初速 = 其视线 × 1.25。
        Vec3 center = source.position().add(0.0D, source.getBbHeight() * 0.5D, 0.0D);
        Vec3 look = source.getLookAngle();
        Vec3 spawn = center.add(look.scale(1.0D)).add(0.0D, 0.5D, 0.0D);

        EntitySoulEnergy orb = new EntitySoulEnergy(level, source, target);
        orb.setPos(spawn.x, spawn.y, spawn.z);
        orb.setDeltaMovement(look.scale(1.25D));
        // 原版 world.playSoundAtEntity(victim, "botania:missile", 2.0F, 0.8F + random * 0.2F)。
        SoundHelper.play(level, source.getX(), source.getY(), source.getZ(), BotaniaSounds.MISSILE,
                SoundSource.PLAYERS, 2.0F, 0.8F + level.random.nextFloat() * 0.2F);
        level.addFreshEntity(orb);
    }

    /**
     * 领域结界环，对应 RE {@code ItemSoulTome#renderAuraBoundary}（1.7.10 原版没有这一段）。
     *
     * <p>RE 的写法（客户端 {@code onUpdate}，只在引导这本典籍时执行）：
     * <ul>
     *   <li><b>外圈</b>：半径 {@code soulTomeSearchRange}（本项目的灵魂抽取搜索半径 20 格），
     *       {@code i += 8} 共 45 颗白色 wisp，尺寸 0.5，初速 {@code (±0.5)*0.15}、竖直 {@code (±0.5)*0.35}；</li>
     *   <li><b>内圈</b>：半径 {@code soulTomeKnockbackRange}，{@code i += 16} 共 23 颗
     *       {@code (1.0, 0.9, 0.9)} 的 sparkle，尺寸 2.0。</li>
     * </ul>
     *
     * <p>现代对应：RE 调的就是 Botania，这里直接用 {@link FRParticles#wisp} 与
     * {@link FRParticles#sparkle}（内部是 {@code WispParticleData} / {@code SparkleParticleData}），
     * 颜色、尺寸、初速、m 全部与 RE 逐字一致（外圈 maxAgeMul 0.8）。
     * 内圈半径用本类的 {@link #KNOCKBACK_RANGE} 常量而不是配置——因为本项目的击退判定就是这个常量
     * （见类注释「偏差」第 2 条），结界要画在真正会触发击退的那一圈上。
     *
     * <p>粒子只在客户端 {@code addParticle}，不走网络、不占带宽；引导期间每 tick 约 68 颗，
     * 与原版 RE 完全同量。
     */
    private static void renderAuraBoundary(Player player) {
        Level level = player.level();
        double y = player.getY();
        // 外圈：白色 wisp，每 8° 一颗。
        // RE：wispFX(x, y, z, 1.0, 1.0, 1.0, size=0.5,
        //          (rand-0.5)*0.15, (rand-0.5)*0.35, (rand-0.5)*0.15, maxAgeMul=0.8)。
        for (int i = 0; i < 360; i += 8) {
            double rad = Math.toRadians(i);
            FRParticles.wisp(level,
                    player.getX() - Math.cos(rad) * SEARCH_RANGE,
                    y,
                    player.getZ() - Math.sin(rad) * SEARCH_RANGE,
                    1.0F, 1.0F, 1.0F, 0.5F,
                    (player.getRandom().nextDouble() - 0.5D) * BOUNDARY_HORIZONTAL_MOTION,
                    (player.getRandom().nextDouble() - 0.5D) * BOUNDARY_VERTICAL_MOTION,
                    (player.getRandom().nextDouble() - 0.5D) * BOUNDARY_HORIZONTAL_MOTION,
                    0.8F);
        }
        // 内圈：淡粉白 sparkle，每 16° 一颗。
        // RE：sparkleFX(x, y, z, 1.0, 0.9, 0.9, size=2.0, m=4)。
        for (int i = 0; i < 360; i += 16) {
            double rad = Math.toRadians(i);
            FRParticles.sparkle(level,
                    player.getX() - Math.cos(rad) * KNOCKBACK_RANGE,
                    y,
                    player.getZ() - Math.sin(rad) * KNOCKBACK_RANGE,
                    BOUNDARY_INNER_RED, BOUNDARY_INNER_GREEN, BOUNDARY_INNER_BLUE,
                    BOUNDARY_INNER_SIZE, 4);
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：1~3、空行、4~5、空行、6~8、空行、9。
        tooltip.add(Component.translatable("item.ItemSoulTome1.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome2.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome4.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome6.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome7.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome8.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSoulTome9.lore"));
    }
}
