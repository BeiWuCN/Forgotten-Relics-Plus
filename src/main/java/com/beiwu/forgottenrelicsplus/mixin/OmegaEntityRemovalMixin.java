package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class OmegaEntityRemovalMixin {
    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardRemoval(Entity.RemovalReason reason, CallbackInfo callback) {
        if (OmegaCoreProtection.blocksRemoval(reason) && OmegaCoreProtection.preventDeath((Entity) (Object) this)) {
            callback.cancel();
        }
    }
}
