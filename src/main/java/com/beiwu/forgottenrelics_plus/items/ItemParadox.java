package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.WeaponAttackBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * 悖论之刃（The Paradox），1.7.10 原版 {@code ItemParadox}。
 *
 * <p>它是一把剑，材质参数取自原版 {@code RelicsMaterialHandler.materialParadoxicalStuff}
 * （见 {@code FRToolTiers#PARADOX}）：攻击力加成是 <b>-4</b>，也就是说本体几乎打不出伤害，
 * 全部威力来自它的悖论效果。
 *
 * <p>原版 {@code onLeftClickEntity}：每次命中时取一个
 * {@code 0 ~ paradoxDamageCap} 的随机数给目标，再把 <b>剩余的那部分</b>打回自己身上。
 * 于是不论随机数是多少，双方承受的总和恒等于上限——这就是「悖论」：
 * 打得越狠，自己挨得越重。
 *
 * <p>1.21.1 已无 {@code onLeftClickEntity}，改用 NeoForge 的 {@link AttackEntityEvent}：
 * 它同样在伤害结算之前触发且可取消，取消即等价于原版返回 {@code true}（由物品自己结算这次攻击）。
 *
 * <p>另外原版实现 {@code IRepairable} 并每 20 tick 自修 1 点耐久（{@code Item#onUpdate}），
 * 这里落在 {@code inventoryTick} 上。附带 {@code paradoxWarp}（原版 8，是全模组最高的一档）。
 *
 * <p>因为必须继承 {@link SwordItem}，本类无法再继承 {@code FRItem}，所以 Shift 展开式 tooltip
 * 复用的是 {@link FRItem#appendShift} 这个静态入口（恐惧之冠同理）。
 */
public class ItemParadox extends SwordItem implements IWarpingGear, WeaponAttackBehaviour {

    /** 每 20 tick 自修 1 点耐久，与 1.7.10 一致。 */
    private static final int REPAIR_INTERVAL = 20;

    public ItemParadox(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !stack.isDamaged() || entity.tickCount % REPAIR_INTERVAL != 0) {
            return;
        }
        stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public void onAttackEntity(AttackEntityEvent event, Player attacker, ItemStack stack) {
        double cap = FRConfig.PARADOX_DAMAGE_CAP.get();
        if (cap <= 0.0D) {
            return;
        }
        double toTarget = attacker.getRandom().nextDouble() * cap;
        double toSelf = cap - toTarget;
        // 原版两处都用 DamageSource.causePlayerDamage(player)，也就是把自伤也算在攻击者头上。
        event.getTarget().hurt(attacker.damageSources().playerAttack(attacker), (float) toTarget);
        attacker.hurt(attacker.damageSources().playerAttack(attacker), (float) toSelf);
        event.setCanceled(true);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.PARADOX_WARP.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FRItem.appendShift(tooltip, () -> {
            tooltip.add(Component.translatable("item.ItemParadox1.lore"));
            tooltip.add(Component.translatable("item.ItemParadox2.lore"));
            tooltip.add(Component.translatable("item.ItemParadox3.lore"));
            tooltip.add(Component.translatable("item.ItemParadox4.lore"));
            tooltip.add(Component.translatable("item.ItemParadox5.lore"));
        });
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // 原版把上限拼进「1-<上限> 攻击伤害」这一行里。
        tooltip.add(Component.translatable("item.ItemParadoxDamage_1.lore")
                .append(String.valueOf(FRConfig.PARADOX_DAMAGE_CAP.get().intValue()))
                .append(Component.translatable("item.ItemParadoxDamage_2.lore")));
    }
}
