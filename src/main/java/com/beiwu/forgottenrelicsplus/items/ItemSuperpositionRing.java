package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.CurioHelper;
import com.beiwu.forgottenrelicsplus.utils.FRDamageTypes;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
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

public class ItemSuperpositionRing extends FRCurioItem implements WearerTickBehaviour, IncomingDamageBehaviour {

    public ItemSuperpositionRing(Properties properties) {
        super(properties);
    }

    @Override
    public int priority() {
        return 60;
    }

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

        if (wearer.tickCount % FRConfig.SUPERPOSITION_RING_CHECK_INTERVAL.get() != 0) {
            return;
        }
        if (Math.random() > FRConfig.SUPERPOSITION_RING_SWAP_CHANCE.get()) {
            return;
        }

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

        float pitch = (float) (0.8D + Math.random() * 0.2D);

        other.teleportTo(hereLevel, here.x, here.y, here.z, other.getYRot(), other.getXRot());
        self.teleportTo(thereLevel, there.x, there.y, there.z, self.getYRot(), self.getXRot());

        SoundHelper.play(hereLevel, here.x, here.y, here.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
        SoundHelper.play(thereLevel, there.x, there.y, there.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static List<ServerPlayer> findOtherWearers(LivingEntity wearer) {
        List<ServerPlayer> result = new ArrayList<>();
        if (!(wearer.level() instanceof ServerLevel level)) {
            return result;
        }
        for (ServerPlayer candidate : level.getServer().getPlayerList().getPlayers()) {
            if (candidate == wearer) {
                continue;
            }
            if (com.beiwu.forgottenrelicsplus.utils.CurioHelper.isEquipped(candidate, FRItems.SUPERPOSITION_RING.get())) {
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
