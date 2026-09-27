package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 「随身携带时影响伤害结算」的行为。
 *
 * <p>与 {@link IncomingDamageBehaviour} 的唯一区别是<b>范围</b>：那个只认穿戴物（饰品栏 + 护甲槽），
 * 这个认整个物品栏。原版 {@code RelicsEventHandler} 里凡是写成
 * {@code player.inventory.hasItem(...)} 的那几段，都归这里。
 *
 * <p>为什么必须分开：把湮灭护符塞进背包不该让它开始吸伤害。用错范围会直接改变游戏行为。
 *
 * <h2>三个钩子为什么是分开的</h2>
 *
 * <p>原版把这三段分别写在两个处理器里，1.21.1 没有 {@code LivingAttackEvent}（只剩
 * {@code LivingIncomingDamageEvent}），所以合并到同一个事件，但<b>先后次序仍按原版实际执行次序</b>
 * 由派发器保证：
 *
 * <ol>
 *   <li>{@link #onCarriedAttack}：携带者是<b>攻击者</b>（原版在 {@code LivingAttackEvent} 里，
 *       风险最高的一段）；</li>
 *   <li>{@link #onCarriedDefend}：携带者是<b>受害者</b>且伤害尚未结算（同上，紧随其后）；</li>
 *   <li>{@link #onCarriedHurt}：携带者是受害者且伤害已过减伤结算（原版在
 *       {@code LivingHurtEvent} 里，也就是较晚的一段）。</li>
 * </ol>
 *
 * <p>混沌之核同时用了这三个钩子，正好是这套划分存在的理由。
 */
public interface CarriedDamageBehaviour {

    /** 默认优先级，数值越小越先执行。 */
    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /** 携带者<b>攻击</b>别人时触发（对应原版 {@code LivingAttackEvent} 里的攻击者分支）。 */
    default void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
    }

    /** 携带者<b>被攻击</b>、伤害尚未结算时触发（对应原版 {@code LivingAttackEvent} 里的受害者分支）。 */
    default void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
    }

    /** 携带者被攻击、且已过减伤结算时触发（对应原版 {@code LivingHurtEvent}）。 */
    default void onCarriedHurt(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
    }
}
