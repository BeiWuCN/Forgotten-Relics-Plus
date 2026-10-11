package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.CurioHelper;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemRingOfDiscord extends FRCurioItem {

    private static Supplier<String> keyHint = () -> "X";

    public ItemRingOfDiscord(Properties properties) {
        super(properties);
    }

    public static void bindKeyHint(Supplier<String> supplier) {
        keyHint = supplier;
    }

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

        tooltip.add(Component.translatable("item.ItemDiscordRing4.lore")
                .append(" ")
                .append(keyHint.get()));
    }
}
