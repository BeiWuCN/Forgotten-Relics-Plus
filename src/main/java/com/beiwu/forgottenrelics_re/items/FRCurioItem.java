package com.beiwu.forgottenrelics_re.items;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 饰品基类。
 *
 * <p>对应 1.12.2 原版的 {@code ItemBaubleBase}（它继承 Botania 的 {@code ItemBaubleBase}，
 * 提供了「拿在手上右键自动装备」、外观物品与幻影墨水、死亡自动卸下等一整套通用行为）。
 *
 * <p>1.21.1 的 Curios 已内置这些通用行为：把物品注册为 {@code ICurioItem} 之后，右键装备、
 * 装备音效、死亡掉落规则都由 Curios 自己接管。因此这里只保留本项目仍然需要的东西：
 *
 * <ul>
 *   <li>堆叠上限固定为 1，对应原版 {@code ItemBaubleBase} 构造器里的 {@code setMaxStackSize(1)}；</li>
 *   <li>统一的 Shift 展开式 tooltip：未按 Shift 只显示一行提示，按下 Shift 才显示详细说明。
 *       这正是原版 {@code ItemBaubleBase.addInformation} 的行为，语言键也沿用原版的
 *       {@code item.FRShiftTooltip.lore} 与 {@code item.FREmpty.lore}；</li>
 *   <li>属性修饰符钩子。原版的 {@code ItemBaubleBaseModifier} 在装备/卸下时手动往玩家属性表上
 *       加/减 {@code AttributeModifier}；Curios 改用 {@code getAttributeModifiers} 由框架统一
 *       施加与撤销，因此这里改成由子类往 multimap 里填。</li>
 * </ul>
 *
 * <p>注意：语言文件里的颜色代码（{@code §5} 等）保持原样，直接复用 1.12.2 的 lang 条目，
 * 这样中文翻译可以原封不动搬过来。
 */
public abstract class FRCurioItem extends Item implements ICurioItem {

    public FRCurioItem(Properties properties) {
        // 原版 ItemBaubleBase 构造器里设置 maxStackSize = 1，这里保持一致。
        super(properties.stacksTo(1));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
        // 只有真的戴在某个槽位上时才给出属性加成；查看 tooltip 时 index 为 -1、持有者为空。
        if (slotContext.entity() != null) {
            fillAttributeModifiers(modifiers, slotContext, stack, id);
        }
        return modifiers;
    }

    /**
     * 子类在这里声明装备时生效的属性修饰符。
     *
     * <p>{@code id} 由 Curios 按槽位生成，同一枚饰品在不同槽位得到的 id 不同，
     * 因此不必自己准备 UUID（原版是用 {@code getBaubleUUID(stack)} 从 NBT 里取的随机 UUID）。
     */
    protected void fillAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers,
                                          SlotContext slotContext, ItemStack stack, ResourceLocation id) {
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            // 按下 Shift：显示该饰品的详细信息，末尾补一行空行
            appendShiftTooltip(stack, context, tooltip, flag);
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        } else {
            // 未按 Shift：只提示可以按住 Shift 看详情
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
    }

    /**
     * 子类在这里追加「按住 Shift 才显示」的详细说明。
     *
     * <p>原版把这段逻辑写在物品自己的 {@code addInformation} 里，每条文案都自带 {@code §5}
     * 之类的颜色代码；这里沿用同一套语言键，因此不需要额外设置样式。
     */
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // 与原版一致：拿在手上右键即可装备。
        return true;
    }
}
