package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 「随身携带时影响伤害结算」的行为。
 *
 * <p>与 {@link IncomingDamageBehaviour} 的区别只在范围：后者只认穿戴物（饰品栏 + 护甲槽），
 * 本接口认整个物品栏。原版 {@code RelicsEventHandler} 中凡是写成
 * {@code player.inventory.hasItem(...)} 的段落都归这里。
 *
 * <p>范围必须区分：湮灭护符放进背包时不应开始吸收伤害。用错范围会直接改变游戏行为。
 *
 * <h2>三个钩子的划分</h2>
 *
 * <p>原版把这三段分别写在两个处理器里；1.21.1 没有 {@code LivingAttackEvent}（只剩
 * {@code LivingIncomingDamageEvent}），因此合并到同一事件，但派发器仍按原版实际执行次序调用：
 *
 * <ol>
 *   <li>{@link #onCarriedAttack}：携带者是攻击者（原版在 {@code LivingAttackEvent} 中，风险最高）；</li>
 *   <li>{@link #onCarriedDefend}：携带者是受害者且伤害尚未结算（同上，紧随其后）；</li>
 *   <li>{@link #onCarriedHurt}：携带者是受害者且伤害已过减伤结算（原版在
 *       {@code LivingHurtEvent} 中，即较晚的一段）。</li>
 * </ol>
 *
 * <p>混沌之核同时使用这三个钩子，这正是该划分存在的理由。
 */
public interface CarriedDamageBehaviour {

    /** 默认优先级，数值越小越先执行。 */
    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /** 携带者作为攻击者攻击他人时触发（对应原版 {@code LivingAttackEvent} 的攻击者分支）。 */
    default void onCarriedAttack(LivingIncomingDamageEvent event, Player attacker, ItemStack stack) {
    }

    /** 携带者作为受害者被攻击、伤害尚未结算时触发（对应原版 {@code LivingAttackEvent} 的受害者分支）。 */
    default void onCarriedDefend(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
    }

    /** 携带者被攻击、且已过减伤结算时触发（对应原版 {@code LivingHurtEvent}）。 */
    default void onCarriedHurt(LivingIncomingDamageEvent event, Player victim, ItemStack stack) {
    }
}
