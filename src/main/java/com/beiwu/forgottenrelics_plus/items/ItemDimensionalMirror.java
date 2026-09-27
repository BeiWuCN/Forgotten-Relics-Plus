package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
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

/**
 * 空间魔镜（Dimensional Mirror）。
 *
 * <p>1.12.2 原版（{@code ItemDimensionalMirror}）逻辑：
 * <ul>
 *   <li>Shift + 右键：把当前位置与维度写进物品 NBT；</li>
 *   <li>普通右键：若已记录，开始 {@code dimensionalMirrorChannelDuration}（默认 80 tick）的蓄力，
 *       蓄力结束传送回记录点；维度不同则走 {@code ExtradimensionalTeleporter} 跨维度传送；</li>
 *   <li>「不能从末地出来」：玩家身处末地（维度 1）而记录点不是末地时，右键直接失效；</li>
 *   <li>{@code interdimensionalMirror}（默认 true）为 false 时，维度不同则不能传送；</li>
 *   <li>Vis 上限 {@code dimensionalMirrorMaxCharge}（默认 100）。</li>
 * </ul>
 *
 * <p>1.21.1 对应关系：
 * <ul>
 *   <li>物品 NBT → 数据组件。1.20.5 之后 {@code ItemStack} 直接存 NBT 的写法被移除，
 *       这里沿用 NeoForge 的 {@code minecraft:custom_data} 组件，
 *       键名保持原版的 {@code IStoredX / IStoredY / IStoredZ / IDimensionID}，
 *       这样从 1.12.2 存档迁移过来的镜子还能读出坐标；</li>
 *   <li>维度编号（原版是 int）→ {@code ResourceKey<Level>}，存的是维度 ID 字符串；</li>
 *   <li>跨维度传送：{@code ServerPlayer#teleportTo(ServerLevel, ...)}
 *       在目标维度不同时会自动完成换维度流程，替代原版的 {@code ExtradimensionalTeleporter}。</li>
 * </ul>
 *
 * <p>注意：原版还会在蓄力时用 {@code SuperpositionHandler.imposeBurst} 发一个网络包给周围玩家做特效。
 * 1.21.1 里粒子由服务端 {@code sendParticles} 直接广播，不再需要网络包。
 */
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
        return FRConfig.DIMENSIONAL_MIRROR_MAX_CHARGE.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.BOW
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FRConfig.DIMENSIONAL_MIRROR_CHANNEL_DURATION.get();
    }

    /** 是否已经记录过坐标。对应原版 {@code stack.hasTagCompound()}。 */
    private static boolean hasStoredLocation(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) && !stack.get(DataComponents.CUSTOM_DATA).isEmpty();
    }

    private static CompoundTag storedTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** 读取已记录的维度。原版存 int，这里为了可读性存维度 ID 字符串。 */
    private static String storedDimension(ItemStack stack) {
        CompoundTag tag = storedTag(stack);
        return tag.contains(TAG_DIMENSION) ? tag.getString(TAG_DIMENSION) : Level.OVERWORLD.location().toString();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            // Shift + 右键：记录当前位置与维度
            if (!level.isClientSide()) {
                CompoundTag tag = storedTag(stack);
                tag.putInt(TAG_X, player.blockPosition().getX());
                tag.putInt(TAG_Y, player.blockPosition().getY());
                tag.putInt(TAG_Z, player.blockPosition().getZ());
                tag.putString(TAG_DIMENSION, level.dimension().location().toString());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 2.0F);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.pass(stack);
        }
        if (!hasStoredLocation(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        // 原版的两条拦截规则
        if (!FRConfig.INTERDIMENSIONAL_MIRROR.get() && !storedDimension(stack).equals(level.dimension().location().toString())) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.dimension() == Level.END && !storedDimension(stack).equals(Level.END.location().toString())) {
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
        // 蓄力过程中持续冒末影粒子。原版这一段是 {@code PacketVoidMessage} 风格的
        // {@code spawnSuperParticle("portalstuff")} → 原版 {@code EntityPortalFX}，
        // 也就是原版传送门粒子本身，所以这里继续用 PORTAL，不算「用原版粒子代替 Botania」。
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypesHolder.PORTAL, center.x, center.y, center.z, 4,
                    (Math.random() - 0.5D) * 3.0D, (Math.random() - 0.5D) * 3.0D, (Math.random() - 0.5D) * 3.0D, 0.05D);
        }
        if (remainingUseDuration != 1) {
            return;
        }
        CompoundTag tag = storedTag(stack);
        int x = tag.getInt(TAG_X);
        int y = tag.getInt(TAG_Y);
        int z = tag.getInt(TAG_Z);
        String dimension = storedDimension(stack);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ServerLevel target = serverLevel.getServer().getLevel(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.parse(dimension)));
            if (target == null) {
                return;
            }
            serverPlayer.teleportTo(target, x + 0.5D, y + 0.5D, z + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());
            // 落地后的 128 个末影粒子（对应原版）
            target.sendParticles(ParticleTypesHolder.PORTAL, x + 0.5D, y - 0.5D, z + 0.5D, 128,
                    (Math.random() - 0.5D) * 3.0D, (Math.random() - 0.5D) * 3.0D, (Math.random() - 0.5D) * 3.0D, 0.05D);
        }
        float pitch = (float) (0.8D + Math.random() * 0.2D);
        SoundHelper.play(level, center.x, center.y, center.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // 已记录坐标时，无论是否按 Shift 都显示坐标（与原版一致）
        if (hasStoredLocation(stack)) {
            CompoundTag tag = storedTag(stack);
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.MirrorLoc.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.MirrorX.lore").append(Component.literal(String.valueOf(tag.getInt(TAG_X))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable("item.MirrorY.lore").append(Component.literal(String.valueOf(tag.getInt(TAG_Y))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable("item.MirrorZ.lore").append(Component.literal(String.valueOf(tag.getInt(TAG_Z))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.MirrorDimension.lore")
                    .append(Component.literal(storedDimension(stack)).withStyle(ChatFormatting.GOLD)));
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDimensionalMirror1.lore"));
        tooltip.add(Component.translatable("item.ItemDimensionalMirror2.lore"));
        tooltip.add(Component.translatable("item.ItemDimensionalMirror3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDimensionalMirror4.lore"));
    }

    /** 单独抽出来只是为了让上面的方法短一点。 */
    private static final class ParticleTypesHolder {
        private static final net.minecraft.core.particles.SimpleParticleType PORTAL = net.minecraft.core.particles.ParticleTypes.PORTAL;

        private ParticleTypesHolder() {
        }
    }
}
