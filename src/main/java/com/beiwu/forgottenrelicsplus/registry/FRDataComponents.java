package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FRDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(ForgottenRelics.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> STORED_DAMAGE =
            DATA_COMPONENTS.registerComponentType("stored_damage",
                    builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INVINCIBILITY_COOLDOWN =
            DATA_COMPONENTS.registerComponentType("invincibility_cooldown",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHINY_STILL_TICKS =
            DATA_COMPONENTS.registerComponentType("shiny_still_ticks",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DORMANT_LIFETIME =
            DATA_COMPONENTS.registerComponentType("dormant_lifetime",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> DEVOURER_TARGET =
            DATA_COMPONENTS.registerComponentType("devourer_target",
                    builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EDICT_TARGET =
            DATA_COMPONENTS.registerComponentType("edict_target",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VOID_GRIMOIRE_TARGET =
            DATA_COMPONENTS.registerComponentType("void_grimoire_target",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FATE_COOLDOWN =
            DATA_COMPONENTS.registerComponentType("fate_cooldown",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> OBLIVION_MODE =
            DATA_COMPONENTS.registerComponentType("oblivion_mode",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> OBLIVION_BOUND_ITEMS =
            DATA_COMPONENTS.registerComponentType("oblivion_bound_items",
                    builder -> builder.persistent(ItemStack.CODEC.listOf())
                            .networkSynchronized(ItemStack.LIST_STREAM_CODEC));

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }

    private FRDataComponents() {
    }
}
