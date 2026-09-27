package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityChaoticOrb;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 原初混沌之典（Tome of Primal Chaos），注册名 {@code tome_of_primal_chaos}，
 * 1.7.10 原版 {@code ItemChaosTome}。堆叠上限 1、稀有度 EPIC、Warp 4，没有冷却。
 *
 * <p>行为：右键进入 {@code EnumAction.bow} 拉弓姿态（可用时长 72000）。只要 {@code count % 2 == 0}
 * 且不是第一个 tick，就尝试抽一次 Vis：六大原初要素各抽 {@code (int)(100 * chaosTomeVisMult *
 * random())} 厘（AIR / EARTH / WATER / FIRE / ORDER / ENTROPY，每项独立随机 0~1 点，六项合计平均
 * 3 点、最高 6 点）；抽不出来就什么都不发生，抽出来就生成一颗能量法球 {@link EntityChaoticOrb}，于是
 * 引导期间每 2 tick 抛出一颗。法球以玩家身体中心为基准，出生点在其 ±1.5（水平）/ ±0.5（竖直）
 * 的随机位置，初速 =（出生点 − 中心）×（0.2 + 随机 × 0.2），35% 概率是追踪型，并播一次
 * Thaumcraft 的 {@code ice} 音效（0.3 音量 / 0.8 + 随机 × 0.1 音调）。
 *
 * <p>1.21.1 对应：{@code onItemRightClick} / {@code onUsingTick} / {@code EnumAction.bow} →
 * {@code Item#use} / {@code Item#onUseTick} / {@link UseAnim#BOW}；<b>「从背包法杖抽 Vis」没有对应 API</b>，
 * 改成 {@link FRRechargable} 的物品自身充能；发射音效 {@code thaumcraft:ice} 换成原版等价物
 * {@link SoundEvents#GLASS_BREAK}（冰裂/碎晶质感），并按本移植约定过 {@link SoundHelper#play}
 * 统一压低音量；原版 tooltip 的「Orbs deal 1-N damage on contact and / explode …」那一行是
 * {@code ItemChaosTome5_1.lore + " 1-" + cap + " " + ItemChaosTome5_2.lore} 拼出来的，
 * RE 已把两段合并成带一个 {@code %s} 的 {@code ItemChaosTome5.lore}，本移植沿用合并后的键，
 * 并把文本改回 1.7.10 的渲染结果。
 *
 * <p>三处刻意的取舍：
 * <ol>
 *   <li>原版每颗法球的 Vis 消耗是六项独立随机、合计 0~6 点（平均 3 点）。充能是整数，按本移植
 *       「把原版单次合计取整成一个固定值」的既有做法（霹雳咒书 2.2 → 2、腥红之咒 8.4 → 8），
 *       这里固定为 {@code tomeOfPrimalChaosVisCost = 3}（即原版平均值）；代价是丢掉了「有时便宜、
 *       有时抽不出来」的那点随机性，换来与其它施法物品一致的整数充能；</li>
 *   <li>原版右键引导没有冷却，节奏完全来自 {@code count % 2 == 0}。本移植为它保留了
 *       {@code tomeOfPrimalChaosCooldown} 配置，但它的语义是两次生成法球之间的间隔（默认 2），
 *       不是施法后的冷却，因此不接 {@code CooldownHelper}；</li>
 *   <li>原版在 {@code spawnOrb} 里算了一个 {@code vector = 中心 + 视线 × 1.0，y += 0.5} 的变量，但此后
 *       从未使用（真正的出生点走的是随机偏移）。这里不保留这段死代码。</li>
 * </ol>
 */
public class ItemChaosTome extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000；实际节奏靠 {@code onUseTick} 每 2 tick 触发。 */
    private static final int USE_DURATION = 72000;

    /** 原版 {@code Math.random() <= 0.35}：生成追踪型法球的概率。 */
    private static final double SEEKER_CHANCE = 0.35D;

    public ItemChaosTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 单次生成的 Vis 消耗：原版六项随机消耗的平均值 3，乘 {@code tomeOfPrimalChaosVisMult}。 */
    public int getVisCost() {
        return (int) (FRConfig.TOME_OF_PRIMAL_CHAOS_VIS_COST.get() * FRConfig.TOME_OF_PRIMAL_CHAOS_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.TOME_OF_PRIMAL_CHAOS_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_PRIMAL_CHAOS_WARP.get();
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
        // 客户端也一起进入姿态，这样弓的拉扯动画才会出现（与符文天象石、虚空吞噬者同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 生成、扣费、音效都只在服务端做；客户端交给服务端同步。
        if (level.isClientSide()) {
            return;
        }
        int interval = FRConfig.TOME_OF_PRIMAL_CHAOS_COOLDOWN.get();
        // 原版条件 count != getMaxItemUseDuration() && count % 2 == 0（第一个 tick 不生成）。
        if (remainingUseDuration <= 0
                || interval <= 0
                || remainingUseDuration % interval != 0
                || remainingUseDuration == USE_DURATION) {
            return;
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        spawnOrb(level, player);
    }

    /** 对应原版 {@code spawnOrb}：随机出生点、随机初速、35% 追踪、一发冰裂音效。 */
    private static void spawnOrb(Level level, Player player) {
        // 原版 Vector3.fromEntityCenter：以身体中心为原点。
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        boolean seeker = level.random.nextDouble() <= SEEKER_CHANCE;
        EntityChaoticOrb orb = new EntityChaoticOrb(level, player, seeker);
        double x = center.x + (level.random.nextDouble() - 0.5D) * 3.0D;
        double y = center.y + (level.random.nextDouble() - 0.5D) * 1.0D;
        double z = center.z + (level.random.nextDouble() - 0.5D) * 3.0D;
        orb.setPos(x, y, z);
        // 原版：初速 = (出生点 - 玩家中心) * (0.2 + rand * 0.2)。
        orb.setDeltaMovement(new Vec3(x, y, z).subtract(center)
                .scale(0.2D + level.random.nextDouble() * 0.2D));
        // 原版在法球处播放 thaumcraft:ice（0.3 音量 / 0.8 + rand * 0.1 音调）。
        SoundHelper.play(level, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS, 0.3F, 0.8F + level.random.nextFloat() * 0.1F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐。
        tooltip.add(Component.translatable("item.ItemChaosTome1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosTome2.lore"));
        tooltip.add(Component.translatable("item.ItemChaosTome3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosTome4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // 原版把 1-100 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.ItemChaosTome5.lore",
                (int) FRConfig.CHAOS_TOME_DAMAGE_CAP.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemChaosTome6.lore"));
    }
}
