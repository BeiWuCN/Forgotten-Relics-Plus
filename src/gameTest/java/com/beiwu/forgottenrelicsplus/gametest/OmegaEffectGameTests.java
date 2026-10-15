package com.beiwu.forgottenrelicsplus.gametest;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgottenRelics.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OmegaEffectGameTests {
    private OmegaEffectGameTests() {}

    private static Player player(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
    }

    private static void carry(Player player) {
        player.getInventory().setItem(0, new ItemStack(FRItems.OMEGA_CORE.get()));
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void blocksRegisteredHarmfulEffectsAndForcedApplications(GameTestHelper helper) {
        Player player = player(helper);
        carry(player);
        BuiltInRegistries.MOB_EFFECT.holders().forEach(effect -> {
            if (effect.value().getCategory() != MobEffectCategory.HARMFUL) return;
            helper.assertTrue(!player.addEffect(new MobEffectInstance(effect, 200, 1)), "Accepted harmful effect " + effect);
            player.forceAddEffect(new MobEffectInstance(effect, 200, 1), null);
            helper.assertTrue(!player.hasEffect(effect), "Forced harmful effect bypassed immunity " + effect);
        });
        helper.assertTrue(player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200)), "Beneficial effects were blocked");
        player.forceAddEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200), null);
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "Forced beneficial effect was blocked");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void pickupPurgesExistingEffectsBeforeTheyTick(GameTestHelper helper) throws Exception {
        Player player = player(helper);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200));
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "Weakness was not applied before pickup");
        carry(player);
        var tick = LivingEntity.class.getDeclaredMethod("tickEffects");
        tick.setAccessible(true);
        tick.invoke(player);
        helper.assertTrue(player.getActiveEffects().stream().noneMatch(
                effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL), "Existing harmful effects survived pickup");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Pickup cleared beneficial effects");
        helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Poison ticked before removal");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200)), "Protection persisted after removal");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void instantDamageBlockedAndHealingPreserved(GameTestHelper helper) {
        Player player = player(helper);
        carry(player);
        player.setHealth(10);
        MobEffects.HARM.value().applyInstantenousEffect(null, null, player, 0, 1);
        helper.assertTrue(player.getHealth() == 10, "Instant damage bypassed immunity");
        MobEffects.HEAL.value().applyInstantenousEffect(null, null, player, 0, 1);
        helper.assertTrue(player.getHealth() == 14, "Instant healing was blocked");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        MobEffects.HARM.value().applyInstantenousEffect(null, null, player, 0, 1);
        helper.assertTrue(player.getHealth() < 14, "Instant damage remained blocked after removal");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void protectsFromEveryInventorySlotIncludingOffhand(GameTestHelper helper) {
        Player player = player(helper);
        for (int slot : new int[] {0, 35, 40}) {
            player.getInventory().setItem(slot, new ItemStack(FRItems.OMEGA_CORE.get()));
            helper.assertTrue(OmegaCoreProtection.isCarriedBy(player), "Missed inventory slot " + slot);
            helper.assertTrue(!player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200)), "Slot did not protect " + slot);
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        helper.assertTrue(!OmegaCoreProtection.isCarriedBy(player), "Carried state persisted without core");
        helper.assertTrue(!OmegaCoreProtection.isCarriedBy(null), "Null camera player was not handled");
        helper.succeed();
    }
}
