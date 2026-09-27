package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.leclowndu93150.thaumaturge.api.items.IGoggles;
import com.leclowndu93150.thaumaturge.api.items.IRevealer;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 恐惧之冠（Crown of Terror），1.12.2 原版 {@code ItemTerrorCrown}。
 *
 * <p>原版是个「同时是头盔和饰品」的物品：继承 {@code ItemArmor}（HEAD 槽、金材质）又实现
 * {@code IBauble}（HEAD 槽）。1.21.1 这边同样两头兼顾——继承 {@link ArmorItem} 让它能戴在
 * 头盔位，再实现 {@link ICurioItem} 让它能放进 Curios 的 {@code head} 槽。
 *
 * <p>原版行为：
 * <ul>
 *   <li>每个 tick 挑拨 {@code terrorCrownHavocRange}（默认 24）格内的怪物互相攻击；</li>
 *   <li>对视线前方 {@code terrorCrownScanRange}（默认 32）格内、准星指着的生物施加失明、
 *       凋零、反胃、缓慢、虚弱；</li>
 *   <li>同时具备「揭示护目镜」与「灵气揭示器」的功能；</li>
 *   <li>附带 {@code terrorCrownWarp}（默认 3）点扭曲；</li>
 *   <li>可用 Botania 的魔力修复耐久，每点耐久 {@code terrorCrownManaCost}（默认 200）。</li>
 * </ul>
 *
 * <p>渲染在客户端：1.12.2 是给 {@code RenderPlayer} 挂一层 {@code LayerCrown}；
 * 1.21.1 改用 Curios 的 {@code ICurioRenderer} 注册，见 {@code client.CrownCurioRenderer}。
 */
public class ItemTerrorCrown extends ArmorItem
        implements ICurioItem, IWarpingGear, IGoggles, IRevealer, WearerTickBehaviour {

    public ItemTerrorCrown(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TERROR_CROWN_WARP.get();
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        // 原版 IGoggles 返回 true：戴上就能看到灵气节点等游戏内提示。
        return true;
    }

    /**
     * 原版 {@code ItemTerrorCrown#hasEffect} 恒为 {@code false}：恐惧之冠永远没有附魔光效。
     * 材质附魔性是 0（禁止在附魔台上附魔），这里再兜一层显示。
     */
    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    /**
     * 原版 {@code ItemTerrorCrown#func_77663_a}（{@code inventoryTick}）的开头：
     * 只要物品还带着附魔，就把 {@code ench} 标签剥掉——所以即使用铁砧硬加上附魔也留不住。
     *
     * <p>原版这段跑在 {@code inventoryTick} 上（背包里、身上都算），因此这里也放在 {@code inventoryTick}，
     * 而不是只在佩戴时处理的 {@link #onWearerTick}。
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (stack.isEnchanted()) {
            stack.remove(DataComponents.ENCHANTMENTS);
        }
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity holder) {
        // 原版 IRevealer 返回 true。
        return true;
    }

    /**
     * 佩戴时每 tick 触发。
     *
     * <p>原版分了两条路：饰品栏走 Curios 的 {@code curioTick}，头盔位走 {@code inventoryTick}
     * 再自己判断「是不是戴在头上」。现在统一由派发器按「是否穿着」调用，饰品栏与护甲槽共用这一份实现。
     */
    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        tickCrown(stack, wearer);
    }

    /**
     * 恐惧之冠的主循环：挑拨离间、凝视诅咒、用魔力修耐久。
     *
     * <p>只在服务端跑，避免客户端重复施加效果。
     */
    private void tickCrown(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof Player player)) {
            return;
        }
        cryHavoc(player);
        applyGazeDebuffs(player);
        repairWithMana(stack, player);
    }

    /**
     * 挑拨附近的怪物互相攻击。
     *
     * <p>原版直接调 Botania 平成 dream 花的 {@code brainwashEntity}；1.21.1 里那个方法搬到了
     * {@code HeiseiDreamBlockEntity}，这里沿用同一套逻辑，保持与原版一致的效果与音效。
     */
    private static void cryHavoc(Player player) {
        int range = FRConfig.TERROR_CROWN_HAVOC_RANGE.get();
        if (range <= 0) {
            return;
        }
        List<net.minecraft.world.entity.Mob> mobs = player.level().getEntitiesOfClass(
                net.minecraft.world.entity.Mob.class,
                player.getBoundingBox().inflate(range),
                mob -> true);
        if (mobs.size() <= 1) {
            return;
        }
        for (net.minecraft.world.entity.Mob mob : mobs) {
            vazkii.botania.common.block.block_entity.flower.functional.HeiseiDreamBlockEntity
                    .brainwashEntity(mob, mobs);
        }
    }

    /**
     * 对注视目标施加负面效果。
     *
     * <p>1.12.2 用的是 Thaumcraft 的 {@code EntityUtils.getPointedEntity}，1.21.1 没有对应工具，
     * 这里用原版的 {@link net.minecraft.world.entity.projectile.ProjectileUtil#getEntityHitResult}
     * 沿视线方向做一次实体射线检测，等价且更省事。
     */
    private static void applyGazeDebuffs(Player player) {
        double range = FRConfig.TERROR_CROWN_SCAN_RANGE.get();
        if (range <= 0.0D) {
            return;
        }
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));
        AABB search = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0D);
        EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player, eye, end, search,
                target -> target instanceof LivingEntity && target != player,
                range * range);
        if (!(hit != null && hit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
                FRConfig.TERROR_CROWN_BLINDNESS_DURATION.get(), 2, true, false));
        // 原版对凋零额外做了「已经有了就别叠加」的判断，其它效果则直接覆盖。
        if (!target.hasEffect(MobEffects.WITHER)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                    FRConfig.TERROR_CROWN_WITHER_DURATION.get(), FRConfig.TERROR_CROWN_WITHER_LEVEL.get(), false, false));
        }
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION,
                FRConfig.TERROR_CROWN_NAUSEA_DURATION.get(), FRConfig.TERROR_CROWN_NAUSEA_LEVEL.get(), true, false));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                FRConfig.TERROR_CROWN_SLOWNESS_DURATION.get(), FRConfig.TERROR_CROWN_SLOWNESS_LEVEL.get(), true, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                FRConfig.TERROR_CROWN_WEAKNESS_DURATION.get(), FRConfig.TERROR_CROWN_WEAKNESS_LEVEL.get(), true, false));
    }

    /**
     * 用 Botania 的魔力修复耐久。
     *
     * <p>原版 {@code ManaItemHandler.requestManaExact(stack, player, cost, true)}：从玩家身上
     * 别的魔力物品里抽魔力充进王冠，成功就减 1 点耐久。1.21.1 的 Botania 把
     * {@code ManaItemHandler} 改成了实例方法，语义不变，故调用方式稍有不同。
     */
    private static void repairWithMana(ItemStack stack, Player player) {
        if (!stack.isDamaged()) {
            return;
        }
        int cost = FRConfig.TERROR_CROWN_MANA_COST.get();
        if (vazkii.botania.api.mana.ManaItemHandler.instance().requestManaExact(stack, player, cost, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FRItem.appendShift(tooltip, () -> {
            tooltip.add(Component.translatable("item.ItemTerrorCrown1.lore", FRConfig.TERROR_CROWN_HAVOC_RANGE.get()));
            tooltip.add(Component.translatable("item.ItemTerrorCrown2.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemTerrorCrown3.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemTerrorCrown4.lore"));
        });
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // 原版 canEquip：头盔位已经戴着同一顶王冠时，不允许再往饰品位塞第二顶。
        LivingEntity wearer = slotContext.entity();
        return wearer == null
                || !wearer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(this);
    }
}
