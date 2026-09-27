package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
 * <p><b>闪电怎么画（1.6.2 重做）</b>：
 * <ul>
 *   <li>1.7.10 用 {@code SuperpositionHandler.imposeLightning(...)} 把「雷电球 → 目标中心」
 *       打包成 {@code LightningMessage}，客户端用 Thaumcraft 的 {@code FXLightningBolt}
 *       画成一道深蓝锯齿电弧；链式那一跳用 {@code main = false}，宽度从 0.075 收窄到 0.04；
 *       1.12.2 移植版（RE）的 {@code EntityThunderpealOrb} 是同一套（{@code LightningMessage}
 *       + 客户端 {@code FXDispatcher.arcBolt(..., 0.4, 0.6, 1.0, width)}）；</li>
 *   <li>本项目改成：{@code FRBoltParticleData.broadcast(...)} 走原版粒子包把两端送到客户端，
 *       客户端的 {@code client/FRBolts} 再交给 Botania 的 {@code BoltRenderer} 画折线
 *       （几何由 {@code BoltParticleOptions.generate()} 生成）。端点、宽度与颜色逐一对齐 RE：
 *       主电弧「雷电球位置 → 目标身体中心」宽 {@link FRBoltParticleData#WIDTH_MAIN}，
 *       链式电弧「主目标中心 → 次目标中心」宽 {@link FRBoltParticleData#WIDTH_CHAIN}，
 *       颜色都是 RE {@code arcBolt} 的 {@code 0.4/0.6/1.0}；</li>
 *   <li>1.7.10 的 {@code shootLightning} 起点其实是「雷电球沿连线前移 0.5 格」，
 *       RE 直接用了雷电球自身坐标。这里按 RE 走（球心即起点），差异只有半格；</li>
 *   <li><b>不再</b>在命中点撒 60 颗 {@code ELECTRIC_SPARK} 冒充电弧——那是本项目上一版自行加的，
 *       原版与 RE 都没有，而且正是玩家抱怨的「散点」。命中处的两发
 *       {@code imposeBurst}（Thaumcraft {@code FXBurst}，非 Botania）仍按既有口径用 {@code FLASH} 近似。</li>
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

    /**
     * 原版 {@code EntityThunderpealOrb#func_70185_h()} 返回 <b>0.05</b>——这是本模组所有弹射物里
     * 唯一有重力的一颗（其余都返回 0，基类 {@link FRHomingProjectile} 已统一处理）。
     * 此前这里沿用基类的 0，弹道变成了纯直线。
     */
    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    /**
     * 拖尾：1.7.10 的 {@code EntityThunderpealOrb} 本身没有粒子（只有自定义闪电网络包），
     * 轨迹粒子是 RE 补的（{@code EntityThunderpealOrb#onUpdate} 客户端分支）。照抄 RE：
     * <pre>
     *   每 tick：wispFX(pos, r=rand*0.1, g=0.4+rand*0.3, b=0.9+rand*0.1,
     *                  size=0.12+rand*0.08, xm/ym/zm=(rand-0.5)*0.05, maxAgeMul=0.6)
     *   ticksExisted % 2 == 0：sparkleFX(pos, 0.4, 0.6, 1.0, size=1.0, m=2)
     * </pre>
     * 对应本项目 {@link FRParticles#wisp} 与 {@link FRParticles#sparkle}（纯客户端 addParticle）。
     */
    @Override
    protected void spawnTrailParticles() {
        FRParticles.wisp(level(), getX(), getY(), getZ(),
                random.nextFloat() * 0.1F,
                0.4F + random.nextFloat() * 0.3F,
                0.9F + random.nextFloat() * 0.1F,
                0.12F + random.nextFloat() * 0.08F,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                0.6F);
        if (tickCount % 2 == 0) {
            FRParticles.sparkle(level(), getX(), getY(), getZ(), 0.4F, 0.6F, 1.0F, 1.0F, 2);
        }
    }

    /**
     * 原版 {@code attackEntityFrom}（{@code EntityThunderpealOrb.java:114-133}）：雷电球本身不吃伤害，
     * 被击中时改为<b>沿攻击者视线方向被打飞</b>（速度 = 视线单位向量 × 0.9），并播放 {@code thaumcraft:zap}。
     *
     * <p>音效按本项目既有口径换成原版等价物（{@link SoundEvents#FIREWORK_ROCKET_BLAST}），
     * 与 {@link EntityCrimsonOrb#hurt} 是同一套实现。
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

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        DamageSource lightning = FRDamageTypes.source(level(), FRDamageTypes.TRUE_LIGHTNING, this);
        Player owner = getOwner() instanceof Player player ? player : null;
        // 原版/RE 的闪电起点就是雷电球自身位置。
        Vec3 origin = position();
        // 原版把「直接命中的那一个」从范围列表里移除（EntityThunderpealOrb.java:71-73），
        // 所以它只吃一次直接伤害，不会再吃一次范围伤害。此前漏了这一步。
        LivingEntity directHit = null;
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity direct) {
            strike(direct, lightning, FRConfig.THUNDERPEAL_DIRECT_DAMAGE.get().floatValue());
            directHit = direct;
        }
        final LivingEntity excludeDirect = directHit;

        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(BLAST_RADIUS),
                entity -> entity != owner && entity != excludeDirect && entity.isAlive());
        for (LivingEntity target : nearby) {
            // 主电弧：雷电球 → 目标身体中心（RE LightningMessage(main=true)，宽 0.075）。
            FRBoltParticleData.broadcast(server, origin, centerOf(target),
                    FRBoltParticleData.WIDTH_MAIN, 1,
                    FRBoltParticleData.ARC_RED, FRBoltParticleData.ARC_GREEN, FRBoltParticleData.ARC_BLUE);
            strike(target, lightning, FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().floatValue());
            List<LivingEntity> chained = level().getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(CHAIN_RADIUS),
                    entity -> entity != owner && entity != target && entity.isAlive());
            while (chained.size() > MAX_CHAIN_TARGETS) {
                chained.remove(random.nextInt(chained.size()));
            }
            for (LivingEntity secondary : chained) {
                // 链式电弧：主目标中心 → 次目标中心（RE LightningMessage(main=false)，宽 0.04）。
                FRBoltParticleData.broadcast(server, centerOf(target), centerOf(secondary),
                        FRBoltParticleData.WIDTH_CHAIN, 1,
                        FRBoltParticleData.ARC_RED, FRBoltParticleData.ARC_GREEN, FRBoltParticleData.ARC_BLUE);
                strike(secondary, lightning, FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().floatValue() / 2.0F);
            }
        }
        // 爆发粒子 + 音效，对应原版<b>两发</b> imposeBurst(.., 2.0f) 与 thaumcraft:shock。
        // imposeBurst → BurstMessage → 模组自带的 FXBurst：青绿色加法柔光精灵，不是白色方片。
        for (int i = 0; i < 2; i++) {
            FRParticles.serverWispBurst(server, getX(), getY(), getZ(),
                    0.0F,
                    (float) (0.8D + random.nextDouble() * 0.2D),
                    (float) (0.4D + random.nextDouble() * 0.6D),
                    2.0F, 1.0F, 1, 0.0D, 0.0D);
        }
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
    }

    /** 实体身体中心，对应原版 {@code Vector3.fromEntityCenter} / RE 的 {@code posY + height / 2}。 */
    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    /** 打一次真雷，并清空无敌帧——原版对每一跳都这么做。 */
    private static void strike(LivingEntity target, DamageSource source, float damage) {
        target.invulnerableTime = 0;
        target.hurt(source, damage);
    }
}
