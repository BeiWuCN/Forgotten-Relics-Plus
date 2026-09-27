package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 错位之典（Tome of Discord），注册名 {@code tome_of_discord}，1.7.10 原版
 * {@code ItemTeleportationTome}（原版注册名就是 ItemTeleportationTome，只是研究键叫 DiscordTome）。
 *
 * <p>原版 {@code onItemRightClick} 的逻辑：
 * <ol>
 *   <li>若玩家所在维度等于 {@code Config.dimensionOuterId}（外域 / Outer Lands），直接原样返回，
 *       <b>不消耗、不传送</b>；</li>
 *   <li>不在共用冷却里且是服务端时，先算一次「视线指向的活体实体」
 *       {@code EntityUtils.getPointedEntity(world, player, 0.0, 128.0, 4.0F)}
 *       ——射线最长 128 格、实体碰撞箱容差 4 格；</li>
 *   <li>按固定优先级试三种模式，每种都单独调用一次
 *       {@code WandManager.consumeVisFromInventory(player, AIR 160 + ORDER 240 + ENTROPY 240)}，
 *       成功才继续，并在成功后 {@code SuperpositionHandler.setCasted(player, 20, false)} 设 20 tick 共用冷却：
 *     <ol>
 *       <li><b>潜行 + 右键</b>：沿视线方向传送 16 格，无视障碍；</li>
 *       <li><b>右键且准星指向活体实体</b>：玩家与目标交换位置；</li>
 *       <li><b>右键且准星指向方块</b>：从命中方块往上最多 32 格，取第一个
 *           「下方是实心、当格与上一格可通行」的 y，落点 {@code x+0.5 / y / z+0.5}；</li>
 *     </ol>
 *   </li>
 *   <li>起止两端各播放一次 {@code mob.endermen.portal}，并用自定义网络包
 *       {@code PortalTraceMessage} 把两点之间的传送轨迹广播给周围 128 格内的玩家。</li>
 * </ol>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；</li>
 *   <li>{@code Config.dimensionOuterId} → Thaumaturge 的
 *       {@link OuterLands#DIMENSION}（{@code ResourceKey<Level>}，值为 {@code thaumaturge:outer_lands}）。
 *       原版只有一个 int 维度 id，1.21.1 维度改成了注册表键，所以这里直接比较 {@code level.dimension()}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见
 *       {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。本模组统一改成
 *       {@link FRRechargable} 的<b>物品自身充能</b>，用 {@link RechargeAccess#consumeCharge}
 *       扣费，充能由周围灵气补充；</li>
 *   <li>原版的 {@code SuperpositionHandler} 冷却 → {@link CooldownHelper}（与其它遗物共用）；</li>
 *   <li>原版用 {@code PortalTraceMessage} 画传送轨迹，这里改用原版 {@code ParticleTypes.PORTAL}
 *       粒子沿起止两点的连线撒点，<b>不引入自定义网络包</b>；</li>
 *   <li>原版 {@code setPosition} → 玩家用 {@code ServerPlayer#teleportTo(ServerLevel, x, y, z, yRot, xRot)}
 *       （这样客户端才会被同步），其它实体用 {@code Entity#teleportTo(x, y, z)}。</li>
 * </ul>
 *
 * <p><b>一处刻意的数值取舍</b>：原版单次合计 6.4 点 Vis（风 1.60 + 秩序 2.40 + 混沌 2.40），
 * 而充能是整数，所以 {@code discordTomeVisCost} 默认取整为 6。逐字保留 6.4 需要把充能换成厘 Vis，
 * 那会让 Thaumonomicon 的「充能 X/Y」与其它物品不同量纲（与霹雳咒书同样的取舍）。
 *
 * <p><b>换位时不碰朝向与速度</b>：原版只写了两次 {@code setPosition}，既没有改 yaw/pitch，
 * 也没有清零 or 交换速度；这里同样只搬坐标。原版对「实体中心」的取法是
 * {@code Vector3.fromEntityCenter(entity)}（{@code position + 高度/2}），且换位分支<b>没有</b>
 * 像潜行分支那样再减 0.5，所以落点会比脚底高出一截 —— 这是原版行为，这里逐字保留。
 */
public class ItemTeleportationTome extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getPointedEntity(world, player, 0.0, 128.0, 4.0F)} 与 {@code getPointedBlock(player, world, 128.0F)} 的射线长度。 */
    private static final double TELEPORT_RANGE = 128.0D;

    /** 原版那个 {@code 4.0F} 参数：实体碰撞箱的容差（格）。 */
    private static final double ENTITY_TOLERANCE = 4.0D;

    /** 原版潜行分支里写死的向前距离。 */
    private static final double FORWARD_DISTANCE = 16.0D;

    /** 原版柱顶搜索循环 {@code for (counter = 0; counter <= 32; ++counter)} 的上限。 */
    private static final int COLUMN_SEARCH = 32;

    /** 原版 {@code getWarp} 返回的固定值。 */
    private static final int WARP = 2;

    public ItemTeleportationTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 单次施法的 Vis 消耗：原版基础值乘 {@code discordTomeVisMult}。
     *
     * <p>原版是 AIR 1.60 + ORDER 2.40 + ENTROPY 2.40 = 6.4，充能为整数，默认取整为 6。
     */
    public int getVisCost() {
        return (int) (FRConfig.DISCORD_TOME_VIS_COST.get() * FRConfig.DISCORD_TOME_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.DISCORD_TOME_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return WARP;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 原版：外域维度里直接返回，不消耗也不传送。
        if (level.dimension() == OuterLands.DIMENSION) {
            return InteractionResultHolder.fail(stack);
        }
        // 传送、扣费、粒子都只在服务端做；客户端交给服务端同步。
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        // 原版优先级：潜行 -> 活体实体 -> 方块。任一模式扣费失败就不发动。
        if (player.isShiftKeyDown()) {
            if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
                return InteractionResultHolder.fail(stack);
            }
            teleportForward(level, player);
            finish(player, hand);
            return InteractionResultHolder.success(stack);
        }

        LivingEntity pointed = findPointedEntity(level, player);
        if (pointed != null) {
            if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
                return InteractionResultHolder.fail(stack);
            }
            swapWith(level, player, pointed);
            finish(player, hand);
            return InteractionResultHolder.success(stack);
        }

        // 先算出落点再扣费：原版是「先扣费、再在最多 32 格内找落点」，
        // 找不到落点时 Vis 已经花掉了却什么都不发生。这里把顺序倒过来，避免白白扣费。
        BlockPos landing = findPointedColumnTop(level, player);
        if (landing != null && RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            teleportToColumnTop(level, player, landing);
            finish(player, hand);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    /** 原版 {@code SuperpositionHandler.setCasted(player, 20, false)} + 挥手动画。 */
    private static void finish(Player player, InteractionHand hand) {
        CooldownHelper.setCooldown(player, FRConfig.DISCORD_TOME_COOLDOWN.get());
        player.swing(hand, true);
    }

    /** 潜行分支：沿视线方向传送 {@link #FORWARD_DISTANCE} 格，无视障碍。 */
    private static void teleportForward(Level level, Player player) {
        // 原版：Vector3.fromEntityCenter(player) 后 y -= 0.5，再沿视线加 16 格。
        Vec3 from = centerOf(player).add(0.0D, -0.5D, 0.0D);
        Vec3 destination = from.add(player.getViewVector(1.0F).scale(FORWARD_DISTANCE));

        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        teleportEntity(level, player, destination);
        SoundHelper.play(level, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        portalTrace(level, from, destination);
    }

    /** 换位分支：玩家与目标互相搬到对方的「实体中心」。 */
    private static void swapWith(Level level, Player player, LivingEntity target) {
        Vec3 playerCenter = centerOf(player);
        Vec3 targetCenter = centerOf(target);

        // 原版：两次 setPosition，不动朝向、不动速度。
        teleportEntity(level, player, targetCenter);
        teleportEntity(level, target, playerCenter);

        SoundHelper.play(level, targetCenter.x, targetCenter.y, targetCenter.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        SoundHelper.play(level, playerCenter.x, playerCenter.y, playerCenter.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        portalTrace(level, playerCenter, targetCenter);
    }

    /** 方块分支：落到 {@code landing} 这根柱子的顶上。 */
    private static void teleportToColumnTop(Level level, Player player, BlockPos landing) {
        // 原版这一支用的是「没有减 0.5」的实体中心。
        Vec3 from = centerOf(player);
        Vec3 destination = new Vec3(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);

        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        teleportEntity(level, player, destination);
        // 原版落点音效用的是整格坐标（没有 +0.5）。
        SoundHelper.play(level, landing.getX(), landing.getY(), landing.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        portalTrace(level, from, destination);
    }

    /** 对应 {@code Vector3.fromEntityCenter(entity)}：脚底坐标 + 碰撞箱高度的一半。 */
    private static Vec3 centerOf(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    /**
     * 统一传送入口。
     *
     * <p>玩家要走 {@code ServerPlayer} 的跨维度重载（同一维度也用它），否则客户端不会收到
     * 位置同步；其它实体用 {@code Entity#teleportTo}。
     */
    private static void teleportEntity(Level level, Entity entity, Vec3 destination) {
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(serverPlayer.serverLevel(), destination.x, destination.y, destination.z,
                    serverPlayer.getYRot(), serverPlayer.getXRot());
        } else {
            entity.teleportTo(destination.x, destination.y, destination.z);
        }
    }

    /**
     * 对应原版 {@code EntityUtils.getPointedEntity(world, player, 0.0, 128.0, 4.0F)}。
     *
     * <p>先按「视线线段外扩 {@link #ENTITY_TOLERANCE} 格」的包围盒粗筛，再对每个候选实体做
     * 线段与「外扩后的碰撞箱」求交，取交点上离眼睛最近的那个。
     */
    private static LivingEntity findPointedEntity(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(TELEPORT_RANGE));
        AABB search = new AABB(eye, end).inflate(ENTITY_TOLERANCE);
        List<Entity> candidates = level.getEntities(player, search,
                entity -> entity instanceof LivingEntity && entity.isPickable());

        Entity best = null;
        double bestDistance = TELEPORT_RANGE;
        for (Entity candidate : candidates) {
            AABB box = candidate.getBoundingBox().inflate(ENTITY_TOLERANCE);
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
                best = candidate;
            }
        }
        return best instanceof LivingEntity living ? living : null;
    }

    /**
     * 对应原版 {@code SuperpositionHandler.getPointedBlock(player, world, 128.0F)} 加上那段柱顶搜索。
     *
     * @return 可以落脚的方块坐标，找不到返回 {@code null}
     */
    private static BlockPos findPointedColumnTop(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(TELEPORT_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos hitPos = hit.getBlockPos();
        // 原版：从命中方块往上最多 32 格，第一个满足「下方实心 + 当格及上一格可通行」的 y。
        for (int counter = 0; counter <= COLUMN_SEARCH; counter++) {
            BlockPos feet = hitPos.offset(0, counter, 0);
            if (isSolidFloor(level, feet.below()) && isPassable(level, feet) && isPassable(level, feet.above())) {
                return feet;
            }
        }
        return null;
    }

    /**
     * 原版这一条是 {@code !isAirBlock(pos) && block.isNormalCube()}，即「实心完整方块」。
     * 1.21.1 用 {@code isCollisionShapeFullBlock} 表达同一个意思。
     */
    private static boolean isSolidFloor(Level level, BlockPos pos) {
        return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }

    /**
     * 原版这一条是 {@code isAirBlock}。任务口径是「可通行」，1.21.1 用「碰撞形状为空」
     * 更贴合「玩家站得进去」的本意（草丛之类的非空气方块也能站）。
     */
    private static boolean isPassable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    /**
     * 传送轨迹粒子，替代原版的 {@code PortalTraceMessage}。
     *
     * <p>原版是发一个网络包，由客户端在起止两点之间画一条带拖尾的传送门痕迹；这里直接在服务端
     * 沿两点连线按每格 2 个的密度撒 {@code PORTAL} 粒子，再在两端各来一圈作为落点特效
     * （对应原版 {@code SuperpositionHandler.imposeBurst}）。
     */
    private static void portalTrace(Level level, Vec3 from, Vec3 to) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int steps = (int) Math.ceil(from.distanceTo(to) * 2.0D);
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : (double) i / (double) steps;
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    from.x + (to.x - from.x) * t,
                    from.y + (to.y - from.y) * t,
                    from.z + (to.z - from.z) * t,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        serverLevel.sendParticles(ParticleTypes.PORTAL, from.x, from.y, from.z, 32, 0.3D, 0.3D, 0.3D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.PORTAL, to.x, to.y, to.z, 32, 0.3D, 0.3D, 0.3D, 0.05D);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemTeleportationTome1.lore"));
        // 原版把 128 硬编码进文案，这里传入真实的射线长度常量。
        tooltip.add(Component.translatable("item.ItemTeleportationTome2.lore", (int) TELEPORT_RANGE));
        tooltip.add(Component.translatable("item.ItemTeleportationTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemTeleportationTome4.lore"));
        tooltip.add(Component.translatable("item.ItemTeleportationTome5.lore", (int) FORWARD_DISTANCE));
        tooltip.add(Component.translatable("item.ItemTeleportationTome6.lore"));
    }
}
