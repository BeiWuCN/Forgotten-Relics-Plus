package com.beiwu.forgottenrelicsplus.gametest;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.config.FRConfig;
import com.beiwu.forgottenrelicsplus.registry.FRItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgottenRelics.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OmegaCoreGameTests {
    private OmegaCoreGameTests() {}

    private static Player carrier(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, new ItemStack(FRItems.OMEGA_CORE.get()));
        return player;
    }

    private static void assertAlive(GameTestHelper helper, Player player, String operation) {
        helper.assertTrue(player.isAlive() && !player.isDeadOrDying() && !player.isRemoved()
                && Float.isFinite(player.getHealth()) && player.getHealth() >= 1, operation + " bypassed protection");
        helper.assertTrue(!player.getInventory().getItem(0).isEmpty(), operation + " dropped the core");
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void lethalHealthWrites(GameTestHelper helper) {
        Player player = carrier(helper);
        for (float health : new float[] {0, -10, Float.NEGATIVE_INFINITY, Float.NaN, Float.POSITIVE_INFINITY}) {
            player.setHealth(health);
            assertAlive(helper, player, "setHealth(" + health + ")");
        }
        player.setHealth(5);
        helper.assertTrue(player.getHealth() == 5, "Nonlethal health writes must remain unchanged");
        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        assertAlive(helper, player, "lethal damage");
        player.kill();
        assertAlive(helper, player, "kill");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void deathAndRemovalPaths(GameTestHelper helper) {
        Player player = carrier(helper);
        player.die(player.damageSources().genericKill());
        assertAlive(helper, player, "Player.die");
        player.remove(Entity.RemovalReason.KILLED);
        assertAlive(helper, player, "remove(KILLED)");
        player.discard();
        assertAlive(helper, player, "discard");
        player.setRemoved(Entity.RemovalReason.KILLED);
        assertAlive(helper, player, "setRemoved(KILLED)");
        player.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
        helper.assertTrue(player.isRemoved(), "Dimension transfer must not be blocked");
        Player logout = carrier(helper);
        logout.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        helper.assertTrue(logout.isRemoved(), "Logout must not be blocked");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void serverPlayerDeath(GameTestHelper helper) {
        Player player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "omega-test"), ClientInformation.createDefault());
        player.getInventory().setItem(0, new ItemStack(FRItems.OMEGA_CORE.get()));
        player.die(player.damageSources().genericKill());
        assertAlive(helper, player, "ServerPlayer.die");
        player.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void rawHealthAndDeathState(GameTestHelper helper) throws ReflectiveOperationException {
        Player player = carrier(helper);
        var healthField = LivingEntity.class.getDeclaredField("DATA_HEALTH_ID");
        healthField.setAccessible(true);
        @SuppressWarnings("unchecked")
        EntityDataAccessor<Float> health = (EntityDataAccessor<Float>) healthField.get(null);
        player.getEntityData().set(health, 0.0F);
        assertAlive(helper, player, "direct synchronized health write");
        var deadField = LivingEntity.class.getDeclaredField("dead");
        deadField.setAccessible(true);
        deadField.setBoolean(player, true);
        player.deathTime = 19;
        assertAlive(helper, player, "direct death flag");
        helper.assertTrue(player.deathTime == 0, "Death timer must be cleared");
        var deathTick = LivingEntity.class.getDeclaredMethod("tickDeath");
        deathTick.setAccessible(true);
        deathTick.invoke(player);
        assertAlive(helper, player, "tickDeath");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void removingCoreRestoresNormalDeath(GameTestHelper helper) {
        Player player = carrier(helper);
        player.getInventory().setItem(0, ItemStack.EMPTY);
        player.setHealth(0);
        helper.assertTrue(player.isDeadOrDying(), "Removing the core must disable protection immediately");
        player.setRemoved(Entity.RemovalReason.KILLED);
        helper.assertTrue(player.isRemoved(), "Unprotected removal must work");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void deepVoidAllowsRealDeath(GameTestHelper helper) {
        Player player = carrier(helper);
        double boundary = helper.getLevel().getMinBuildHeight() - 256.0D;
        player.setPos(0, boundary, 0);
        player.setHealth(0);
        assertAlive(helper, player, "exact void boundary");
        player.setPos(0, boundary - 0.01D, 0);
        player.setHealth(0);
        helper.assertTrue(player.isDeadOrDying() && !player.isAlive(), "Deep void must permit lethal health writes");
        player.die(player.damageSources().genericKill());
        player.setRemoved(Entity.RemovalReason.KILLED);
        helper.assertTrue(player.isRemoved(), "Deep void death must not be canceled by the death-event fallback");
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void revocationNoticeUsesActionbarWithoutSpam(GameTestHelper helper) {
        int[] notices = {0};
        boolean[] actionbar = {false};
        String[] key = {null};
        Player player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "omega-notice-test"), ClientInformation.createDefault()) {
            @Override
            public void displayClientMessage(Component message, boolean overlay) {
                notices[0]++;
                actionbar[0] = overlay;
                key[0] = ((TranslatableContents) message.getContents()).getKey();
            }
        };
        player.getInventory().setItem(0, new ItemStack(FRItems.OMEGA_CORE.get()));
        player.setPos(0, helper.getLevel().getMinBuildHeight() - 257.0D, 0);
        for (int attempt = 0; attempt < 10; attempt++) player.setHealth(0);
        helper.assertTrue(notices[0] == 1 && actionbar[0], "Revocation must use the actionbar and not spam");
        helper.assertTrue("message.forgotten_relics_plus.omega_revoked".equals(key[0]), "Wrong revocation message");
        player.setPos(0, helper.getLevel().getMinBuildHeight(), 0);
        player.setHealth(0);
        assertAlive(helper, player, "return above void limit");
        player.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        helper.succeed();
    }

    @GameTest(template = "omega_test", templateNamespace = ForgottenRelics.MOD_ID)
    public static void disablingProtectionRestoresNormalDeath(GameTestHelper helper) {
        boolean previous = FRConfig.OMEGA_CORE_PREVENT_DEATH.get();
        try {
            FRConfig.OMEGA_CORE_PREVENT_DEATH.set(false);
            Player player = carrier(helper);
            player.setHealth(0);
            helper.assertTrue(player.isDeadOrDying(), "Disabled protection must allow lethal health writes");
            player.setRemoved(Entity.RemovalReason.KILLED);
            helper.assertTrue(player.isRemoved(), "Disabled protection must allow removal");
        } finally {
            FRConfig.OMEGA_CORE_PREVENT_DEATH.set(previous);
        }
        helper.succeed();
    }
}
