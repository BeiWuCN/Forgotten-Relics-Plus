package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.CurioHelper;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 叠加之戒（Ring of Superposition）。
 *
 * <p>RE 的 {@code ItemSuperpositionRing} 逻辑：
 * <ul>
 *   <li>占用 Baubles 的 RING 槽；佩戴时把受到伤害的一部分分摊给「世界上所有其他佩戴者」
 *       （RE 里这段派发写在 {@code RelicsEventHandler}，1.21.1 由本类的
 *       {@code onIncomingDamage} 实现）；</li>
 *   <li>每 {@code superpositionRingCheckInterval}（默认 600）tick 判定一次，
 *       有 {@code superpositionRingSwapChance}（默认 0.025）的概率把自己和随机另一名佩戴者
 *       的位置互换；若两人不在同一维度，则连维度一起交换。</li>
 * </ul>
 *
 * <p>1.21.1 对应关系：
 * <ul>
 *   <li>Baubles 的 RING 槽 → Curios 的 {@code ring} 槽；</li>
 *   <li>{@code onWornTick} → 派发器 {@code FRCommonEvents#onPlayerTick}，饰品栏与护甲槽统一处理；</li>
 *   <li>跨维度换位：RE 用 {@code transferPlayerToDimension}，
 *       1.21.1 用 {@code ServerPlayer#teleportTo(ServerLevel, x, y, z, yRot, xRot)}，
 *       该方法在目标维度不同时会走完整的换维度流程，等价于 RE 行为。</li>
 * </ul>
 *
 * <p>与 RE 一致的判定顺序：先随机、再筛玩家。这一点很重要——RE 在
 * {@code Math.random() <= chance} 为真时才会去取佩戴者列表，
 * 因此即使世界上只有一名佩戴者，也会照常消耗随机数，行为保持一致。
 */
public class ItemSuperpositionRing extends FRCurioItem implements WearerTickBehaviour, IncomingDamageBehaviour {

    public ItemSuperpositionRing(Properties properties) {
        super(properties);
    }

    /**
     * 夹在远古之庇护（50）与湮灭护符（70）之间。
     *
     * <p>RE 的次序是「庇护减伤 → 分摊 → 湮灭吸收」（{@code RelicsEventHandler:256 -> 263 -> 286}），
     * 湮灭护符吸收的必须是分摊<b>之后</b>的余量，所以这个位置不能随便放。
     */
    @Override
    public int priority() {
        return 60;
    }

    /**
     * 伤害分摊：把 12%~74% 的伤害平均分给服务端上所有<b>其他</b>佩戴者，自己少挨那一部分。
     *
     * <p>范围是整个服务端（跨维度），与 RE 的 {@code getBaubleOwnersList} 取的
     * {@code MinecraftServer.getPlayerList().getPlayers()} 一致。
     *
     * <p>递归保护靠伤害类型本身：分摊出去的那一份用本移植自有的「超维」类型，而本方法开头会跳过
     * 超维类型，因此不会二次分摊。这正是 RE 要多造两个 {@code DamageSourceSuperposition*} 的原因，
     * 也是 {@code FRDamageTypes.isAbsolute} 要包含它们的原因。
     */
    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        DamageSource source = event.getSource();
        if (source.is(FRDamageTypes.SUPERPOSITION) || source.is(FRDamageTypes.SUPERPOSITION_DEFINED)) {
            return;
        }
        if (!(wearer.level() instanceof ServerLevel level)) {
            return;
        }
        List<Player> others = new ArrayList<>();
        for (ServerPlayer candidate : level.getServer().getPlayerList().getPlayers()) {
            if (candidate != wearer && CurioHelper.isEquipped(candidate, FRItems.SUPERPOSITION_RING.get())) {
                others.add(candidate);
            }
        }
        if (others.isEmpty()) {
            return;
        }
        double min = FRConfig.SUPERPOSITION_RING_SPLIT_MIN.get();
        double max = FRConfig.SUPERPOSITION_RING_SPLIT_MAX.get();
        float split = (float) (event.getAmount()
                * (min + wearer.getRandom().nextDouble() * Math.max(0.0D, max - min)));
        // RE 把来源实体的信息一并带过去，这里同样区分「有来源」与「无来源」两种超维类型。
        DamageSource shared = source.getEntity() == null
                ? FRDamageTypes.source(level, FRDamageTypes.SUPERPOSITION)
                : FRDamageTypes.source(level, FRDamageTypes.SUPERPOSITION_DEFINED, source.getEntity());
        for (Player other : others) {
            other.hurt(shared, split / others.size());
        }
        event.setAmount(event.getAmount() - split);
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        // 换位要改位置、要跨维度，只放在服务端做；派发器本身也只在服务端调用。
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

        // 音调只随机一次，双方听到的参数一致（与 RE 一样：一次随机、两处播放）。
        float pitch = (float) (0.8D + Math.random() * 0.2D);

        other.teleportTo(hereLevel, here.x, here.y, here.z, other.getYRot(), other.getXRot());
        self.teleportTo(thereLevel, there.x, there.y, there.z, self.getYRot(), self.getXRot());

        SoundHelper.play(hereLevel, here.x, here.y, here.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
        SoundHelper.play(thereLevel, there.x, there.y, there.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
    }

    /**
     * 找出所有「其他」佩戴者，对应 RE 的 {@code SuperpositionHandler.getBaubleOwnersList}。
     *
     * <p>RE 遍历的是当前世界（{@code world.playerEntities}）里的玩家，即只在同一维度内找人；
     * 但换位逻辑里又写了「维度不同就交换维度」——只有在 RE 用了服务端全量玩家列表时才可能出现。
     * 这里按服务端全量玩家列表来找，功能上覆盖 RE 并让那段跨维度代码真正有意义。
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
            if (com.beiwu.forgottenrelics_plus.utils.CurioHelper.isEquipped(candidate, FRItems.SUPERPOSITION_RING.get())) {
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
