package com.beiwu.forgottenrelics_plus.items;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 饰品基类。
 *
 * <p>对应 1.12.2 原版 {@code ItemBaubleBase}（它继承了 Botania 的同类，带一整套「右键装备、
 * 外观物品、幻影墨水、死亡自动卸下」）。1.21.1 的 Curios 已内置这些通用行为，因此这里只保留：
 *
 * <ul>
 *   <li>堆叠上限固定为 1（对应原版构造器里的 {@code setMaxStackSize(1)}）；</li>
 *   <li>属性修饰符钩子 —— 原版是在装备/卸下时手动往属性表加加减减，Curios 改成由框架统一
 *       施加与撤销，子类只要往 multimap 里填；</li>
 *   <li>「拿在手上右键即可装备」。</li>
 * </ul>
 *
 * <p>Shift 展开式 tooltip 由父类 {@link FRItem} 提供。
 */
public abstract class FRCurioItem extends FRItem implements ICurioItem {

    protected FRCurioItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = com.google.common.collect.LinkedHashMultimap.create();
        // 只有真的戴在某个槽位上时才给出加成；查看 tooltip 时 index 为 -1、持有者为空。
        if (slotContext.entity() != null) {
            fillAttributeModifiers(modifiers, slotContext, stack, id);
        }
        return modifiers;
    }

    /**
     * 子类在这里声明装备时生效的属性修饰符。
     *
     * <p>{@code id} 由 Curios 按槽位生成，同一枚饰品在不同槽位拿到的 id 不同，因此不必自己准备 UUID。
     */
    protected void fillAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers,
                                          SlotContext slotContext, ItemStack stack, ResourceLocation id) {
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // 与原版一致：拿在手上右键即可装备。
        return true;
    }
}
