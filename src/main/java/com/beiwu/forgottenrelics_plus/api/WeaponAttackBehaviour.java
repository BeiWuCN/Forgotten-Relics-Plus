package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * 「用手里的东西攻击实体时」的行为。
 *
 * <p>对应原版的 {@code Item#onLeftClickEntity}：该钩子允许物品<b>接管</b>这次攻击
 * （原版返回 {@code true} 即取消原版伤害）。1.21.1 已无该钩子，改用 NeoForge 的
 * {@link AttackEntityEvent}：它同样在伤害结算之前触发且可取消，语义一致。
 *
 * <p>派发范围只有手持的那一份（主手 + 副手），见 {@code FRCarriedItems#forEachHeld}。
 */
public interface WeaponAttackBehaviour {

    /**
     * @param event    可取消；取消即表示本次攻击由物品自己结算，原版伤害不再发生
     * @param attacker 攻击者
     * @param stack    手持的那一份物品
     */
    void onAttackEntity(AttackEntityEvent event, Player attacker, ItemStack stack);
}
