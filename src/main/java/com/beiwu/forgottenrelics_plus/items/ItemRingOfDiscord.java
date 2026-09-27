package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.CurioHelper;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 不和谐之戒（Ring of Discord，注册名 {@code discord_ring}），RE 的 {@code ItemRingOfDiscord}，戒指槽。
 *
 * <p>它本身不产生任何效果，只在按键（默认 X）时替玩家<b>远程施放背包里的《错位之典》</b>：
 * 客户端按键 → 发一个空载荷到服务端 → 服务端调 {@link #triggerTome}，由它对那本书执行一次右键。
 * 书本身的行为一字不改。
 *
 * <p>RE（1.12.2）用的是 {@code SimpleNetworkWrapper} 加 {@code IMessage}；1.21.1 换成了
 * {@code CustomPacketPayload} + {@code RegisterPayloadHandlersEvent}，见 {@code network} 包。
 *
 * <p>注意：{@code ItemDiscordRing} 是 RE 的遗留类，注册表里用的是 {@code ItemRingOfDiscord}，
 * 这里只移植后者。
 */
public class ItemRingOfDiscord extends FRCurioItem {

    /**
     * 键位提示文案。
     *
     * <p>键位是客户端概念，服务端没有 {@code KeyMapping}。RE 靠 {@code @SideOnly(CLIENT)} 规避，
     * 1.21.1 没有这个注解，改成由客户端在初始化时把显示名注入进来；服务端保留 "X" 兜底，
     * 这样 tooltip 在两端都不会因为没有客户端类而崩。
     */
    private static Supplier<String> keyHint = () -> "X";

    public ItemRingOfDiscord(Properties properties) {
        super(properties);
    }

    /** 客户端调用：绑定实际键位的显示名。 */
    public static void bindKeyHint(Supplier<String> supplier) {
        keyHint = supplier;
    }

    /**
     * 按下键时：戴着戒指且背包里有错位之典 → <b>直接施放那本书</b>。
     *
     * <p>对应 1.7.10 {@code DiscordKeybindMessage.Handler}（{@code :46-52}）：戒指只负责
     * 「找到书并调用它的右键」，书本身的行为一字不改。
     *
     * <p>注意：1.7.10 没有「不谐模式」这种开关，那是 RE 的设计；本移植以 1.7.10 为准，
     * 按键就是一次远程施放。
     *
     * @return 没戴着戒指、或背包里没有那本书时返回 {@code false}
     */
    public static boolean triggerTome(ServerPlayer player) {
        if (CurioHelper.findEquipped(player, FRItems.DISCORD_RING.get()).isEmpty()) {
            return false;
        }
        ItemStack tome = findTome(player);
        if (tome.isEmpty()) {
            return false;
        }
        FRItems.TELEPORTATION_TOME.get().castFromRing(player, tome);
        return true;
    }

    /** 原版 {@code SuperpositionHandler.findFirst(player, itemTeleportationTome)}：扫主背包。 */
    private static ItemStack findTome(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (candidate.is(FRItems.TELEPORTATION_TOME.get())) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDiscordRing1.lore"));
        tooltip.add(Component.translatable("item.ItemDiscordRing2.lore"));
        tooltip.add(Component.translatable("item.ItemDiscordRing3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // RE 的 ItemDiscordRing.java:67：一行「Current Keybind:」+ 实际键位名。
        tooltip.add(Component.translatable("item.ItemDiscordRing4.lore")
                .append(" ")
                .append(keyHint.get()));
    }
}
