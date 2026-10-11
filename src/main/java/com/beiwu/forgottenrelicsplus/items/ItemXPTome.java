package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class ItemXPTome extends FRItem {

    private static final String TAG_ACTIVE = "IsActive";

    private static final String TAG_ABSORPTION = "AbsorptionMode";

    private static final String TAG_XP_STORED = "XPStored";

    public ItemXPTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean isActive(ItemStack stack) {
        return tag(stack).getBoolean(TAG_ACTIVE);
    }

    private static boolean isAbsorption(ItemStack stack) {
        CompoundTag tag = tag(stack);

        return !tag.contains(TAG_ABSORPTION) || tag.getBoolean(TAG_ABSORPTION);
    }

    private static int getStoredXp(ItemStack stack) {
        return tag(stack).getInt(TAG_XP_STORED);
    }

    private static void setStoredXp(ItemStack stack, int amount) {
        CompoundTag tag = tag(stack);
        tag.putInt(TAG_XP_STORED, Math.max(0, amount));
        setTag(stack, tag);
    }

    public static int getPlayerXp(Player player) {
        int total = 0;
        if (player.experienceLevel > 0) {
            total = getXpForLevel(player.experienceLevel);
        }
        return total + (int) (player.experienceProgress * player.getXpNeededForNextLevel());
    }

    public static int getXpForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5D * level * level - 40.5D * level + 360.0D);
        }
        return (int) (4.5D * level * level - 162.5D * level + 2220.0D);
    }

    public static void drainPlayerXp(Player player, int amount) {
        addPlayerXp(player, -amount);
    }

    public static void addPlayerXp(Player player, int amount) {
        int total = getPlayerXp(player) + amount;
        if (total < 0) {
            total = 0;
        }
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.totalExperience = 0;
        while (total >= player.getXpNeededForNextLevel()) {
            total -= player.getXpNeededForNextLevel();
            player.experienceLevel++;
        }
        player.experienceProgress = player.getXpNeededForNextLevel() == 0
                ? 0.0F
                : (float) total / (float) player.getXpNeededForNextLevel();
        player.totalExperience = getXpForLevel(player.experienceLevel) + total;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player) || !isActive(stack)) {
            return;
        }
        int rate = FRConfig.XP_TOME_TRANSFER_RATE.get();
        if (isAbsorption(stack)) {
            int playerXp = getPlayerXp(player);
            if (playerXp <= 0) {
                return;
            }
            int moved = Math.min(rate, playerXp);
            drainPlayerXp(player, moved);
            setStoredXp(stack, getStoredXp(stack) + moved);
        } else {
            int stored = getStoredXp(stack);
            if (stored <= 0) {
                return;
            }
            int moved = Math.min(rate, stored);
            setStoredXp(stack, stored - moved);
            addPlayerXp(player, moved);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (!player.isShiftKeyDown()) {
                CompoundTag tag = tag(stack);
                tag.putBoolean(TAG_ABSORPTION, !isAbsorption(stack));
                setTag(stack, tag);
                SoundHelper.play(level, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                        1.0F, (float) (0.4D + Math.random() * 0.1D));
            } else {
                CompoundTag tag = tag(stack);
                boolean nowActive = !isActive(stack);
                tag.putBoolean(TAG_ACTIVE, nowActive);
                setTag(stack, tag);

                SoundHelper.play(level, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                        1.0F, (float) (0.8D + Math.random() * 0.2D));
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, nowActive);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {

        return isActive(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Component mode = !isActive(stack)
                ? Component.translatable("item.ItemXPTomeDeactivated.lore")
                : Component.translatable(isAbsorption(stack)
                        ? "item.ItemXPTomeAbsorption.lore"
                        : "item.ItemXPTomeExtraction.lore");
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.ItemXPTome1.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome2.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome3.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome4.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome5.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome6.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome7.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome8.lore"));
            tooltip.add(Component.translatable("item.ItemXPTome9.lore"));
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemXPTomeMode.lore").append(Component.literal(" ")).append(mode));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemXPTomeExp.lore"));
        int stored = getStoredXp(stack);
        tooltip.add(Component.literal(String.valueOf(stored)).withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" "))
                .append(Component.translatable("item.ItemXPTomeUnits.lore"))
                .append(Component.literal(" "))
                .append(Component.literal(String.valueOf(levelFromXp(stored))).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" "))
                .append(Component.translatable("item.ItemXPTomeLevels.lore")));
    }

    public static int levelFromXp(int xp) {
        int level = 0;
        while (xp >= xpNeededForLevel(level)) {
            xp -= xpNeededForLevel(level);
            level++;
        }
        return level;
    }

    private static int xpNeededForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }
}
