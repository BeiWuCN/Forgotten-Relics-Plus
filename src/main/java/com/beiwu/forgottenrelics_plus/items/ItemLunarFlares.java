package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityLunarFlare;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 月耀咒书（Tome of Lunar Flares），注册名 {@code tome_of_lunar_flares}，
 * 1.7.10 原版 {@code ItemLunarFlares}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>右键 {@code setItemInUse(stack, 72000)}，进入 {@code EnumAction.bow} 的拉弓姿态，<b>没有冷却</b>；</li>
 *   <li>{@code onUsingTick}：只要 {@code count % 2 == 0} 且不是第一个 tick，就先用
 *       {@code SuperpositionHandler.getPointedBlock(player, world, 128.0F)} 取准星指向的方块
 *       （射线长 128 格）；拿不到方块就什么都不发生；</li>
 *   <li>拿到方块后抽一次 Vis：风（Aer）{@code 35} + 火（Ignis）{@code 50} + 秩序（Ordo）{@code 65}
 *       厘 Vis（合计 1.5 点，三项都乘 {@code lunarFlaresVisMult}），抽得出来才发射一颗
 *       {@code EntityLunarFlare}。于是引导期间<b>每 2 tick 降下一颗耀月之辉</b>；</li>
 *   <li>每 4 tick 在玩家处补一次 {@code ForgottenRelics:sound.starfall}（音量 2.0，音调 1.0 + rand × 0.5）；</li>
 *   <li>物品堆叠上限 1，稀有度 EPIC，{@code getWarp} 返回 3。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code getMaxItemUseDuration} → {@code Item#getUseDuration}；{@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见 {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。
 *       按本模组统一约定改成 {@link FRRechargable} 的<b>物品自身充能</b>，用
 *       {@link RechargeAccess#consumeCharge} 扣除；</li>
 *   <li>取方块改用原版 {@code ClipContext} 射线（透视液体、与传送之典同一写法）；</li>
 *   <li>发射音效 {@code ForgottenRelics:sound.starfall} 换成原版等价物
 *       {@link SoundEvents#FIREWORK_ROCKET_LAUNCH}（流星下坠的呼啸感），
 *       并按项目约定过 {@link SoundHelper#play} 统一压低音量。</li>
 * </ul>
 *
 * <p><b>两处刻意的取舍</b>：
 * <ol>
 *   <li>原版每颗耀月之辉的 Vis 消耗是风 0.35 + 火 0.50 + 秩序 0.65 = 1.5 点，充能是整数，
 *       按本项目「把原版单次合计取整成一个固定值」的既有做法（霹雳咒书 2.2 → 2、错位之典 6.4 → 6）
 *       就近取 2；</li>
 *   <li>原版右键引导<b>没有冷却</b>，节奏完全来自 {@code count % 2 == 0}。本项目为它保留了
 *       {@code tomeOfLunarFlaresCooldown} 配置，但它的语义是<b>两次发射之间的间隔</b>（默认 2），
 *       不是施法后的冷却，因此不接 {@code CooldownHelper}（与原初混沌之典同一处理）。</li>
 * </ol>
 *
 * <p>原版 {@code spawnLunarFlare} 里先按 {@code mop} 周围 ±6 格做过一次 {@code setPosition}，
 * 但紧接着又被第二段（散布向量那段）覆盖，是一段死代码，这里不保留。
 */
public class ItemLunarFlares extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000；实际节奏靠 {@code onUseTick} 每 2 tick 触发。 */
    private static final int USE_DURATION = 72000;

    /** 原版 {@code SuperpositionHandler.getPointedBlock(player, world, 128.0F)} 的射线长度。 */
    private static final double LOOK_RANGE = 128.0D;

    /** 原版 {@code spawnLunarFlare} 里写死的水平散布（格）。 */
    private static final double SPREAD = 18.0D;

    /** 原版写死的竖直基准：{@code (24 + rand * 18) * 2}，即 48 ± 18 格。 */
    private static final double HEIGHT_BASE = 24.0D;

    /** 原版写死的俯冲初速（格 / tick）。 */
    private static final double LAUNCH_SPEED = 4.0D;

    public ItemLunarFlares(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 单次发射的 Vis 消耗：原版基础值乘 {@code lunarFlaresVisMult}。 */
    public int getVisCost() {
        return (int) (FRConfig.TOME_OF_LUNAR_FLARES_VIS_COST.get() * FRConfig.TOME_OF_LUNAR_FLARES_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.TOME_OF_LUNAR_FLARES_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_LUNAR_FLARES_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版 onItemRightClick 只是 setItemInUse(stack, 72000)，真正的节奏在 onUsingTick。
        // 客户端也一起进入姿态，这样弓的拉扯动画才会出现（与符文天象石、原初混沌之典同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 发射、扣费、音效都只在服务端做；客户端交给服务端同步。
        if (level.isClientSide()) {
            return;
        }
        int interval = FRConfig.TOME_OF_LUNAR_FLARES_COOLDOWN.get();
        // 原版条件 count != getMaxItemUseDuration() && count % 2 == 0（第一个 tick 不发射）。
        if (remainingUseDuration <= 0
                || interval <= 0
                || remainingUseDuration % interval != 0
                || remainingUseDuration == USE_DURATION) {
            return;
        }
        BlockHitResult hit = findPointedBlock(level, player);
        if (hit == null) {
            return;
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        spawnLunarFlare(level, player, hit);
        // 原版每 4 tick 在玩家处播一次 sound.starfall（音量 2.0、音调 1.0 + rand * 0.5）。
        if (remainingUseDuration % 4 == 0) {
            SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.0F,
                    1.0F + level.random.nextFloat() * 0.5F);
        }
    }

    /**
     * 对应原版 {@code SuperpositionHandler.getPointedBlock(player, world, 128.0F)}。
     *
     * <p>原版用 {@code player.posY + 1.62 - yOffset} 当射线起点、{@code player.getLook(1.0F)} 当方向，
     * 1.21.1 直接取 {@code getEyePosition(1.0F)} 与 {@code getViewVector(1.0F)}。
     * 原版的 {@code rayTraceBlocks(vec1, vec2)} 默认不因液体停下，这里用 {@code ClipContext.Fluid.NONE} 对齐。
     */
    private static BlockHitResult findPointedBlock(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LOOK_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    /**
     * 对应原版 {@code spawnLunarFlare}。
     *
     * <p>目标点取命中方块的「方块中心」，即 {@code (x + 0.5, y, z + 0.5)}；
     * 出生点 = 目标点 + 散布向量 {@code ((r-0.5)*18, (24 + (r-0.5)*18)*2, (r-0.5)*18)}，
     * 即落在目标上方 30~66 格、水平 ±9 格的伞状空域；初速 = 散布向量取反后归一化再乘 4，
     * 也就是从出生点朝目标方块俯冲。
     */
    private static void spawnLunarFlare(Level level, Player player, BlockHitResult hit) {
        int x = hit.getBlockPos().getX();
        int y = hit.getBlockPos().getY();
        int z = hit.getBlockPos().getZ();

        EntityLunarFlare flare = new EntityLunarFlare(level, player, x, y, z);
        double offsetX = (level.random.nextDouble() - 0.5D) * SPREAD;
        double offsetY = (HEIGHT_BASE + (level.random.nextDouble() - 0.5D) * SPREAD) * 2.0D;
        double offsetZ = (level.random.nextDouble() - 0.5D) * SPREAD;
        flare.setPos(x + 0.5D + offsetX, y + offsetY, z + 0.5D + offsetZ);
        flare.setDeltaMovement(new Vec3(offsetX, offsetY, offsetZ).normalize().scale(-LAUNCH_SPEED));
        level.addFreshEntity(flare);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：1 / 空 / 2 / 3（含范围伤害） / 35 / 空 / 4（含直击伤害）。
        tooltip.add(Component.translatable("item.LunarFlares1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.LunarFlares2.lore"));
        // 原版把 40 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.LunarFlares3.lore",
                (int) FRConfig.TOME_OF_LUNAR_FLARES_IMPACT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.LunarFlares35.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // 原版把 72 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.LunarFlares4.lore",
                (int) FRConfig.TOME_OF_LUNAR_FLARES_DIRECT_DAMAGE.get().doubleValue()));
    }
}
