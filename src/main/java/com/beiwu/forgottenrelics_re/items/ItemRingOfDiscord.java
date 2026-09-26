package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.registry.FRDataComponents;
import com.beiwu.forgottenrelics_re.registry.FRItems;
import com.beiwu.forgottenrelics_re.utils.CurioHelper;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 不和谐之戒（Ring of Discord，注册名 {@code discord_ring}），1.12.2 原版 {@code ItemRingOfDiscord}，戒指槽。
 *
 * <p>它本身不产生任何效果，只保存一个「是否开启不和谐模式」的开关，
 * 供后续的《错位之典》读取（原版 {@code isDiscordActive}）。开关由默认 X 键切换：
 * 客户端按键 → 发一个空载荷到服务端 → 服务端改写开关并回一条状态提示。
 *
 * <p>1.12.2 用的是 {@code SimpleNetworkWrapper} 加 {@code IMessage}；1.21.1 换成了
 * {@code CustomPacketPayload} + {@code RegisterPayloadHandlersEvent}，见 {@code network} 包。
 *
 * <p>注意：{@code ItemDiscordRing} 是原版的遗留类，注册表里用的是 {@code ItemRingOfDiscord}，
 * 这里只移植后者。
 */
public class ItemRingOfDiscord extends FRCurioItem {

    /**
     * 键位提示文案。
     *
     * <p>键位是客户端概念，服务端没有 {@code KeyMapping}。原版靠 {@code @SideOnly(CLIENT)} 规避，
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

    public static boolean isDiscordEnabled(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.DISCORD_ENABLED.get(), false);
    }

    public static void setDiscordEnabled(ItemStack stack, boolean enabled) {
        stack.set(FRDataComponents.DISCORD_ENABLED.get(), enabled);
    }

    /** 佩戴者是否戴着戒指且开关为开。对应原版 {@code ItemRingOfDiscord.isDiscordActive}。 */
    public static boolean isDiscordActive(LivingEntity entity) {
        return CurioHelper.findEquipped(entity, FRItems.DISCORD_RING.get())
                .map(ItemRingOfDiscord::isDiscordEnabled)
                .orElse(false);
    }

    /**
     * 切换开关，并把新的状态念给玩家听。
     *
     * @return 没戴着戒指就返回 false
     */
    public static boolean toggle(ServerPlayer player) {
        return CurioHelper.findEquipped(player, FRItems.DISCORD_RING.get())
                .map(ring -> {
                    boolean next = !isDiscordEnabled(ring);
                    setDiscordEnabled(ring, next);
                    player.displayClientMessage(Component.translatable(
                            next ? "item.ItemDiscordRing5.lore" : "item.ItemDiscordRing4.lore"), false);
                    return true;
                })
                .orElse(false);
    }

    /** 开启时带附魔光效，与原版 {@code hasEffect} 一致。 */
    @Override
    public boolean isFoil(ItemStack stack) {
        return isDiscordEnabled(stack);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDiscordRing1.lore"));
        tooltip.add(Component.translatable("item.ItemDiscordRing2.lore"));
        tooltip.add(Component.translatable("item.ItemDiscordRing3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable(isDiscordEnabled(stack)
                ? "item.ItemDiscordRing5.lore"
                : "item.ItemDiscordRing4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDiscordRing6.lore")
                .append(" ")
                .append(keyHint.get()));
    }
}
