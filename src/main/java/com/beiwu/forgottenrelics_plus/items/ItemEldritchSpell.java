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
 * 邪术之咒（Eldritch Spell），注册名 {@code eldritch_spell}，1.7.10 原版 {@code ItemEldritchSpell}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>右键发射一颗暗物质法球 {@code EntityDarkMatterOrb}：从视线前方 1.0 格、抬高 0.5 处出现，
 *       速度是视线的 1.5 倍，并在玩家脚下播放 {@code thaumcraft:egattack}；</li>
 *   <li>消耗<b>背包里法杖的 Vis</b>：混沌（ENTROPY / Perditio）{@code 400} 厘 Vis，即 4 点，
 *       乘 {@code eldritchSpellVisMult}；</li>
 *   <li>随后进入 {@code SuperpositionHandler.setCasted(player, 20, true)} 的 20 tick 共用冷却；</li>
 *   <li>物品堆叠上限 1，稀有度 EPIC，{@code getWarp} 返回 4。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>。按本模组统一约定改成
 *       {@link FRRechargable} 的<b>物品自身充能</b>，用 {@link RechargeAccess#consumeCharge} 扣除；</li>
 *   <li>原版的 {@code SuperpositionHandler} 冷却 → {@link CooldownHelper}（与其它遗物共用）；</li>
 *   <li>发射音效 {@code thaumcraft:egattack} 换成原版 {@link SoundEvents#FIREWORK_ROCKET_BLAST}
 *       （与霹雳咒书同一套替代方案，且照原版音量 0.6 / 音调 0.8 + 随机 0.2）；</li>
 *   <li>原版 tooltip 用 {@code ItemEldritchSpell5_1.lore + " " + 伤害 + " " + ItemEldritchSpell5_2.lore}
 *       拼出「造成 X 伤害」那一行；RE 已把它合并成带 {@code %s} 的 {@code ItemEldritchSpell5.lore}，
 *       本项目的 lang 沿用合并后的键，这里直接传配置里的基础伤害值。</li>
 * </ul>
 *
 * <p><b>两处刻意的取舍</b>：
 * <ol>
 *   <li>原版 tooltip 在按住 Shift 时显示的是「按持有者是否身处外域」取的伤害值
 *       （{@code eldritchSpellDamageEx} 或 {@code eldritchSpellDamage}）。1.21.1 的
 *       {@code appendHoverText} 拿不到玩家实例，本项目也不在通用物品类里引入客户端专属类，
 *       所以这里固定显示基础伤害 {@code eldritchSpellDamage}（RE 的 1.12.2 版同样只显示基础值）；</li>
 *   <li>原版的「Ctrl 查看 Vis 消耗」那一支依赖 {@code FRVisPerCast.lore} / {@code FRPerditioCost.lore}，
 *       而本项目的共享基类 {@code FRItem} 只实现了 Shift 展开，近几件施法物品（霹雳咒书、错位之典）
 *       也都没有该行，这里保持一致、不新增语言键。</li>
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
        // 见类注释「取舍」第 1 条：1.7.10 这里按维度取值，这里固定显示基础伤害。
        tooltip.add(Component.translatable("item.ItemEldritchSpell5.lore", FRConfig.ELDRITCH_SPELL_DAMAGE.get()));
        tooltip.add(Component.translatable("item.ItemEldritchSpell6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell7.lore"));
    }
}
