package com.beiwu.forgottenrelics_plus.entity;

import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
 *   <li>客户端每 tick 撒 6 颗颜色索引 0~5 的 {@code Thaumcraft.proxy.wispFX4} + 1 颗随机颜色的
 *       {@code wispFX2}（<b>都是 Thaumcraft 的粒子，不是 Botania</b>）；</li>
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
 *   <li>1.7.10 没有实体贴图，形体本来就是粒子。原版那几处调的是 Thaumcraft 的
 *       {@code wispFX4 / wispFX2 / wispFX3}（非 Botania），而 RE 的 {@code EntityPrimalOrb}
 *       已经把它们全部换成了 Botania 的 {@code wispFX} / {@code sparkleFX}，本处按 RE 复刻：
 *       拖尾 1 颗 wisp（颜色 = 法球自身的要素色，与 {@link #getColorIndex()} 同色）+
 *       每 3 tick 一颗 sparkle，命中时 6 色 × 4 颗 = 24 颗朝外飞散的 wisp。</li>
 *   <li><b>形体与颜色</b>：形体现在由 {@code client/FROrbRenderer} 画（公告板 + 尖刺爆闪）。
 *       颜色取 {@link #getColorIndex()} 这个<b>同步</b>索引——对应 RE 的
 *       {@code RenderPrimalOrb} 读 {@code entity.getColorIndex()} 从 6 色表取色，
 *       这里是服务端生成时随机 0~5、经 {@link SynchedEntityData} 同步给客户端。</li>
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

    /**
     * 普通法球的随机游走加速度。
     *
     * <p>原版是 {@code (nextFloat() - nextFloat()) * 0.01}。但配合原版 0.5/tick 的阻尼，
     * 终端速度只有约 0.01 格/tick（0.2 格/秒）——观感上几乎是悬停，玩家反馈「不会自己无序运动」。
     * 按玩家要求改成「能看出乱窜但不野」，放大到 4 倍。<b>这是刻意偏离原版</b>，想回原版改回 0.01。
     */
    private static final double WANDER_ACCEL = 0.04D;

    /** 「从未锁定过目标」的球在出生多少 tick 后开始淡出（玩家设定：7 秒）。 */
    private static final int NO_TARGET_FADE_TICKS = 140;

    /** 淡出的持续时长（tick）：这段时间里渲染缩放从 1 线性降到 0，然后直接消失，不爆炸。 */
    private static final int FADE_TICKS = 20;

    /**
     * 非追踪球里「随机选一个方向飞出去」的比例，其余留在出生点附近做无序游走。
     * （玩家设定；刻意偏离原版——原版所有非追踪球都是原地游走。）
     */
    private static final double TRAVELER_CHANCE = 0.35D;

    /** 飞出去的球的恒定速度（格/tick，约 3 格/秒）。 */
    private static final double TRAVEL_SPEED = 0.15D;

    /** 从未锁定目标的球在 7 秒时「原地爆炸（不破坏方块）」而不是淡出的概率。 */
    private static final double SELFDESTRUCT_CHANCE = 0.15D;

    /** 原版追踪时对发射者的「跳过」概率（{@code e.id == oi && Math.random() < 0.8}）。 */
    private static final float OWNER_SKIP_CHANCE = 0.8F;

    /** 原版爆炸威力 {@code 1.0 + Math.random() * 6.0} 的上限附加值。 */
    private static final double EXPLOSION_MAX_EXTRA = 6.0D;

    /**
     * 原版 wispFX4 的颜色索引 0~5 对应六大原初要素。
     * 颜色取自 Thaumaturge 的 aspect JSON，顺序为 aer / aqua / ignis / terra / ordo / perditio。
     *
     * <p><b>与 RE 的对应</b>：RE 的 {@code RenderPrimalOrb} 用 {@code entity.getColorIndex()} 从它自己的
     * {@code ASPECT_COLORS}（顺序 Aer / Terra / Ignis / Aqua / Ordo / Perditio）取色。本项目按项目约定
     * 改用 Thaumaturge 的官方要素色（就是这里这张表），索引语义同样是「随机一种原初要素」，
     * 只是第 2/4 项（aqua 与 terra）的相对顺序与 RE 不同——见提交说明里的偏差记录。
     */
    private static final int[] PRIMAL_COLORS = {
        0xFFFF7E, // aer
        0x3CD4FC, // aqua
        0xFF5A01, // ignis
        0x56C000, // terra
        0xD5D4EC, // ordo
        0x404040, // perditio
    };

    /**
     * 同步给客户端的颜色索引（0~5），对应 RE 的 {@code EntityPrimalOrb#getColorIndex()}。
     *
     * <p>RE 用 {@code IEntityAdditionalSpawnData} 在生成包里带这个索引；1.21.1 用
     * {@link SynchedEntityData}，语义一致且不必自己写生成包。
     */
    private static final EntityDataAccessor<Integer> DATA_COLOR_INDEX =
            SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.INT);

    /** 原版自增的那个 {@code count}，只用于随机游走的种子。 */
    private int count;

    /** 原版由物品以 35% 概率决定的追踪开关。只在服务端有意义。 */
    private boolean seeker;

    /** 是否<b>曾经</b>锁定过目标。只要锁定过一次，就不再执行「7 秒淡出」。 */
    private boolean everLockedTarget;

    /** 出生以来「尚未锁定过目标」的累计 tick。 */
    private int noTargetTicks;

    /** 出生时定下：非追踪球里「选一个方向飞出去」的那种（仅服务端有意义）。 */
    private boolean traveler;

    /** 飞出去时的方向，首次需要时算定后一直保持。 */
    private Vec3 travelDirection;

    /** 出生时定下：7 秒时原地爆炸（不破坏方块）而不是淡出。 */
    private boolean selfDestruct;

    /** 淡出剩余进度（0 = 没在淡出）。同步给客户端，由 {@link #renderScale()} 换算成缩放。 */
    private static final EntityDataAccessor<Integer> DATA_FADE =
            SynchedEntityData.defineId(EntityChaoticOrb.class, EntityDataSerializers.INT);

    public EntityChaoticOrb(EntityType<? extends EntityChaoticOrb> type, Level level) {
        super(type, level);
        entityData.set(DATA_COLOR_INDEX, random.nextInt(PRIMAL_COLORS.length));
    }

    /** 由物品发射：{@code seeker} 即原版生成包里的那个开关（位置与初速由物品的 {@code spawnOrb} 设置）。 */
    public EntityChaoticOrb(Level level, LivingEntity caster, boolean seeker) {
        super(FREntities.PRIMAL_ORB.get(), level);
        this.seeker = seeker;
        // 玩家设定的三分：35% 追踪、剩下的再分「飞出去」与「原地无序游走」；
        // 另有 15% 的球 7 秒时原地爆炸而不是淡出。
        this.traveler = !seeker && random.nextDouble() < TRAVELER_CHANCE;
        this.selfDestruct = random.nextDouble() < SELFDESTRUCT_CHANCE;
        setOwner(caster);
        entityData.set(DATA_COLOR_INDEX, random.nextInt(PRIMAL_COLORS.length));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR_INDEX, 0);
        builder.define(DATA_FADE, 0);
    }

    /**
     * 同步的颜色索引，渲染器用它取色（对应 RE 的 {@code getColorIndex()}）。
     *
     * <p>两个构造里各就地抽一次随机值（0~5），对应 RE 在 {@code EntityPrimalOrb} 构造里抽的
     * {@code colorIndex}。客户端那一侧构造时也会抽，但随即被服务端的同步值覆盖；
     * 单人存档里服务端与客户端同进程，值一致。这里直接在构造里对 {@code entityData} 赋值、
     * 不经过实例方法，是为了避开 javac 的 this-escape 警告。
     */
    public int getColorIndex() {
        return entityData.get(DATA_COLOR_INDEX);
    }

    /** 写入颜色索引；越界自动回绕。 */
    public void setColorIndex(int index) {
        entityData.set(DATA_COLOR_INDEX, Math.floorMod(index, PRIMAL_COLORS.length));
    }

    /**
     * 取某个原初要素索引的 {@code 0xRRGGBB} 颜色。
     *
     * <p>渲染器（{@code client/FROrbRenderer}）与粒子（{@link #primalColor(int)}）共用这同一张表，
     * 对应 RE 里渲染器与粒子各自持有同一组颜色的做法。
     */
    public static int primalColorRgb(int index) {
        return PRIMAL_COLORS[Math.floorMod(index, PRIMAL_COLORS.length)];
    }

    @Override
    protected int maxLifeTicks() {
        return MAX_LIFE_TICKS;
    }

    /** 淡出期间线性缩小到 0；渲染器（{@code FROrbRenderer}）用它实现「淡出」。 */
    @Override
    public float renderScale() {
        int fade = entityData.get(DATA_FADE);
        if (fade <= 0) {
            return 1.0F;
        }
        return Math.max(0.0F, 1.0F - (float) fade / FADE_TICKS);
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
                    if (seek()) {
                        everLockedTarget = true;
                    }
                } else if (traveler) {
                    // 「选一个方向飞出去」的那部分：方向只取一次并一直保持，所以是直线流线。
                    if (travelDirection == null) {
                        travelDirection = randomTravelDirection();
                    }
                    setDeltaMovement(travelDirection.scale(TRAVEL_SPEED));
                } else {
                    // 其余留在出生点附近做无序游走：wander 的加速度很小（终端速度约 0.08 格/tick），
                    // 7 秒内的随机位移不到 1 格，观感就是「在生成位置乱动」。
                    wander();
                }
            }
            // 从未锁定过目标的球：7 秒后消失。其中 SELFDESTRUCT_CHANCE 比例是「原地爆炸」
            // （复用撞击路径，本项目的爆炸已经是 ExplosionInteraction.NONE，不破坏方块），其余淡出。
            // 只要期间锁定到过一次目标就不再消失（玩家口径：「只对一直没锁定到目标的球」）。
            if (!everLockedTarget) {
                if (++noTargetTicks > NO_TARGET_FADE_TICKS) {
                    if (selfDestruct) {
                        onImpact(new EntityHitResult(this, position()));
                        discard();
                        return;
                    }
                    int fade = entityData.get(DATA_FADE) + 1;
                    if (fade >= FADE_TICKS) {
                        discard();
                        return;
                    }
                    entityData.set(DATA_FADE, fade);
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

    /** 飞行球的方向：水平随机一整周，竖直只给一点扰动，避免直接钻地或冲天。 */
    private Vec3 randomTravelDirection() {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        return new Vec3(Math.cos(angle), (random.nextDouble() - 0.5D) * 0.4D, Math.sin(angle)).normalize();
    }

    /**
     * 追踪：对应原版 {@code EntityUtils.getEntitiesInRange(...)} 那一段。
     *
     * <p>注意原版有两个特征必须保留：发射者每次评估有 80% 概率被跳过；除数用的是<b>平方距离</b>。
     */
    private boolean seek() {
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
            return false;
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
        return true;
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
        // 原版 createExplosion(null, x, y, z, 1.0 + random * 6.0, true)：isSmoking=true，会连方块一起炸掉。
        //
        // 玩家实测后指出「爆炸破坏地形是 bug」，所以这里**刻意偏离原版**：保留爆炸的威力、
        // 对实体的伤害与击退，但把方块破坏关掉（ExplosionInteraction.NONE），同时不引燃（fire=false）。
        level().explode(null, getX(), getY(), getZ(),
                (float) (1.0D + random.nextDouble() * EXPLOSION_MAX_EXTRA), false, Level.ExplosionInteraction.NONE);
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

    /**
     * 命中时的爆发粒子：照抄 RE {@code EntityPrimalOrb#onImpact} 的客户端分支——
     * 6 色 × 4 颗 = 24 颗，每颗
     * {@code wispFX(pos + f, color[rand], size=0.2+rand*0.3, xm=fx*2, ym=fy*2, zm=fz*2, maxAgeMul=0.9)}
     * 其中 {@code f = (rand - rand) * 0.5}。
     *
     * <p>1.7.10 这里是 6×6 = 36 颗 Thaumcraft {@code wispFX3}（同样不是 Botania）；
     * 按 RE 的 24 颗 Botania 版本复刻。逐颗单独发包，保留「每颗随机颜色 + 朝外初速」的观感。
     */
    private void spawnImpactParticles(ServerLevel server) {
        for (int colorStep = 0; colorStep < PRIMAL_COLORS.length; colorStep++) {
            for (int particle = 0; particle < 4; particle++) {
                float fx = (float) (random.nextDouble() - random.nextDouble()) * 0.5F;
                float fy = (float) (random.nextDouble() - random.nextDouble()) * 0.5F;
                float fz = (float) (random.nextDouble() - random.nextDouble()) * 0.5F;
                int rgb = PRIMAL_COLORS[random.nextInt(PRIMAL_COLORS.length)];
                FRParticles.serverWisp(server,
                        getX() + fx, getY() + fy, getZ() + fz,
                        ((rgb >> 16) & 0xFF) / 255.0F,
                        ((rgb >> 8) & 0xFF) / 255.0F,
                        (rgb & 0xFF) / 255.0F,
                        0.2F + random.nextFloat() * 0.3F,
                        fx * 2.0D, fy * 2.0D, fz * 2.0D,
                        0.9F);
            }
        }
    }

    /**
     * 拖尾：照抄 RE {@code EntityPrimalOrb#onUpdate} 的客户端分支——
     * <pre>
     *   每 tick：wispFX(pos, r, g, b, size=0.15+rand*0.1,
     *                  xm/ym/zm=(rand-0.5)*0.05, maxAgeMul=0.8)
     *   ticksExisted % 3 == 0：sparkleFX(pos, r, g, b, size=1.0, m=3)
     * </pre>
     * 颜色取同步过来的要素索引（与 {@code FROrbRenderer} 同一张 {@link #PRIMAL_COLORS}）。
     *
     * <p><b>刻意偏离 1.7.10 的一点</b>：1.7.10 的拖尾是 Thaumcraft {@code wispFX4}（每 tick 6 颗、
     * 颜色索引 0~5 循环）+ 1 颗随机色 {@code wispFX2}，不是 Botania；RE 把它收敛成「单色 wisp +
     * 偶发 sparkle」，本处按 RE 走，于是拖尾颜色与法球本体颜色一致。
     */
    @Override
    protected void spawnTrailParticles() {
        int rgb = primalColorRgb(getColorIndex());
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        FRParticles.wisp(level(), getX(), getY(), getZ(), r, g, b,
                0.15F + random.nextFloat() * 0.1F,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                (random.nextDouble() - 0.5D) * 0.05D,
                0.8F);
        if (tickCount % 3 == 0) {
            FRParticles.sparkle(level(), getX(), getY(), getZ(), r, g, b, 1.0F, 3);
        }
    }
}
