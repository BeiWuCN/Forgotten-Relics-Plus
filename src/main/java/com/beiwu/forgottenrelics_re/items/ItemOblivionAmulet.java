package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.registry.FRDataComponents;
import com.beiwu.forgottenrelics_re.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 湮灭护符（Amulet of The Oblivion），1.12.2 原版 {@code ItemOblivionAmulet}，护身符槽。
 *
 * <p>原版行为：
 * <ul>
 *   <li>佩戴期间吸收<b>所有</b>对佩戴者造成的伤害，代价是按 {@code 伤害 × 8 × oblivionAmuletVisMult}
 *       扣 Vis；吸收下来的伤害累计存在物品上（原版存 NBT 字段 {@code IDamageStored}）；</li>
 *   <li>每 tick 有 {@code oblivionAmuletDamageReleaseChance}（默认 0.0008）的概率把储存的伤害
 *       随机释放一部分回佩戴者身上——这是它的代价，故伤害来源用模组自有的「湮灭」类型，
 *       以免被自身再次吸收形成死循环；</li>
 *   <li>另有 {@code oblivionAmuletPotionChance}（默认 0.0004）的概率施加随机负面效果；</li>
 *   <li>附带 {@code oblivionAmuletWarp}（默认 4）点扭曲。</li>
 * </ul>
 *
 * <p>储存值用数据组件 {@link FRDataComponents#STORED_DAMAGE}，对应原版的 NBT 写法。
 */
public class ItemOblivionAmulet extends FRRechargableCurioItem implements IWarpingGear {

    /** 原版随机负面效果的候选池：挖掘疲劳、失明、虚弱、凋零。 */
    private static final List<Holder<MobEffect>> RANDOM_DEBUFFS = List.of(
            MobEffects.DIG_SLOWDOWN,
            MobEffects.BLINDNESS,
            MobEffects.WEAKNESS,
            MobEffects.WITHER);

    public ItemOblivionAmulet(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.OBLIVION_AMULET_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.OBLIVION_AMULET_WARP.get();
    }

    /** 读取累计储存的伤害，没有该组件时按 0 处理（对应原版 {@code ItemNBTHelper.getFloat(..., 0)}）。 */
    public static float getStoredDamage(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.STORED_DAMAGE.get(), 0.0F);
    }

    /** 写回累计储存的伤害。 */
    public static void setStoredDamage(ItemStack stack, float value) {
        stack.set(FRDataComponents.STORED_DAMAGE.get(), Math.max(0.0F, value));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof LivingEntity wearer) || wearer.level().isClientSide()) {
            return;
        }
        float stored = getStoredDamage(stack);

        // 先判定「释放伤害」；原版写的是 if / else if，两者同一 tick 只会触发一个。
        if (stored > 0.0F && wearer.getRandom().nextDouble() <= FRConfig.OBLIVION_AMULET_DAMAGE_RELEASE_CHANCE.get()) {
            float released = (float) (stored * wearer.getRandom().nextDouble());
            if (released > FRConfig.OBLIVION_AMULET_DAMAGE_CAP.get()
                    && wearer.getRandom().nextDouble() <= FRConfig.OBLIVION_AMULET_HIGH_DAMAGE_REDUCTION_CHANCE.get()) {
                released = (float) (FRConfig.OBLIVION_AMULET_DAMAGE_CAP.get() * wearer.getRandom().nextDouble());
            }
            setStoredDamage(stack, stored - released);
            wearer.hurt(FRDamageTypes.source(wearer.level(), FRDamageTypes.OBLIVION), released);
            return;
        }

        if (wearer.getRandom().nextDouble() <= FRConfig.OBLIVION_AMULET_POTION_CHANCE.get()) {
            Holder<MobEffect> effect = RANDOM_DEBUFFS.get(wearer.getRandom().nextInt(RANDOM_DEBUFFS.size()));
            int min = FRConfig.OBLIVION_AMULET_POTION_DURATION_MIN.get();
            int max = FRConfig.OBLIVION_AMULET_POTION_DURATION_MAX.get();
            int duration = min + wearer.getRandom().nextInt(Math.max(1, max - min));
            int levelMin = FRConfig.OBLIVION_AMULET_POTION_LEVEL_MIN.get();
            int levelMax = FRConfig.OBLIVION_AMULET_POTION_LEVEL_MAX.get();
            int amplifier = levelMin + wearer.getRandom().nextInt(Math.max(1, levelMax - levelMin));
            // 原版构造 PotionEffect 时用的是 (true, false)：环境效果、不显示粒子。
            wearer.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false));
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemOblivionAmulet1.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet2.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemOblivionAmulet5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // 原版只要有 NBT 就额外显示一行「已储存伤害」，这里改为数值大于 0 才显示。
        float stored = getStoredDamage(stack);
        if (stored > 0.0F) {
            tooltip.add(Component.translatable("item.ItemOblivionAmuletDamage.lore")
                    .append(" " + Math.round(stored * 100.0D) / 100.0D));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        }
    }
}
