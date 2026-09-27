package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 霹雳咒书的雷电球，1.7.10 原版 {@code EntityThunderpealOrb}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>无重力直线飞行，500 tick 后自毁；</li>
 *   <li>命中实体时先对它造成 {@code damageThunderpealDirect} 的直接伤害；</li>
 *   <li>然后以落点为中心、{@code area}（原版初值 4，发射方 +2 变成 6）格内的活体各受一次
 *       {@code damageThunderpealBolt}，并从每个被击中者再向它 4 格内最多 3 个目标打出
 *       <b>一半伤害</b>的链式闪电；</li>
 *   <li>所有伤害都带「真雷」类型，并各自清空无敌帧，保证同一次落雷里的每一跳都能打实。</li>
 * </ul>
 *
 * <p>与原版的两处差异：
 * <ul>
 *   <li>原版的闪电是自定义网络包画出来的（{@code imposeLightning} / {@code imposeArcLightning}）；
 *       1.21.1 里改成原版粒子 {@code ELECTRIC_SPARK} 组成的电弧，视觉接近且不需要自定义渲染；</li>
 *   <li>原版用裸字段算落点，这里用 {@code Vec3} 运算，语义一致。</li>
 * </ul>
 */
public class EntityThunderpealOrb extends FRHomingProjectile {

    /** 原版 {@code area} 初值 4，发射方会再 +2。 */
    private static final double BLAST_RADIUS = 6.0D;

    /** 链式闪电的搜索半径与目标数上限，与原版一致。 */
    private static final double CHAIN_RADIUS = 4.0D;
    private static final int MAX_CHAIN_TARGETS = 3;

    private static final int MAX_LIFE_TICKS = 500;

    public EntityThunderpealOrb(EntityType<? extends EntityThunderpealOrb> type, Level level) {
        super(type, level);
    }

    /** 由物品发射：从视线前方 1.25 格、抬高 0.5 处出现，速度是视线的 1.5 倍（原版 {@code spawnOrb}）。 */
    public EntityThunderpealOrb(Level level, LivingEntity shooter) {
        super(FREntities.THUNDERPEAL_ORB.get(), level);
        setOwner(shooter);
        Vec3 look = shooter.getLookAngle();
        Vec3 spawn = shooter.position()
                .add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(1.25D))
                .add(0.0D, 0.5D, 0.0D);
        setPos(spawn.x, spawn.y, spawn.z);
        setDeltaMovement(look.scale(1.5D));
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    @Override
    protected void spawnTrailParticles() {
        // 一条青白色电弧拖尾，代替原版的自定义闪电网络包。
        for (int i = 0; i < 2; i++) {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + (random.nextDouble() - 0.5D) * 0.3D,
                    getY() + (random.nextDouble() - 0.5D) * 0.3D,
                    getZ() + (random.nextDouble() - 0.5D) * 0.3D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        DamageSource lightning = FRDamageTypes.source(level(), FRDamageTypes.TRUE_LIGHTNING, this);
        Player owner = getOwner() instanceof Player player ? player : null;
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity direct) {
            strike(direct, lightning, FRConfig.THUNDERPEAL_DIRECT_DAMAGE.get().floatValue());
        }

        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(BLAST_RADIUS), entity -> entity != owner && entity.isAlive());
        for (LivingEntity target : nearby) {
            strike(target, lightning, FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().floatValue());
            // 链式：从被击中者再向它附近最多 3 个目标打出一半伤害。
            List<LivingEntity> chained = level().getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(CHAIN_RADIUS),
                    entity -> entity != owner && entity != target && entity.isAlive());
            while (chained.size() > MAX_CHAIN_TARGETS) {
                chained.remove(random.nextInt(chained.size()));
            }
            for (LivingEntity secondary : chained) {
                strike(secondary, lightning, FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().floatValue() / 2.0F);
            }
        }
        // 爆发粒子 + 音效，对应原版两发 imposeBurst 与 thaumcraft:shock。
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 60, 1.5D, 1.5D, 1.5D, 0.2D);
        server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
    }

    /** 打一次真雷，并清空无敌帧——原版对每一跳都这么做。 */
    private static void strike(LivingEntity target, DamageSource source, float damage) {
        target.invulnerableTime = 0;
        target.hurt(source, damage);
    }
}
