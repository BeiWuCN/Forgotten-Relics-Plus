package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.FRCarriedItems;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 欧米伽之核（Omega Core），1.7.10 原版 {@code ItemOmegaCore}。
 *
 * <p>原版行为分两处：
 * <ol>
 *   <li>{@code ItemOmegaCore#func_77663_a}：随身携带时，为背包里每根法杖的<b>每个原初要素</b>
 *       各补 1 点 Vis（同一要素随机挑一根还有余量的法杖）；</li>
 *   <li>{@code RelicsEventHandler#onPlayerDeath}：<b>致死时免死</b>，把血量设为 1。</li>
 * </ol>
 *
 * <p>第二处的原版是 if / else if：欧米伽之核在前，破碎的命运巨著在后——也就是说带着欧米伽之核
 * 时永远不会去消耗命运巨著。这里用 {@link #priority()} 表达同一优先级关系。
 *
 * <p><b>关于获取方式</b>：1.7.10 里它<b>没有研究词条、也没有灌注配方</b>，只能创造模式获取
 * （原版源码里 {@code RelicsResearchRegistry} 完全没有提到它）。按「只以原版为准」的原则，
 * 本项目同样不配配方与研究，和休眠浑浊之核一样属于「由其它途径得到的物品」
 * （区别是它连转化的来源都没有）。这是刻意保留原版现状，不是漏做。
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onUpdate} → 原生的 {@code Item#inventoryTick}，无需经过行为派发器；</li>
 *   <li>「玩家背包里的 Vis」在 1.21.1 不存在，法杖的 Vis 由 Thaumaturge 的
 *       {@link WandVisHelper} 管理（Thaumaturge 自己的充能基座就是这么补的）；</li>
 *   <li>致死拦截 → {@link DeathPreventionBehaviour}，由 {@code FRCommonEvents} 从
 *       <b>随身携带物</b>派发。</li>
 * </ul>
 */
public class ItemOmegaCore extends FRItem implements DeathPreventionBehaviour {

    /** 排在破碎的命运巨著之前：原版带着它就绝不会去消耗命运巨著。 */
    private static final int PRIORITY = 100;

    public ItemOmegaCore(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        int amount = FRConfig.OMEGA_CORE_VIS_PER_TICK.get();
        int recharge = FRConfig.OMEGA_CORE_RECHARGE_PER_TICK.get();
        if (amount <= 0 && recharge <= 0) {
            return;
        }
        // 原版对六个原初要素各挑一根「有效的」法杖补 1 点。
        if (amount > 0) {
            for (ResourceKey<IAspect> aspect : TCAspects.PRIMALS) {
                ItemStack wand = findWandWithRoom(player, aspect);
                if (wand != null) {
                    WandVisHelper.addVis(wand, aspect, amount, true);
                }
            }
        }
        // ---- 本移植新增：同时给其他遗物充能 ----
        //
        // 1.7.10 原版的所有遗物消耗的都是「玩家背包里法杖的 Vis」，欧米伽之核补的也是它；
        // 但 1.21.1 没有玩家 Vis 池，本模组把这类消耗统一改成了物品自身充能
        // （FRRechargable + RechargeAccess，见 HANDOVER.md 第 3 节）。如果欧米伽之核还只补法杖，
        // 它就等于对其他遗物完全失效——这正是玩家报的问题。
        //
        // RE 版的 ItemOmegaCore#fillAllVis 已经给出了正确的对应做法：遍历主背包 / 护甲 / 副手 /
        // 饰品栏，对所有 IRechargable 物品调 rechargeItemBlindly。这里沿用同一范围。
        if (recharge > 0) {
            FRCarriedItems.forEach(player, carried -> {
                if (carried.getItem() instanceof FRRechargable && !(carried.getItem() == this)) {
                    RechargeAccess.rechargeItemBlindly(carried, player, recharge);
                }
            });
        }
    }

    /**
     * 在玩家背包里随机挑一根「该要素还没满」的法杖，对应原版
     * {@code SuperpositionHandler.getRandomValidWand}。
     */
    private static ItemStack findWandWithRoom(Player player, ResourceKey<IAspect> aspect) {
        Inventory inventory = player.getInventory();
        List<ItemStack> candidates = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !(candidate.getItem() instanceof ItemWand)) {
                continue;
            }
            if (WandVisHelper.getVis(candidate, aspect) < WandVisHelper.getMaxVis(candidate)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack) {
        if (!FRConfig.OMEGA_CORE_PREVENT_DEATH.get()) {
            return;
        }
        event.setCanceled(true);
        // 原版：player.setHealth(1.0f)。没有代价也没有冷却，这是原样保留的设计。
        player.setHealth(1.0F);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.OmegaCore1.lore"));
        tooltip.add(Component.translatable("item.OmegaCore2.lore"));
    }
}
