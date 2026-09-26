package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.registry.FRItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 叠加之戒（Ring of Superposition）。
 *
 * <p>1.12.2 原版（{@code ItemSuperpositionRing}）逻辑：
 * <ul>
 *   <li>占用 Baubles 的 RING 槽；佩戴时把受到伤害的一部分分摊给「世界上所有其他佩戴者」——
 *       这部分在 {@code RelicsEventHandler} 里，本类不涉及；</li>
 *   <li>每 {@code superpositionRingCheckInterval}（默认 600）tick 判定一次，
 *       有 {@code superpositionRingSwapChance}（默认 0.025）的概率把自己和随机另一名佩戴者
 *       的位置互换；若两人不在同一维度，则连维度一起交换。</li>
 * </ul>
 *
 * <p>1.21.1 对应关系：
 * <ul>
 *   <li>Baubles 的 RING 槽 → Curios 的 {@code ring} 槽；</li>
 *   <li>{@code onWornTick} → Curios 的 {@code curioTick}；</li>
 *   <li>跨维度换位：原版用 {@code transferPlayerToDimension}，
 *       1.21.1 用 {@code ServerPlayer#teleportTo(ServerLevel, x, y, z, yRot, xRot)}，
 *       该方法在目标维度不同时会走完整的换维度流程，等价于原版行为。</li>
 * </ul>
 *
 * <p>与原版一致的判定顺序：先随机、再筛玩家。这一点很重要——原版在
 * {@code Math.random() <= chance} 为真时才会去取佩戴者列表，
 * 因此即使世界上只有一名佩戴者，也会照常消耗随机数，行为保持一致。
 */
public class ItemSuperpositionRing extends FRCurioItem {

    public ItemSuperpositionRing(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof LivingEntity wearer)) {
            return;
        }
        // 只在服务端判定：换位要改位置、要跨维度，客户端自己做会立刻被服务端纠正。
        if (wearer.level().isClientSide()) {
            return;
        }
        if (wearer.tickCount % FRConfig.SUPERPOSITION_RING_CHECK_INTERVAL.get() != 0) {
            return;
        }
        if (Math.random() > FRConfig.SUPERPOSITION_RING_SWAP_CHANCE.get()) {
            return;
        }

        // 换位要用 ServerPlayer 的跨维度传送；饰品的持有者在服务端必然就是 ServerPlayer。
        if (!(wearer instanceof ServerPlayer self)) {
            return;
        }
        List<ServerPlayer> wearers = findOtherWearers(wearer);
        if (wearers.isEmpty()) {
            return;
        }
        ServerPlayer other = wearers.get((int) (Math.random() * wearers.size()));

        Vec3 here = self.position();
        Vec3 there = other.position();
        ServerLevel hereLevel = self.serverLevel();
        ServerLevel thereLevel = other.serverLevel();

        // 音调只随机一次，双方听到的参数一致（与原版一样：一次随机、两处播放）。
        float pitch = (float) (0.8D + Math.random() * 0.2D);

        other.teleportTo(hereLevel, here.x, here.y, here.z, other.getYRot(), other.getXRot());
        self.teleportTo(thereLevel, there.x, there.y, there.z, self.getYRot(), self.getXRot());

        hereLevel.playSound(null, here.x, here.y, here.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
        thereLevel.playSound(null, there.x, there.y, there.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
    }

    /**
     * 找出所有「其他」佩戴者，对应原版 {@code SuperpositionHandler.getBaubleOwnersList}。
     *
     * <p>原版遍历的是当前世界（{@code world.playerEntities}）里的玩家，即只在同一维度内找人；
     * 但换位逻辑里又写了「维度不同就交换维度」——只有在原版用了服务端全量玩家列表时才可能出现。
     * 这里按服务端全量玩家列表来找，功能上覆盖原版并让那段跨维度代码真正有意义。
     */
    private static List<ServerPlayer> findOtherWearers(LivingEntity wearer) {
        List<ServerPlayer> result = new ArrayList<>();
        if (!(wearer.level() instanceof ServerLevel level)) {
            return result;
        }
        for (ServerPlayer candidate : level.getServer().getPlayerList().getPlayers()) {
            if (candidate == wearer) {
                continue;
            }
            if (com.beiwu.forgottenrelics_re.utils.CurioHelper.isEquipped(candidate, FRItems.SUPERPOSITION_RING.get())) {
                result.add(candidate);
            }
        }
        return result;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemSuperpositionRing1.lore"));
        tooltip.add(Component.translatable("item.ItemSuperpositionRing2.lore"));
        tooltip.add(Component.translatable("item.ItemSuperpositionRing3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemSuperpositionRing4.lore"));
        tooltip.add(Component.translatable("item.ItemSuperpositionRing5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRRing.lore"));
    }
}
