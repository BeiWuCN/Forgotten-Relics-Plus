package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityThunderpealOrb;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
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

public class ItemThunderpeal extends FRItem implements FRRechargable {

    public ItemThunderpeal(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.THUNDERPEAL_VIS_COST.get() * FRConfig.THUNDERPEAL_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.THUNDERPEAL_MAX_CHARGE.get()
                : FRConfig.THUNDERPEAL_MAX_CHARGE.getDefault();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

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

    private static void spawnOrb(Level level, Player player) {
        EntityThunderpealOrb orb = new EntityThunderpealOrb(level, player);
        SoundHelper.play(
                level,
                orb.getX(),
                orb.getY(),
                orb.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS,
                1.0F,
                1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemThunderpeal1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemThunderpeal2.lore"));

        tooltip.add(Component.translatable("item.ItemThunderpeal3.lore", (int)
                FRConfig.THUNDERPEAL_DIRECT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemThunderpeal4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemThunderpeal5.lore", (int)
                FRConfig.THUNDERPEAL_BOLT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemThunderpeal6.lore"));
        tooltip.add(Component.translatable("item.ItemThunderpeal7.lore"));
    }
}
