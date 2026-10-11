package com.beiwu.forgottenrelicsplus.items;

import com.beiwu.forgottenrelicsplus.api.FRRechargable;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.entity.EntityDarkMatterOrb;
import com.beiwu.forgottenrelicsplus.utils.CooldownHelper;
import com.beiwu.forgottenrelicsplus.utils.SoundHelper;
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

public class ItemEldritchSpell extends FRItem implements FRRechargable, IWarpingGear {

    private static final int WARP = 4;

    public ItemEldritchSpell(Properties properties) {
        super(properties.stacksTo(1));
    }

    public int getVisCost() {
        return (int) (FRConfig.ELDRITCH_SPELL_VIS_COST.get() * FRConfig.ELDRITCH_SPELL_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.SPEC.isLoaded()
                ? FRConfig.ELDRITCH_SPELL_MAX_CHARGE.get()
                : FRConfig.ELDRITCH_SPELL_MAX_CHARGE.getDefault();
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return WARP;
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
        CooldownHelper.setCooldown(player, FRConfig.ELDRITCH_SPELL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    private static void spawnOrb(Level level, Player player) {
        EntityDarkMatterOrb orb = new EntityDarkMatterOrb(level, player);

        SoundHelper.play(
                level,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS,
                0.6F,
                0.8F + level.random.nextFloat() * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    protected void appendShiftTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemEldritchSpell1.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell2.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell4.lore"));

        tooltip.add(Component.translatable("item.ItemEldritchSpell5.lore", FRConfig.ELDRITCH_SPELL_DAMAGE.get()));
        tooltip.add(Component.translatable("item.ItemEldritchSpell6.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemEldritchSpell7.lore"));
    }
}
