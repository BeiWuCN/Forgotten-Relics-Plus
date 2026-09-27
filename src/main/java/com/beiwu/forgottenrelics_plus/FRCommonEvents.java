package com.beiwu.forgottenrelics_plus;

import com.beiwu.forgottenrelics_plus.api.AllyProtectionBehaviour;
import com.beiwu.forgottenrelics_plus.api.BreakSpeedBehaviour;
import com.beiwu.forgottenrelics_plus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.FRWornItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 唯一的「可穿戴物品行为」派发器。
 *
 * <p>对应 1.12.2 原版的 {@code RelicsEventHandler}，但结构完全不同：那边是一个两百多行的
 * 上帝类，写死了每件物品的判断，加物品就得回来改；这里<b>不认识任何具体物品</b>，
 * 只遍历玩家身上被穿着的东西，谁实现了行为接口就把事件转给谁。
 *
 * <p>于是新增物品只需写物品类，事件类一行都不用动；每件物品的触发顺序与副作用都写在它自己
 * 类里，不会再出现「两段相关逻辑分别写在 LivingAttackEvent 与 LivingHurtEvent 里导致顺序错乱」
 * 这类历史遗留问题（原版七阳之戒正是如此）。
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID)
public final class FRCommonEvents {

    /**
     * 佩戴时每 tick 派发。
     *
     * <p>用 {@code PlayerTickEvent.Post} 而不是 Curios 的 {@code curioTick}：恐惧之冠既能放进饰品栏
     * 也能当头盔戴，挂在 Curios 的钩子上会漏掉护甲槽。统一按「是否穿着」派发，物品只实现一次。
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        // 冷却也要往前走。这个方法此前没有任何调用者，于是 setCooldown 写进去的数字只减不动就变成了
        // 永久冷却：食尸鬼之颅与符文天象石用过一次之后再也不能用，直到退出重进（表只存在内存里）——
        // 也就是「只能使用一次」。冷却归这里管，和物品是否佩戴无关。
        CooldownHelper.tick(player);
        FRWornItems.forEach(player, stack -> {
            if (stack.getItem() instanceof WearerTickBehaviour behaviour) {
                behaviour.onWearerTick(player, stack);
            }
        });
    }

    /**
     * 受伤时派发，分两遍。
     *
     * <p>第一遍是受害者<b>自己</b>身上的物品，按 {@link IncomingDamageBehaviour#priority()} 升序执行；
     * 一旦有物品取消了事件，后面的就不再收到通知。顺序是有讲究的：原版七阳之戒的伤害免除、
     * 湮灭护符的吸收、神圣护符的无敌帧延长、庇护的减伤、浑浊之核的闪避，就按这个先后关系。
     *
     * <p>第二遍是<b>附近别人</b>身上的「替人承伤」物品（远古之庇护的伤害转嫁），
     * 只在第一遍没有取消伤害时才进行，与原版「减免 40% 伤害」的写法一致。
     *
     * <p>同一件物品被戴了多份时只触发一次，与原版「同时佩戴第二枚七阳之戒不会产生额外效果」
     * 的意图一致。
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        dispatchWearerBehaviours(event, player);
        if (!event.isCanceled()) {
            dispatchAllyProtection(event, player);
        }
    }

    /** 第一遍：受害者自己身上的物品。 */
    private static void dispatchWearerBehaviours(LivingIncomingDamageEvent event, Player player) {
        List<Active> active = new ArrayList<>();
        FRWornItems.forEach(player, stack -> {
            if (!(stack.getItem() instanceof IncomingDamageBehaviour behaviour)) {
                return;
            }
            for (Active existing : active) {
                if (existing.behaviour() == behaviour) {
                    return;
                }
            }
            active.add(new Active(behaviour, stack));
        });
        if (active.isEmpty()) {
            return;
        }
        active.sort(Comparator.comparingInt(entry -> entry.behaviour().priority()));

        for (Active entry : active) {
            if (event.isCanceled()) {
                return;
            }
            entry.behaviour().onIncomingDamage(event, player, entry.stack());
        }
    }

    /**
     * 第二遍：附近玩家身上的「替人承伤」物品。
     *
     * <p>原版 {@code findPlayerWithBauble} 是在附近候选者里随机挑一位，这里照做：把候选者随机
     * 洗出来后，同一件物品只派发一次，因此「一群人围着戴庇护」时只有一个会承伤。
     * 遍历的是当前维度里的玩家列表，几十人的规模下一次受伤的开销可以忽略。
     */
    private static void dispatchAllyProtection(LivingIncomingDamageEvent event, Player victim) {
        List<Ally> candidates = new ArrayList<>();
        for (Player guardian : victim.level().players()) {
            if (guardian == victim || guardian.isSpectator()) {
                continue;
            }
            FRWornItems.forEach(guardian, stack -> {
                if (stack.getItem() instanceof AllyProtectionBehaviour behaviour
                        && guardian.distanceToSqr(victim) <= behaviour.radius() * behaviour.radius()) {
                    candidates.add(new Ally(behaviour, guardian, stack));
                }
            });
        }
        if (candidates.isEmpty()) {
            return;
        }

        Set<AllyProtectionBehaviour> attempted = new HashSet<>();
        while (!candidates.isEmpty() && !event.isCanceled()) {
            Ally ally = candidates.remove(victim.getRandom().nextInt(candidates.size()));
            if (attempted.add(ally.behaviour())) {
                ally.behaviour().onAllyDamage(event, victim, ally.guardian(), ally.stack());
            }
        }
    }

    /** 挖掘速度派发。对应原版 {@code RelicsEventHandler.miningStuff}。 */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        // 先把同一玩家身上所有物品的贡献相加，再一次性乘上去——与原版的「相加成总倍率」一致。
        float[] boost = {0.0F};
        FRWornItems.forEach(player, stack -> {
            if (stack.getItem() instanceof BreakSpeedBehaviour behaviour) {
                boost[0] += behaviour.breakSpeedBoost(player, stack);
            }
        });
        if (boost[0] != 0.0F) {
            event.setNewSpeed(event.getNewSpeed() * (1.0F + boost[0]));
        }
    }

    /** 一次派发里「哪个行为 + 对应哪一份物品栈」。 */
    private record Active(IncomingDamageBehaviour behaviour, ItemStack stack) {
    }

    /** 第二遍派发里的「哪个行为 + 哪位佩戴者 + 哪一份物品栈」。 */
    private record Ally(AllyProtectionBehaviour behaviour, Player guardian, ItemStack stack) {
    }

    private FRCommonEvents() {
    }
}
