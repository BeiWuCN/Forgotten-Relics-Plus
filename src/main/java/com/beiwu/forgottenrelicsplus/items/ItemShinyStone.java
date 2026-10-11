package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityShinyEnergy;
import com.beiwu.forgottenrelicsplus.registry.FRDataComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;

public class ItemShinyStone extends FRCurioItem implements WearerTickBehaviour {

    public ItemShinyStone(Properties properties) {
        super(properties);
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        int checkRate = Math.max(1, FRConfig.SHINY_STONE_CHECK_RATE.get());

        if (wearer.tickCount % checkRate == 0) {

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
