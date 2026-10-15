package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class OmegaEffectMixin {
    @Shadow @Final private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;
    @Shadow protected abstract void onEffectRemoved(MobEffectInstance effect);

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$blockNegativeEffect(MobEffectInstance effect, Entity source,
            CallbackInfoReturnable<Boolean> callback) {
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL
                && OmegaCoreProtection.isCarriedBy((LivingEntity) (Object) this)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "forceAddEffect", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$blockForcedNegativeEffect(MobEffectInstance effect, Entity source, CallbackInfo callback) {
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL
                && OmegaCoreProtection.isCarriedBy((LivingEntity) (Object) this)) {
            callback.cancel();
        }
    }

    @Inject(method = "tickEffects", at = @At("HEAD"))
    private void forgottenRelicsPlus$clearNegativeEffects(CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player) || entity.level().isClientSide() || activeEffects.isEmpty()) return;
        boolean carried = false;
        var effects = activeEffects.values().iterator();
        while (effects.hasNext()) {
            MobEffectInstance effect = effects.next();
            if (effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
            if (!carried) {
                if (!OmegaCoreProtection.isCarriedBy(entity)) return;
                carried = true;
            }
            effects.remove();
            onEffectRemoved(effect);
        }
    }
}
