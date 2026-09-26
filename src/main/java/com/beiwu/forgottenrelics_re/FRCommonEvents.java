package com.beiwu.forgottenrelics_re;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.registry.FRItems;
import com.beiwu.forgottenrelics_re.utils.CurioHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 通用事件处理器（双端）。
 *
 * <p>对应 1.12.2 原版的 {@code RelicsEventHandler}。原版用一个
 * {@code @Mod.EventBusSubscriber} 类把十几个 {@code @SubscribeEvent} 全塞在一起；
 * 这里按功能拆开，本类先承载与「饰品属性加成」相关的事件。
 *
 * <p>NeoForge 1.21.1 里 {@code @EventBusSubscriber} 需要显式指明挂哪个总线：
 * 游戏事件挂 {@code Bus.GAME}（默认），注册表事件挂 {@code Bus.MOD}。
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID)
public final class FRCommonEvents {

    /**
     * 挖掘速度加成。
     *
     * <p>直接照搬 1.12.2 {@code RelicsEventHandler.miningStuff} 的逻辑：
     * <pre>
     * float miningBoost = 1.0f;
     * if (戴着以太采矿护符) miningBoost += advancedMiningCharmBoost;
     * if (戴着采矿护符)     miningBoost += miningCharmBoost;
     * event.setNewSpeed(event.getNewSpeed() * miningBoost);
     * </pre>
     *
     * <p>为什么不能用属性修饰符（{@code Attributes.BLOCK_BREAK_SPEED}）来做：
     * 原版用的是「倍率乘算」，而属性修饰符走的是「基础值加算/乘算」的公式，
     * 两者在叠加、与效率附魔的相互作用上并不等价。既然目标是复刻行为，就保持原版的写法。
     *
     * <p>{@code getNewSpeed()} 返回方块的基础破坏速度乘以玩家当前的挖掘速度系数，
     * 在其上再乘一个倍率，与 1.12.2 语义一致。
     */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float miningBoost = 1.0F;
        if (CurioHelper.isEquipped(player, FRItems.ADVANCED_MINING_CHARM.get())) {
            miningBoost += FRConfig.ADVANCED_MINING_CHARM_BOOST.get().floatValue();
        }
        if (CurioHelper.isEquipped(player, FRItems.MINING_CHARM.get())) {
            miningBoost += FRConfig.MINING_CHARM_BOOST.get().floatValue();
        }
        if (miningBoost != 1.0F) {
            event.setNewSpeed(event.getNewSpeed() * miningBoost);
        }
    }

    private FRCommonEvents() {
    }
}
