package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.MirrorLocationData;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemDimensionalMirror extends FRItem implements FRRechargable {

    private static final String TAG_X = "IStoredX";
    private static final String TAG_Y = "IStoredY";
    private static final String TAG_Z = "IStoredZ";
    private static final String TAG_DIMENSION = "IDimensionID";

    public ItemDimensionalMirror(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.DIMENSIONAL_MIRROR_MAX_CHARGE.get()
                : FRConfig.DIMENSIONAL_MIRROR_MAX_CHARGE.getDefault();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FRConfig.DIMENSIONAL_MIRROR_CHANNEL_DURATION.get();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasStoredLocation(stack);
    }

    public static void onServerLeftClick(ServerPlayer player) {
        if (!player.isShiftKeyDown()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(FRItems.DIMENSIONAL_MIRROR.get()) || !hasStoredLocation(stack)) {
            return;
        }
        CompoundTag tag = storedTag(stack);
        MirrorLocationData.clear(tag);
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        SoundHelper.play(
                player.level(),
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.STONE_BUTTON_CLICK_OFF,
                SoundSource.PLAYERS,
                1.0F,
                0.6F);
        player.displayClientMessage(Component.translatable("message.mirror_cleared"), true);
    }

    private static boolean hasStoredLocation(ItemStack stack) {
        return MirrorLocationData.isValid(storedTag(stack));
    }

    private static CompoundTag storedTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static String storedDimension(ItemStack stack) {
        CompoundTag tag = storedTag(stack);
        return tag.contains(TAG_DIMENSION)
                ? tag.getString(TAG_DIMENSION)
                : Level.OVERWORLD.location().toString();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                CompoundTag tag = storedTag(stack);
                tag.putInt(TAG_X, player.blockPosition().getX());
                tag.putInt(TAG_Y, player.blockPosition().getY());
                tag.putInt(TAG_Z, player.blockPosition().getZ());
                tag.putString(TAG_DIMENSION, level.dimension().location().toString());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                SoundHelper.play(
                        level,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS,
                        1.0F,
                        2.0F);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.pass(stack);
        }
        if (!hasStoredLocation(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!FRConfig.INTERDIMENSIONAL_MIRROR.get()
                && !storedDimension(stack).equals(level.dimension().location().toString())) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.dimension() == Level.END
                && !storedDimension(stack).equals(Level.END.location().toString())) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        Vec3 center = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypesHolder.PORTAL,
                    center.x,
                    center.y,
                    center.z,
                    4,
                    (Math.random() - 0.5D) * 3.0D,
                    (Math.random() - 0.5D) * 3.0D,
                    (Math.random() - 0.5D) * 3.0D,
                    0.05D);
        }
        if (remainingUseDuration != 1 || !hasStoredLocation(stack)) {
            return;
        }
        CompoundTag tag = storedTag(stack);
        int x = tag.getInt(TAG_X);
        int y = tag.getInt(TAG_Y);
        int z = tag.getInt(TAG_Z);
        String dimension = storedDimension(stack);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            var targetDimension = net.minecraft.resources.ResourceLocation.tryParse(dimension);
            if (targetDimension == null) {
                return;
            }
            ServerLevel target = serverLevel
                    .getServer()
                    .getLevel(net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.DIMENSION, targetDimension));
            if (target == null) {
                return;
            }
            serverPlayer.teleportTo(
                    target, x + 0.5D, y + 0.5D, z + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());

            target.sendParticles(
                    ParticleTypesHolder.PORTAL,
                    x + 0.5D,
                    y - 0.5D,
                    z + 0.5D,
                    128,
                    (Math.random() - 0.5D) * 3.0D,
                    (Math.random() - 0.5D) * 3.0D,
                    (Math.random() - 0.5D) * 3.0D,
                    0.05D);
        }
        float pitch = (float) (0.8D + Math.random() * 0.2D);
        SoundHelper.play(
                level, center.x, center.y, center.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                pitch);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        if (hasStoredLocation(stack)) {
            CompoundTag tag = storedTag(stack);
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.MirrorLoc.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.MirrorX.lore")
                    .append(Component.literal(String.valueOf(tag.getInt(TAG_X))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable("item.MirrorY.lore")
                    .append(Component.literal(String.valueOf(tag.getInt(TAG_Y))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable("item.MirrorZ.lore")
                    .append(Component.literal(String.valueOf(tag.getInt(TAG_Z))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.MirrorDimension.lore")
                    .append(Component.literal(storedDimension(stack)).withStyle(ChatFormatting.GOLD)));
        }
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDimensionalMirror1.lore"));
        tooltip.add(Component.translatable("item.ItemDimensionalMirror2.lore"));
        tooltip.add(Component.translatable("item.ItemDimensionalMirror3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDimensionalMirror4.lore"));

        tooltip.add(Component.translatable("item.ItemDimensionalMirror5.lore"));
    }

    private static final class ParticleTypesHolder {
        private static final net.minecraft.core.particles.SimpleParticleType PORTAL =
                net.minecraft.core.particles.ParticleTypes.PORTAL;

        private ParticleTypesHolder() {}
    }
}
