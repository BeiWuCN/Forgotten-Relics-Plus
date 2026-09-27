package com.beiwu.forgottenrelics_re.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 「替附近玩家承伤」的行为（远古之庇护的伤害转嫁）。
 *
 * <p>它与 {@link IncomingDamageBehaviour} 的区别在<b>触发者是谁</b>：后者的触发者是受伤的人
 * 本人（受害者身上戴了什么就触发什么），而这里的触发者是附近<b>别人</b>身上的物品。
 * 因此派发方向相反——从受害者出发，去找附近戴着该物品的玩家。
 *
 * <p>原版 {@code findPlayerWithBauble} 在候选者里随机挑一个：同一件物品附近有多位佩戴者时，
 * 只有随机挑中的那一位承伤。派发器照此实现。
 */
public interface AllyProtectionBehaviour {

    /** 生效半径（格）。超出这个距离的佩戴者不会被派发。 */
    double radius();

    /**
     * @param event    可修改数值、可取消
     * @param victim   受伤的玩家（<b>没有</b>佩戴本物品的那位）
     * @param guardian 附近佩戴本物品的玩家
     * @param stack    佩戴者身上那一份物品
     * @return 返回 {@code true} 表示已经接管这次伤害；不管返回什么，
     *         同一次伤害里同一件物品都只会被派发一次
     */
    boolean onAllyDamage(LivingIncomingDamageEvent event, Player victim, Player guardian, ItemStack stack);
}
