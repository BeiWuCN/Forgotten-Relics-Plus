package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.api.OmegaDeathState;
import com.beiwu.forgottenrelicsplus.utils.OmegaCoreProtection;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class OmegaLivingEntityMixin implements OmegaDeathState {
    @Shadow @Final private static EntityDataAccessor<Float> DATA_HEALTH_ID;
    @Shadow protected boolean dead;
    @Shadow public int deathTime;
    @Unique private int forgottenRelicsPlus$nextRevocationNotice = Integer.MIN_VALUE;

    @Override
    public boolean forgottenRelicsPlus$canNotifyRevocation(int tick) {
        if (tick < forgottenRelicsPlus$nextRevocationNotice) return false;
        forgottenRelicsPlus$nextRevocationNotice = tick + 100;
        return true;
    }

    @Override
    public void forgottenRelicsPlus$restoreLife() {
        LivingEntity entity = (LivingEntity) (Object) this;
        float health = entity.getEntityData().get(DATA_HEALTH_ID);
        if (OmegaCoreProtection.needsHealthRepair(health)) entity.getEntityData().set(DATA_HEALTH_ID, 1.0F);
        dead = false;
        deathTime = 0;
        if (entity.getPose() == Pose.DYING) entity.setPose(Pose.STANDING);
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardSetHealth(float health, CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (OmegaCoreProtection.needsHealthRepair(health) && OmegaCoreProtection.protects(entity)) {
            entity.getEntityData().set(DATA_HEALTH_ID, 1.0F);
            forgottenRelicsPlus$restoreLife();
            callback.cancel();
        }
    }

    @ModifyReturnValue(method = "getHealth", at = @At("RETURN"))
    private float forgottenRelicsPlus$guardRawHealth(float health) {
        if (OmegaCoreProtection.needsHealthRepair(health)
                && OmegaCoreProtection.protects((LivingEntity) (Object) this)) {
            forgottenRelicsPlus$restoreLife();
            return 1.0F;
        }
        return health;
    }

    @ModifyReturnValue(method = "isAlive", at = @At("RETURN"))
    private boolean forgottenRelicsPlus$guardDeathFlag(boolean alive) {
        if ((!alive || dead || deathTime > 0)
                && OmegaCoreProtection.preventDeath((LivingEntity) (Object) this)) {
            return true;
        }
        return alive;
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardDeath(DamageSource source, CallbackInfo callback) {
        if (OmegaCoreProtection.preventDeath((LivingEntity) (Object) this)) callback.cancel();
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardDeathTick(CallbackInfo callback) {
        if (OmegaCoreProtection.preventDeath((LivingEntity) (Object) this)) callback.cancel();
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void forgottenRelicsPlus$guardDeathRemoval(Entity.RemovalReason reason, CallbackInfo callback) {
        if (OmegaCoreProtection.blocksRemoval(reason)
                && OmegaCoreProtection.preventDeath((LivingEntity) (Object) this)) callback.cancel();
    }
}
