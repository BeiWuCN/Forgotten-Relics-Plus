package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 原初混沌之典的能量法球（Orb of Primordial Energy），1.7.10 原版 {@code EntityChaoticOrb}，
 * 本项目注册名 {@code primal_orb}（沿用 lang 里现成的 {@code entity.forgotten_relics_plus.primal_orb}）。
 *
 * <p>原版逻辑（{@code EntityThrowable} 子类）：
 * <ul>
 *   <li><b>重力 0.001、阻尼 0.5</b>（原版覆写了 {@code getGravityVelocity} 与 0.99 阻尼那个方法）。
 *       阻尼 0.5 意味着速度每 tick 减半，法球出膛后几乎立刻慢下来，只在原地附近飘；</li>
 *   <li>每 tick 先 {@code count++}；身处水中则立刻用「撞到自己」的命中结算引爆；</li>
 *   <li>客户端每 tick 撒 6 颗颜色索引 0~5 的 wispFX4 + 1 颗随机颜色的 wispFX2；</li>
 *   <li>出生 20 tick 之后开始运动：
 *       <ul>
 *         <li><b>普通法球</b>：用 {@code new Random(getId() + count)} 做种子，三个速度分量各加
 *             {@code (nextFloat() - nextFloat()) * 0.01} 的随机游走；</li>
 *         <li><b>追踪法球（seeker，35% 概率）</b>：在 16 格内找最近的活体，瞄准点取目标高度 90% 处，
 *             每 tick 三个分量各加 {@code dx / d * 0.2}（<b>注意 d 是平方距离</b>，见下文「原版笔误」），
 *             然后把速度三分量各自夹到 ±0.2；发射者（{@code oi}）每次评估有 80% 概率被跳过，
 *             所以追踪法球有概率反过来打施法者（lore 里也这么写）；</li>
 *       </ul>
 *   </li>
 *   <li>生存时限 5000 tick；</li>
 *   <li>命中结算：命中实体（且发射者非空）时造成 {@code 1 + random * chaosTomeDamageCap} 的
 *       {@code DamageSourceMagic} 伤害；随后以 {@code 1 + random * 6} 的威力、{@code isFlaming = true}
 *       引爆；<b>非追踪</b>法球还有 {@code rand.nextInt(100) <= specialchance} 的概率追加一次
 *       「腐化爆裂（taintSplosion）」或「就地生成一个灵气节点」二选一，
 *       默认 {@code specialchance = 1}（约 2%），撞方块且身处水中时是 10（约 11%）。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>基类换成 {@link FRHomingProjectile}（无重力、有生存时限、命中钩子）。但本实体<b>不用它的追踪</b>
 *       （{@code homingStrength} 保持 0）：原版的追踪是「跳发射者概率 + 平方距离 + 分量限速」那一套，
 *       与基类通用的「朝目标中心按单位向量加速」不是一回事，所以整段自己实现，放在 {@code super.tick()}
 *       之前，保持原版「先加速、后移动、再阻尼」的顺序；</li>
 *   <li>原版的阻尼 0.5、重力 0.001 覆盖了 {@code EntityThrowable} 的默认值。1.21.1 的
 *       {@code ThrowableProjectile} 把 0.99 阻尼写死在 {@code tick()} 里（不可覆写），
 *       所以这里在 {@code super.tick()} 之后把速度乘回 {@code 0.5 / 0.99}、再减去 0.001，
 *       净效果与原版完全一致；</li>
 *   <li>原版用 {@code IEntityAdditionalSpawnData} 把 {@code seeker} / {@code oi} 同步给客户端，
 *       是因为原版<b>两端都跑</b>追踪/游走逻辑。本项目沿用既有约定：轨迹以服务端为准，
 *       客户端只负责粒子（见 {@code EntityCrimsonOrb}），所以 {@code seeker} 只是服务端字段，无需同步；</li>
 *   <li>原版 {@code DamageSourceMagic} → {@link FRDamageTypes#FORGOTTEN_MAGIC}
 *       （上一件腥红之咒已建好该类型，直接复用）；</li>
 *   <li>原版的 {@code ThaumcraftWorldGenerator.createRandomNodeAt} 与 {@code Utils.setBiomeAt} /
 *       {@code blockTaintFibres} 在 1.21.1 由 Thaumaturge 承担：节点用 {@link NodeGenerator}，
 *       腐化用 {@link TaintBiomeManager#taintColumn} + {@link BlockTaintFibre}。
 *       这三处的参数与用法与 Thaumaturge 自家对「原初法杖核心」的复刻（{@code FocusEffectPrimal}）
 *       保持一致，语义对齐 1.7.10；</li>
 *   <li>1.7.10 没有实体贴图，形体本来就是粒子：这里用原版 {@code ParticleTypes.ENTITY_EFFECT}
 *       按六大原初要素的官方颜色染色，替代原版 6 种颜色索引的 wisp 粒子。</li>
 * </ul>
 *
 * <p><b>四处刻意的取舍 / 原版笔误</b>：
 * <ol>
 *   <li>原版取最近目标时写的是 {@code dd = getDistanceSqToEntity(e)}，随后却用这个<b>平方距离</b>
 *       当除数（{@code dx /= d}）。这会让远目标的加速度小到几乎为零，是明显的笔误。
 *       按「1.7.10 是唯一行为参照」的原则，这里<b>逐字保留</b>，不「修正」成除以真实距离；</li>
 *   <li>原版 {@code taintSplosion} 的落点写的是 {@code nextFloat() - nextFloat() * 6.0f}——
 *       6 只乘了第二个随机数，落点因此偏向坐标轴的负方向。同样逐字保留；</li>
 *   <li>原版的 {@code expl}（撞方块入水时改成 4）是个从未被读取的死变量，这里省略，只保留
 *       {@code specialchance} 的变化；</li>
 *   <li>原版在水里撞自己会对自己造成一次魔法伤害（1.7.10 里对投掷物是空操作），这里跳过自伤，
 *       只做爆炸与特殊效果。</li>
 * </ol>
 */
public class EntityChaoticOrb extends FRHomingProjectile {

    /** 原版 {@code func_70185_h()} 返回的轻微重力。 */
    private static final double GRAVITY = 0.001D;

    /** 原版覆写的阻尼系数（{@code func_70182_d()}）。 */
    private static final double DRAG = 0.5D;

    /** 1.21.1 {@code ThrowableProjectile} 内建的阻尼；用它换算上面那个 0.5。 */
    private static final double BASE_DRAG = 0.99D;

    /** 原版 {@code ticksExisted > 20} 之后才开始游走 / 追踪。 */
    private static final int WANDER_START_TICK = 20;

    /** 生存时限。原版 {@code ticksExisted > 5000} 移除；基类判的也是自增后的 tickCount。 */
    private static final int MAX_LIFE_TICKS = 5000;

    /** 原版 {@code EntityUtils.getEntitiesInRange(..., 16.0)} 的搜索半径。 */
    private static final double SEEK_RANGE = 16.0D;

    /** 原版追踪时朝目标叠加的加速度系数。 */
    private static final double SEEK_ACCEL = 0.2D;

    /** 原版把追踪法球的速度三分量各自夹在 ±0.2。 */
    private static final double SEEK_MAX_SPEED = 0.2D;

    /** 原版普通法球的随机游走加速度 {@code (nextFloat() - nextFloat()) * 0.01}。 */
    private static final double WANDER_ACCEL = 0.01D;

    /** 原版追踪时对发射者的「跳过」概率（{@code e.id == oi && Math.random() < 0.8}）。 */
    private static final float OWNER_SKIP_CHANCE = 0.8F;

    /** 原版爆炸威力 {@code 1.0 + Math.random() * 6.0} 的上限附加值。 */
    private static final double EXPLOSION_MAX_EXTRA = 6.0D;

    /**
     * 原版 wispFX4 的颜色索引 0~5 对应六大原初要素。
     * 颜色取自 Thaumaturge 的 aspect JSON，顺序为 aer / aqua / ignis / terra / ordo / perditio。
     */
    private static final int[] PRIMAL_COLORS = {
        0xFFFF7E, // aer
        0x3CD4FC, // aqua
        0xFF5A01, // ignis
        0x56C000, // terra
        0xD5D4EC, // ordo
        0x404040, // perditio
    };

    /** 原版自增的那个 {@code count}，只用于随机游走的种子。 */
    private int count;

    /** 原版由物品以 35% 概率决定的追踪开关。只在服务端有意义。 */
    private boolean seeker;

    public EntityChaoticOrb(EntityType<? extends EntityChaoticOrb> type, Level level) {
        super(type, level);
    }

    /** 由物品发射：{@code seeker} 即原版生成包里的那个开关（位置与初速由物品的 {@code spawnOrb} 设置）。 */
    public EntityChaoticOrb(Level level, LivingEntity caster, boolean seeker) {
        super(FREntities.PRIMAL_ORB.get(), level);
        this.seeker = seeker;
        setOwner(caster);
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    @Override
    public void tick() {
        count++;
        if (!level().isClientSide()) {
            // 原版：泡在水里就立刻引爆（onImpact(new MovingObjectPosition(this))）。
            if (isInWater()) {
                onImpact(new EntityHitResult(this, position()));
                discard();
                return;
            }
            // 原版这段在 super.onUpdate() 之前，此刻 ticksExisted 尚未自增；
            // 这里的 tickCount 同样还没自增，两者对齐。
            if (tickCount > WANDER_START_TICK) {
                if (seeker) {
                    seek();
                } else {
                    wander();
                }
            }
        }
        super.tick();
        if (isRemoved()) {
            return;
        }
        // 1.21.1 内建的是 0.99 阻尼，原版这里是 0.5；把差额补上，再减去原版的 0.001 重力。
        Vec3 motion = getDeltaMovement().scale(DRAG / BASE_DRAG);
        setDeltaMovement(motion.x, motion.y - GRAVITY, motion.z);
    }

    /** 普通法球的随机游走：种子是 {@code getId() + count}，与原版 {@code new Random(id + count)} 一致。 */
    private void wander() {
        Random rr = new Random(getId() + count);
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(
                motion.x + (rr.nextFloat() - rr.nextFloat()) * WANDER_ACCEL,
                motion.y + (rr.nextFloat() - rr.nextFloat()) * WANDER_ACCEL,
                motion.z + (rr.nextFloat() - rr.nextFloat()) * WANDER_ACCEL);
    }

    /**
     * 追踪：对应原版 {@code EntityUtils.getEntitiesInRange(...)} 那一段。
     *
     * <p>注意原版有两个特征必须保留：发射者每次评估有 80% 概率被跳过；除数用的是<b>平方距离</b>。
     */
    private void seek() {
        Entity owner = getOwner();
        int ownerId = owner == null ? -1 : owner.getId();
        List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(SEEK_RANGE));
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            // 原版：((e.id == oi && Math.random() < 0.8) || e.isDead || (dd = getDistanceSq(e)) >= d) -> continue。
            // 短路顺序也要保留——被跳过的发射者根本不参与距离计算。
            if (candidate.getId() == ownerId && random.nextFloat() < OWNER_SKIP_CHANCE) {
                continue;
            }
            if (!candidate.isAlive()) {
                continue;
            }
            double distanceSq = distanceToSqr(candidate);
            if (distanceSq >= best) {
                continue;
            }
            best = distanceSq;
            target = candidate;
        }
        if (target == null) {
            return;
        }
        double dx = target.getX() - getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() * 0.9D - getY();
        double dz = target.getZ() - getZ();
        Vec3 motion = getDeltaMovement();
        // 原版这里除以的是平方距离（笔误），逐字保留。
        setDeltaMovement(
                Mth.clamp(motion.x + dx / best * SEEK_ACCEL, -SEEK_MAX_SPEED, SEEK_MAX_SPEED),
                Mth.clamp(motion.y + dy / best * SEEK_ACCEL, -SEEK_MAX_SPEED, SEEK_MAX_SPEED),
                Mth.clamp(motion.z + dz / best * SEEK_ACCEL, -SEEK_MAX_SPEED, SEEK_MAX_SPEED));
    }

    @Override
    protected void onImpact(HitResult result) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        // 原版：mop.typeOfHit == BLOCK && isInsideOfMaterial(water) 时 specialchance 变 10。
        boolean blockInWater = result instanceof BlockHitResult && isInWater();
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() != this
                && getOwner() != null) {
            float damage = (float) (1.0D + random.nextDouble() * FRConfig.CHAOS_TOME_DAMAGE_CAP.get());
            entityHit.getEntity().hurt(
                    FRDamageTypes.source(level(), FRDamageTypes.FORGOTTEN_MAGIC, getOwner()), damage);
        }
        // 原版 createExplosion(null, x, y, z, 1.0 + random * 6.0, true)：无来源、会引燃。
        level().explode(null, getX(), getY(), getZ(),
                (float) (1.0D + random.nextDouble() * EXPLOSION_MAX_EXTRA), true, Level.ExplosionInteraction.MOB);
        float specialChance = blockInWater ? 10.0F : 1.0F;
        if (!seeker && random.nextInt(100) <= specialChance) {
            if (random.nextBoolean()) {
                taintSplosion(server);
            } else {
                NodeGenerator.createRandomNodeAt(server, BlockPos.containing(getX(), getY(), getZ()), random,
                        false, false, true,
                        NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
            }
        }
        spawnImpactParticles(server);
    }

    /**
     * 对应原版 {@code taintSplosion()}：10 次尝试把落点周围染成腐化之地并铺腐化纤维。
     *
     * <p>落点算术里的 {@code nextFloat() - nextFloat() * 6.0F} 是原版笔误，逐字保留。
     */
    private void taintSplosion(ServerLevel server) {
        int x = Mth.floor(getX());
        int z = Mth.floor(getZ());
        for (int attempt = 0; attempt < 10; attempt++) {
            int xx = x + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            int zz = z + (int) (random.nextFloat() - random.nextFloat() * 6.0F);
            if (!random.nextBoolean()) {
                continue;
            }
            int yy = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xx, zz);
            BlockPos target = new BlockPos(xx, yy, zz);
            if (!TaintBiomeManager.taintColumn(server, target)) {
                continue;
            }
            if (server.getBlockState(target).canBeReplaced()
                    && BlockTaintFibre.hasSolidAttachment(server, target)) {
                server.setBlock(target, BlockTaintFibre.stateForWorld(server, target), Block.UPDATE_ALL);
            }
        }
    }

    /** 命中时的爆发粒子：原版客户端撒的是 6x6 = 36 颗朝外飞散的 wispFX3，颜色索引 b = 0~5。 */
    private void spawnImpactParticles(ServerLevel server) {
        for (int a = 0; a < PRIMAL_COLORS.length; a++) {
            for (int b = 0; b < PRIMAL_COLORS.length; b++) {
                server.sendParticles(primalColor(b),
                        getX(), getY(), getZ(), 1, 0.5D, 0.5D, 0.5D, 0.05D);
            }
        }
    }

    /** 拖尾：原版每 tick 6 颗颜色索引 0~5 的 wispFX4 + 1 颗随机颜色的 wispFX2。 */
    @Override
    protected void spawnTrailParticles() {
        for (int a = 0; a < PRIMAL_COLORS.length; a++) {
            level().addParticle(primalColor(a),
                    getX() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                    getY() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                    getZ() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                    0.0D, 0.0D, 0.0D);
        }
        level().addParticle(primalColor(random.nextInt(PRIMAL_COLORS.length)),
                getX() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                getY() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                getZ() + (random.nextDouble() - random.nextDouble()) * 0.2D,
                0.0D, 0.0D, 0.0D);
    }

    /** 把某个原初要素的颜色包成原版 {@code entity_effect} 粒子。 */
    private static ColorParticleOption primalColor(int index) {
        return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, PRIMAL_COLORS[index]);
    }
}
