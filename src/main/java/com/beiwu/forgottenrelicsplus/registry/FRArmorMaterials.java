package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FRArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TERROR_CROWN =
            ARMOR_MATERIALS.register("terror_crown", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 3),
                    0,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "crown_prs"))),
                    0.0F,
                    0.0F));

    public static void register(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
    }

    private FRArmorMaterials() {
    }
}
