package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityThunderpealOrb;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 霹雳咒书（Thunderpeal），注册名 {@code thunderpeal}，1.7.10 原版 {@code ItemThunderpeal}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>右键发射一颗雷电球 {@code EntityThunderpealOrb}：从视线前方 1.25 格、抬高 0.5 处出现，
 *       速度是视线的 1.5 倍；</li>
 *   <li>消耗背包里法杖的 Vis：风（Aer）{@code 135}、火（Ignis）{@code 85}（单位是厘 Vis，
 *       即 1.35 + 0.85 = 2.2 点 Vis），两者都乘 {@code thunderpealVisMult}；</li>
 *   <li>随后进入 {@code SuperpositionHandler.setCasted(player, 30, true)} 的 30 tick 冷却，
 *       该冷却是原版所有施法类遗物共用的那一个；</li>
 *   <li>物品堆叠上限 1，稀有度 EPIC。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；</li>
 *   <li>「从背包法杖抽 Vis」在 1.21.1 没有对应 API。本移植对这一类消耗统一改为
 *       {@link FRRechargable} 的物品自身充能，用 {@link RechargeAccess#consumeCharge}
 *       扣除，充能由周围灵气补充。符文天象石、食尸鬼之颅等走的都是这条路，
 *       1.12.2 移植版（RE）的 {@code ItemThunderpeal} 也是这么改的，这里沿用同一约定；</li>
 *   <li>原版的 {@code SuperpositionHandler} 冷却 → {@link CooldownHelper}，同样是全体共用；</li>
 *   <li>发射音效：原版 {@code thaumcraft:zap}，这里沿用 RE 的替代方案
 *       {@link SoundEvents#FIREWORK_ROCKET_BLAST}（本移植对 Thaumcraft 音效一律换原版等价物）。</li>
 * </ul>
 *
 * <p><b>一处刻意的数值取舍</b>：原版单次合计 2.2 点 Vis，而充能是整数，
 * 所以 {@code thunderpealVisCost} 默认取整为 2。逐字保留 1.35 / 0.85 需要把充能单位换成厘 Vis，
 * 那会让 Thaumonomicon 的「充能 X/Y」显示与其它物品不同量纲，得不偿失。
 */
public class ItemThunderpeal extends FRItem implements FRRechargable {

    public ItemThunderpeal(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 单次施法的 Vis 消耗：原版基础值乘 {@code thunderpealVisMult}。 */
    public int getVisCost() {
        return (int) (FRConfig.THUNDERPEAL_VIS_COST.get() * FRConfig.THUNDERPEAL_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.THUNDERPEAL_MAX_CHARGE.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 发射与充能扣除都只在服务端做，客户端交给服务端同步。
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return InteractionResultHolder.fail(stack);
        }
        spawnOrb(level, player);
        CooldownHelper.setCooldown(player, FRConfig.THUNDERPEAL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    /** 对应原版 {@code spawnOrb}：位置与初速都由实体的发射构造器决定。 */
    private static void spawnOrb(Level level, Player player) {
        EntityThunderpealOrb orb = new EntityThunderpealOrb(level, player);
        SoundHelper.play(level, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemThunderpeal1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemThunderpeal2.lore"));
        // 原版是把 24 / 16 硬编码进文案；这里改成显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.ItemThunderpeal3.lore",
                (int) FRConfig.THUNDERPEAL_DIRECT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemThunderpeal4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemThunderpeal5.lore",
                (int) FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemThunderpeal6.lore"));
        tooltip.add(Component.translatable("item.ItemThunderpeal7.lore"));
    }
}
