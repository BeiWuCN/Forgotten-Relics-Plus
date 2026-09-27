package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.CarriedDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 虚伪审判（False Justice），1.7.10 原版 {@code ItemFalseJustice}。
 *
 * <p>物品类本身只有 tooltip 与扭曲，全部效果都写在 {@code RelicsEventHandler} 里，
 * 而且用 {@code player.inventory.hasItem(...)} 判断——也就是说它是<b>随身携带</b>生效。
 * 效果分两块，共四处：
 *
 * <ol>
 *   <li><b>伤害转化为真实伤害</b>（{@code :227} / {@code :238}）：携带者<b>受到</b>的、
 *       以及携带者<b>造成</b>的伤害，只要不是绝对伤害，就取消原结算，改以「真伤」重新结算
 *       <b>两倍</b>的数值。真伤属于绝对伤害，所以不会被这条再拦一次（原版靠的就是这个递归保护）；</li>
 *   <li><b>阻止死亡</b>（{@code :298} / {@code :303}）：携带者不会死，<b>被携带者打死的目标也不会死</b>
 *       ——「虚伪审判」这个名字指的就是这个：无人受审，谁都不死。</li>
 * </ol>
 *
 * <p>与原版的一处必要差异：原版取消 {@code LivingDeathEvent} 时并不设置血量，而 1.21.1 里
 * {@code LivingEntity#isAlive()} 要求血量大于 0，0 血会被继续判定为死亡状态。所以这里统一补到
 * 1 点生命，否则「不死」会变成「每秒死一次」。这一点在 {@link #keepAlive} 上也有注释。
 *
 * <p>附带 {@code falseJusticeWarp}（原版 4）点扭曲。
 */
public class ItemFalseJustice extends FRItem
        implements IWarpingGear, CarriedDamageBehaviour, DeathPreventionBehaviour {

    public ItemFalseJustice(Properties properties) {
        super(properties);
    }

    /**
     * 排在最前的一批（原版 {@code :227} 是 {@code onEntityHurt} 的第一段，早于混沌之核与七阳之戒）。
     */
    @Override
    public int priority() {
        return 30;
    }

    /** 携带者<b>受到</b>伤害（原版 {@code :227}）。 */
    @Override
    public void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
        convertToTrueDamage(event, victim);
    }

    /** 携带者<b>造成</b>伤害（原版 {@code :238}）。 */
    @Override
    public void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
        convertToTrueDamage(event, event.getEntity());
    }

    /**
     * 取消原结算，改以真伤重算。
     *
     * <p>原版对「有来源」与「无来源」分别用了两个伤害类型，这里同样区分：有来源时把来源实体带上，
     * 死亡提示才写得出「被某某审判致死」。
     */
    private static void convertToTrueDamage(LivingIncomingDamageEvent event, LivingEntity target) {
        DamageSource source = event.getSource();
        if (FRDamageTypes.isAbsolute(source)) {
            return;
        }
        float amount = event.getAmount() * FRConfig.FALSE_JUSTICE_DAMAGE_MULTIPLIER.get().floatValue();
        event.setCanceled(true);
        DamageSource converted = source.getEntity() == null
                ? FRDamageTypes.source(target.level(), FRDamageTypes.TRUE_DAMAGE_UNDEF)
                : FRDamageTypes.source(target.level(), FRDamageTypes.TRUE_DAMAGE, source.getEntity());
        target.hurt(converted, amount);
    }

    /** 携带者自己将要死亡（原版 {@code :296-299}）。真伤不在免死范围内。 */
    @Override
    public void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack) {
        if (!FRConfig.FALSE_JUSTICE_PREVENT_DEATH.get() || isTrueDamage(event.getSource())) {
            return;
        }
        event.setCanceled(true);
        keepAlive(player);
    }

    /** 携带者打死了一个生物（原版 {@code :301-305}）：那个生物也不死。同样排除真伤。 */
    @Override
    public void onLethalDamageCaused(LivingDeathEvent event, Player attacker, ItemStack stack) {
        if (!FRConfig.FALSE_JUSTICE_PREVENT_DEATH.get() || !(event.getEntity() instanceof LivingEntity victim)
                || isTrueDamage(event.getSource())) {
            return;
        }
        event.setCanceled(true);
        keepAlive(victim);
    }

    /**
     * 原版 {@code RelicsEventHandler.java:296,301} 在免死判定里明确排除了两种真伤来源
     * （{@code DamageSourceTrueDamage} 与 {@code DamageSourceTrueDamageUndef}）——
     * 即「虚伪审判挡不住真正的伤害」。
     */
    private static boolean isTrueDamage(DamageSource source) {
        return source.is(FRDamageTypes.TRUE_DAMAGE) || source.is(FRDamageTypes.TRUE_DAMAGE_UNDEF);
    }

    /** 原版只取消事件、不设血量；1.21.1 里 0 血仍被判定为死亡状态，所以这里补到 1 点。 */
    private static void keepAlive(LivingEntity entity) {
        entity.setHealth(1.0F);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.FALSE_JUSTICE_WARP.get();
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.FalseJustice1.lore"));
        tooltip.add(Component.translatable("item.FalseJustice2.lore"));
        tooltip.add(Component.translatable("item.FalseJustice3.lore"));
    }
}
