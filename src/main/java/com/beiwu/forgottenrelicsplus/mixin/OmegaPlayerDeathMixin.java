package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Player.class, ServerPlayer.class})
public abstract class OmegaPlayerDeathMixin {
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardPlayerDeath(DamageSource source, CallbackInfo callback) {
        if (OmegaCoreProtection.preventDeath((Player) (Object) this)) callback.cancel();
    }
}
