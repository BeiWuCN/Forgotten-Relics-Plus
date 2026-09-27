package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import java.util.Random;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 核子之怒的追踪导弹（Rageous Missile），1.7.10 原版 {@code EntityRageousMissile}，
 * 本项目注册名 {@code rageous_missile}（沿用 RE 的实体注册名）。
 *
 * <p>原版逻辑（{@code EntityThrowable} 子类）：
 * <ul>
 *   <li><b>每 tick 完全重写速度</b>：有目标时把速度设成「朝目标中心方向 × 0.5」（不是叠加加速），
 *       因此 {@code EntityThrowable} 自带的 0.99 阻尼与 0.03 重力都被这段覆盖，实际不生效；
 *       发射后的前 30 tick 若竖直分量为负，强制取绝对值（先把弹体抬起来再追）；</li>
 *   <li>没有目标时，在自身 ±16 格的随机点方向以 0.5 速度乱飞；</li>
 *   <li><b>目标搜索</b>：每 tick 先校验当前目标（血量 &gt; 0、未死、仍在已加载实体表里），
 *       失效则清空，然后在自身 ±32 格（{@code double range = 32.0}）内随机挑一个活体，
 *       并<b>排除发射者</b>；</li>
 *   <li><b>自毁</b>：服务端上「没有目标」且 {@code time > 160} 时消失；{@code evil} 开关
 *       （原版只由物品传 {@code false}，另有未使用的 {@code true} 分支）还会在距目标 &lt; 1 格时自毁；</li>
 *   <li><b>命中结算</b>：只有撞到的实体<b>正好是当前锁定的目标</b>时才造成
 *       {@code nuclearFuryDamageMIN + random * (MAX - MIN)} 的 {@code DamageSourceMagic} 伤害
 *       （原版确实读了配置，而不是硬编码）、播 {@code random.fizz} 并消失；
 *       撞方块或非目标实体一律<b>穿过</b>，既不结算也不消失（对应 lore 的
 *       "Charges can pass through most blocks"）；</li>
 *   <li>粒子：从上一 tick 位置到当前位置沿途每隔 0.05 格撒一颗 Botania sparkle
 *       （颜色 r=0、g=0.8~1.0、b=0.4~1.0），另有约 2/steps 的概率额外再撒一颗；
 *       客户端在自己与某个活体重叠（±0.5）时再撒 12 颗带随机微速的 wisp。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}，但<b>不用它的追踪</b>（{@code homingStrength} 保持 0）：
 *       原版是「直接重写速度」而不是「叠加加速度」，且目标是随机搜出来的，整段自己实现；</li>
 *   <li>原版 {@code IEntityAdditionalSpawnData} 同步的 {@code evil} / {@code TARGET_ID} /
 *       thrower 名字，在 1.21.1 由基类的 {@code SynchedEntityData} 与 {@code getOwner()} 承担：
 *       目标沿用基类的 {@code setTarget/resolvedTarget}，发射者直接用 {@code getOwner()}，
 *       {@code evil} 只有服务端逻辑用到，无需同步；</li>
 *   <li><b>「撞上非目标不消失」要覆写 {@code onHit}</b>：基类对任何命中都会 {@code discard()}，
 *       原版却只在命中锁定目标时结算，所以这里整段覆写，把结算放进 {@code onImpact}；</li>
 *   <li>原版 {@code DamageSourceMagic} → {@link FRDamageTypes#FORGOTTEN_MAGIC}（复用已建好的类型）；</li>
 *   <li>粒子：原版就是 Botania 的 {@code sparkleFX}（拖尾）与 {@code wispFX}（与活体重叠时的 12 颗），
 *       这里直接用 {@link FRParticles#sparkle} / {@link FRParticles#wisp} 复刻，颜色与尺寸逐字对齐原版
 *       （{@code r=0、g=0.8+rand*0.2、b=0.4+rand*0.6}），由客户端本地生成，与服务端轨迹无关；</li>
 *   <li>音效 {@code random.fizz} → 原版 {@link SoundEvents#FIRE_EXTINGUISH}
 *       （与 EntityDarkMatterOrb 同一替代方案），照原版音量 2.0 / 音调 0.8 + 随机 0.2。</li>
 * </ul>
 *
 * <p><b>两处与 RE 的差异</b>：
 * <ol>
 *   <li>RE 的 {@code EntityRageousMissile} 把伤害硬编码成 {@code 24.0F + random * 8.0F}，
 *       并把尺寸缩到 0.15；1.7.10 读的是配置、尺寸是 {@code EntityThrowable} 的 0.25，
 *       这里按 1.7.10 走（尺寸在 {@link FREntities} 里统一为 0.25×0.25）；</li>
 *   <li>RE 给物品加了「左键清除 32 格内自己的导弹」，1.7.10 的 {@code ItemMissileTome}
 *       <b>没有</b>这个功能，所以本项目不实现；配置 {@code nuclearFuryClearRange} 改用作本实体的
 *       目标搜索半径——1.7.10 那里写死的正是 32。</li>
 * </ol>
 *
 * <p><b>原版两处死代码 / 笔误</b>（按「1.7.10 是唯一行为参照」逐字保留需要的部分）：
 * {@code lockX / lockY / lockZ} 三个字段只会被赋值、从不被读取；导弹出生 Y 写成
 * {@code posY + 3.8 + (random - 1.55)}（RE 文档怀疑应为 {@code (random - 0.5) * 3.1}），
 * 出生点算术在物品侧逐字保留。
 */
public class EntityRageousMissile extends FRHomingProjectile {

    /** 发射后强制上抬的 tick 数（原版 {@code time < 30}）。 */
    private static final int AIM_UP_TICKS = 30;

    /** 没有目标时的自毁时限（原版 {@code time > 160}）。 */
    private static final int LIFESPAN_WITHOUT_TARGET = 160;

    /** 原版每 tick 把速度重写成「单位方向 × 0.5」。 */
    private static final double SPEED = 0.5D;

    /** 没有目标时乱飞的搜索半径（原版 ±16 格的随机点）。 */
    private static final double WANDER_RANGE = 16.0D;

    /** {@code evil} 为真时，距目标小于该值即自毁（原版 {@code diffVec.mag() < 1.0}）。 */
    private static final double EVIL_DETONATE_DISTANCE = 1.0D;

    /** 拖尾粒子：沿途每隔 0.05 格一颗（原版 {@code step = normalize(diff).multiply(0.05)}）。 */
    private static final double TRAIL_STEP = 0.05D;

    /** 1.7.10 存档里的 {@code time} 标签。 */
    private static final String TAG_TIME = "time";

    /** 原版自增的那个 {@code time}；只用于 160 tick 自毁判定，并随 NBT 存档。 */
    private int time;

    /** 原版由物品传 {@code false} 的开关；为真时会在贴近目标时自毁。只有服务端有意义。 */
    private boolean evil;

    /** 本 tick 移动前的位置（身体中心），供客户端拖尾粒子使用。 */
    private Vec3 trailStart = Vec3.ZERO;

    public EntityRageousMissile(EntityType<? extends EntityRageousMissile> type, Level level) {
        super(type, level);
    }

    /**
     * 由物品发射。
     *
     * <p>对应原版 {@code EntityRageousMissile(EntityPlayer thrower, boolean evil)}：原版用反射把
     * thrower 塞进 {@code EntityThrowable}，这里直接用 {@code setOwner}；位置与初速由调用方设置。
     */
    public EntityRageousMissile(Level level, LivingEntity shooter, boolean evil) {
        super(FREntities.RAGEOUS_MISSILE.get(), level);
        setOwner(shooter);
        this.evil = evil;
    }

    /** 原版没有硬生存时限（有目标时能一直追），自毁只由「无目标超时」与命中决定。 */
    @Override
    protected int maxLifeTicks() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick() {
        // 原版在 super.onUpdate() 之前抓 prevPos：这里同样先记下移动前的位置。
        trailStart = center();
        super.tick();
        if (isRemoved()) {
            return;
        }

        // 速度<b>两端都算</b>——与基类 FRHomingProjectile 在 1.6.2 做的修正同一口径。
        //
        // 此前这里写的是「客户端直接 return」，于是客户端只能被动跟随每 tick 的服务端位置包，
        // 一旦有延迟或抖动就表现为「一卡一卡」。1.6.2 那次修正只改到了基类的 applyHoming，
        // 而这颗导弹不用基类的追踪（homingStrength 保持 0、速度由这里整条重写），所以没被覆盖到。
        //
        // 客户端的职责仍然只有「把速度算成与服务端一致的那一份」：不写同步数据、不做任何结算。
        boolean client = level().isClientSide();
        // 原版 getTarget() 因为用的是非短路 &，每 tick 都会跑一次；客户端只读服务端同步下来的目标 id。
        boolean hasTarget = client || refreshTarget();
        if (!hasTarget && time > LIFESPAN_WITHOUT_TARGET) {
            if (!client) {
                discard();
            }
            return;
        }

        Vec3 thisVec = center();
        LivingEntity target = resolvedTarget();
        if (target != null) {
            Vec3 diff = center(target).subtract(thisVec);
            Vec3 motion = diff.normalize().scale(SPEED);
            if (time < AIM_UP_TICKS) {
                // 原版：this.motionY = Math.abs(this.motionY)。
                motion = new Vec3(motion.x, Math.abs(motion.y), motion.z);
            }
            setDeltaMovement(motion);
            if (evil && diff.length() < EVIL_DETONATE_DISTANCE) {
                if (!client) {
                    discard();
                }
                return;
            }
        } else {
            // 原版：targetVec = 自身 ±16 格的随机点，速度 = 朝它的单位方向 × 0.5。
            // 随机种子改用 getId() + time（原版用的是实体自身的 rand，两端不同源）：
            // 无目标期间的每一 tick 都要让两端算出同一个方向，否则又会互相打架。
            Random rr = new Random(getId() + time);
            Vec3 wander = thisVec.add(
                    (rr.nextDouble() - 0.5D) * WANDER_RANGE,
                    (rr.nextDouble() - 0.5D) * WANDER_RANGE,
                    (rr.nextDouble() - 0.5D) * WANDER_RANGE);
            setDeltaMovement(wander.subtract(thisVec).normalize().scale(SPEED));
        }
        time++;
    }

    /**
     * 对应原版 {@code getTarget()}：先校验当前目标，失效就清空，再在
     * {@code nuclearFuryClearRange}（原版写死 32）格内随机搜一个活体，排除发射者。
     *
     * @return 刷新后是否持有有效目标
     */
    private boolean refreshTarget() {
        if (resolvedTarget() != null) {
            return true;
        }
        // 原版：目标失效时 setTarget(null)。
        setTarget(null);
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        double range = FRConfig.NUCLEAR_FURY_CLEAR_RANGE.get();
        List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(range), e -> e != owner && e.isAlive());
        if (candidates.isEmpty()) {
            return false;
        }
        // 原版是在列表里随机取一个（非活体/已死的会被剔除后重掷），等价于在活体候选里等概率取。
        setTarget(candidates.get(random.nextInt(candidates.size())));
        return true;
    }

    /**
     * 整段覆写基类的 {@code onHit}：基类对任何命中都 {@code discard()}，
     * 原版却只在命中<b>当前锁定的那个实体</b>时结算并消失，其余（方块、非目标实体）一律穿过。
     */
    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide()
                && result instanceof EntityHitResult entityHit
                && entityHit.getEntity() == resolvedTarget()) {
            onImpact(result);
        }
    }

    /** 原版 {@code func_70184_a}：命中锁定目标时造成魔法伤害、播 fizz 音并消失。 */
    @Override
    protected void onImpact(HitResult result) {
        if (!(result instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        // 原版：thrower 非空才结算伤害（不是无来源伤害）。
        if (owner != null) {
            double min = FRConfig.NUCLEAR_FURY_DAMAGE_MIN.get();
            double max = FRConfig.NUCLEAR_FURY_DAMAGE_MAX.get();
            float damage = (float) (min + random.nextDouble() * (max - min));
            target.hurt(FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, owner), damage);
        }
        // 原版 world.playSoundAtEntity(this, "random.fizz", 2.0F, 0.8F + random * 0.2F)。
        SoundHelper.play(level(), getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                2.0F, 0.8F + random.nextFloat() * 0.2F);
        discard();
    }

    /**
     * 拖尾：原版从上一 tick 位置到当前位置沿途每隔 0.05 格撒一颗 sparkle，另有小概率再撒一颗。
     *
     * <p>颜色逐颗随机：{@code r = 0}、{@code g = 0.8 + random * 0.2}、{@code b = 0.4 + random * 0.6}。
     * 只在客户端调用（由基类负责）。
     */
    @Override
    protected void spawnTrailParticles() {
        Vec3 to = center();
        Vec3 diff = to.subtract(trailStart);
        double length = diff.length();
        if (length < 1.0E-4D) {
            return;
        }
        int steps = (int) (length / TRAIL_STEP);
        if (steps <= 0) {
            return;
        }
        Vec3 step = diff.scale(TRAIL_STEP / length);
        Vec3 pos = trailStart;
        for (int i = 0; i < steps; i++) {
            addSparkle(pos);
            if (random.nextInt(steps) <= 1) {
                addSparkle(pos.add(
                        (random.nextDouble() - 0.5D) * 0.4D,
                        (random.nextDouble() - 0.5D) * 0.4D,
                        (random.nextDouble() - 0.5D) * 0.4D));
            }
            pos = pos.add(step);
        }

        // 原版客户端：与某个活体重叠（±0.5）时再撒 12 颗带随机微速的 wisp。
        // wispFX(中心, r=0, g=0.8+rand*0.2, b=0.4+rand*0.6, size=0.2+rand*0.1,
        //        xm/ym/zm=(rand-0.5)*0.15)。
        if (!level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.5D)).isEmpty()) {
            Vec3 origin = center();
            for (int i = 0; i < 12; i++) {
                FRParticles.wisp(level(), origin.x, origin.y, origin.z,
                        0.0F,
                        (float) (0.8D + random.nextDouble() * 0.2D),
                        (float) (0.4D + random.nextDouble() * 0.6D),
                        0.2F + random.nextFloat() * 0.1F,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D);
            }
        }
    }

    /** 原版拖尾的一颗 sparkleFX：{@code r=0}、{@code g=0.8~1.0}、{@code b=0.4~1.0}，尺寸 0.8、m=2。 */
    private void addSparkle(Vec3 pos) {
        FRParticles.sparkle(level(), pos.x, pos.y, pos.z,
                0.0F,
                (float) (0.8D + random.nextDouble() * 0.2D),
                (float) (0.4D + random.nextDouble() * 0.6D),
                0.8F, 2);
    }

    /** 身体中心，对应 Botania 的 {@code Vector3.fromEntityCenter}。 */
    private Vec3 center() {
        return position().add(0.0D, getBbHeight() / 2.0D, 0.0D);
    }

    private static Vec3 center(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // 原版 func_70014_b：把 time 存档。
        tag.putInt(TAG_TIME, time);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // 原版 func_70037_a：读回 time。
        time = tag.getInt(TAG_TIME);
    }
}
