package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 永恒放逐之诫（Edict of Eternal Banishment），注册名 {@code edict_of_banishment}，
 * 1.7.10 原版 {@code ItemOverthrower}。堆叠上限 1、Warp 2，原版没有施法冷却。
 *
 * <p>行为：右键锁定准星指向的活体（射线 64 格、搜索盒外扩 3 格），进入 150 tick 拉弓引导；
 * 引导期间每 tick 向目标喷 5 颗向内收束的红橙 wisp、施加 30 tick / amplifier 2 的缓慢，每 10 tick
 * 在玩家与目标处各播一次火焰音效；最后一 tick 最多重试 9 次把目标丢进下界（随机 X/Z ±10001，
 * 从 y=124 往下找落脚点），9 次都失败且目标不是玩家时直接抹除，并在原地劈 3 道真雷 + 129 颗地狱粒子。
 * 身处下界时右键完全无效。
 *
 * <p>1.21.1 对应：{@code onItemRightClick} / {@code onUsingTick} / {@code EnumAction.bow} →
 * {@code use} / {@code onUseTick} / {@link UseAnim#BOW}。原版「从背包法杖抽 Vis」没有对应 API，
 * 按本移植统一约定改成 {@link FRRechargable} 的物品自身充能；以玩家为键的静态 map {@code targetList} →
 * {@link FRDataComponents#EDICT_TARGET} 存实体 id（与虚空吞噬者锁定方尖碑坐标同源）；原版写 NBT
 * 重建实体 → {@code Entity#teleportTo(ServerLevel, ...)}；不写任何自定义网络包，粒子走服务端
 * {@link FRParticles}、真雷直接生成 {@link LightningBolt} 实体、全服公告走原版 {@code Component} +
 * {@code PlayerList#broadcastSystemMessage}；{@code thaumcraft:fireloop} → {@link SoundEvents#FIRE_AMBIENT}
 * 并过 {@link SoundHelper#play} 统一压低音量。
 *
 * <p>Vis 折算：原版每 tick 抽 18 厘 Vis = 3.6 点/秒，充能是整数，按核子之怒的先例向上取整为
 * 4 点/秒，扣在每一秒的第一 tick（一次完整引导 150 tick 共扣 8 次 = 32 点，原版 27 点）。
 *
 * <p>与原版的偏差：
 * <ol>
 *   <li>落点搜索的 {@code y == 124} 哨兵逐字保留：原版从 124 往下找，若 124 本身合法也仍被当成
 *       「没找到落点」——原版自身的怪癖，不修正；</li>
 *   <li>落点由整格 {@code (x, y, z)} 改为 {@code (x + 0.5, y, z + 0.5)}，否则实体会卡在方块角上；</li>
 *   <li>目标锁定改用实体 id：目标若在引导途中跨维度离开，{@code ServerLevel#getEntity(id)} 取不到，
 *       引导会提前中止（原版保留对象引用、仍会结算；单人放逐场景不可辨）；</li>
 *   <li>真雷与地狱粒子是纯服务端生成（原版为服务端生成 + 网络包通知），观感一致；微粒传播半径由
 *       {@code sendParticles} 决定（默认 32 格，原版约 64~128 格）；</li>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerTick.lore} + 各要素成本）依赖
 *       {@code GuiScreen.isCtrlKeyDown}，共享基类 {@code FRItem} 只实现 Shift 展开，这里保持一致。</li>
 * </ol>
 */
public class ItemOverthrower extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getPointedEntity(world, player, 0.0, 64.0, 3.0F)} 的射线长度与搜索盒外扩量。 */
    private static final double SEARCH_RANGE = 64.0D;
    private static final double SEARCH_TOLERANCE = 3.0D;

    /** 原版 {@code Entity#getCollisionBorderSize()} 的默认值：每个候选实体的碰撞箱再外扩 0.1。 */
    private static final double COLLISION_BORDER = 0.1D;

    /** 原版音效节奏：{@code count % 10 == 0} 且 {@code count != getMaxItemUseDuration()}。 */
    private static final int SOUND_INTERVAL = 10;

    /** 原版每 tick 给目标施加的缓慢：{@code new PotionEffect(Potion.field_76421_d.id, 30, 2, true)}（amplifier 2）。 */
    private static final int SLOWNESS_DURATION = 30;
    private static final int SLOWNESS_AMPLIFIER = 2;

    /** 折算后的扣费节奏：每 1 秒（20 tick）扣一次，且扣在这一秒的第一 tick。 */
    private static final int VIS_INTERVAL = 20;

    /** 原版下界随机落点：{@code (int)((Math.random() - 0.5) * 20002.0)}。 */
    private static final double NETHER_SPREAD = 20002.0D;

    /** 原版落点搜索的起始高度，同时也是「没找到」的哨兵值。 */
    private static final int SEARCH_TOP = 124;

    /** 原版对同一个目标最多重试 9 次（{@code counter = 8; counter >= 0; --counter}）。 */
    private static final int MAX_ATTEMPTS = 9;

    /** 原版实体消失时最多在周围点 12 次火。 */
    private static final int IGNITE_ATTEMPTS = 12;

    public ItemOverthrower(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 每秒的 Vis 消耗：原版每 tick 火（Ignis）8 + 秩序（Ordo）5 + 混沌（Perditio）5 厘 Vis
     * = 0.18 点/tick，即 3.6 点/秒，充能为整数故向上取整为 4。
     */
    public int getVisCostPerSecond() {
        return (int) (FRConfig.EDICT_OF_BANISHMENT_VIS_COST.get() * FRConfig.EDICT_OF_BANISHMENT_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.EDICT_OF_BANISHMENT_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.EDICT_OF_BANISHMENT_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // 原版 getMaxItemUseDuration(stack) 返回 150。
        return FRConfig.EDICT_OF_BANISHMENT_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版：玩家身处下界时右键完全无反应（不能再把人放逐到下界）。
        if (level.dimension() == Level.NETHER) {
            return InteractionResultHolder.fail(stack);
        }
        // 原版 getPointedEntity(world, player, 0.0, 64.0, 3.0F)：锁定准星指向的活体。
        LivingEntity target = findPointedEntity(level, player);
        if (target == null) {
            // 原版把 targetList 里该玩家置为 null；这里清掉锁定的组件。
            if (!level.isClientSide()) {
                stack.remove(FRDataComponents.EDICT_TARGET.get());
            }
            return InteractionResultHolder.fail(stack);
        }
        // 原版把目标写进以玩家为键的静态 map；1.21.1 改用物品数据组件存目标实体 id。
        if (!level.isClientSide()) {
            stack.set(FRDataComponents.EDICT_TARGET.get(), target.getId());
        }
        // 原版 setItemInUse(stack, 150) 进入拉弓姿态；客户端也一起进入，弓的拉扯动画才会出现
        //（与其它法术典籍同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 原版 onUsingTick 第一行就是「若是客户端直接 return」：整套逻辑只在服务端跑。
        if (level.isClientSide()) {
            return;
        }
        ServerLevel server = (ServerLevel) level;

        Integer targetId = stack.get(FRDataComponents.EDICT_TARGET.get());
        if (targetId == null) {
            // 对应原版 !targetList.containsKey(player) -> stopUsingItem。
            player.stopUsingItem();
            return;
        }
        // 原版每个 tick 都从背包法杖抽一次 Vis，抽不出来立即中断引导；
        // 这里折算成「每 20 tick 扣一次 4 点」，扣在每一秒的第一 tick。
        int duration = getUseDuration(stack, player);
        int elapsed = duration - remainingUseDuration;
        if (elapsed % VIS_INTERVAL == 0
                && !RechargeAccess.consumeCharge(stack, player, getVisCostPerSecond())) {
            stack.remove(FRDataComponents.EDICT_TARGET.get());
            player.stopUsingItem();
            return;
        }

        Entity targetEntity = server.getEntity(targetId);
        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
            // 原版目标死亡/消失时把 map 项清空并停止引导。
            stack.remove(FRDataComponents.EDICT_TARGET.get());
            player.stopUsingItem();
            return;
        }

        // 原版每 10 tick（且不是第一个 tick）在玩家与目标处各播一次 thaumcraft:fireloop。
        if (remainingUseDuration % SOUND_INTERVAL == 0 && remainingUseDuration != duration) {
            SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.33F, 2.0F);
            SoundHelper.play(level, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.33F, 2.0F);
        }

        // 原版 Vector3.fromEntityCenter(target)：以实体中心为粒子与真雷的基准点。
        Vec3 center = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D);

        // 原版每 tick 向周围广播 BanishmentCastingMessage：目标中心附近 5 颗红橙 wisp 向内收束。
        banishingParticles(server, center);

        // 原版对目标施加 30 tick、amplifier 2 的移动缓慢。
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, true, true));

        if (remainingUseDuration != 1) {
            return;
        }

        // ---- 引导的最后一 tick：结算放逐 ----
        if (target.level().dimension() != Level.NETHER) {
            boolean banished = false;
            // 原版 for (counter = 8; counter >= 0; --counter)：最多重试 9 次。
            for (int attempt = MAX_ATTEMPTS - 1; attempt >= 0; attempt--) {
                if (overthrow(server, target, player)) {
                    banished = true;
                    break;
                }
            }
            // 原版：9 次都没找到落点、且目标不是玩家时直接抹除（是玩家则留在原地）。
            if (!banished && !(target instanceof ServerPlayer)) {
                target.teleportTo(0.0D, 0.0D, 0.0D);
                target.discard();
            }
        }

        // 原版无论放逐成功与否，都会在目标原地劈 3 道真雷并撒一簇地狱粒子。
        strikeLightning(server, center, target);
        infernalBurst(server, center);

        stack.remove(FRDataComponents.EDICT_TARGET.get());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        // 提前松手时清掉锁定的目标（原版的静态 map 项会留到下次右键，这里顺手清干净）。
        if (!level.isClientSide()) {
            stack.remove(FRDataComponents.EDICT_TARGET.get());
        }
    }

    /**
     * 对应原版 {@code overthrow(entity, overthrower)}：把目标丢进下界。
     *
     * <p>原版先 {@code getChunkProvider().loadChunk(x >> 4, z >> 4)} 确保落点区块已加载，
     * 再从 y=124 往下找第一个合法落脚点。{@code y == 124} 这个哨兵被逐字保留
     *（见类注释「偏差」第 1 条）。
     *
     * @return 是否成功找到落点并完成放逐
     */
    private static boolean overthrow(ServerLevel sourceLevel, LivingEntity entity, Player overthrower) {
        ServerLevel nether = sourceLevel.getServer().getLevel(Level.NETHER);
        if (nether == null) {
            return false;
        }
        // 原版 X/Z 各取 ±10001 之间的随机整数。
        int x = (int) ((nether.random.nextDouble() - 0.5D) * NETHER_SPREAD);
        int z = (int) ((nether.random.nextDouble() - 0.5D) * NETHER_SPREAD);
        // 原版 loadChunk(x >> 4, z >> 4)：1.21.1 的 getChunk 会同步加载/生成该区块。
        nether.getChunk(x >> 4, z >> 4);

        int y = SEARCH_TOP;
        for (int counter = SEARCH_TOP; counter > 0; counter--) {
            if (!validatePosition(nether, x, counter, z)) {
                continue;
            }
            y = counter;
            break;
        }
        if (y == SEARCH_TOP) {
            return false;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            // 原版 transferPlayerToDimension(-1) 后再 setLocationAndAngles(x, y, z)。
            serverPlayer.teleportTo(nether, x + 0.5D, y, z + 0.5D,
                    serverPlayer.getYRot(), serverPlayer.getXRot());
            broadcastOverthrow(overthrower, serverPlayer);
            ForgottenRelics.LOGGER.info("{} has overthrown {} into the Nether.",
                    overthrower.getDisplayName().getString(), serverPlayer.getDisplayName().getString());
            return true;
        }

        // 非玩家实体：原版写 NBT -> 在下界重建 -> 杀死原实体 -> 在原位置周围点火。
        // 先记下原位置（换维度后原实体就被移除了）。
        Vec3 origin = entity.position();
        Vec3 eye = entity.getEyePosition();
        boolean moved = entity.teleportTo(nether, x + 0.5D, y, z + 0.5D,
                Set.<RelativeMovement>of(), entity.getYRot(), entity.getXRot());
        if (!moved) {
            // 原版这一段整个被 try/catch 包着：即便重建失败也照样杀死原实体。
            entity.discard();
        }
        igniteAround(sourceLevel, origin, eye);
        return true;
    }

    /**
     * 对应原版 {@code SuperpositionHandler.validatePosition}：
     * {@code !isAir(y-1) & block(y-1).isOpaqueCube() & isAir(y) & isAir(y+1)}。
     *
     * <p>1.21.1 用 {@code isSolidRender} 表达 {@code isOpaqueCube}。
     */
    private static boolean validatePosition(ServerLevel level, int x, int y, int z) {
        BlockPos below = new BlockPos(x, y - 1, z);
        BlockState belowState = level.getBlockState(below);
        return !belowState.isAir()
                && belowState.isSolidRender(level, below)
                && level.getBlockState(new BlockPos(x, y, z)).isAir()
                && level.getBlockState(new BlockPos(x, y + 1, z)).isAir();
    }

    /**
     * 对应原版实体消失时那段「在周围点 12 次火」：
     * 从原位置上方 4 格往下找第一块非空气方块，若它上方是空气、看得见且不是火，就在那里放火。
     *
     * @param origin 实体的原始脚底坐标
     * @param eye    实体的原始眼睛坐标（用于视线判定）
     */
    private static void igniteAround(ServerLevel level, Vec3 origin, Vec3 eye) {
        BlockPos base = BlockPos.containing(origin);
        for (int attempt = 0; attempt < IGNITE_ATTEMPTS; attempt++) {
            int xx = base.getX() + level.random.nextInt(4) - level.random.nextInt(4);
            int zz = base.getZ() + level.random.nextInt(4) - level.random.nextInt(4);
            int yy;
            for (yy = base.getY() + 4;
                    level.getBlockState(new BlockPos(xx, yy, zz)).isAir() && yy > base.getY() - 4;
                    yy--) {
            }
            BlockPos ground = new BlockPos(xx, yy, zz);
            BlockPos firePos = ground.above();
            if (!level.getBlockState(firePos).isAir()
                    || level.getBlockState(ground).isAir()
                    || level.getBlockState(firePos).is(Blocks.FIRE)
                    || !canEntityBeSeen(level, eye, firePos)) {
                continue;
            }
            // 原版 setBlock(x, y, z, Blocks.fire, 0, 3)：通知客户端与邻居。
            level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    /**
     * 对应原版 {@code EntityUtils.canEntityBeSeen(entity, x + 0.5, y + 1.5, z + 0.5)}：
     * 从实体眼睛到目标点做一次方块射线，没被挡住即为「看得见」。
     */
    private static boolean canEntityBeSeen(ServerLevel level, Vec3 eye, BlockPos pos) {
        Vec3 target = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        // 这里**不能**传 null 实体：ClipContext 会走 CollisionContext.of(entity) ->
        // EntityCollisionContext.<init>，里面直接 entity.isDescending()，传 null 会 NPE 崩服。
        // 原版 EntityUtils.canEntityBeSeen 只是纯方块射线（不涉及实体碰撞上下文），
        // 所以这里用 CollisionContext.empty() 语义完全对应。
        BlockHitResult hit = level.clip(new ClipContext(eye, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS;
    }

    /**
     * 对应原版 {@code EntityUtils.getPointedEntity(world, player, 0.0, 64.0, 3.0F)}：
     * 先按「视线线段外扩 3 格」的包围盒粗筛，再对每个候选实体的碰撞箱（外扩 0.1，即原版
     * {@code getCollisionBorderSize()}）做线段求交，取交点上离眼睛最近的活体。
     */
    private static LivingEntity findPointedEntity(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(SEARCH_RANGE));
        AABB search = new AABB(eye, end).inflate(SEARCH_TOLERANCE);
        LivingEntity best = null;
        double bestDistance = SEARCH_RANGE;
        for (Entity candidate : level.getEntities(player, search,
                entity -> entity instanceof LivingEntity && entity.isPickable())) {
            AABB box = candidate.getBoundingBox().inflate(COLLISION_BORDER);
            Optional<Vec3> hit = box.clip(eye, end);
            double distance;
            if (hit.isPresent()) {
                distance = eye.distanceTo(hit.get());
            } else if (box.contains(eye)) {
                distance = 0.0D;
            } else {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (LivingEntity) candidate;
            }
        }
        return best;
    }

    /**
     * 对应原版 {@code BanishmentCastingMessage} 的客户端渲染（Botania wispFX）：
     * <pre>
     *   BanishmentCastingMessage(x, y, z, 5)
     *   for (i = 0; i &lt; 5; i++):
     *     p = 中心 + (rand-0.5)*8 的三轴偏移
     *     wispFX(p, r=0.9+rand*0.1, g=0.1+rand*0.15, b=0,
     *            size=0.2+rand*0.2, 初速=(中心-p)*0.08, maxAgeMul=0.5)
     * </pre>
     * 每颗粒子颜色 / 尺寸 / 初速都不同，所以逐颗单独发包（{@link FRParticles#serverWisp} 的
     * {@code count == 0} 分支正好能精确指定初速）。
     */
    private static void banishingParticles(ServerLevel level, Vec3 center) {
        for (int i = 0; i < 5; i++) {
            double px = center.x + (level.random.nextDouble() - 0.5D) * 8.0D;
            double py = center.y + (level.random.nextDouble() - 0.5D) * 8.0D;
            double pz = center.z + (level.random.nextDouble() - 0.5D) * 8.0D;
            FRParticles.serverWisp(level, px, py, pz,
                    0.9F + level.random.nextFloat() * 0.1F,
                    0.1F + level.random.nextFloat() * 0.15F,
                    0.0F,
                    0.2F + level.random.nextFloat() * 0.2F,
                    (center.x - px) * 0.08D,
                    (center.y - py) * 0.08D,
                    (center.z - pz) * 0.08D,
                    0.5F);
        }
    }

    /**
     * 对应原版 {@code InfernalParticleMessage(x, y, z, 128)}：
     * {@code for (i = 0; i <= 128; i++)} 即 129 颗从中心原地炸开，
     * {@code wispFX(中心, r=0.9+rand*0.1, g=0.1+rand*0.15, b=0, size=0.4+rand*0.4,
     * xm/ym/zm=(rand-0.5)*0.5, maxAgeMul=1.0)}。
     *
     * <p>颜色 / 尺寸各抽一次（原版逐颗随机），初速幅度按 {@code 0.5/√12 ≈ 0.144} 折算。
     */
    private static void infernalBurst(ServerLevel level, Vec3 center) {
        FRParticles.serverWispBurst(level, center.x, center.y, center.z,
                0.9F + level.random.nextFloat() * 0.1F,
                0.1F + level.random.nextFloat() * 0.15F,
                0.0F,
                0.4F + level.random.nextFloat() * 0.4F, 1.0F,
                129, 0.0D, 0.144D);
    }

    /**
     * 对应原版引导结束时在目标原地生成 3 道真雷。
     *
     * <p>原版写的是 {@code new EntityLightningBolt(world, thisPos.x - 0.5, thisPos.y - height / 2, thisPos.z - 0.5)}；
     * {@code thisPos} 是实体中心，所以 y 再减去半个身高正好落在脚底。
     */
    private static void strikeLightning(ServerLevel level, Vec3 center, LivingEntity target) {
        double y = center.y - target.getBbHeight() / 2.0D;
        for (int counter = 3; counter > 0; counter--) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) {
                continue;
            }
            bolt.moveTo(center.x - 0.5D, y, center.z - 0.5D);
            level.addFreshEntity(bolt);
        }
    }

    /**
     * 对应原版 {@code OverthrowChatMessage(type 0)} 的全服广播：
     * {@code <施法者> has overthrown <目标> into the Nether.}
     *
     * <p>本移植不写自定义网络包，改用原版 {@link Component} + {@code PlayerList#broadcastSystemMessage}
     *（等价于原版的 {@code sendToAll}）。
     */
    private static void broadcastOverthrow(Player overthrower, ServerPlayer victim) {
        MinecraftServer server = overthrower.getServer();
        if (server == null) {
            return;
        }
        Component message = Component.literal(overthrower.getDisplayName().getString() + " ")
                .append(Component.translatable("message.overthrown1"))
                .append(Component.literal(" " + victim.getDisplayName().getString() + " "))
                .append(Component.translatable("message.overthrown2"));
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：1~2、空行、3~4、空行、5~7。
        tooltip.add(Component.translatable("item.ItemOverthrower1.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower2.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower3.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower5.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower6.lore"));
        tooltip.add(Component.translatable("item.ItemOverthrower7.lore"));
    }
}
