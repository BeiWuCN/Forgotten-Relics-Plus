package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式标签页。
 *
 * <p>图标以原版为准：原版 {@code Main} 里那个匿名 {@code CreativeTabs("tabForgottenRelics")}
 * 返回的是 {@code itemApotheosis}（「神化」，{@code Main.java:363-369}）；
 * 改成「恐惧之冠」的是 RE，本移植不沿用。
 * 1.21.1 的标签页是数据驱动的注册对象，图标通过 {@code CreativeModeTab.builder()} 指定。
 *
 * <p>注意：翻译键沿用原版的 {@code itemGroup.tabForgottenRelics}。
 */
public final class FRCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FORGOTTEN_RELICS =
            TABS.register("forgotten_relics_plus", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabForgottenRelics"))
                    // 原版的标签页图标是「神化」(itemApotheosis)，不是恐惧之冠。
                    .icon(() -> new ItemStack(FRItems.APOTHEOSIS.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(FRItems.MINING_CHARM.get());
                        output.accept(FRItems.ADVANCED_MINING_CHARM.get());
                        output.accept(FRItems.SUPERPOSITION_RING.get());
                        output.accept(FRItems.WEATHER_STONE.get());
                        output.accept(FRItems.XP_TOME.get());
                        output.accept(FRItems.DIMENSIONAL_MIRROR.get());
                        output.accept(FRItems.ANCIENT_AEGIS.get());
                        output.accept(FRItems.DARK_SUN_RING.get());
                        output.accept(FRItems.DEIFIC_AMULET.get());
                        output.accept(FRItems.OBLIVION_AMULET.get());
                        output.accept(FRItems.TERROR_CROWN.get());
                        output.accept(FRItems.SHINY_STONE.get());
                        output.accept(FRItems.ARCANUM.get());
                        output.accept(FRItems.DORMANT_ARCANUM.get());
                        output.accept(FRItems.DISCORD_RING.get());
                        // 原创补完物品，排在最后。
                        output.accept(FRItems.GHASTLY_SKULL.get());
                        output.accept(FRItems.CHAOS_CORE.get());
                        output.accept(FRItems.OMEGA_CORE.get());
                        output.accept(FRItems.PARADOX.get());
                        output.accept(FRItems.FALSE_JUSTICE.get());
                        output.accept(FRItems.THUNDERPEAL.get());
                        output.accept(FRItems.TELEPORTATION_TOME.get());
                        output.accept(FRItems.DEVOURER_OF_THE_VOID.get());
                        output.accept(FRItems.ELDRITCH_SPELL.get());
                        output.accept(FRItems.CRIMSON_SPELL.get());
                        output.accept(FRItems.TOME_OF_PRIMAL_CHAOS.get());
                        output.accept(FRItems.NUCLEAR_FURY.get());
                        output.accept(FRItems.SOUL_TOME.get());
                        output.accept(FRItems.EDICT_OF_BANISHMENT.get());
                        output.accept(FRItems.VOID_GRIMOIRE.get());
                        output.accept(FRItems.TOME_OF_PREDESTINY.get());
                        output.accept(FRItems.TOME_OF_LUNAR_FLARES.get());
                        output.accept(FRItems.APOTHEOSIS.get());
                        output.accept(FRItems.TOME_OF_BROKEN_FATES.get());
                        output.accept(FRItems.OBLIVION_STONE.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private FRCreativeTabs() {
    }
}
