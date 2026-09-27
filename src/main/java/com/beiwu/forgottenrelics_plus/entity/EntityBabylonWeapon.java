package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;

/**
 * 神化召唤的巴比伦武器，1.7.10 原版 {@code EntityBabylonWeaponSS}，
 * 本项目注册名 {@code babylon_weapon}（语言键 {@code entity.forgotten_relics_plus.babylon_weapon}）。
 *
 * <p>原版逻辑（{@code EntityThrowableCopy} 子类，即 Botania 版 {@code EntityThrowable} 的拷贝）：
 * <ul>
 *   <li>前 15 tick（{@code ticksExisted <= 15}）<b>滞空蓄力</b>：速度清零，每 20 tick 有 1 次
 *       以 0.1 音量播 {@code botania:babylonSpawn}；</li>
 *   <li>之后进入 {@code liveTicks} 计时：{@code liveTicks < delay} 时继续悬停；
 *       {@code liveTicks == delay} 的那一 tick <b>锁定发射</b>——从玩家眼睛沿视线做 64 格射线
 *       （{@code ToolCommons.raytraceFromEntity(world, player, true, 64.0)}，遇液体也停），
 *       命中方块就瞄准方块中心，没命中就取「视线 × 64 + 玩家位置」；速度 =
 *       {@code normalize(瞄准点 - 自身中心) × 3}，并播 {@code botania:babylonAttack}。
 *       神化把 {@code delay} 设成 0，所以蓄力一结束就发射；{@code delay} 是给「先铺场、再齐射」
 *       那类用法留的（王之宝钥）。</li>
 *   <li>飞行途中<b>每 tick</b>检查「上一 tick 位置到当前位置」这条线段外扩 2 格的盒子里的活体
 *       （排除发射者）：第一个命中者吃一发 {@code damageApotheosisDirect} 的魔法伤害，并被沿
 *       「目标中心 − 自身中心」方向推开（推力大小 {@code 1/距离}，超过 1 归一化，Boss 减半），
 *       然后<b>立刻原地爆炸</b>；</li>
 *   <li>爆炸（原版 {@code onImpact}）：{@code imposeBurst} + 对自身包围盒外扩 3 格内的所有活体
 *       各造成一发 {@code damageApotheosisImpact} 并同样推开 + 播 {@code random.explode}
 *       （音量 8.0）+ 给周围 128 格内的玩家发 40 颗黄白色 wisp 粒子包，最后 {@code setDead}。
 *       命中<b>草/树叶/液体</b>不触发爆炸，弹体继续飞；命中发射者本身也不结算、不消失；</li>
 *   <li>速度每 tick 都被写回原值，所以 {@code EntityThrowable} 自带的 0.99 阻尼与重力都等于被取消，
 *       实际是一条 3 格/tick 的直线；</li>
 *   <li>{@code liveTicks > delay} 后每 tick 在自身位置撒一颗黄色 wisp；
 *       {@code liveTicks > 200 + delay} 自毁。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}（无重力、有生存时限、命中钩子）。<b>不用它的追踪</b>
 *       （{@code homingStrength} 保持 0）：原版是一次性锁定后直线飞，不是持续追踪；</li>
 *   <li>原版把「移动」交给 {@code super.onUpdate()}（{@code EntityThrowable}），并<b>在移动后把速度
 *       写回</b>来抵消阻尼。这里同样：{@link #tick()} 先调 {@code super.tick()} 完成移动与碰撞，
 *       再在每个飞行 tick 用 {@link #launchVelocity} 覆盖回速度；</li>
 *   <li>原版 {@code DamageSourceMagic} → {@link FRDamageTypes#FORGOTTEN_MAGIC}（复用既有类型）；</li>
 *   <li>原版用 {@code IBossDisplayData} 判断 Boss 把推力减半；1.21.1 没有共同接口，直接列
 *       {@link EnderDragon} 与 {@link WitherBoss}（与 {@code EntityLunarFlare} 同一写法）；</li>
 *   <li><b>不写任何自定义网络包</b>：{@code ApotheosisParticleMessage}（40 颗 wisp）与
 *       {@code SuperpositionHandler.imposeBurst}（Thaumcraft 的爆裂特效）都改成服务端
 *       {@code ServerLevel#sendParticles}；每 tick 的拖尾则和其余法球一样挪到客户端本地生成
 *       （见 {@link #spawnTrailParticles()}），不再占带宽；</li>
 *   <li>音效 {@code botania:babylonSpawn} / {@code botania:babylonAttack} 在 Botania 1.21.1 里
 *       改名为 {@link BotaniaSounds#TREASURE_WEAPON_SPAWN} / {@link BotaniaSounds#TREASURE_WEAPON_ATTACK}
 *       （音效文件就是 {@code treasureweaponspawn.ogg} / {@code treasureweaponattack.ogg}），
 *       所以直接引用原音效，不需要换原版替代物；{@code random.explode} →
 *       {@link SoundEvents#GENERIC_EXPLODE}。两者都过 {@link SoundHelper#play} 统一压低音量。</li>
 * </ul>
 *
 * <p><b>与 Botania 现代版 {@code BabylonWeaponEntity} 的对应</b>：
 * 六个同步字段里本实体保留 {@code variety / chargeTicks / liveTicks / delay / rotation}
 * （渲染器 {@code client/FRBabylonWeaponRenderer} 全部要用）；唯独 {@code charging}
 * 不保留——本项目用 {@code tickCount <= 15} 直接推得蓄力窗口，没有需要同步的独立状态。
 * 原版 {@code rotation} 由 Apotheosis 在召唤时写入
 * （{@code ItemApotheosis.java:150}：{@code setRotation(wrapAngleTo180_float(-player.rotationYawHead + 180))}），
 * 渲染器用它决定武器绕 Y 轴的朝向。此前这里错误地写成「原版从不写入」，
 * 相应地召唤路径漏了这一步，武器朝向恒为 0——1.6.3 已修正。
 *
 * <p><b>与原版的偏差</b>：
 * <ol>
 *   <li>武器形体现在由 {@code client/FRBabylonWeaponRenderer} 画：用 Botania 的
 *       {@code MiscellaneousModels.INSTANCE.kingKeyWeaponModels[variety]} 模型 + 光晕 quad。
 *       模型不可用时（未烘焙 / 索引越界）退回本模组的金色公告板光球；</li>
 *   <li>拖尾仍用 {@code variety} 的 12 档暖金色调上色（见 {@link #VARIETY_COLORS}），
 *       作为模型之外的额外汇聚视觉区分；</li>
 *   <li>原版实体碰撞箱是 0；本项目实体按约定 0.25×0.25。直击扫掠盒与爆炸范围仍用
 *       {@code inflate(2)} / {@code inflate(3)} 近似，半径误差约 0.125 格；</li>
 *   <li>原版那 40 颗 wisp 是自定义网络包定点生成、各自带 ±0.125 的初速
 *       （{@code ApotheosisParticleMessage(x, y, z, 40)}，每颗
 *       {@code wispFX(pos, 0.8+rand*0.2, 0.8+rand*0.2, 0, size=0.3+rand*0.3, xm/ym/zm=(rand-0.5)*0.25, 1.0)}）；
 *       这里用一次 {@link FRParticles#serverWispBurst}（定点 + gaussian 初速）发同一簇，
 *       颜色/尺寸各抽一次，位置不变、初速幅度按 {@code 0.25/√12 ≈ 0.072} 折算。</li>
 * </ol>
 */
public class EntityBabylonWeapon extends FRHomingProjectile {

    /** 原版 {@code ticksExisted <= 15}：悬停蓄力时长。 */
    private static final int CHARGE_TICKS = 15;

    /** 原版蓄力期间每 20 tick 播一次轻音效（{@code rand.nextInt(20) == 0}）。 */
    private static final int CHARGE_SOUND_INTERVAL = 20;

    /** 发射速度：原版 {@code normalize(...).multiply(3.0)}。 */
    private static final double LAUNCH_SPEED = 3.0D;

    /** 锁定发射时从玩家眼睛出发的射线长度（原版 64.0）。 */
    private static final double LOOK_RANGE = 64.0D;

    /** 直击扫掠盒：原版 {@code AxisAlignedBB(现在, 上一 tick).expand(2,2,2)}。 */
    private static final double SWEEP_RADIUS = 2.0D;

    /** 爆炸范围：原版 {@code boundingBox.expand(3,3,3)}。 */
    private static final double IMPACT_RADIUS = 3.0D;

    /** 原版 {@code liveTicks > 200 + delay} 自毁。 */
    private static final int MAX_LIVE_TICKS = 200;

    /** 推力超过该值时归一化（原版 {@code diff.mag() > 1.0}）。 */
    private static final double MAX_KNOCKBACK = 1.0D;

    /** 原版 Boss（{@code IBossDisplayData}）的推力倍率。 */
    private static final double BOSS_KNOCKBACK_SCALE = 0.5D;

    /** 爆炸粒子数：原版 {@code ApotheosisParticleMessage(.., 40)}。 */
    private static final int IMPACT_PARTICLES = 40;

    /** 拖尾 wisp 的基色：原版 {@code (1.0, 1.0, 0.0)} 的纯黄。 */
    private static final int WISP_COLOR = 0xFFFF00;

    /**
     * 12 档暖金色调，替代原版 {@code variety} 的 12 种武器形体。
     *
     * <p>原版用 {@code setVariety(rand.nextInt(12))} 决定武器模型外形；没有几何体之后，
     * 把它降级成拖尾色差，让一次召唤出来的一整排武器仍有视觉区分度。
     */
    private static final int[] VARIETY_COLORS = {
            0xFFFF00, 0xFFE800, 0xFFD700, 0xFFC800, 0xFFB800, 0xFFA800,
            0xFFF000, 0xFFDD00, 0xFFCC00, 0xFFBB00, 0xFFAA00, 0xFF9900
    };

    private static final String TAG_VARIETY = "variety";
    private static final String TAG_CHARGE_TICKS = "chargeTicks";
    private static final String TAG_LIVE_TICKS = "liveTicks";
    private static final String TAG_DELAY = "delay";
    private static final String TAG_ROTATION = "rotation";

    /** 原版 {@code variety}（服务端随机 0~11，客户端渲染要读它挑武器模型）。 */
    private static final EntityDataAccessor<Integer> DATA_VARIETY =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    /** 原版 {@code chargeTicks}：蓄力期间累加，客户端用它算发光强度。 */
    private static final EntityDataAccessor<Integer> DATA_CHARGE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    /** 原版 {@code liveTicks}：蓄力结束之后才开始累加。 */
    private static final EntityDataAccessor<Integer> DATA_LIVE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    /** 原版 {@code delay}：蓄力结束后还要悬停多久才发射；神化传 0。 */
    private static final EntityDataAccessor<Integer> DATA_DELAY =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.INT);

    /** 原版 {@code rotation}：绕 Y 轴的朝向；神化/RE 的召唤路径不设置它，保持 0。 */
    private static final EntityDataAccessor<Float> DATA_ROTATION =
            SynchedEntityData.defineId(EntityBabylonWeapon.class, EntityDataSerializers.FLOAT);

    /** 是否已经完成锁定发射。 */
    private boolean launched;

    /** 发射时定下的速度；每个飞行 tick 都写回，用来抵消 {@code ThrowableProjectile} 的 0.99 阻尼。 */
    private Vec3 launchVelocity = Vec3.ZERO;

    public EntityBabylonWeapon(EntityType<? extends EntityBabylonWeapon> type, Level level) {
        super(type, level);
    }

    /** 由物品召唤：位置、朝向、variety、delay 都由 {@code ItemApotheosis#spawnBabylonWeapon} 设置。 */
    public EntityBabylonWeapon(Level level, Player shooter) {
        super(FREntities.BABYLON_WEAPON.get(), level);
        setOwner(shooter);
    }

    /**
     * 同步字段（对应 Botania {@code BabylonWeaponEntity#defineSynchedData}）：
     * {@code charging / variety / chargeTicks / liveTicks / delay / rotation} 六个里，
     * 本实体用 {@code tickCount <= 15} 直接推得蓄力状态，不需要 {@code charging}，
     * 其余五个全部保留——客户端渲染要读。
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIETY, 0);
        builder.define(DATA_CHARGE_TICKS, 0);
        builder.define(DATA_LIVE_TICKS, 0);
        builder.define(DATA_DELAY, 0);
        builder.define(DATA_ROTATION, 0.0F);
    }

    public int getVariety() {
        return entityData.get(DATA_VARIETY);
    }

    public void setVariety(int variety) {
        entityData.set(DATA_VARIETY, variety);
    }

    public int getChargeTicks() {
        return entityData.get(DATA_CHARGE_TICKS);
    }

    public void setChargeTicks(int ticks) {
        entityData.set(DATA_CHARGE_TICKS, ticks);
    }

    public int getLiveTicks() {
        return entityData.get(DATA_LIVE_TICKS);
    }

    public void setLiveTicks(int ticks) {
        entityData.set(DATA_LIVE_TICKS, ticks);
    }

    public int getDelay() {
        return entityData.get(DATA_DELAY);
    }

    public void setDelay(int delay) {
        entityData.set(DATA_DELAY, delay);
    }

    public float getRotation() {
        return entityData.get(DATA_ROTATION);
    }

    public void setRotation(float rotation) {
        entityData.set(DATA_ROTATION, rotation);
    }

    /** 原版没有基类那种「生存时限」概念，时限由 {@link #tick()} 自己按 {@code liveTicks} 判。 */
    @Override
    protected int maxLifeTicks() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick() {
        // 客户端不做任何结算：位置由服务端同步，拖尾粒子在基类的客户端钩子里本地生成。
        if (level().isClientSide()) {
            super.tick();
            return;
        }
        // 原版：thrower 不是活着的玩家就直接 setDead，连移动都不做。
        if (!(getOwner() instanceof Player player) || !player.isAlive()) {
            discard();
            return;
        }
        // 先让基类完成「用上一 tick 的速度移动 + 碰撞检测」。
        super.tick();
        if (isRemoved()) {
            return;
        }

        int liveTime = getLiveTicks();
        int delay = getDelay();
        if (tickCount <= CHARGE_TICKS) {
            // 原版 ticksExisted <= 15：原地滞空蓄力。
            setDeltaMovement(Vec3.ZERO);
            // Botania 在这一支里 setChargeTicks(chargeTime + 1)；同步给客户端算发光强度。
            setChargeTicks(getChargeTicks() + 1);
            if (random.nextInt(CHARGE_SOUND_INTERVAL) == 0) {
                SoundHelper.play(level(), getX(), getY(), getZ(),
                        BotaniaSounds.TREASURE_WEAPON_SPAWN, SoundSource.PLAYERS, 0.1F,
                        1.0F + random.nextFloat() * 3.0F);
            }
        } else {
            if (liveTime < delay) {
                setDeltaMovement(Vec3.ZERO);
            } else if (liveTime == delay) {
                launch(player);
            }
            // 原版在 super.onUpdate() 之后把 motionX/Y/Z 写回原值，抵消 0.99 阻尼与重力。
            if (launched) {
                setDeltaMovement(launchVelocity);
            }
            setLiveTicks(liveTime + 1);
            // 原版每 tick 的直击扫掠；命中并爆炸时本轮结束。
            if (sweepDirectHit()) {
                return;
            }
        }

        if (liveTime > MAX_LIVE_TICKS + delay) {
            discard();
        }
    }

    /**
     * 原版 {@code liveTicks == delay} 分支：朝玩家视线的落点锁定，然后以 3 格/tick 发射。
     *
     * <p>射线用 {@code ClipContext.Fluid.ANY} 对齐原版 {@code raytraceFromEntity(.., true, 64.0)}
     * 的「遇液体也停」；没打到任何方块时退化成「视线 × 64 + 玩家脚底位置」，
     * 与原版 {@code new Vector3(player.getLookVec()).multiply(64).add(Vector3.fromEntity(player))} 一致。
     */
    private void launch(Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LOOK_RANGE));
        BlockHitResult hit = level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY, player));
        Vec3 aim;
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            aim = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        } else {
            aim = player.getLookAngle().scale(LOOK_RANGE).add(player.position());
        }
        Vec3 motion = aim.subtract(position());
        if (motion.length() > 1.0E-4D) {
            motion = motion.normalize().scale(LAUNCH_SPEED);
        } else {
            motion = Vec3.ZERO;
        }
        launchVelocity = motion;
        launched = true;
        setDeltaMovement(motion);
        // 原版 world.playSoundAtEntity(this, "botania:babylonAttack", 2.0F, 0.1F + random * 3.0F)。
        SoundHelper.play(level(), getX(), getY(), getZ(),
                BotaniaSounds.TREASURE_WEAPON_ATTACK, SoundSource.PLAYERS, 2.0F,
                0.1F + random.nextFloat() * 3.0F);
    }

    /**
     * 原版 {@code tick()} 里那段直击扫掠：
     * {@code AxisAlignedBB(现在, 上一 tick).expand(2,2,2)} 内的第一个活体吃一发直接伤害，
     * 被推开，然后触发一次完整的爆炸结算。
     *
     * @return 是否命中并因此结束了本实体
     */
    private boolean sweepDirectHit() {
        AABB sweep = new AABB(new Vec3(xo, yo, zo), position()).inflate(SWEEP_RADIUS);
        Entity owner = getOwner();
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, sweep,
                entity -> entity != owner && entity.isAlive());
        if (targets.isEmpty()) {
            return false;
        }
        LivingEntity target = targets.get(0);
        if (owner != null) {
            target.hurt(FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner),
                    FRConfig.APOTHEOSIS_DIRECT_DAMAGE.get().floatValue());
        }
        push(target, position());
        // 原版这里直接 this.onImpact(new MovingObjectPosition(living))：命中即爆炸并 setDead。
        explode();
        return true;
    }

    /**
     * 整段覆写基类的命中处理，对齐原版 {@code onImpact(MovingObjectPosition)} 的三个分支：
     * 草/树叶/液体不触发、命中发射者不触发，其余命中（方块或非发射者实体）一律爆炸。
     */
    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide()) {
            return;
        }
        if (result instanceof BlockHitResult blockHit && isSoftBlock(blockHit.getBlockPos())) {
            // 原版：BlockBush / BlockLeaves / BlockLiquid 直接 return，弹体继续飞。
            return;
        }
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() == getOwner()) {
            // 原版：pos.entityHit != null && pos.entityHit == thrower 时什么都不做。
            return;
        }
        explode();
    }

    /** 原版那一组「不算命中」的方块：{@code BlockBush} / {@code BlockLeaves} / {@code BlockLiquid}。 */
    private boolean isSoftBlock(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.getBlock() instanceof BushBlock
                || state.getBlock() instanceof LeavesBlock
                || state.getBlock() instanceof LiquidBlock;
    }

    /**
     * 原版 {@code onImpact} 的爆炸段 + {@code invokeDamageEffects()}：
     * 爆裂特效、3 格内所有活体的范围伤害与推开、爆炸音效、40 颗粒子，最后消失。
     */
    private void explode() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Entity owner = getOwner();
        // 原版 SuperpositionHandler.imposeBurst(world, dim, x, y, z, 1.5f)：Thaumcraft 的爆裂特效。
        burst(server, position());
        if (owner != null) {
            DamageSource magic = FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner);
            List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(IMPACT_RADIUS),
                    entity -> entity != owner && entity.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(magic, FRConfig.APOTHEOSIS_IMPACT_DAMAGE.get().floatValue());
                push(target, position());
            }
        }
        // 原版 world.playSoundEffect(x, y, z, "random.explode", 8.0F, 0.8F + random * 0.2F)。
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 8.0F, 0.8F + random.nextFloat() * 0.2F);
        // 原版 ApotheosisParticleMessage(x, y, z, 40)：40 颗 (0.8~1.0, 0.8~1.0, 0) 的 wisp，
        // 尺寸 0.3+rand*0.3，初速 (rand-0.5)*0.25，maxAgeMul=1.0。
        FRParticles.serverWispBurst(server, getX(), getY(), getZ(),
                0.8F + random.nextFloat() * 0.2F,
                0.8F + random.nextFloat() * 0.2F,
                0.0F,
                0.3F + random.nextFloat() * 0.3F, 1.0F,
                IMPACT_PARTICLES, 0.0D, 0.072D);
        discard();
    }

    /**
     * 基类要求实现的命中钩子。本实体的结算已经全部由 {@link #onHit} 与 {@link #sweepDirectHit}
     * 接管，这个实现只是兜底：万一还有别的调用路径落到这里，行为与普通命中一致（爆炸）。
     */
    @Override
    protected void onImpact(HitResult result) {
        explode();
    }

    /**
     * 对应原版 {@code SuperpositionHandler.imposeBurst(.., 1.5f)}（Thaumcraft {@code proxy.burst}）。
     * 1.21.1 没有等价特效，这里用「1 颗闪光 + 一圈暖金色 effect 粒子」近似
     * （与 {@code ItemVoidGrimoire#voidBurst} 同一方案）。
     */
    private static void burst(ServerLevel level, Vec3 center) {
        // 原版 imposeBurst(.., 1.5f) → BurstMessage → 模组<b>自带</b>的 FXBurst 粒子：
        // 一颗加法混合的青绿色柔光精灵（颜色 0 / 0.8+rand*0.2 / 0.4+rand*0.6，寿命 31 tick，尺寸 ×1.5）。
        // 此前用 ParticleTypes.FLASH 顶替——那是一张巨大的白色方片，玩家反馈「一层白色遮罩，很不好看」。
        FRParticles.serverWispBurst(level, center.x, center.y, center.z,
                0.0F,
                (float) (0.8D + level.random.nextDouble() * 0.2D),
                (float) (0.4D + level.random.nextDouble() * 0.6D),
                1.5F, 1.0F, 1, 0.0D, 0.0D);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                center.x, center.y, center.z, 24, 0.5D, 0.5D, 0.5D, 0.1D);
    }

    /**
     * 原版每 tick 的黄色 wisp 拖尾：{@code wispFX(world, x, y, z, 1.0, 1.0, 0.0, size=0.3, gravity=0.0)}
     *（9 参重载里第 8 个是 size、第 9 个是 gravity），对应
     * {@link FRParticles#wisp}（maxAgeMul 默认 1.0、gravity 默认 0）。
     *
     * <p>1.7.0 起改由客户端本地生成（{@code Level#addParticle}），不再走
     * {@code ServerLevel#sendParticles} 的每 tick 发包；位置取客户端插值后的 {@code getX/Y/Z}，
     * 与原先服务端坐标等价。
     *
     * <p>服务端原条件是 {@code liveTime > delay && launched}。客户端没有 {@code launched} 字段，
     * 但 {@code launched} 只在 {@code liveTime == delay} 那一 tick 变真、紧接着 {@code liveTicks}
     * 自增，所以「已发射」与「{@code liveTicks > delay}」恒等价，原来那个 {@code launched} 项
     * 其实是冗余的；这里直接用同步过来的 {@code liveTicks} / {@code delay} 判断，逐 tick 一致。
     *
     * <p>没有几何体之后，用 {@code variety} 的 12 档暖金色调给整排武器做区分（见
     * {@link #VARIETY_COLORS}）。
     */
    @Override
    protected void spawnTrailParticles() {
        if (getLiveTicks() <= getDelay()) {
            return;
        }
        int color = VARIETY_COLORS[Math.floorMod(getVariety(), VARIETY_COLORS.length)];
        FRParticles.wisp(level(), getX(), getY(), getZ(),
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                0.3F, 0.0D, 0.0D, 0.0D);
    }

    /**
     * 原版那段推开逻辑：{@code normalize(目标中心 − 自身中心) / 距离}，长度超过 1 时归一化，
     * Boss 再减半，然后累加到目标速度上。
     */
    private void push(LivingEntity target, Vec3 source) {
        Vec3 diff = center(target).subtract(source);
        double distance = distanceTo(target);
        if (distance > 1.0E-4D) {
            diff = diff.normalize().scale(1.0D / distance);
        } else {
            diff = Vec3.ZERO;
        }
        if (diff.length() > MAX_KNOCKBACK) {
            diff = diff.normalize();
        }
        if (isBoss(target)) {
            diff = diff.scale(BOSS_KNOCKBACK_SCALE);
        }
        target.push(diff.x, diff.y, diff.z);
    }

    /**
     * 对应原版 {@code IBossDisplayData} 那一次减半判定。
     *
     * <p>1.21.1 没有共同的 Boss 接口，直接列出原版会被这个接口覆盖的两类实体
     * （与 {@code EntityLunarFlare#isBoss} 同一写法）。
     */
    private static boolean isBoss(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss;
    }

    /** 原版 {@code Vector3.fromEntityCenter}：脚底位置抬高半个身高。 */
    private static Vec3 center(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    /** {@link SynchedEntityData} 不随存档持久化，和 Botania 一样把这些渲染字段手动写进 NBT。 */
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_VARIETY, getVariety());
        tag.putInt(TAG_CHARGE_TICKS, getChargeTicks());
        tag.putInt(TAG_LIVE_TICKS, getLiveTicks());
        tag.putInt(TAG_DELAY, getDelay());
        tag.putFloat(TAG_ROTATION, getRotation());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariety(tag.getInt(TAG_VARIETY));
        setChargeTicks(tag.getInt(TAG_CHARGE_TICKS));
        setLiveTicks(tag.getInt(TAG_LIVE_TICKS));
        setDelay(tag.getInt(TAG_DELAY));
        setRotation(tag.getFloat(TAG_ROTATION));
    }

    /**
     * 对应 Botania {@code Vector3#rotate(angle, axis)} 的绕 Y 轴旋转。
     *
     * <p>手性不影响结果：调用处每次都以 ±80° 随机旋转，两种手性只是把两个分支对调，
     * 落点分布完全相同。
     */
    private static Vec3 rotateY(Vec3 vector, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vector.x * cos + vector.z * sin, vector.y, -vector.x * sin + vector.z * cos);
    }

    /** 供物品侧的召唤散布使用：绕 Y 轴旋转，语义同 {@link #rotateY}。 */
    public static Vec3 rotateAroundY(Vec3 vector, double degrees) {
        return rotateY(vector, degrees);
    }
}
