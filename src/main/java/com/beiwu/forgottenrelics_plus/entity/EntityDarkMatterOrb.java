package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 邪术之咒的暗物质法球（Dark Matter Orb），1.7.10 原版 {@code EntityDarkMatterOrb}。
 *
 * <p>原版逻辑（{@code EntityThrowable} 子类，{@code getGravityVelocity} 返回 0）：
 * <ul>
 *   <li>无重力沿初始方向直线飞行，每个 tick 只做 0.99 的速度衰减；</li>
 *   <li>{@code ticksExisted >= 200} 时静静消失（<b>不结算伤害</b>）；
 *       {@code ticksExisted >= 100} 且速度的三个分量都小于 {@code 0.01} 时<b>原地自爆</b>；</li>
 *   <li>撞到方块时，若方块是 {@code BlockBush} / {@code BlockLeaves} / {@code BlockLiquid}
 *       就直接返回，<b>穿过</b>继续飞；否则冒烟消失、不造成伤害；</li>
 *   <li>命中实体（或原地自爆）时，对<b>自身碰撞箱外扩 1.0 格</b>内的所有
 *       {@code EntityLivingBase}（排除发射者）造成一次暗物质魔法伤害，并施加三种负面效果，
 *       外域（Outer Lands）里伤害与效果都更强。原版这段范围结算只在
 *       {@code getThrower() != null} 时执行。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}（无重力、有生存时限、命中钩子），
 *       但本实体<b>不追踪</b>（{@code homingStrength} 保持 0）；</li>
 *   <li>原版 {@code onImpact} 里「草丛/树叶/液体穿过」的写法，1.21.1 的射线检测默认
 *       只命中带碰撞箱的方块——液体与草丛本来就不拦射线，只有<b>树叶</b>需要显式放行。
 *       即便如此这里仍按原版的三类逐条判断，语义保持一致；</li>
 *   <li>原版用 {@code Thaumcraft.proxy.wispFXEG} 画拖尾、用状态码 16 让客户端爆出 30 个怨灵粒子；
 *       这两处都是 <b>Thaumcraft 自己的粒子</b>，但 RE 的 {@code EntityDarkMatterOrb} 已经把它们
 *       换成了 Botania 的 {@code wispFX}，本项目照 RE 复刻：
 *       拖尾每 tick 2 颗 {@code (0.05, 0.05, 0.1)} 的小 wisp，命中/冒烟时 30 颗 {@code (0.1, 0.1, 0.15)}
 *       的 wisp 再叠一颗 {@code (0.4, 0.4, 0.6)} 的 sparkle；</li>
 *   <li>原版冒烟音效是 {@code random.fizz}，这里换成等价的原版 {@link SoundEvents#FIRE_EXTINGUISH}；</li>
 *   <li>{@code Config.dimensionOuterId} → Thaumaturge 的 {@link OuterLands#DIMENSION}；</li>
 *   <li>原版 {@code DamageSourceDarkMatter} → {@link FRDamageTypes#DARK_MATTER}。</li>
 * </ul>
 *
 * <p><b>与原版的两处已知偏差</b>：
 * <ol>
 *   <li>原版那段判定写的是 {@code double absMotionX = this.field_70159_w;} 然后单独调
 *       {@code Math.abs(absMotionX);} —— 反编译出来 {@code Math.abs} 的返回值被丢掉了，
 *       实际比的是<b>带符号</b>的三个分量。这里按注释描述的本意（速度几乎为零）取绝对值判断，
 *       两者只在「朝负方向高速飞行」这种极端情形下才有区别，而速度衰减到 0.01 需要 500 tick，
 *       早已超过 200 tick 的生存时限，所以实际几乎不可触发；</li>
 *   <li>原版在命中实体后把 {@code ticksExisted} 置 199、下一 tick 才死；这里结算完立即
 *       {@code discard()}，观感一致。</li>
 * </ol>
 */
public class EntityDarkMatterOrb extends FRHomingProjectile {

    /** 原版 {@code ticksExisted >= 100} 起才参与「原地自爆」判定。 */
    private static final int SELF_DESTRUCT_TICK = 100;

    /**
     * 生存时限。原版在 {@code ticksExisted >= 200} 时移除；基类判的是
     * {@code tickCount > maxLifeTicks()}，所以这里给 199，正好在 tickCount 到 200 时移除。
     */
    private static final int MAX_LIFE_TICKS = 199;

    /** 原版 {@code this.field_70121_D.func_72314_b(1.0, 1.0, 1.0)}：命中判定范围为碰撞箱外扩 1 格。 */
    private static final double EXPLOSION_RADIUS = 1.0D;

    /** 判定「几乎静止」的阈值，原版写 0.01。 */
    private static final double STOPPED_EPSILON = 0.01D;

    public EntityDarkMatterOrb(EntityType<? extends EntityDarkMatterOrb> type, Level level) {
        super(type, level);
    }

    /** 由物品发射：从视线前方 1.0 格、抬高 0.5 处出现，速度是视线的 1.5 倍（原版 {@code spawnOrb}）。 */
    public EntityDarkMatterOrb(Level level, LivingEntity shooter) {
        super(FREntities.DARK_MATTER_ORB.get(), level);
        setOwner(shooter);
        Vec3 look = shooter.getLookAngle();
        Vec3 origin = shooter.position()
                .add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(1.0D));
        setPos(origin.x, origin.y + 0.5D, origin.z);
        setDeltaMovement(look.scale(1.5D));
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved() || level().isClientSide()) {
            return;
        }
        // 原版：tick >= 100 且速度几乎为 0 时原地自爆（走一次范围结算再消失）。
        Vec3 motion = getDeltaMovement();
        if (tickCount >= SELF_DESTRUCT_TICK
                && Math.abs(motion.x) < STOPPED_EPSILON
                && Math.abs(motion.y) < STOPPED_EPSILON
                && Math.abs(motion.z) < STOPPED_EPSILON) {
            detonate();
            discard();
        }
    }

    /**
     * 拖尾：照抄 RE {@code EntityDarkMatterOrb#onUpdate} 客户端分支的 2 颗 wisp——
     * <pre>
     *   wispFX(posX + fx, posY + fy + 0.22*height, posZ + fz,
     *          0.05, 0.05, 0.1, size=0.25,
     *          xm/ym/zm=(rand-0.5)*0.04, maxAgeMul=0.6)
     *   其中 fx/fy/fz = (rand - rand) * 0.25
     * </pre>
     * 1.7.10 这里用的是 Thaumcraft 的 {@code wispFXEG}（非 Botania），按 RE 的 Botania 版本复刻。
     */
    @Override
    protected void spawnTrailParticles() {
        for (int i = 0; i < 2; i++) {
            FRParticles.wisp(level(),
                    getX() + (random.nextDouble() - random.nextDouble()) * 0.25D,
                    getY() + 0.22D * getBbHeight() + (random.nextDouble() - random.nextDouble()) * 0.25D,
                    getZ() + (random.nextDouble() - random.nextDouble()) * 0.25D,
                    0.05F, 0.05F, 0.1F, 0.25F,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    (random.nextDouble() - 0.5D) * 0.04D,
                    0.6F);
        }
    }

    /** 原版 {@code EntityThrowable} 的碰撞列表会排除发射者，这里在实体命中筛选上补回同一条。 */
    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner() && super.canHitEntity(target);
    }

    /**
     * 整段覆写基类的 {@code onHit}：基类对任何命中都会 {@code discard()}，
     * 但原版撞到草丛/树叶/液体时要<b>穿过去</b>，所以这几类必须提前返回、既不结算也不销毁。
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
     * 原版 {@code func_70184_a}：方块命中只冒烟消失；其余（实体命中、原地自爆）才做范围结算。
     *
     * <p>基类的 {@code onHit} 已经负责 {@code discard()}，这里只管结算。
     */
    @Override
    protected void onImpact(HitResult result) {
        if (result instanceof BlockHitResult) {
            fizzle();
        } else {
            detonate();
        }
    }

    /** 原版命中实体 / 原地自爆的那段范围伤害与负面效果。 */
    private void detonate() {
        if (!(level() instanceof ServerLevel)) {
            return;
        }
        Entity owner = getOwner();
        // 原版整段结算都在 getThrower() != null 的前提下执行。
        if (owner != null) {
            boolean outerLands = level().dimension() == OuterLands.DIMENSION;
            DamageSource source = FRDamageTypes.source(level(), FRDamageTypes.DARK_MATTER, owner);
            float damage = outerLands
                    ? FRConfig.ELDRITCH_SPELL_DAMAGE_EX.get().floatValue()
                    : FRConfig.ELDRITCH_SPELL_DAMAGE.get().floatValue();
            List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(EXPLOSION_RADIUS), target -> target != owner && target.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(source, damage);
                applyDebuffs(target, outerLands);
            }
        }
        fizzle();
    }

    /**
     * 三种负面效果，两套数值逐字照抄原版
     * （原版字段 {@code Potion.field_76437_t / field_76421_d / field_82731_v}，
     * 对照 1.12.2 RE 版可读名依次为 {@code MobEffects.WEAKNESS / SLOWNESS / WITHER}）。
     */
    private static void applyDebuffs(LivingEntity target, boolean outerLands) {
        if (outerLands) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 320, 2));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 250, 3));
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
        }
    }

    /** 原版 {@code BlockBush || BlockLeaves || BlockLiquid} 三类放行。 */
    private static boolean isPenetrable(BlockState state) {
        return state.getBlock() instanceof BushBlock
                || state.is(BlockTags.LEAVES)
                || !state.getFluidState().isEmpty();
    }

    /** 冒烟消失：原版 {@code random.fizz}（0.5 音量）+ 状态码 16 的 30 个怨灵粒子。 */
    private void fizzle() {
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.NEUTRAL, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
        if (level() instanceof ServerLevel server) {
            // RE #spawnHitParticles：30 颗 wispFX(pos + fx, 0.1, 0.1, 0.15, size=0.25,
            //                                  xm/ym/zm = fx*0.5, maxAgeMul=0.8)，
            // 其中 fx/fy/fz = (rand - rand) * 0.3。位置抖动用 spread=0.3 近似，
            // 速度取 fx*0.5 的等效 gaussian 幅度（0.3*0.5*0.289 ≈ 0.043）。
            FRParticles.serverWispBurst(server, getX(), getY(), getZ(),
                    0.1F, 0.1F, 0.15F, 0.25F, 0.8F, 30, 0.3D, 0.043D);
            // 末尾那颗 sparkleFX(pos, 0.4, 0.4, 0.6, size=2.0, m=4)。
            FRParticles.serverSparkle(server, getX(), getY(), getZ(), 0.4F, 0.4F, 0.6F, 2.0F, 4);
        }
    }
}
