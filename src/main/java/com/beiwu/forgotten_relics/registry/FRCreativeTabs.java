package com.beiwu.forgottenrelics_re.registry;

import com.beiwu.forgottenrelics_re.ForgottenRelics;
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
            TABS.register("forgotten_relics", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabForgottenRelics"))
                    // 原版图标是恐惧王冠；该物品尚未移植，先用已有的采矿护符占位，
                    // 等 ItemTerrorCrown 移植过来再换回去。
                    .icon(() -> new ItemStack(FRItems.ADVANCED_MINING_CHARM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(FRItems.MINING_CHARM.get());
                        output.accept(FRItems.ADVANCED_MINING_CHARM.get());
                        output.accept(FRItems.SUPERPOSITION_RING.get());
                        output.accept(FRItems.WEATHER_STONE.get());
                        output.accept(FRItems.XP_TOME.get());
                        output.accept(FRItems.DIMENSIONAL_MIRROR.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private FRCreativeTabs() {
    }
}
