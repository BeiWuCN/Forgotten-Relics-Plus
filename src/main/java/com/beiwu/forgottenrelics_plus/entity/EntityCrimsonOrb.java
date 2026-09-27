package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 腥红之咒的猩红法球（Crimson Orb），1.7.10 原版 {@code EntityCrimsonOrb}。
 *
 * <p>原版逻辑（{@code EntityThrowable} 子类，{@code getGravityVelocity} 返回 0）：
 * <ul>
 *   <li>无重力飞行，每 tick 做 0.99 的速度衰减；</li>
 *   <li><b>追踪</b>：若已有目标，就朝目标（瞄准点取目标身高的 60% 处）每 tick 叠加
 *       一个单位向量的 0.3 倍加速度，然后把速度的三个分量各自夹到 ±0.25；
 *       发射后的前 5 tick 里若竖直速度为负，强制取绝对值（先向上抬一下再追）；
 *       若目标为空（初始就没锁定，或目标已死 / 已卸载）则重新在 32 格内搜一个「能看见法球」的活体，
 *       且这一 tick 不做加速与限速；</li>
 *   <li>生存时限 1000 tick（原版 {@code ticksExisted > 1000} 移除），另有 {@code red} 开关控制是否
 *       立即消失——那个字段只有物品发射时传 {@code true} 一种取值，这里直接省掉；</li>
 *   <li>命中实体（且发射者非空）时造成 <b>MIN ~ MAX 之间随机</b>的遗落魔法伤害（原版 42~100），
 *       命中实心方块时同样放一次「放电」表现，随后消失；</li>
 *   <li>撞到草丛（{@code BlockBush}）、树叶（{@code BlockLeaves}）、液体（{@code BlockLiquid}）时
 *       <b>直接穿过去</b>，不结算也不消失；</li>
 *   <li>被玩家攻击时可以「反弹」：速度被改成攻击者视线方向 × 0.9，并播放 {@code thaumcraft:zap}。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}（无重力、可追踪、生存时限、命中钩子），
 *       追踪加速度直接由 {@link FRHomingProjectile#homingStrength()} 提供；</li>
 *   <li>原版的 {@code IEntityAdditionalSpawnData}（把目标 id / 发射者 id / red 写进生成包）
 *       在 1.21.1 由基类的 {@code SynchedEntityData} 承担，两端都能解析出目标实体，
 *       不必自己写生成包，所以这里没有构造器同步字段；</li>
 *   <li>原版命中时调的 {@code SuperpositionHandler.imposeBurst} 是<b>纯客户端粒子爆发</b>
 *       （自定义网络包 → {@code Thaumcraft.proxy.burst}），<b>没有任何范围伤害</b>。
 *       1.7.10 这一段不是 Botania 粒子，但 RE 的 {@code EntityCrimsonOrb#spawnHitParticles}
 *       已经把它换成了 Botania：<b>15 颗猩红 wisp + 1 颗 sparkle</b>，本项目照 RE 复刻；</li>
 *   <li>拖尾：1.7.10 这个实体没有拖尾粒子（形体由 {@code RenderCrimsonOrb} 画），
 *       是 RE 在客户端 {@code onUpdate} 里补的 Botania sparkle 轨迹，本项目同样照 RE 复刻；</li>
 *   <li>原版放电音 {@code thaumcraft:shock} 换成原版等价物 {@link SoundEvents#LIGHTNING_BOLT_IMPACT}
 *       （与霹雳咒书命中音同一替代方案），反弹音 {@code thaumcraft:zap} 换成
 *       {@link SoundEvents#FIREWORK_ROCKET_BLAST}（与霹雳咒书发射音同一替代方案）；</li>
 *   <li>原版 {@code DamageSourceMagic} → {@link FRDamageTypes#FORGOTTEN_MAGIC}；</li>
 *   <li>原版 {@code BlockLeaves} / {@code BlockLiquid} 同样用标签与流体状态判断，语义一致。</li>
 * </ul>
 *
 * <p><b>一处已知偏差</b>：原版在目标刚死时会立刻改追新目标（改不到就继续追那个已死目标的
 * 最后位置）；这里的 {@code resolvedTarget()} 对已死目标直接返回 {@code null}，于是那一 tick
 * 只重新搜目标、不加速，下一 tick 才继续追。差异只有 1 tick，且目标已死，观感不可辨。
 */
public class EntityCrimsonOrb extends FRHomingProjectile {

    /** 原版 {@code getNewTarget} 的搜索半径（格）。 */
    private static final double SEARCH_RANGE = 32.0D;

    /** 原版每 tick 朝目标叠加的加速度（{@code * 0.3}）。 */
    private static final double HOMING_STRENGTH = 0.3D;

    /** 原版把速度三分量各自夹在 ±0.25。 */
    private static final double MAX_SPEED = 0.25D;

    /** 原版：前 5 tick 若在下降就强制上抬。 */
    private static final int LIFT_TICKS = 5;

    /**
     * 生存时限。原版在 {@code ticksExisted > 1000} 时移除；基类判的是
     * {@code tickCount > maxLifeTicks()}，两者对齐（tickCount 与 ticksExisted 同起点）。
     */
    private static final int MAX_LIFE_TICKS = 1000;

    public EntityCrimsonOrb(EntityType<? extends EntityCrimsonOrb> type, Level level) {
        super(type, level);
    }

    /** 由物品发射：记录发射者与初始目标（位置与初速由物品的 {@code spawnOrb} 设置）。 */
    public EntityCrimsonOrb(Level level, LivingEntity caster, LivingEntity target) {
        super(FREntities.CRIMSON_ORB.get(), level);
        setOwner(caster);
        setTarget(target);
    }

    @Override
    protected double homingStrength() {
        return HOMING_STRENGTH;
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    /**
     * 基类负责飞行、生存时限与朝目标加速；这里补上原版额外的两步——
     * 速度分量限幅、以及发射初期的强制上抬——并在没有目标时重新搜索。
     */
    @Override
    public void tick() {
        super.tick();
        if (isRemoved() || level().isClientSide()) {
            return;
        }
        if (resolvedTarget() == null) {
            // 原版：目标为空（初始未锁定 / 已死 / 已卸载）时只重新搜索，这一 tick 不做限速。
            getNewTarget();
            return;
        }
        Vec3 motion = getDeltaMovement();
        double x = Mth.clamp(motion.x, -MAX_SPEED, MAX_SPEED);
        double y = Mth.clamp(motion.y, -MAX_SPEED, MAX_SPEED);
        double z = Mth.clamp(motion.z, -MAX_SPEED, MAX_SPEED);
        // 原版顺序是先限速、再上抬。
        if (tickCount < LIFT_TICKS && y < 0.0D) {
            y = Math.abs(y);
        }
        setDeltaMovement(x, y, z);
    }

    /** 原版 {@code getNewTarget}：32 格内、能看见法球、排除发射者的活体里随机挑一个。 */
    private void getNewTarget() {
        LivingEntity caster = getOwner() instanceof LivingEntity living ? living : null;
        List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(SEARCH_RANGE),
                e -> e != caster && e.isAlive() && e.hasLineOfSight(this));
        // 原版还会再 remove(caster) 一次，这里一并保留语义。
        if (caster != null) {
            candidates.remove(caster);
        }
        if (!candidates.isEmpty()) {
            setTarget(candidates.get(random.nextInt(candidates.size())));
        }
        // 一个都没搜到时，原版保持原目标不变；这里同样什么都不做。
    }

    /**
     * 拖尾：照抄 RE {@code EntityCrimsonOrb#onUpdate} 客户端分支——
     * 从上一 tick 位置到当前位置按 0.05 格采样，每一步一颗 sparkle
     * （{@code r=0.8+rand*0.2、g=0.1+rand*0.2、b=rand*0.1、size=0.8、m=2}），
     * 另有 {@code 1/steps} 的概率在 ±0.4 的偏移处再补一颗同参数 sparkle。
     *
     * <p>对应 {@link FRParticles#sparkle}（纯客户端 addParticle，无网络开销）。
     */
    @Override
    protected void spawnTrailParticles() {
        Vec3 from = new Vec3(xo, yo, zo);
        Vec3 to = position();
        Vec3 diff = to.subtract(from);
        double length = diff.length();
        if (length < TRAIL_STEP) {
            return;
        }
        int steps = (int) (length / TRAIL_STEP);
        Vec3 step = diff.scale(TRAIL_STEP / length);
        Vec3 pos = from;
        for (int i = 0; i < steps; i++) {
            float r = 0.8F + random.nextFloat() * 0.2F;
            float g = 0.1F + random.nextFloat() * 0.2F;
            float b = random.nextFloat() * 0.1F;
            FRParticles.sparkle(level(), pos.x, pos.y, pos.z, r, g, b, 0.8F, 2);
            if (random.nextInt(steps) <= 1) {
                FRParticles.sparkle(level(),
                        pos.x + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.y + (random.nextDouble() - 0.5D) * 0.4D,
                        pos.z + (random.nextDouble() - 0.5D) * 0.4D,
                        r, g, b, 0.8F, 2);
            }
            pos = pos.add(step);
        }
    }

    /**
     * 整段覆写基类的 {@code onHit}：基类对任何命中都会 {@code discard()}，
     * 但原版撞到草丛 / 树叶 / 液体时要<b>穿过去</b>，所以这几类必须提前返回、既不结算也不销毁。
     */
    @Override
    protected void onHit(HitResult result) {
        if (result instanceof BlockHitResult blockHit
                && isPenetrable(level().getBlockState(blockHit.getBlockPos()))) {
            return;
        }
        if (!level().isClientSide()) {
            onImpact(result);
        }
        discard();
    }

    /**
     * 原版 {@code func_70184_a}：实体命中与实心方块命中共用同一套「放电」表现。
     *
     * <p>基类的 {@code onHit} 已被本类覆写并负责 {@code discard()}，这里只管结算。
     */
    @Override
    protected void onImpact(HitResult result) {
        Entity owner = getOwner();
        if (result instanceof EntityHitResult entityHit) {
            // 原版这段只在 getThrower() != null 时执行；本模组里发射者恒为玩家。
            if (owner == null) {
                return;
            }
            double min = FRConfig.CRIMSON_SPELL_DAMAGE_MIN.get();
            double max = FRConfig.CRIMSON_SPELL_DAMAGE_MAX.get();
            float damage = (float) (min + random.nextDouble() * (max - min));
            entityHit.getEntity().hurt(FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner), damage);
        }
        shock();
    }

    /** 原版命中时的 {@code thaumcraft:shock} + {@code imposeBurst}（纯粒子，无范围伤害）。 */
    private void shock() {
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        if (level() instanceof ServerLevel server) {
            // RE #spawnHitParticles 的 15 颗猩红 wisp：
            // wispFX(hx, hy, hz, 0.8+rand*0.2, 0.1+rand*0.2, rand*0.1,
            //        size=0.1+rand*0.3, xm/ym/zm=(rand-0.5)*0.15, maxAgeMul=0.9)。
            // 逐颗单独发包以保留「每颗颜色/尺寸都不同」的原版观感（旧版同样是一颗一颗 addParticle）。
            for (int i = 0; i < 15; i++) {
                FRParticles.serverWisp(server, getX(), getY(), getZ(),
                        0.8F + random.nextFloat() * 0.2F,
                        0.1F + random.nextFloat() * 0.2F,
                        random.nextFloat() * 0.1F,
                        0.1F + random.nextFloat() * 0.3F,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        0.9F);
            }
            // RE 末尾那一颗 sparkleFX(hx, hy, hz, 1.0, 0.2, 0.1, size=2.0, m=4)。
            FRParticles.serverSparkle(server, getX(), getY(), getZ(), 1.0F, 0.2F, 0.1F, 2.0F, 4);
        }
    }

    /**
     * 原版 {@code attackEntityFrom}：法球本身不吃伤害，而是被「打飞」——
     * 速度改成攻击者视线方向 × 0.9，并播放 {@code thaumcraft:zap}。
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        setDeltaMovement(attacker.getLookAngle().scale(0.9D));
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        return true;
    }

    /** RE 拖尾的采样步长：{@code diff.normalize().multiply(0.05)}。 */
    private static final double TRAIL_STEP = 0.05D;

    /** 原版 {@code BlockBush || BlockLeaves || BlockLiquid} 三类放行。 */
    private static boolean isPenetrable(BlockState state) {
        return state.getBlock() instanceof BushBlock
                || state.is(BlockTags.LEAVES)
                || !state.getFluidState().isEmpty();
    }
}
