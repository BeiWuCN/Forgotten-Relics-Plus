package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.CarriedDamageBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 混沌之核（Chaos Core），1.7.10 原版 {@code ItemChaosCore}。
 *
 * <p>它的效果分散在两个类里：物品类只有一个（随身携带时按概率施加随机状态效果），
 * 其余三条全部写在 {@code RelicsEventHandler} 中，用 {@code player.inventory.hasItem(...)} 判断
 * ——也就是说它是<b>随身携带</b>生效，不需要佩戴。四条完整效果：
 *
 * <ol>
 *   <li><b>攻击者携带</b>（原版 {@code RelicsEventHandler:177}，45%）：把 {@code 伤害 × rand×2}
 *       转嫁给目标 16 格内的随机实体，其中 15% 的概率改为<b>反弹给攻击者自己</b>，并取消原伤害；</li>
 *   <li><b>受害者携带</b>（{@code :190}，42%）：把 {@code 伤害 × rand×2} 转嫁给 16 格内随机实体，
 *       取消原伤害（没有反弹分支）；</li>
 *   <li><b>受害者携带</b>（{@code :246}）：受到的伤害乘以 {@code rand×2}；</li>
 *   <li><b>随身携带</b>（本类 {@code inventoryTick}，即原版 {@code func_77663_a}）：每 tick 有
 *       {@code chaosCoreChance}（原版写死 2.08E-4）的概率触发；随机取 1~21 号「药水 ID」，
 *       其中 6 / 7（瞬间治疗 / 瞬间伤害）改判为 20（凋零）；时长 {@code 100 + rand(2400)} tick，
 *       等级 {@code rand(3)}。</li>
 * </ol>
 *
 * <p>1.21.1 没有 {@code LivingAttackEvent}，前三条的落地方式见 {@link CarriedDamageBehaviour}——
 * 那个接口的三个钩子正是为了让这三段的先后次序与原版完全一致。
 *
 * <p>1.21.1 也没有数字药水 ID，所以按同一顺序列出对应的现代效果，见 {@link #EFFECT_POOL}：
 * 索引 0 对应原版 ID 1，索引 5 / 6 都指向凋零（对应原版 6 / 7 的改判）。
 * 附带 {@code chaosCoreWarp}（原版 2）点扭曲。
 */
public class ItemChaosCore extends FRItem implements IWarpingGear, CarriedDamageBehaviour {

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

    /**
     * 排在穿戴物之前。
     *
     * <p>原版把「转嫁」写在 {@code LivingAttackEvent}、「随机系数」写在 {@code LivingHurtEvent} 中较靠前的
     * 位置，都早于七阳之戒的超限免除等穿戴物效果，所以这里的优先级取得比它们都小。
     */
    @Override
    public int priority() {
        return 10;
    }

    /**
     * 携带者是<b>攻击者</b>时（原版 {@code :177}，45%）。
     *
     * <p>把 {@code 伤害 × rand×2} 转嫁给目标 16 格内的随机实体；其中还有 15% 的概率改为<b>反弹给
     * 攻击者自己</b>——原版就写在 else 分支之前，是刻意的「玩火」设计。
     */
    @Override
    public void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        if (attacker.getRandom().nextDouble() >= FRConfig.CHAOS_CORE_REDIRECT_ATTACK.get()) {
            return;
        }
        LivingEntity random = randomNearby(event.getEntity(), attacker);
        if (random == null) {
            return;
        }
        float redirected = rollRedirected(event.getAmount(), attacker);
        if (attacker.getRandom().nextDouble() < FRConfig.CHAOS_CORE_SELF_REFLECT.get()) {
            attacker.hurt(event.getSource(), redirected);
        } else {
            random.hurt(event.getSource(), redirected);
        }
        event.setCanceled(true);
    }

    /** 携带者是<b>受害者</b>、伤害尚未结算时（原版 {@code :190}，42%，没有反弹分支）。 */
    @Override
    public void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
        if (FRDamageTypes.isAbsolute(event.getSource())) {
            return;
        }
        if (victim.getRandom().nextDouble() >= FRConfig.CHAOS_CORE_REDIRECT_DEFEND.get()) {
            return;
        }
        LivingEntity random = randomNearby(victim, victim);
        if (random == null) {
            return;
        }
        random.hurt(event.getSource(), rollRedirected(event.getAmount(), victim));
        event.setCanceled(true);
    }

    /**
     * 携带者是受害者、已过减伤结算时（原版 {@code :246}）：伤害乘以 0~2 的随机系数。
     *
     * <p>刻意<b>不做</b>绝对伤害判断——原版这一句确实没有那个判断，照抄。
     */
    @Override
    public void onCarriedHurt(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
        event.setAmount(event.getAmount() * (float) (victim.getRandom().nextDouble()
                * FRConfig.CHAOS_CORE_DAMAGE_MULT_MAX.get()));
    }

    /** 原版两处都是 {@code amount * (Math.random() * 2.0)}。 */
    private static float rollRedirected(float amount, LivingEntity roller) {
        return (float) (amount * roller.getRandom().nextDouble() * FRConfig.CHAOS_CORE_DAMAGE_MULT_MAX.get());
    }

    /**
     * 在中心实体 16 格内随机挑一个活体。
     *
     * <p>两处与原版的差异，都写在明面上：原版用的是 {@code getEntitiesWithinAABBExcludingEntity}
     * （含掉落物、弹射物等一切实体），但把伤害「转嫁」给掉落物没有意义，所以这里限定活体；
     * 另外原版取下标用的是 {@code (int)(Math.random() * (size - 1))}，最后一个元素永远取不到，
     * 属笔误，这里按正常随机处理。
     */
    private static LivingEntity randomNearby(LivingEntity center, LivingEntity roller) {
        List<LivingEntity> candidates = center.level().getEntitiesOfClass(LivingEntity.class,
                center.getBoundingBox().inflate(16.0D), entity -> entity != center && entity.isAlive());
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(roller.getRandom().nextInt(candidates.size()));
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
