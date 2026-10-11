package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FRSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LUNAR_FLARE = register("lunar_flare");

    public static final DeferredHolder<SoundEvent, SoundEvent> STARFALL = register("starfall");

    public static final DeferredHolder<SoundEvent, SoundEvent> MD_CHARGE = register("md_charge");

    public static final DeferredHolder<SoundEvent, SoundEvent> DIRECTED = register("directed");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private FRSounds() {
    }
}
