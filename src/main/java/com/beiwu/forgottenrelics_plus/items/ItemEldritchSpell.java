package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityDarkMatterOrb;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
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
 * 邪术之咒（Eldritch Spell），注册名 {@code eldritch_spell}，原版 {@code ItemEldritchSpell}。
 * 堆叠上限 1、稀有度 EPIC、Warp 4。
 *
 * <p>行为：右键发射一颗暗物质法球 {@code EntityDarkMatterOrb}——从视线前方 1.0 格、抬高 0.5 处出现，
 * 速度是视线的 1.5 倍，并在玩家脚下播 {@code thaumcraft:egattack}；消耗背包里法杖的混沌（Perditio）
 * {@code 400} 厘 Vis（= 4 点，乘 {@code eldritchSpellVisMult}，正好是整数、无需取整取舍）；随后进入
 * {@code SuperpositionHandler.setCasted(player, 20, true)} 的 20 tick 共用冷却。
 *
 * <p>1.21.1 对应：{@code onItemRightClick} → {@code Item#use}；<b>「从背包法杖抽 Vis」没有对应 API</b>，
 * 按本移植的统一约定改成 {@link FRRechargable} 的物品自身充能，用 {@link RechargeAccess#consumeCharge}
 * 扣除；原版的 {@code SuperpositionHandler} 冷却 → {@link CooldownHelper}（与其它遗物共用）；发射音效
 * {@code thaumcraft:egattack} → {@link SoundEvents#FIREWORK_ROCKET_BLAST}（与霹雳咒书同一套替代方案，
 * 且照原版音量 0.6 / 音调 0.8 + 随机 0.2）；RE 已把原版的 {@code ItemEldritchSpell5_1.lore + " " +
 * 伤害 + " " + ItemEldritchSpell5_2.lore} 合并成带 {@code %s} 的 {@code ItemEldritchSpell5.lore}，
 * 本移植的 lang 沿用合并后的键，这里直接传配置里的基础伤害值。
 *
 * <p>两处刻意的取舍：
 * <ol>
 *   <li>原版 tooltip 按住 Shift 时显示的是按「持有者是否身处外域」取的伤害值（{@code eldritchSpellDamageEx}
 *       或 {@code eldritchSpellDamage}）。1.21.1 的 {@code appendHoverText} 拿不到玩家实例，本移植也不在
 *       通用物品类里引入客户端专属类，所以固定显示基础伤害（RE 的 1.12.2 版同样只显示基础值）；</li>
 *   <li>原版的「Ctrl 查看 Vis 消耗」分支依赖 {@code FRVisPerCast.lore} / {@code FRPerditioCost.lore}，
 *       而共享基类 {@code FRItem} 只实现 Shift 展开，其它施法物品（霹雳咒书、错位之典）也都没有该行，
 *       这里保持一致、不新增语言键。</li>
 * </ol>
 */
public class ItemEldritchSpell extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getWarp} 返回的固定值。 */
    private static final int WARP = 4;

    public ItemEldritchSpell(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 单次施法的 Vis 消耗：原版基础值 4 乘 {@code eldritchSpellVisMult}。
     *
     * <p>原版是混沌（Perditio）400 厘 Vis = 4 点，正好是整数，无需取整取舍。
     */
    public int getVisCost() {
        return (int) (FRConfig.ELDRITCH_SPELL_VIS_COST.get() * FRConfig.ELDRITCH_SPELL_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.ELDRITCH_SPELL_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return WARP;
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
        CooldownHelper.setCooldown(player, FRConfig.ELDRITCH_SPELL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    /** 对应原版 {@code spawnOrb}：位置与初速都由实体的发射构造器决定。 */
    private static void spawnOrb(Level level, Player player) {
        EntityDarkMatterOrb orb = new EntityDarkMatterOrb(level, player);
        // 原版是在玩家脚下按 0.6 音量、0.8 + 随机 0.2 音调播放 thaumcraft:egattack。
        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                0.6F, 0.8F + level.random.nextFloat() * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemEldritchSpell1.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell2.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell4.lore"));
        // 见类注释「取舍」第 1 条：原版按维度取值，这里固定显示基础伤害。
        tooltip.add(Component.translatable("item.ItemEldritchSpell5.lore", FRConfig.ELDRITCH_SPELL_DAMAGE.get()));
        tooltip.add(Component.translatable("item.ItemEldritchSpell6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell7.lore"));
    }
}
