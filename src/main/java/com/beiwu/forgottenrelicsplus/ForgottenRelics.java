package com.beiwu.forgottenrelicsplus;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRArmorMaterials;
import com.beiwu.forgottenrelicsplus.registry.FRConditions;
import com.beiwu.forgottenrelicsplus.registry.FRCreativeTabs;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import com.beiwu.forgottenrelicsplus.registry.FREntities;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.registry.FRParticleTypes;
import com.beiwu.forgottenrelicsplus.registry.FRRecipeSerializers;
import com.beiwu.forgottenrelicsplus.registry.FRSounds;
import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ForgottenRelics.MOD_ID)
public final class ForgottenRelics {

    public static final String MOD_ID = "forgotten_relics_plus";

    public static final String MOD_NAME = "Forgotten Relics Plus";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public ForgottenRelics(IEventBus modBus, ModContainer container) {
        FRItems.register(modBus);
        FRConditions.register(modBus);
        FREntities.register(modBus);
        FRArmorMaterials.register(modBus);
        FRDataComponents.register(modBus);

        FRParticleTypes.register(modBus);
        FRRecipeSerializers.register(modBus);
        FRCreativeTabs.register(modBus);

        FRSounds.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, FRConfig.SPEC);

        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onDefaultComponents);

        LOGGER.info("Forgotten Relics Plus 正在加载……");
    }

    private void onDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (var registered : FRItems.ITEMS.getEntries()) {
            var item = registered.get();
            if (item instanceof FRRechargable rechargeable) {
                int capacity = rechargeable.getMaxCharge(new ItemStack(item), null);
                event.modify(
                        item,
                        builder -> builder.set(
                                TTDataComponents.RECHARGEABLE.get(),
                                new ChargeProfile(capacity, ChargeDisplay.ON_CHANGE)));
            }
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        long count = BuiltInRegistries.ITEM.keySet().stream()
                .filter(key -> key.getNamespace().equals(MOD_ID))
                .count();
        LOGGER.info("Forgotten Relics Plus：已注册 {} 件物品", count);
    }
}
