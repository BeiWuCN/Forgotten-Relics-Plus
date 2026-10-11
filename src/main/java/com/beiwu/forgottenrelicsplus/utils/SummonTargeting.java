package com.beiwu.forgottenrelicsplus.utils;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityThaumaturgeBoss;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

public final class SummonTargeting {
    private static final TagKey<EntityType<?>> ELITES = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forgotten_relics_plus", "elite_targets"));

    private SummonTargeting() {}

    public static boolean isEnemy(LivingEntity candidate, LivingEntity owner) {
        if (candidate == owner || !candidate.isAlive() || candidate.isSpectator() || !candidate.isAttackable()) {
            return false;
        }
        if (owner != null && (owner.isAlliedTo(candidate)
                || candidate instanceof TamableAnimal tame && owner.getUUID().equals(tame.getOwnerUUID()))) {
            return false;
        }
        return candidate instanceof Enemy || isBoss(candidate)
                || candidate instanceof Mob mob && owner != null && mob.getTarget() == owner;
    }

    private static boolean isBoss(LivingEntity candidate) {
        return candidate instanceof EnderDragon || candidate instanceof WitherBoss
                || candidate instanceof EntityThaumaturgeBoss || candidate.getType().is(Tags.EntityTypes.BOSSES);
    }

    private static int tier(LivingEntity candidate) {
        if (isBoss(candidate)) return 2;
        return candidate.getType().is(ELITES) || MobTraits.isChampion(candidate) ? 1 : 0;
    }

    public static int priority(boolean aimed, boolean forward, int tier) {
        return aimed ? 100 : (forward ? 10 : 0) + tier;
    }

    public static LivingEntity select(Entity source, LivingEntity owner, double range, LivingEntity current) {
        List<LivingEntity> candidates = source.level().getEntitiesOfClass(LivingEntity.class,
                source.getBoundingBox().inflate(range), candidate -> isEnemy(candidate, owner));
        Vec3 eye = owner == null ? source.position() : owner.getEyePosition();
        Vec3 look = owner == null ? source.getDeltaMovement().normalize() : owner.getLookAngle();
        double aimRange = owner == null ? range : Math.min(128.0D, range + owner.distanceTo(source));
        Vec3 end = eye.add(look.scale(aimRange));
        end = source.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, owner == null ? source : owner)).getLocation();
        LivingEntity best = null;
        int bestPriority = -1;
        double bestCost = Double.POSITIVE_INFINITY;
        for (LivingEntity candidate : candidates) {
            if (source.distanceToSqr(candidate) > range * range) continue;
            Vec3 direction = candidate.getBoundingBox().getCenter().subtract(eye);
            double distance = direction.length();
            double alignment = distance < 1.0E-6D ? 1.0D : direction.dot(look) / distance;
            var intersection = candidate.getBoundingBox().inflate(0.25D).clip(eye, end);
            boolean aimed = candidate.getBoundingBox().contains(eye) || intersection.isPresent();
            int priority = priority(aimed, alignment >= 0.5D, tier(candidate));
            double cost = aimed ? intersection.map(eye::distanceToSqr).orElse(0.0D)
                    : (1.0D - alignment) * range * range + distance * distance * 0.25D
                            + source.distanceToSqr(candidate) * 0.1D;
            if (!aimed && candidate == current) cost *= 0.85D;
            if (priority < bestPriority || priority == bestPriority && cost >= bestCost) continue;
            if (!candidate.hasLineOfSight(source) || owner != null && !owner.hasLineOfSight(candidate)) continue;
            best = candidate;
            bestPriority = priority;
            bestCost = cost;
        }
        return best;
    }
}
