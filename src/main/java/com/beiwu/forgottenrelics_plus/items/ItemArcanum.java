package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.CurioHelper;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 浑浊之核（Nebulous Core，注册名 {@code arcanum}），1.12.2 原版 {@code ItemArcanum}，护符槽。
 *
 * <p>原版行为：
 * <ul>
 *   <li>每 tick 有 2.5% × {@code arcanumGenRate} 的概率，给快捷栏 / 饰品栏 / 护甲上的物品补 1 点 Vis；</li>
 *   <li>有 {@code arcanumTeleportChance}（默认 0.000208）的概率把佩戴者随机传送走；</li>
 *   <li>有 {@code arcanumDormantTransformChance}（默认 0.000027）的概率陷入休眠，变成休眠浑浊之核，
 *       寿命取 {@code arcanumDormantLifeMin}～{@code arcanumDormantLifeMax} 的随机值；</li>
 *   <li>提供 {@code arcanumVisDiscount}（默认 35%）的 Vis 折扣；</li>
 *   <li>受击时有 {@code nebulousCoreDodgeChance}（默认 40%）的概率闪避并随机传送，见 {@link #onIncomingDamage}。</li>
 * </ul>
 *
 * <p>与原版的两点差异：
 * <ul>
 *   <li>原版补 Vis 的顺序是「快捷栏 → 饰品栏 → 护甲」，找到第一个能充的就停，这里保持一致；</li>
 *   <li>原版转化休眠态时写死塞进饰品槽 6，槽位一变就会换错东西；这里改为遍历找到浑浊之核真正所在的槽位再替换。</li>
 * </ul>
 */
public class ItemArcanum extends FRCurioItem
        implements FRRechargable, IVisDiscountGear, WearerTickBehaviour, IncomingDamageBehaviour {

    /** 原版硬编码的基础生成概率：每 tick 2.5%。 */
    private static final double BASE_GEN_CHANCE = 0.025D;

    public ItemArcanum(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.ARCANUM_MAX_CHARGE.get();
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return FRConfig.ARCANUM_VIS_DISCOUNT.get().intValue();
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (!(wearer instanceof ServerPlayer player)) {
            return;
        }

        if (player.getRandom().nextDouble() <= BASE_GEN_CHANCE * FRConfig.ARCANUM_GEN_RATE.get()) {
            if (rechargeOne(player)) {
                return;
            }
        }

        // 原版这两个分支是 if / else if：同一 tick 只会发生一件。
        if (player.getRandom().nextDouble() <= FRConfig.ARCANUM_TELEPORT_CHANCE.get()) {
            randomTeleport(player, FRConfig.ARCANUM_TELEPORT_RANGE.get());
            return;
        }

        if (player.getRandom().nextDouble() <= FRConfig.ARCANUM_DORMANT_TRANSFORM_CHANCE.get()) {
            int min = FRConfig.ARCANUM_DORMANT_LIFE_MIN.get();
            int max = FRConfig.ARCANUM_DORMANT_LIFE_MAX.get();
            int span = Math.max(1, max - min);
            int lifetime = (int) ((min + player.getRandom().nextInt(span))
                    * FRConfig.DORMANT_ARCANUM_VIS_MULT.get());
            ItemStack dormant = new ItemStack(FRItems.DORMANT_ARCANUM.get());
            dormant.set(FRDataComponents.DORMANT_LIFETIME.get(), Math.max(0, lifetime));
            CurioHelper.replaceFirst(player, FRItems.ARCANUM.get(), dormant);
        }
    }

    /**
     * 受击闪避时用的随机传送。
     *
     * <p>原版这段写死的半径是 16 格（与自身被动触发时用的 {@code arcanumTeleportRange} 不同），
     * 这里保持原样。
     *
     * @return 成功传送才返回 true
     */
    public static boolean tryDodgeTeleport(ServerPlayer player) {
        return randomTeleport(player, 16);
    }

    /** 按「快捷栏 → 饰品栏 → 护甲」的顺序，给第一个能充能的物品补 1 点 Vis。 */
    private static boolean rechargeOne(ServerPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (tryRecharge(player, player.getInventory().items.get(slot))) {
                return true;
            }
        }
        var handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler != null) {
            var equipped = handler.getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                if (tryRecharge(player, equipped.getStackInSlot(slot))) {
                    return true;
                }
            }
        }
        for (ItemStack armor : player.getInventory().armor) {
            if (tryRecharge(player, armor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryRecharge(ServerPlayer player, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }
        return RechargeAccess.rechargeItem(player.serverLevel(), candidate, player.blockPosition(), player, 1) > 0.0F;
    }

    /**
     * 随机传送。
     *
     * <p>1.12.2 用的是 {@code SuperpositionHandler.validTeleportRandomly}：在半径内随机取点、
     * 校验落点安全后再传送。1.21.1 没有现成的对应工具（{@code Entity#randomTeleport} 在这个版本不存在），
     * 所以这里自己实现：随机取 XZ 与 Y 偏移，往下/往上找一小段，挑一个「脚下有实体方块、
     * 身上两格是空的」位置落下去，最多试 32 轮。
     */
    private static boolean randomTeleport(ServerPlayer player, int range) {
        if (range <= 0) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        for (int attempt = 0; attempt < 32; attempt++) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 2.0D * range;
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 2.0D * range;
            int y = Mth.floor(player.getY()) + player.getRandom().nextInt(range * 2 + 1) - range;
            y = Mth.clamp(y, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2);
            BlockPos origin = BlockPos.containing(x, y, z);
            for (int offset = 0; offset <= 4; offset++) {
                BlockPos feet = origin.above(offset);
                if (!isSafeSpot(level, feet)) {
                    continue;
                }
                // 这个重载返回 void，传送成功与否无从判断，走到这里就算成功。
                player.teleportTo(level, feet.getX() + 0.5D, feet.getY(),
                        feet.getZ() + 0.5D, player.getYRot(), player.getXRot());
                return true;
            }
        }
        return false;
    }

    /** 落点判定：脚下要是实体方块，脚部与头部都要没有碰撞体积。 */
    private static boolean isSafeSpot(ServerLevel level, BlockPos feet) {
        if (!level.isLoaded(feet)) {
            return false;
        }
        BlockState below = level.getBlockState(feet.below());
        if (!below.blocksMotion()) {
            return false;
        }
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
    }

    /** 放在最后：前面的免除/吸收/无敌帧都处理完，才轮到「闪避掉这次伤害」。 */
    @Override
    public int priority() {
        return 500;
    }

    /**
     * 受击时按概率闪避，并随机传送走。
     *
     * <p>原版最多尝试 32 次随机传送（半径写死 16 格），成功一次就把这次伤害整个取消掉，
     * 并给 20 tick 无敌。
     */
    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        if (wearer.getRandom().nextDouble() >= FRConfig.NEBULOUS_CORE_DODGE_CHANCE.get()) {
            return;
        }
        if (wearer instanceof ServerPlayer serverPlayer && tryDodgeTeleport(serverPlayer)) {
            event.setInvulnerabilityTicks(20);
            event.setCanceled(true);
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemArcanum2.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum3.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum4.lore",
                Math.round(FRConfig.NEBULOUS_CORE_DODGE_CHANCE.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemArcanum5.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum7.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum8.lore"));
        tooltip.add(Component.translatable("item.ItemArcanum9.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
    }
}
