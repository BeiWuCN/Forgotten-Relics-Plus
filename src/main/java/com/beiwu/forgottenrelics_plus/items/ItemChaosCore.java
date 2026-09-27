package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 混沌之核（Chaos Core），1.7.10 原版 {@code ItemChaosCore}。
 *
 * <p><b>一件需要说明的重要差异。</b>我们的语言文件（从 1.12.2 移植版继承而来）写的是：
 * 「持有此遗物时，所有受到的伤害将被乘以 0.0~2.0 之间的随机系数」「提供 50% 概率将你造成或
 * 受到的伤害转移至 16 格范围内随机实体」「可能对你施加随机状态效果」。
 *
 * <p>但 1.7.10 原版的全部源码里，{@code chaosCore} 只出现在研究、配方与要素分配三处，
 * <b>没有任何伤害逻辑</b>；物品类里只实现了第三条（{@code Item#onUpdate} 里按概率施加随机状态
 * 效果）。前两条是 1.12.2 移植版自行添加的内容。
 *
 * <p>本项目唯一的参照是 1.7.10 原版，因此这里<b>只实现原版行为</b>，并已把中英文案改写为与之一致
 * （见 {@code item.ItemChaosCore1..7.lore}）。这是刻意的取舍，不是漏做。
 *
 * <p>原版逻辑（{@code func_77663_a}）：
 * <ul>
 *   <li>每 tick 有 {@code chaosCoreChance}（原版写死 2.08E-4）的概率触发；</li>
 *   <li>随机取 1~21 号「药水 ID」，其中 6 / 7（瞬间治疗 / 瞬间伤害）改判为 20（凋零）——
 *       原版刻意把两个瞬间效果换成持续效果；</li>
 *   <li>时长 {@code 100 + rand(2400)} tick，等级 {@code rand(3)}（0~2 级）。</li>
 * </ul>
 *
 * <p>1.21.1 没有数字 ID，所以按同一顺序列出对应的现代效果，见 {@link #EFFECT_POOL}。
 * 索引 0 对应原版 ID 1，索引 5 / 6 都指向凋零（对应原版 6 / 7 的改判）。
 * 附带 {@code chaosCoreWarp}（原版 2）点扭曲。
 */
public class ItemChaosCore extends FRItem implements IWarpingGear {

    /** 原版 1~21 号药水 ID 按顺序对应的现代状态效果。 */
    private static final List<Holder<MobEffect>> EFFECT_POOL = List.of(
            MobEffects.MOVEMENT_SPEED,      // 1  速度
            MobEffects.MOVEMENT_SLOWDOWN,   // 2  缓慢
            MobEffects.DIG_SPEED,           // 3  急迫
            MobEffects.DIG_SLOWDOWN,        // 4  挖掘疲劳
            MobEffects.DAMAGE_BOOST,        // 5  力量
            MobEffects.WITHER,              // 6  原版为瞬间治疗，改判为凋零
            MobEffects.WITHER,              // 7  原版为瞬间伤害，改判为凋零
            MobEffects.JUMP,                // 8  跳跃提升
            MobEffects.CONFUSION,           // 9  反胃
            MobEffects.REGENERATION,        // 10 生命恢复
            MobEffects.DAMAGE_RESISTANCE,   // 11 抗性提升
            MobEffects.FIRE_RESISTANCE,     // 12 抗火
            MobEffects.WATER_BREATHING,     // 13 水下呼吸
            MobEffects.INVISIBILITY,        // 14 隐身
            MobEffects.BLINDNESS,           // 15 失明
            MobEffects.NIGHT_VISION,        // 16 夜视
            MobEffects.HUNGER,              // 17 饥饿
            MobEffects.WEAKNESS,            // 18 虚弱
            MobEffects.POISON,              // 19 中毒
            MobEffects.WITHER,              // 20 凋零
            MobEffects.HEALTH_BOOST);       // 21 生命提升

    public ItemChaosCore(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        // 只在服务端判定：状态效果本来就是服务端同步的，客户端再判一次只会浪费随机数。
        if (level.isClientSide() || !(entity instanceof LivingEntity wearer)) {
            return;
        }
        if (wearer.getRandom().nextDouble() > FRConfig.CHAOS_CORE_CHANCE.get()) {
            return;
        }
        Holder<MobEffect> effect = EFFECT_POOL.get(wearer.getRandom().nextInt(EFFECT_POOL.size()));
        int duration = FRConfig.CHAOS_CORE_DURATION_MIN.get()
                + wearer.getRandom().nextInt(FRConfig.CHAOS_CORE_DURATION_SPAN.get());
        int amplifier = wearer.getRandom().nextInt(FRConfig.CHAOS_CORE_MAX_AMPLIFIER.get() + 1);
        // 原版 new PotionEffect(id, duration, amplifier, false)：第四个参数是 ambient=false，可见粒子保留。
        wearer.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true));
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.CHAOS_CORE_WARP.get();
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemChaosCore1.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore2.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore4.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore5.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemChaosCore7.lore"));
    }
}
