package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.registry.FRDataComponents;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
 *   <li>原版为了那点粒子专门做了一个自定义实体 {@code EntityShinyEnergy}。1.21.1 里
 *       直接用 {@link ServerLevel#sendParticles} 发原版粒子即可，省掉实体注册与同步开销。</li>
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
                spawnStillnessParticles(wearer, healRate(accumulated));
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

    /** 档位越高粒子越多，与原版 {@code particleNumber} 的递增方向一致。 */
    private static void spawnStillnessParticles(LivingEntity wearer, int rate) {
        if (rate <= 0 || !(wearer.level() instanceof ServerLevel level)) {
            return;
        }
        level.sendParticles(ParticleTypes.END_ROD,
                wearer.getX(), wearer.getY() + wearer.getBbHeight() * 0.5D, wearer.getZ(),
                rate, 0.35D, 0.35D, 0.35D, 0.02D);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemShinyStone1.lore"));
        tooltip.add(Component.translatable("item.ItemShinyStone2.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRAmulet.lore"));
    }
}
