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
 * <p>原版逻辑：右键沿视线逐格搜索活体——每格取一个以（<b>累加推进的</b>）视线落点为中心的方框，
 * 半边长基础 3 格、超过 10 格加到 6、超过 20 格加到 8，找到第一个就停；扫到 32 格仍没有，就兜底取
 * 玩家周围 32 格内所有能看见玩家的活体，再从候选里随机挑一个。消耗背包法杖的 Vis：火 480 + 混沌 360
 * 厘（合计 8.4 点），发射一颗 {@code EntityCrimsonOrb}（出生点 = 身体中心 + 视线 × 1.0 再抬高 0.5，
 * 初速 = 视线 × 0.75），随后进入 {@code setCasted(player, 30, true)} 的 30 tick 共用冷却。
 *
 * <p>1.21.1 对应：{@code onItemRightClick} → {@code Item#use}；<b>「从背包法杖抽 Vis」没有对应 API</b>
 * （见 {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1），按模组统一约定改成
 * {@link FRRechargable} 物品自身充能；共用冷却 → {@link CooldownHelper}；发射音效
 * {@code thaumcraft:egattack} 换成 {@link SoundEvents#FIREWORK_ROCKET_BLAST}（与邪术之咒、霹雳咒书
 * 同一替代方案）；tooltip 的伤害行沿用 RE 合并后的 {@code ItemCrimsonSpell6.lore}（两处 %s 占位）。
 *
 * <p><b>三处刻意的取舍</b>：
 * <ol>
 *   <li>原版单次 8.4 点 Vis，充能是整数，{@code crimsonSpellVisCost} 默认取整为 8
 *       （与霹雳咒书 2.2 → 2 同一处理）；</li>
 *   <li>原版 {@code IgnisCost} / {@code PerditioCost} 乘的其实是 {@code chaosTomeVisMult}，而专门为它
 *       准备的 {@code crimsonSpellVisMult} 从未被使用——这是原版的笔误。这里按配置本意改用本物品自己的
 *       倍率，并把这条偏差写进提交说明；</li>
 *   <li>原版「Ctrl 查看 Vis 消耗」那一支依赖 {@code FRVisPerCast.lore} 等键，本项目共享基类
 *       {@code FRItem} 只实现 Shift 展开，这里保持一致、不新增语言键。</li>
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
