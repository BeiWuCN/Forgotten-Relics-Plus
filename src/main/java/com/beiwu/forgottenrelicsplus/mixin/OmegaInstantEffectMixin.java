package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MobEffect.class, targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public abstract class OmegaInstantEffectMixin {
    @Inject(method = "applyInstantenousEffect", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$blockInstantHarm(Entity source, Entity indirectSource, LivingEntity target,
            int amplifier, double potency, CallbackInfo callback) {
        if (((MobEffect) (Object) this).getCategory() == MobEffectCategory.HARMFUL
                && OmegaCoreProtection.isCarriedBy(target)) {
            callback.cancel();
        }
    }
}
