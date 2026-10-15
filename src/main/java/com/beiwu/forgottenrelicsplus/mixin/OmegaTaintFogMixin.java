package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import com.leclowndu93150.thaumaturge.client.taint.TaintEnvironmentClientEvents;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TaintEnvironmentClientEvents.class, remap = false)
public abstract class OmegaTaintFogMixin {
    @Inject(method = "onFog", at = @At("HEAD"), cancellable = true)
    private static void forgottenRelicsPlus$disableTaintFog(ViewportEvent.RenderFog event, CallbackInfo callback) {
        if (OmegaCoreProtection.isCarriedBy(Minecraft.getInstance().player)) {
            callback.cancel();
        }
    }
}
