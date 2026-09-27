package com.beiwu.forgottenrelics_re.registry;

import com.beiwu.forgottenrelics_re.ForgottenRelics;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 护甲材质注册表。
 *
 * <p>恐惧之冠在 1.12.2 里是 {@code ItemArmor} + {@code ArmorMaterial.GOLD}（金材质头盔），
 * 但覆写了 {@code getArmorTexture} 指到自己的贴图。1.21.1 的护甲贴图由 {@code ArmorMaterial.Layer}
 * 决定，路径固定为 {@code textures/models/armor/<名字>_layer_1.png}，
 * 所以这里要单独建一个材质，而不是借用原版金材质。
 *
 * <p>数值沿用金：护甲值 2、附魔性 25、装备音效与修复材料都用金。
 * 耐久度在 1.21.1 由 {@code Item.Properties#durability} 单独给（原版是 {@code setMaxDamage(1000)}），
 * 所以不写在材质里。
 */
public final class FRArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ForgottenRelics.MOD_ID);

    /** 恐惧之冠的护甲材质，贴图取自 {@code textures/models/armor/crown_prs_layer_1.png}。 */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TERROR_CROWN =
            ARMOR_MATERIALS.register("terror_crown", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.HELMET, 2),
                    25,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT),
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
