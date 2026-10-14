package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class OmegaPlayerRemovalMixin {
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardPlayerRemoval(Entity.RemovalReason reason, CallbackInfo callback) {
        if (OmegaCoreProtection.blocksRemoval(reason)
                && OmegaCoreProtection.preventDeath((Player) (Object) this)) callback.cancel();
    }
}
