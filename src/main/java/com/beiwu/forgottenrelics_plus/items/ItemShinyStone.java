package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityShinyEnergy;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;

/**
 * 日耀石（Shiny Stone），1.12.2 原版 {@code ItemShinyStone}，护符槽。
 *
 * <p>原版行为：每隔 {@code shinyStoneCheckrate}（默认 4）tick 记录一次位置；若位置与上次完全相同，
 * 就把「静止累计量」加上 {@code shinyStoneStillIncrement}（默认 4），并按累计量分四档加快回血；
 * 一旦移动就清零。档位越高，回血间隔越短（10 / 5 / 2 / 1 倍），同时身上的能量粒子越多。
 *
 * <p>两处现代化改写：
 * <ul>
 *   <li>「是否静止」不再往物品 NBT 里存上一 tick 的坐标再逐位比较，直接用
 *       {@code Entity#xo/yo/zo}（上一 tick 的位置）与当前坐标比较，语义相同但少一个组件；</li>
 *   <li>原版为了那点粒子专门做了一个自定义实体 {@code EntityShinyEnergy}。上一版图省事，
 *       直接把「每档几颗」换成几颗 {@code END_ROD}，观感与原版差得远（玩家反馈「日耀石 VFX 未实现」）。
 *       本次把实体真正做出来（见 {@link EntityShinyEnergy}）：它自己每 tick 发 8 颗橙黄色 sparkle、
 *       以 0.15 的速度朝佩戴者飞、30 tick 后消失、碰到佩戴者时来一发 24 颗黄绿 wisp 的爆发。
 *       本类只负责「按档位生成 1~4 颗」，出生点与初速逐字照抄原版 {@code spawnEnergyParticle}。</li>
 * </ul>
 */
public class ItemShinyStone extends FRCurioItem implements WearerTickBehaviour {

    public ItemShinyStone(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        int checkRate = Math.max(1, FRConfig.SHINY_STONE_CHECK_RATE.get());

        if (wearer.tickCount % checkRate == 0) {
            // xo/yo/zo 是上一 tick 的位置：与当前坐标相等即视为这一 tick 没动。
            boolean still = wearer.getX() == wearer.xo && wearer.getY() == wearer.yo && wearer.getZ() == wearer.zo;
            if (still) {
                int accumulated = stack.getOrDefault(FRDataComponents.SHINY_STILL_TICKS.get(), 0)
                        + FRConfig.SHINY_STONE_STILL_INCREMENT.get();
                stack.set(FRDataComponents.SHINY_STILL_TICKS.get(), accumulated);
                spawnEnergy(wearer, healRate(accumulated));
            } else {
                stack.set(FRDataComponents.SHINY_STILL_TICKS.get(), 0);
            }
        }

        int rate = healRate(stack.getOrDefault(FRDataComponents.SHINY_STILL_TICKS.get(), 0));
        if (rate <= 0) {
            return;
        }
        // 原版把判定间隔除以 4 作为基准，再按档位乘上 10 / 5 / 2 / 1。间隔太小时兜底为 1，避免除零。
        int base = Math.max(1, checkRate / 4);
        int divisor = switch (rate) {
            case 1 -> 10;
            case 2 -> 5;
            case 3 -> 2;
            default -> 1;
        };
        if (wearer.tickCount % (base * divisor) == 0) {
            wearer.heal(FRConfig.SHINY_STONE_HEAL_AMOUNT.get().floatValue());
        }
    }

    /** 把「静止累计量」换算成 0～4 档回血速度。 */
    private static int healRate(int stillTicks) {
        if (stillTicks <= 0) {
            return 0;
        }
        int rate = 1;
        if (stillTicks >= FRConfig.SHINY_STONE_THRESHOLD_2.get()) {
            rate = 2;
        }
        if (stillTicks >= FRConfig.SHINY_STONE_THRESHOLD_3.get()) {
            rate = 3;
        }
        if (stillTicks >= FRConfig.SHINY_STONE_THRESHOLD_4.get()) {
            rate = 4;
        }
        return rate;
    }

    /**
     * 生成能量体，对应原版 {@code ItemShinyStone#spawnEnergyParticle}。
     *
     * <p>原版一次 {@code onWornTick} 里按 {@code particleNumber} 循环生成
     * {@code 4 - particleNumber} 颗（档位 1~4 → 1~4 颗），本方法的 {@code rate} 就是这个数量。
     * 每颗的出生点 = 佩戴者身体中心 + 每轴 {@code (random - 0.5) * 3.0}，
     * 初速 = 该偏移取反归一化后乘 {@code 0.1}（下一 tick 就会被实体自己改成「朝佩戴者 0.15」，
     * 这颗初速只影响第一 tick 的位移，但既然原版写了就照样写）。
     */
    private static void spawnEnergy(LivingEntity wearer, int rate) {
        if (rate <= 0 || wearer.level().isClientSide()) {
            return;
        }
        Vec3 center = wearer.position().add(0.0D, wearer.getBbHeight() * 0.5D, 0.0D);
        for (int i = 0; i < rate; i++) {
            Vec3 offset = new Vec3(
                    (wearer.getRandom().nextDouble() - 0.5D) * 3.0D,
                    (wearer.getRandom().nextDouble() - 0.5D) * 3.0D,
                    (wearer.getRandom().nextDouble() - 0.5D) * 3.0D);
            EntityShinyEnergy energy = new EntityShinyEnergy(wearer.level(), wearer);
            energy.setPos(center.x + offset.x, center.y + offset.y, center.z + offset.z);
            if (offset.lengthSqr() > 1.0E-8D) {
                energy.setDeltaMovement(offset.normalize().scale(-0.1D));
            }
            wearer.level().addFreshEntity(energy);
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemShinyStone1.lore"));
        tooltip.add(Component.translatable("item.ItemShinyStone2.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
    }
}
