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
 * <p>对应 1.12.2 原版 {@code Main} 里那个匿名 {@code CreativeTabs("tabForgottenRelics")}，
 * 它的图标是「恐惧王冠」（{@code terrorCrown}）。1.21.1 的标签页是数据驱动的注册对象，
 * 图标通过 {@code CreativeModeTab.builder()} 指定。
 *
 * <p>注意：翻译键沿用原版的 {@code itemGroup.tabForgottenRelics}。
 */
public final class FRCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FORGOTTEN_RELICS =
            TABS.register("forgotten_relics_plus", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabForgottenRelics"))
                    // 原版标签页图标就是恐惧之冠，移植完成后换回它。
                    .icon(() -> new ItemStack(FRItems.TERROR_CROWN.get()))
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
                        // 第四波：核心类与武器。
                        output.accept(FRItems.CHAOS_CORE.get());
                        output.accept(FRItems.OMEGA_CORE.get());
                        output.accept(FRItems.PARADOX.get());
                        output.accept(FRItems.FALSE_JUSTICE.get());
                        // 第六波：弹射物书籍。
                        output.accept(FRItems.THUNDERPEAL.get());
                        // 第七波：错位之典。
                        output.accept(FRItems.TELEPORTATION_TOME.get());
                        // 第七波：虚空吞噬者。
                        output.accept(FRItems.DEVOURER_OF_THE_VOID.get());
                        // 第七波：邪术之咒。
                        output.accept(FRItems.ELDRITCH_SPELL.get());
                        // 第七波：腥红之咒。
                        output.accept(FRItems.CRIMSON_SPELL.get());
                        // 第七波：原初混沌之典。
                        output.accept(FRItems.TOME_OF_PRIMAL_CHAOS.get());
                        // 第六波：核子之怒。
                        output.accept(FRItems.NUCLEAR_FURY.get());
                        // 第七波：千咒之诫。
                        output.accept(FRItems.SOUL_TOME.get());
                        // 第七波：永恒放逐之诫。
                        output.accept(FRItems.EDICT_OF_BANISHMENT.get());
                        // 第七波：深渊魔典。
                        output.accept(FRItems.VOID_GRIMOIRE.get());
                        // 第七波：预言之典。
                        output.accept(FRItems.TOME_OF_PREDESTINY.get());
                        // 第七波：月耀咒书。
                        output.accept(FRItems.TOME_OF_LUNAR_FLARES.get());
                        // 第七波：神化。
                        output.accept(FRItems.APOTHEOSIS.get());
                        // 第七波：破碎的命运巨著。
                        output.accept(FRItems.TOME_OF_BROKEN_FATES.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private FRCreativeTabs() {
    }
}
