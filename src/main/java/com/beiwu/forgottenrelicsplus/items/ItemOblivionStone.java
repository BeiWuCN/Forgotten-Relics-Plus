package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemOblivionStone extends FRItem implements IWarpingGear {

    private static final int INACTIVE_OFFSET = 100;

    private static final int CHECK_INTERVAL = 10;

    public ItemOblivionStone(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int getMode(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.OBLIVION_MODE.get(), 0);
    }

    public static void setMode(ItemStack stack, int mode) {
        stack.set(FRDataComponents.OBLIVION_MODE.get(), mode);
    }

    public static boolean isActive(ItemStack stack) {
        return getMode(stack) < INACTIVE_OFFSET;
    }

    public static List<ItemStack> getBoundItems(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.OBLIVION_BOUND_ITEMS.get(), List.of());
    }

    public static ItemStack normalizeSample(ItemStack sample) {
        ItemStack entry = sample.copy();
        entry.setCount(1);
        if (entry.isDamageableItem()) {
            entry.setDamageValue(0);
        }
        return entry;
    }

    public static boolean matchesBound(ItemStack candidate, ItemStack bound) {
        if (candidate.isEmpty()) {
            return false;
        }
        if (bound.isDamageableItem()) {
            return candidate.is(bound.getItem());
        }
        return ItemStack.isSameItemSameComponents(candidate, bound);
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.OBLIVION_STONE_WARP.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            int mode = getMode(stack);
            if (player.isShiftKeyDown()) {
                if (mode < INACTIVE_OFFSET) {

                    setMode(stack, mode + INACTIVE_OFFSET);
                    SoundHelper.play(
                            level,
                            player.blockPosition(),
                            SoundEvents.STONE_BUTTON_CLICK_OFF,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                } else {

                    setMode(stack, mode - INACTIVE_OFFSET);
                    SoundHelper.play(
                            level,
                            player.blockPosition(),
                            SoundEvents.STONE_BUTTON_CLICK_ON,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                }
            } else {
                int next;
                if (mode == 0 || mode == 1 || mode == INACTIVE_OFFSET || mode == INACTIVE_OFFSET + 1) {
                    next = mode + 1;
                } else if (mode == 2 || mode == INACTIVE_OFFSET + 2) {
                    next = mode - 2;
                } else {
                    next = mode;
                }
                setMode(stack, next);

                SoundHelper.play(
                        level,
                        player.blockPosition(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        1.0F,
                        (float) (0.8D + level.random.nextDouble() * 0.2D));
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        if (!isActive(stack)) {
            return;
        }
        List<ItemStack> bound = getBoundItems(stack);
        if (bound.isEmpty()) {
            return;
        }
        consumeStuff(player, bound, getMode(stack));
    }

    public static void consumeStuff(Player player, List<ItemStack> bound, int mode) {
        Inventory inventory = player.getInventory();

        int size = inventory.items.size();

        int filledStacks = 0;
        for (int slot = 0; slot < size; slot++) {
            if (!inventory.items.get(slot).isEmpty()) {
                filledStacks++;
            }
        }

        switch (mode) {
            case 0 -> consumeTotal(inventory, bound, size);
            case 1 -> consumeLastStack(inventory, bound, size);
            case 2 -> consumeExcess(inventory, bound, size, filledStacks);
            default -> {}
        }
    }

    private static void consumeTotal(Inventory inventory, List<ItemStack> bound, int size) {

        boolean any = false;
        for (int slot = 0; slot < size; slot++) {
            if (!inventory.items.get(slot).isEmpty() && !isOblivionStone(inventory.items.get(slot))) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        for (ItemStack entry : bound) {
            for (int slot = 0; slot < size; slot++) {
                ItemStack candidate = inventory.items.get(slot);
                if (candidate.isEmpty() || isOblivionStone(candidate)) {
                    continue;
                }
                if (matchesBound(candidate, entry)) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private static void consumeLastStack(Inventory inventory, List<ItemStack> bound, int size) {
        for (ItemStack entry : bound) {
            List<Integer> slots = matchingSlots(inventory, entry, size);
            if (slots.size() <= 1) {
                continue;
            }
            int keep = slots.get(0);
            for (int slot : slots) {
                if (inventory.items.get(slot).getCount()
                        > inventory.items.get(keep).getCount()) {
                    keep = slot;
                }
            }
            for (int slot : slots) {
                if (slot != keep) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private static void consumeExcess(Inventory inventory, List<ItemStack> bound, int size, int filledStacks) {
        if (filledStacks < size) {
            return;
        }
        for (ItemStack entry : bound) {
            List<Integer> slots = matchingSlots(inventory, entry, size);
            if (slots.isEmpty()) {
                continue;
            }
            int target = slots.get(0);
            for (int slot : slots) {
                int current = inventory.items.get(slot).getCount();
                int best = inventory.items.get(target).getCount();
                if (current < best || (current == best && slot > target)) {
                    target = slot;
                }
            }
            inventory.setItem(target, ItemStack.EMPTY);
            return;
        }
    }

    private static List<Integer> matchingSlots(Inventory inventory, ItemStack entry, int size) {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) {
            ItemStack candidate = inventory.items.get(slot);
            if (candidate.isEmpty() || isOblivionStone(candidate)) {
                continue;
            }
            if (matchesBound(candidate, entry)) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static boolean isOblivionStone(ItemStack stack) {
        return stack.is(FRItems.OBLIVION_STONE.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.OblivionStone1.lore"));
            tooltip.add(Component.translatable("item.OblivionStone2.lore"));
            tooltip.add(Component.translatable("item.OblivionStone2_more.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone3.lore"));
            tooltip.add(Component.translatable("item.OblivionStone4.lore"));
            tooltip.add(Component.translatable("item.OblivionStone5.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone6.lore"));
            tooltip.add(Component.translatable("item.OblivionStone7.lore"));
            tooltip.add(Component.translatable("item.OblivionStone8.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone9.lore"));
            tooltip.add(Component.translatable("item.OblivionStone10.lore"));
            tooltip.add(Component.translatable("item.OblivionStone11.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone12.lore"));
            tooltip.add(Component.translatable("item.OblivionStone13.lore"));
            tooltip.add(Component.translatable("item.OblivionStone14.lore"));
            tooltip.add(Component.translatable("item.OblivionStone15.lore"));
        } else if (Screen.hasControlDown()) {
            tooltip.add(Component.translatable("item.OblivionStoneCtrlList.lore"));
            addBoundListTooltip(stack, tooltip);
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
            tooltip.add(Component.translatable("item.OblivionStoneCtrlTooltip.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            int mode = getMode(stack);
            Component modeName = mode < INACTIVE_OFFSET
                    ? Component.translatable("item.OblivionMode" + mode + ".lore")
                    : Component.translatable("item.OblivionStoneDeactivated.lore");
            tooltip.add(Component.translatable("item.OblivionStoneMode.lore")
                    .append(" ")
                    .append(modeName));
        }

        tooltip.add(Component.translatable("item.FREmpty.lore"));
    }

    private static void addBoundListTooltip(ItemStack stack, List<Component> tooltip) {
        List<ItemStack> bound = getBoundItems(stack);
        if (bound.isEmpty()) {
            return;
        }
        int softCap = FRConfig.OBLIVION_STONE_SOFT_CAP.get();
        if (bound.size() <= softCap) {
            for (ItemStack entry : bound) {
                tooltip.add(Component.literal(" - " + entry.getHoverName().getString())
                        .withStyle(ChatFormatting.GOLD));
            }
        } else {
            for (int i = 0; i < softCap; i++) {
                ItemStack entry = bound.get(ThreadLocalRandom.current().nextInt(bound.size()));
                tooltip.add(Component.literal(" - " + entry.getHoverName().getString())
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
