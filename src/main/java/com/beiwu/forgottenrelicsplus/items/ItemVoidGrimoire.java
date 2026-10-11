package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.client.FRParticles;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FRSounds;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ItemVoidGrimoire extends FRItem implements FRRechargable, IWarpingGear {

    private static final double SEARCH_RANGE = 64.0D;
    private static final double SEARCH_TOLERANCE = 3.0D;

    private static final double COLLISION_BORDER = 0.1D;

    private static final int VIS_INTERVAL = 20;

    private static final int SLOWNESS_DURATION = 30;
    private static final int SLOWNESS_AMPLIFIER = 100;

    private static final double ASCEND_MOTION = 0.03D;

    private static final double ABYSS_Y = -100000.0D;
    private static final double VOID_SPREAD = 20002.0D;

    private static final int WISP_COLOR = 0x590099;

    public ItemVoidGrimoire(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCostPerSecond() {
        return (int) (FRConfig.VOID_GRIMOIRE_VIS_COST.get() * FRConfig.VOID_GRIMOIRE_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.VOID_GRIMOIRE_MAX_CHARGE.get()
                : FRConfig.VOID_GRIMOIRE_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.VOID_GRIMOIRE_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {

        return FRConfig.VOID_GRIMOIRE_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity target = findPointedEntity(level, player);
        if (target == null) {

            if (!level.isClientSide()) {
                stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            stack.set(FRDataComponents.VOID_GRIMOIRE_TARGET.get(), target.getId());
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        Integer targetId = stack.get(FRDataComponents.VOID_GRIMOIRE_TARGET.get());

        if (level.isClientSide()) {
            if (targetId != null && level.getEntity(targetId) instanceof LivingEntity clientTarget) {
                channelingParticles(level, particleAnchor(clientTarget));
            }
            return;
        }

        ServerLevel server = (ServerLevel) level;

        int duration = getUseDuration(stack, player);
        int elapsed = duration - remainingUseDuration;
        if (elapsed % VIS_INTERVAL == 0 && !RechargeAccess.consumeCharge(stack, player, getVisCostPerSecond())) {
            stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
            player.stopUsingItem();
            return;
        }

        if (targetId == null) {

            player.stopUsingItem();
            return;
        }
        Entity targetEntity = server.getEntity(targetId);
        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {

            stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
            player.stopUsingItem();
            return;
        }

        target.resetFallDistance();

        target.addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, true, true));

        Vec3 motion = target.getDeltaMovement();
        target.setDeltaMovement(motion.x, ASCEND_MOTION, motion.z);

        target.hurtMarked = true;

        target.noPhysics = true;

        Vec3 thisPos = particleAnchor(target);

        if (remainingUseDuration == duration) {
            SoundHelper.play(
                    level, thisPos.x, thisPos.y, thisPos.z, FRSounds.MD_CHARGE.get(), SoundSource.PLAYERS, 4.0F, 0.75F);
        }

        if (remainingUseDuration != 1) {
            return;
        }

        voidBurst(server, thisPos);

        finishBurst(server, thisPos);

        SoundHelper.play(
                level,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS,
                4.0F,
                0.8F + server.random.nextFloat() * 0.2F);

        overthrow(server, target, player);
        stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());

        CooldownHelper.setCooldown(player, FRConfig.VOID_GRIMOIRE_COOLDOWN.get());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {

        if (!level.isClientSide()) {
            stack.remove(FRDataComponents.VOID_GRIMOIRE_TARGET.get());
        }
    }

    private static void overthrow(ServerLevel level, LivingEntity target, Player caster) {
        double x = (level.random.nextDouble() - 0.5D) * VOID_SPREAD;
        double z = (level.random.nextDouble() - 0.5D) * VOID_SPREAD;
        double y = ABYSS_Y + (level.random.nextDouble() - 0.5D) * VOID_SPREAD;

        if (target instanceof ServerPlayer serverPlayer) {

            serverPlayer.teleportTo(level, x, y, z, serverPlayer.getYRot(), serverPlayer.getXRot());
            broadcastOverthrow(caster, serverPlayer);
            return;
        }

        target.teleportTo(x, y, z);
        target.discard();
    }

    private static void broadcastOverthrow(Player caster, ServerPlayer victim) {
        MinecraftServer server = caster.getServer();
        if (server == null) {
            return;
        }
        Component message = Component.literal(caster.getDisplayName().getString() + " ")
                .append(Component.translatable("message.overthrown1"))
                .append(Component.literal(" " + victim.getDisplayName().getString() + " "))
                .append(Component.translatable("message.overthrown3"));
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private static Vec3 particleAnchor(LivingEntity target) {
        return target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D).add(0.0D, 0.03D, 0.0D);
    }

    private static void channelingParticles(Level level, Vec3 center) {

        for (int i = 0; i < 8; i++) {
            double px = center.x + (level.random.nextDouble() - 0.5D) * 12.0D;
            double py = center.y + (level.random.nextDouble() - 0.5D) * 12.0D;
            double pz = center.z + (level.random.nextDouble() - 0.5D) * 12.0D;
            FRParticles.wisp(
                    level,
                    px,
                    py,
                    pz,
                    0.2F + level.random.nextFloat() * 0.3F,
                    0.0F,
                    0.5F + level.random.nextFloat() * 0.2F,
                    0.2F + level.random.nextFloat() * 0.2F,
                    (center.x - px) * 0.08D,
                    (center.y - py) * 0.08D,
                    (center.z - pz) * 0.08D,
                    0.45F);
        }
        for (int i = 0; i < 5; i++) {
            level.addParticle(
                    ParticleTypes.PORTAL,
                    center.x,
                    center.y,
                    center.z,
                    (level.random.nextDouble() - 0.5D) * 8.0D,
                    (level.random.nextDouble() - 0.5D) * 8.0D,
                    (level.random.nextDouble() - 0.5D) * 8.0D);
        }
    }

    private static void finishBurst(ServerLevel level, Vec3 center) {
        FRParticles.serverWispBurst(
                level,
                center.x,
                center.y,
                center.z,
                0.2F + level.random.nextFloat() * 0.3F,
                0.0F,
                0.5F + level.random.nextFloat() * 0.2F,
                0.4F + level.random.nextFloat() * 0.4F,
                1.0F,
                129,
                0.0D,
                0.144D);
    }

    private static void voidBurst(ServerLevel level, Vec3 center) {

        FRParticles.serverWispBurst(
                level,
                center.x,
                center.y,
                center.z,
                0.0F,
                (float) (0.8D + level.random.nextDouble() * 0.2D),
                (float) (0.4D + level.random.nextDouble() * 0.6D),
                2.0F,
                1.0F,
                1,
                0.0D,
                0.0D);
        level.sendParticles(
                ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, WISP_COLOR),
                center.x,
                center.y,
                center.z,
                32,
                0.5D,
                0.5D,
                0.5D,
                0.1D);
    }

    private static LivingEntity findPointedEntity(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(SEARCH_RANGE));
        AABB search = new AABB(eye, end).inflate(SEARCH_TOLERANCE);
        LivingEntity best = null;
        double bestDistance = SEARCH_RANGE;
        for (Entity candidate :
                level.getEntities(player, search, entity -> entity instanceof LivingEntity && entity.isPickable())) {
            AABB box = candidate.getBoundingBox().inflate(COLLISION_BORDER);
            Optional<Vec3> hit = box.clip(eye, end);
            double distance;
            if (hit.isPresent()) {
                distance = eye.distanceTo(hit.get());
            } else if (box.contains(eye)) {
                distance = 0.0D;
            } else {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (LivingEntity) candidate;
            }
        }
        return best;
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.ItemVoidGrimoire1.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire2.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire4.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemVoidGrimoire6.lore"));
    }
}
