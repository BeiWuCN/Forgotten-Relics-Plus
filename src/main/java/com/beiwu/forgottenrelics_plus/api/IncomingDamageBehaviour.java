package com.beiwu.forgottenrelics_plus.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 「佩戴者受到伤害时」的行为。
 *
 * <p>取代原版把所有减伤/吸收/反弹逻辑堆在 {@code RelicsEventHandler} 里的写法。
 * 物品自己实现，由 {@code FRWornItems} 派发。
 *
 * <p>多件物品同时生效时按 {@link #priority()} 从小到大依次执行；一旦有物品取消了事件，
 * 后面的物品就不再收到通知。这样「谁先谁后」是每件物品各自显式声明的，
 * 不会再出现原版那种「分别写在 LivingAttackEvent 和 LivingHurtEvent 里导致顺序反了」的隐患。
 */
public interface IncomingDamageBehaviour {

    /** 默认优先级，数值越小越先执行。 */
    int DEFAULT_PRIORITY = 1000;

    default int priority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * @param event  可修改数值、可取消
     * @param wearer 受伤的玩家
     * @param stack  正在生效的那一份物品
     */
    void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack);
}
