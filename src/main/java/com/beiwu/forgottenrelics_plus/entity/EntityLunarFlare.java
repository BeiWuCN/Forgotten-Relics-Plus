package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.registry.FRSounds;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 月耀咒书降下的耀月之辉，1.7.10 原版 {@code EntityLunarFlare}，
 * 本项目注册名 {@code lunar_flare}（沿用 lang 里现成的 {@code entity.forgotten_relics_plus.lunar_flare}）。
 *
 * <p>原版逻辑（{@code EntityThrowable} 子类）：
 * <ul>
 *   <li><b>重力 0</b>，直线飞行，1000 tick 后自毁；</li>
 *   <li>构造时锁定「准星指向的那个方块」{@code (lockX, lockY, lockZ)}，初速由物品算成朝该方块俯冲；</li>
 *   <li>命中活体（且不是发射者）时造成固定的<b>直击伤害</b>；<b>但不会因此消失</b>，
 *       耀月之辉继续飞向锁定方块；</li>
 *   <li>命中「锁定方块」的那一刻才引爆：以自身为中心 5×5×5 格内的所有活体（发射者除外）
 *       各受一次<b>范围伤害</b>，并被从耀月之辉中心向外推开——推力是
 *       {@code normalize(目标中心 - 自身中心) / 距离}，超过 1.0 时归一化，
 *       Boss（{@code IBossDisplayData}）额外减半；随后播放方块破坏粒子、
 *       {@code ForgottenRelics:sound.lunarFlare} 与两发粒子网络包（48 颗 wisp + 一团大光斑），
 *       {@code setDead()}；</li>
 *   <li>发射者为 null 时直接结束，不结算任何伤害；</li>
 *   <li>客户端每 tick 从上 tick 位置到当前位置按 0.05 格铺一条 sparkle 拖尾
 *       （颜色 r=0、g=0.8~1.0、b=0.4~1.0），并有 {@code 1/steps} 的概率叠一颗更大的。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}（无重力、有生存时限、命中钩子）。本实体<b>不用它的追踪</b>
 *       （{@code homingStrength} 保持 0）：原版本来就没有追踪，只认初始速度；</li>
 *   <li>原版 {@code DamageSourceMagic} → {@link FRDamageTypes#FORGOTTEN_MAGIC}
 *       （既有类型，直接复用，见 {@code damage_type/forgotten_magic.json}）；</li>
 *   <li>原版用 {@code IEntityAdditionalSpawnData} 同步 lock 坐标，但客户端从不读它
 *       （爆炸结算全程 {@code !world.isRemote}），所以这里 lock 只是服务端字段，不需要同步；</li>
 *   <li>原版客户端 tick 里的 sparkleFX / 两发网络包都是 Botania / 自定义网络包画的；1.21.1 一律改原版粒子：
 *       拖尾用 {@code END_ROD}（白色流星感），偶尔叠一颗 {@code SOUL_FIRE_FLAME} 表月华的青白色，
 *       爆发用一圈 {@code END_ROD} + 中心一个 {@code FLASH}；</li>
 *   <li>原版 {@code world.playAuxSFX(2001, ...)}（方块破坏粒子）→ {@code ServerLevel#levelEvent(2001, ...)}，
 *       方块状态 id 用 {@code Block.getId(state)}；</li>
 *   <li>原版 {@code sound.lunarFlare}（音量 16）→ 模组自带音效 {@link FRSounds#LUNAR_FLARE}，
 *       经 {@link SoundHelper#play} 统一压低音量。</li>
 * </ul>
 *
 * <p><b>三处刻意的取舍</b>：
 * <ol>
 *   <li>原版直击伤害与范围伤害在代码里是<b>写死的 100 / 75</b>，而配置项
 *       {@code damageLunarFlareDirect}/{@code damageLunarFlareImpact}（默认 72 / 40）只被 tooltip 读取——
 *       这是原版的疏漏（配置与实际行为不一致）。本项目把配置接到实际结算上，
 *       于是开箱即为 72 / 40，与 tooltip 显示的数值一致；</li>
 *   <li>原版命中活体后不 {@code setDead}，耀月之辉会继续飞向目标；本项目逐字保留这一点
 *       （{@link #onHit} 只对非活体命中移除实体）；</li>
 *   <li>原版判定范围是零尺寸实体的 {@code boundingBox.expand(2.5, 2.5, 2.5)}；本项目实体是
 *       0.25×0.25（见 {@code FREntities}），用 {@code getBoundingBox().inflate(2.5)} 近似，误差约 0.125 格。</li>
 * </ol>
 */
public class EntityLunarFlare extends FRHomingProjectile {

    /** 原版 {@code ticksExisted > 1000} 后自毁。 */
    private static final int MAX_LIFE_TICKS = 1000;

    /** 原版 {@code boundingBox.expand(2.5, 2.5, 2.5)}：以自身为中心 5×5×5。 */
    private static final double IMPACT_RADIUS = 2.5D;

    /** 原版推开力超过该值时归一化。 */
    private static final double MAX_KNOCKBACK = 1.0D;

    /** 原版 Boss（{@code IBossDisplayData}）的推力倍率。 */
    private static final double BOSS_KNOCKBACK_SCALE = 0.5D;

    /** 原版拖尾的取样步长（格）。 */
    private static final double TRAIL_STEP = 0.05D;

    /** 锁定的目标方块坐标（原版 lockX / lockY / lockZ），只在服务端有意义。 */
    private int lockX;
    private int lockY;
    private int lockZ;

    public EntityLunarFlare(EntityType<? extends EntityLunarFlare> type, Level level) {
        super(type, level);
    }

    /** 由物品发射：锁定准星指向的方块（位置与初速由物品的 {@code spawnLunarFlare} 设置）。 */
    public EntityLunarFlare(Level level, LivingEntity shooter, int x, int y, int z) {
        super(FREntities.LUNAR_FLARE.get(), level);
        setOwner(shooter);
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    // 原版没有追踪：homingStrength 保持基类默认的 0。

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Entity owner = getOwner();
        // 原版：getThrower() == null 时 setDead 并 return，不结算任何伤害。
        if (owner == null) {
            return;
        }
        DamageSource magic = FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner);

        // 直击：命中活体（且不是发射者）时造成直击伤害。
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() != owner
                && entityHit.getEntity() instanceof LivingEntity direct) {
            direct.hurt(magic, FRConfig.TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE.get().floatValue());
        }

        // 只有命中「当初锁定的那个方块」才引爆。
        if (result instanceof BlockHitResult blockHit && isLockedBlock(blockHit.getBlockPos())) {
            explode(server, owner, magic);
        }
    }

    /**
     * 覆写基类的命中处理：原版只在「命中锁定方块」和「发射者为 null」两处 {@code setDead}，
     * 命中活体并不移除实体。基类一律 {@code discard()}，所以这里自己控制。
     */
    @Override
    protected void onHit(HitResult result) {
        // 客户端不结算，等同步。
        if (level().isClientSide()) {
            return;
        }
        onImpact(result);
        // 命中活体且发射者仍在时继续飞行；其余情况（命中方块 / 无发射者）移除。
        if (!(result instanceof EntityHitResult) || getOwner() == null) {
            discard();
        }
    }

    /** 命中锁定方块时的爆炸结算，对应原版 {@code onImpact} 里 BLOCK + 坐标相同的那一整段。 */
    private void explode(ServerLevel server, Entity owner, DamageSource magic) {
        List<LivingEntity> affected = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(IMPACT_RADIUS),
                entity -> entity.isAlive() && entity != owner);
        Vec3 center = position();
        for (LivingEntity target : affected) {
            target.hurt(magic, FRConfig.TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE.get().floatValue());
            // 原版：normalize(目标中心 - 自身中心) / 距离，超过 1.0 时归一化，Boss 再减半。
            Vec3 push = target.position().subtract(center);
            double distance = distanceTo(target);
            if (distance > 1.0E-4D) {
                push = push.normalize().scale(1.0D / distance);
            } else {
                push = Vec3.ZERO;
            }
            if (push.length() > MAX_KNOCKBACK) {
                push = push.normalize();
            }
            if (isBoss(target)) {
                push = push.scale(BOSS_KNOCKBACK_SCALE);
            }
            target.push(push.x, push.y, push.z);
        }

        // 原版 LunarFlaresParticleMessage(lockX+0.5, lockY+1.25, lockZ+0.5, 48)：
        //   for (i = 0; i <= 48; i++)  —— 共 49 颗 ——
        //   wispFX(锁定点, r=0, g=0.8+rand*0.2, b=0.4+rand*0.6,
        //          size=0.3+rand*0.3, xm/ym/zm=(rand-0.5)*0.4, maxAgeMul=1.0)。
        // 颜色/尺寸各抽一次（原版逐颗随机），位置固定、初速用 m=0.4 的等效 gaussian（0.4/√12 ≈ 0.116）。
        FRParticles.serverWispBurst(server, lockX + 0.5D, lockY + 1.25D, lockZ + 0.5D,
                0.0F,
                (float) (0.8D + random.nextDouble() * 0.2D),
                (float) (0.4D + random.nextDouble() * 0.6D),
                0.3F + random.nextFloat() * 0.3F, 1.0F, 49, 0.0D, 0.116D);
        // 原版 LunarBurstMessage → Main.proxy.lunarBurst → 自定义 FXBurst（FR 自己的粒子，不是 Botania），
        // 按「原版本来就不是 Botania 就保持原样」的口径保留这一发 FLASH。
        server.sendParticles(ParticleTypes.FLASH,
                lockX + 0.5D, lockY + 1.5D, lockZ + 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        // 原版 world.playAuxSFX(2001, lockX, lockY, lockZ, blockId + meta << 12)：方块破坏粒子。
        BlockPos lockPos = new BlockPos(lockX, lockY, lockZ);
        BlockState state = level().getBlockState(lockPos);
        server.levelEvent(2001, lockPos, Block.getId(state));
        // 原版 sound.lunarFlare：音量 16、音调 0.8 + rand * 0.2。现用模组自带音效，不再用原版爆炸音近似。
        SoundHelper.play(level(), lockX, lockY, lockZ, FRSounds.LUNAR_FLARE.get(),
                SoundSource.PLAYERS, 16.0F, 0.8F + random.nextFloat() * 0.2F);
        discard();
    }

    /** 是否命中了物品当初锁定的那个方块。 */
    private boolean isLockedBlock(BlockPos pos) {
        return pos.getX() == lockX && pos.getY() == lockY && pos.getZ() == lockZ;
    }

    /**
     * 对应原版 {@code IBossDisplayData} 那一次减半判定。
     *
     * <p>1.21.1 没有共同的 Boss 接口，直接列出原版会被这个接口覆盖的两类实体。
     */
    private static boolean isBoss(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss;
    }

    /**
     * 原版 {@code tick()} 里那段拖尾：从上 tick 位置到当前中心按 0.05 格采样，
     * 每一步撒一颗 sparkle，偶尔（1/steps）再叠一颗抖动更大的。
     *
     * <p>原版实体尺寸是 0，所以「中心」就等于 {@code position()}；这里保持一致。
     */
    @Override
    protected void spawnTrailParticles() {
        Vec3 previous = new Vec3(xo, yo, zo);
        Vec3 current = position();
        Vec3 diff = current.subtract(previous);
        double length = diff.length();
        if (length < TRAIL_STEP) {
            return;
        }
        Vec3 step = diff.normalize().scale(TRAIL_STEP);
        int steps = (int) (length / TRAIL_STEP);
        Vec3 particlePos = previous;
        for (int i = 0; i < steps; i++) {
            // 原版 sparkleFX(..., r=0, g=0.8+rand*0.2, b=0.4+rand*0.6, size=2.0, m=1)。
            float sparkleG = (float) (0.8D + random.nextDouble() * 0.2D);
            float sparkleB = (float) (0.4D + random.nextDouble() * 0.6D);
            FRParticles.sparkle(level(),
                    particlePos.x + (random.nextDouble() - 0.5D) * 0.2D,
                    particlePos.y + (random.nextDouble() - 0.5D) * 0.2D,
                    particlePos.z + (random.nextDouble() - 0.5D) * 0.2D,
                    0.0F, sparkleG, sparkleB, 2.0F, 1);
            // 原版 nextInt(steps) <= 1：偶尔叠一颗 size=2.4、m=4、抖动 ±1.0 的大 sparkle。
            if (random.nextInt(steps) <= 1) {
                FRParticles.sparkle(level(),
                        particlePos.x + (random.nextDouble() - 0.5D) * 1.0D,
                        particlePos.y + (random.nextDouble() - 0.5D) * 1.0D,
                        particlePos.z + (random.nextDouble() - 0.5D) * 1.0D,
                        0.0F,
                        (float) (0.8D + random.nextDouble() * 0.2D),
                        (float) (0.4D + random.nextDouble() * 0.6D),
                        2.4F, 4);
            }
            particlePos = particlePos.add(step);
        }
    }
}
