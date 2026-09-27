package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
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

/**
 * 护甲材质注册表。
 *
 * <p>1.7.10 用的不是原版材质，而是 {@code RelicsMaterialHandler.materialNobleGold}
 * （{@code EnumHelper.addArmorMaterial("NOBLEGOLD", 40, new int[]{3, 9, 6, 3}, 0)}）：
 * 头盔护甲值 <b>3</b>、附魔性 <b>0</b>（配合 ItemTerrorCrown 的剥离逻辑，等于禁止附魔）。
 * 1.12.2 移植版（RE）改用了金材质（护甲 2、附魔性 25），本项目以 1.7.10 为准取 NOBLEGOLD。
 *
 * <p>1.21.1 的护甲贴图由 {@code ArmorMaterial.Layer} 决定，路径固定为
 * {@code textures/models/armor/<名字>_layer_1.png}，所以这里要单独建一个材质。
 * 装备音效与修复材料仍沿用金。
 * 耐久度在 1.21.1 由 {@code Item.Properties#durability} 单独给（原版是 {@code setMaxDamage(1000)}），
 * 所以不写在材质里。
 */
public final class FRArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ForgottenRelics.MOD_ID);

    /** 恐惧之冠的护甲材质，贴图取自 {@code textures/models/armor/crown_prs_layer_1.png}。 */
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
