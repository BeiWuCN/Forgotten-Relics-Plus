package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityRageousMissile;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import vazkii.botania.common.handler.BotaniaSounds;

/**
 * 核子之怒（Nuclear Fury），注册名 {@code nuclear_fury}，
 * 1.7.10 原版类名是 {@code ItemMissileTome}（这里沿用 RE 的 {@code ItemNuclearFury}）。
 * 堆叠上限 1、Warp 5、{@code EnumAction.bow}，<b>原版没有施法冷却</b>。
 *
 * <p>行为：右键进入 72000 tick 的拉弓引导；引导期间只要 {@code count % 2 == 0} 且不是第一 tick，
 * 就尝试抽一次 Vis——火（Ignis）20 + 秩序（Ordo）10 + 混沌（Perditio）15 厘 = 每颗法球 0.45 点
 *（各项都乘 {@code nuclearFuryVisMult}）——抽得出来才生成一颗 {@code EntityRageousMissile}，于是
 * 引导期间每 2 tick 抛出一颗（10 颗/秒）。导弹出生点水平 ±3.1 随机、竖直
 * {@code Y = posY + 3.8 + (random - 1.55)}；在出生点播 {@code botania:missile}（0.6 / 0.8 + rand×0.2）
 * 并炸一小簇粒子，同一 tick 还在玩家上方 {@code posY + 2.4} 撒一颗颜色 {@code (1.0, 0.4, 1.0)} 的
 * Botania sparkle。
 *
 * <p>1.21.1 对应：{@code onItemRightClick / onUsingTick / getMaxItemUseDuration / EnumAction.bow} →
 * {@code use / onUseTick / getUseDuration / UseAnim.BOW}；<b>「从背包法杖抽 Vis」没有对应 API</b>
 *（见 {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1），按本模组统一约定改成 {@link FRRechargable}
 * 的物品自身充能；导弹音 {@code botania:missile} 本就是 {@link BotaniaSounds#MISSILE}，直接引用并按项目
 * 约定过 {@link SoundHelper#play} 压音量；Thaumcraft 的 {@code proxy.burst}（纯客户端粒子，<b>不是
 * Botania</b>）→ 服务端 {@link ParticleTypes#WITCH}，玩家上方的 Botania sparkle →
 * {@link FRParticles#serverSparkle}（颜色 {@code (1.0, 0.4, 1.0)}、尺寸 6.0、m=6）；原版
 * {@code spawnMissile} 返回 {@code boolean} 但调用方忽略、{@code isFull3D()} 无对应概念，都略过。
 *
 * <p><b>Vis 折算（沿用 RE）</b>：原版每秒 10 颗 × 每颗 0.45 = 4.5 点/秒，而充能是整数。RE 把它折算成
 * {@code nuclearFuryVisCostPerSecond = 5}（4.5 向上取整）、{@code nuclearFuryMaxCharge = 500}（正好
 * 100 秒连续引导）、{@code nuclearFuryClearRange = 32}。本项目沿用这套值：导弹仍然每 2 tick 一发，
 * <b>在每一秒的第一发导弹之前扣一次 {@code getVisCostPerSecond()} 点</b>，扣不出来就停止引导——于是
 * 500 充能恰好打出 100 秒（1000 颗）。扣费放在该秒第一发之前（而不是固定每 20 tick 的边界上），是为了
 * 不让「0 充能先白打半秒、松手再按」变成可反复白嫖的漏洞；代价是「不满 1 秒就松手会按满 1 秒计费」。
 *
 * <p><b>与 1.7.10 的偏差</b>：
 * <ol>
 *   <li>原版是「每颗法球从法杖抽 0.45 点，抽不到就不发」，这里是「每秒从物品充能扣 5 点，扣不到就停」，
 *       见上面的折算说明；</li>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerSecond.lore} + 各要素成本）依赖
 *       {@code GuiScreen.isCtrlKeyDown}，而共享基类 {@code FRItem} 只实现 Shift 展开，近几件施法物品
 *       （霹雳咒书、腥红之咒、邪术之咒）也都没有该行，这里保持一致；lang 里现成的
 *       {@code item.NuclearFuryVisCost.lore} 因此不引用（留给以后复用它做 tooltip 的物品）；</li>
 *   <li>RE 给本物品加了「左键清除 32 格内自己的导弹」（{@code clearMissiles} + 自定义网络包），
 *       1.7.10 的 {@code ItemMissileTome} <b>没有</b>这个功能，所以本项目不实现；配置
 *       {@code nuclearFuryClearRange} 转交给实体做目标搜索半径。</li>
 * </ol>
 */
public class ItemNuclearFury extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000。 */
    private static final int USE_DURATION = 72000;

    /** 原版发射节奏：{@code count % 2 == 0}，每 2 tick 一发。 */
    private static final int SHOT_INTERVAL = 2;

    /**
     * 折算后的扣费节奏：每 1 秒（20 tick）扣一次 {@code nuclearFuryVisCostPerSecond}，
     * 且扣在「这一秒的第一发导弹之前」（{@code elapsed == SHOT_INTERVAL}），避免 0 充能白拿半秒。
     */
    private static final int VIS_INTERVAL = 20;

    /** 出生点：水平 ±3.1 的随机量。 */
    private static final double HORIZONTAL_SPREAD = 3.1D;

    /** 出生点：竖直基础抬高。 */
    private static final double VERTICAL_OFFSET = 3.8D;

    /**
     * 出生点：竖直方向的 {@code (random - 1.55)}——原版就是这么写的（RE 文档怀疑本意是
     * {@code (random - 0.5) * 3.1}，两者相差一个左括号）。按「1.7.10 是唯一行为参照」逐字保留。
     */
    private static final double VERTICAL_SPREAD = 1.55D;

    public ItemNuclearFury(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 每秒的 Vis 消耗：原版每秒 10 颗 × 每颗 0.45 = 4.5，RE 取整为 5，再乘 {@code nuclearFuryVisMult}。
     */
    public int getVisCostPerSecond() {
        return (int) (FRConfig.NUCLEAR_FURY_VIS_COST_PER_SECOND.get() * FRConfig.NUCLEAR_FURY_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.NUCLEAR_FURY_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.NUCLEAR_FURY_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版 onItemRightClick 只是 setItemInUse(stack, 72000)，真正的节奏在 onUsingTick。
        // 客户端也一起进入姿态，这样弓的拉扯动画才会出现（与符文天象石、原初混沌之典同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 生成、扣费、音效与粒子都只在服务端做；客户端交给服务端同步。
        if (level.isClientSide()) {
            return;
        }
        // 原版条件 count != getMaxItemUseDuration()（第一个 tick 不触发）。
        if (remainingUseDuration <= 0 || remainingUseDuration == USE_DURATION) {
            return;
        }
        int elapsed = USE_DURATION - remainingUseDuration;
        // 先扣费、后开火：在「每一秒的第一发导弹之前」扣一次，扣不出来就整段停止引导
        //（对应原版「抽不到 Vis 就不生成法球」）。放在第一发之前是为了避免
        //「0 充能也能先白拿半秒的导弹、松手再按反复白嫖」这个漏洞。
        if (elapsed > 0 && elapsed % VIS_INTERVAL == SHOT_INTERVAL
                && !RechargeAccess.consumeCharge(stack, player, getVisCostPerSecond())) {
            player.stopUsingItem();
            return;
        }
        // 原版 count % 2 == 0：每 2 tick 抛出一颗导弹。
        if (remainingUseDuration % SHOT_INTERVAL == 0) {
            fire(level, player);
        }
    }

    /**
     * 对应原版 {@code spawnMissile} + {@code onUsingTick} 里的 sparkle：
     * 随机出生点、<b>初速为 0</b>（原版不设 motion，第一颗 tick 由实体自己朝目标重写速度）、
     * 一发 {@code botania:missile}、一小簇爆发粒子，以及玩家上方那颗粉色 sparkle。
     */
    private static void fire(Level level, Player player) {
        double x = player.getX() + (level.random.nextDouble() - 0.5D) * HORIZONTAL_SPREAD;
        // 原版：posY + 3.8 + (Math.random() - 1.55)。逐字保留这个括号位置。
        double y = player.getY() + VERTICAL_OFFSET + (level.random.nextDouble() - VERTICAL_SPREAD);
        double z = player.getZ() + (level.random.nextDouble() - 0.5D) * HORIZONTAL_SPREAD;

        EntityRageousMissile missile = new EntityRageousMissile(level, player, false);
        missile.setPos(x, y, z);
        if (level instanceof ServerLevel server) {
            // 原版 world.playSoundAtEntity(x, y, z, "botania:missile", 0.6F, 0.8F + random * 0.2F)。
            SoundHelper.play(level, x, y, z, BotaniaSounds.MISSILE, SoundSource.PLAYERS,
                    0.6F, 0.8F + level.random.nextFloat() * 0.2F);
            // 原版 Thaumcraft.proxy.burst(world, x, y, z, 0.25F) 的纯粒子替身
            //（Thaumcraft 粒子，非 Botania，按口径保留原版 WITCH）。
            server.sendParticles(ParticleTypes.WITCH, x, y, z, 8, 0.15D, 0.15D, 0.15D, 0.02D);
            // 原版 onUsingTick 每 2 tick 在 player.posY + 2.4 处撒一颗 (1.0, 0.4, 1.0) 的 Botania sparkle：
            // sparkleFX(player, y + 2.4, 1.0, 0.4, 1.0, size=6.0, m=6)。
            FRParticles.serverSparkle(server, player.getX(), player.getY() + 2.4D, player.getZ(),
                    1.0F, 0.4F, 1.0F, 6.0F, 6);
        }
        level.addFreshEntity(missile);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：第 1~3 行、空行、第 4~6 行、空行、第 7 行。
        tooltip.add(Component.translatable("item.NuclearFury1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.NuclearFury2.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // 原版把 24-32 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.NuclearFury3.lore",
                (int) FRConfig.NUCLEAR_FURY_DAMAGE_MIN.get().doubleValue(),
                (int) FRConfig.NUCLEAR_FURY_DAMAGE_MAX.get().doubleValue()));
    }
}
