package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityCrimsonOrb;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 腥红之咒（Crimson Spell），注册名 {@code crimson_spell}，1.7.10 原版 {@code ItemCrimsonSpell}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>右键沿视线方向逐格搜索一个活体目标：每格取一个以视线落点为中心的方框，
 *       半边长 3 格，距离超过 10 格加到 6、超过 20 格加到 8；找到第一个就停下。
 *       若一路扫到 32 格都没有，则退而求其次，取玩家周围 32 格内所有「能看见玩家」的活体。
 *       最后从候选里随机挑一个（扫不到就是 {@code null}）；</li>
 *   <li>消耗<b>背包里法杖的 Vis</b>：火（Ignis）{@code 480} + 混沌（Perditio）{@code 360}
 *       厘 Vis = 4.8 + 3.6 = 8.4 点；</li>
 *   <li>发射一颗猩红法球 {@code EntityCrimsonOrb}：出生点 = 身体中心 + 视线 × 1.0 再抬高 0.5，
 *       初速 = 视线 × 0.75（比霹雳球慢得多）；</li>
 *   <li>随后进入 {@code SuperpositionHandler.setCasted(player, 30, true)} 的 30 tick 共用冷却；</li>
 *   <li>物品堆叠上限 1，稀有度 EPIC，{@code getWarp} 返回 3。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见
 *       {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。按本模组统一约定改成
 *       {@link FRRechargable} 的<b>物品自身充能</b>，用 {@link RechargeAccess#consumeCharge} 扣除；</li>
 *   <li>原版的 {@code SuperpositionHandler} 冷却 → {@link CooldownHelper}（与其它遗物共用）；</li>
 *   <li>发射音效 {@code thaumcraft:egattack}（0.6 音量 / 0.8 + rand*0.2 音调）换成原版等价物
 *       {@link SoundEvents#FIREWORK_ROCKET_BLAST}——与邪术之咒、霹雳咒书同一套替代方案；</li>
 *   <li>原版 tooltip 的「造成 X-Y 伤害」那一行是 {@code ItemCrimsonSpell6_1.lore
 *       + " " + MIN + "-" + MAX + " " + ItemCrimsonSpell6_2.lore} 拼出来的，RE 已把三段合并成
 *       带两个 {@code %s} 的 {@code ItemCrimsonSpell6.lore}，本项目的 lang 沿用合并后的键。</li>
 * </ul>
 *
 * <p><b>三处刻意的取舍</b>：
 * <ol>
 *   <li>原版单次合计 8.4 点 Vis，而充能是整数，所以 {@code crimsonSpellVisCost} 默认取整为 8
 *       （与霹雳咒书 2.2 → 2 同一处理）；</li>
 *   <li>原版 {@code IgnisCost} / {@code PerditioCost} 乘的其实是 {@code chaosTomeVisMult}，
 *       而那个专门为它准备的 {@code crimsonSpellVisMult} 从未被使用——这是原版的笔误。
 *       这里按配置本意改用本物品自己的 {@code crimsonSpellVisMult}，并把这条偏差写进提交说明；</li>
 *   <li>原版的「Ctrl 查看 Vis 消耗」那一支依赖 {@code FRVisPerCast.lore} / {@code FRIgnisCost.lore} /
 *       {@code FRPerditioCost.lore}，本项目的共享基类 {@code FRItem} 只实现了 Shift 展开，
 *       近几件施法物品也都没有该行，这里保持一致、不新增语言键。</li>
 * </ol>
 */
public class ItemCrimsonSpell extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版索敌的最大距离（格），也是「扫不到目标」时的兜底搜索半径。 */
    private static final int SCAN_RANGE = 32;

    /** 扫描方框的基础半边长（原版 {@code SEARCH_RANGE = 3.0f}）。 */
    private static final double BASE_BOX = 3.0D;

    public ItemCrimsonSpell(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 单次施法的 Vis 消耗：原版基础值 8 乘 {@code crimsonSpellVisMult}。
     *
     * <p>原版合计 4.8（火）+ 3.6（混沌）= 8.4 点，充能为整数故取 8，见类注释「取舍」第 1 条。
     */
    public int getVisCost() {
        return (int) (FRConfig.CRIMSON_SPELL_VIS_COST.get() * FRConfig.CRIMSON_SPELL_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.CRIMSON_SPELL_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.CRIMSON_SPELL_WARP.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 索敌、发射与充能扣除都只在服务端做，客户端交给服务端同步。
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }
        // 原版先算完目标列表再扣 Vis；扣费失败就什么都不发生。
        LivingEntity target = findTarget(level, player);
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return InteractionResultHolder.fail(stack);
        }
        spawnOrb(level, player, target);
        CooldownHelper.setCooldown(player, FRConfig.CRIMSON_SPELL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    /**
     * 原版 {@code onItemRightClick} 里那段「沿视线找目标」的复刻。
     *
     * <p><b>注意：原版的位置推进写得很怪</b>——{@code vec.add(look.multiply(distance))} 是<b>累加</b>，
     * 而不是常规的「起点 + 视线 × 距离」，并且每轮还要再 {@code vec.y += 0.5}。31 轮累计下来，
     * 视线方向的偏移是 1+2+…+31 = 496 格，远超 32 格上限，等于扫出一条迅速飞出视野、且不断爬升的射线。
     * 好在每轮方框有 3~8 格半边长，实际仍覆盖面前十几格；这里逐字保留，不「修正」成正常射线。
     */
    private static LivingEntity findTarget(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        // 原版 Vector3.fromEntityCenter：以身体中心为起点。
        Vec3 cursor = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        List<LivingEntity> found = List.of();
        for (int distance = 1; found.isEmpty() && distance < SCAN_RANGE; distance++) {
            // 原版的额外半边长：>20 格 +5、>10 格 +3。
            double extra = distance > 20 ? 5.0D : (distance > 10 ? 3.0D : 0.0D);
            cursor = cursor.add(look.scale(distance)).add(0.0D, 0.5D, 0.0D);
            double half = BASE_BOX + extra;
            AABB box = new AABB(cursor.x - half, cursor.y - half, cursor.z - half,
                    cursor.x + half, cursor.y + half, cursor.z + half);
            found = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
        }
        if (!found.isEmpty()) {
            return pick(found, level);
        }

        // 兜底：玩家周围 SCAN_RANGE 格内所有「能看见玩家」的活体（原版 canEntityBeSeen）。
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(player.getX() - SCAN_RANGE, player.getY() - SCAN_RANGE, player.getZ() - SCAN_RANGE,
                        player.getX() + SCAN_RANGE, player.getY() + SCAN_RANGE, player.getZ() + SCAN_RANGE),
                e -> e != player && e.isAlive() && e.hasLineOfSight(player));
        return nearby.isEmpty() ? null : pick(nearby, level);
    }

    /**
     * 从候选里随机挑一个。
     *
     * <p>原版写的是 {@code entities.get((int)((entities.size() - 1) * Math.random()))}，
     * 因此<b>最后一个元素永远挑不到</b>（size 为 2 时只会选第 0 个）。这里逐字保留该行为。
     */
    private static LivingEntity pick(List<LivingEntity> candidates, Level level) {
        return candidates.get((int) ((candidates.size() - 1) * level.random.nextDouble()));
    }

    /** 对应原版 {@code spawnOrb}：出生点 = 身体中心 + 视线 × 1.0 再抬高 0.5，初速 = 视线 × 0.75。 */
    private static void spawnOrb(Level level, Player player, LivingEntity target) {
        Vec3 look = player.getLookAngle();
        Vec3 spawn = player.position()
                .add(0.0D, player.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(1.0D))
                .add(0.0D, 0.5D, 0.0D);
        EntityCrimsonOrb orb = new EntityCrimsonOrb(level, player, target);
        orb.setPos(spawn.x, spawn.y, spawn.z);
        orb.setDeltaMovement(look.scale(0.75D));
        // 原版在玩家脚下按 0.6 音量、0.8 + 随机 0.2 音调播放 thaumcraft:egattack。
        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 0.6F, 0.8F + level.random.nextFloat() * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemCrimsonSpell1.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell2.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell4.lore"));
        tooltip.add(Component.translatable("item.ItemCrimsonSpell5.lore"));
        // 原版把 42-100 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.ItemCrimsonSpell6.lore",
                (int) FRConfig.CRIMSON_SPELL_DAMAGE_MIN.get().doubleValue(),
                (int) FRConfig.CRIMSON_SPELL_DAMAGE_MAX.get().doubleValue()));
    }
}
