package com.beiwu.forgottenrelicsplus.registry;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.entity.EntityBabylonWeapon;
import com.beiwu.forgottenrelicsplus.entity.EntityChaoticOrb;
import com.beiwu.forgottenrelicsplus.entity.EntityCrimsonOrb;
import com.beiwu.forgottenrelicsplus.entity.EntityDarkMatterOrb;
import com.beiwu.forgottenrelicsplus.entity.EntityLunarFlare;
import com.beiwu.forgottenrelicsplus.entity.EntityRageousMissile;
import com.beiwu.forgottenrelicsplus.entity.EntityShinyEnergy;
import com.beiwu.forgottenrelicsplus.entity.EntitySoulEnergy;
import com.beiwu.forgottenrelicsplus.entity.EntityThunderpealOrb;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FREntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ForgottenRelics.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityThunderpealOrb>> THUNDERPEAL_ORB =
            register("thunderpeal_orb",
                    () -> EntityType.Builder.<EntityThunderpealOrb>of(EntityThunderpealOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityDarkMatterOrb>> DARK_MATTER_ORB =
            register("dark_matter_orb",
                    () -> EntityType.Builder.<EntityDarkMatterOrb>of(EntityDarkMatterOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCrimsonOrb>> CRIMSON_ORB =
            register("crimson_orb",
                    () -> EntityType.Builder.<EntityCrimsonOrb>of(EntityCrimsonOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityChaoticOrb>> PRIMAL_ORB =
            register("primal_orb",
                    () -> EntityType.Builder.<EntityChaoticOrb>of(EntityChaoticOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRageousMissile>> RAGEOUS_MISSILE =
            register("rageous_missile",
                    () -> EntityType.Builder.<EntityRageousMissile>of(EntityRageousMissile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityLunarFlare>> LUNAR_FLARE =
            register("lunar_flare",
                    () -> EntityType.Builder.<EntityLunarFlare>of(EntityLunarFlare::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBabylonWeapon>> BABYLON_WEAPON =
            register("babylon_weapon",
                    () -> EntityType.Builder.<EntityBabylonWeapon>of(EntityBabylonWeapon::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityShinyEnergy>> SHINY_ENERGY =
            register("shiny_energy",
                    () -> EntityType.Builder.<EntityShinyEnergy>of(EntityShinyEnergy::new, MobCategory.MISC)

                            .sized(0.1F, 0.1F)

                            .clientTrackingRange(8)
                            .updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySoulEnergy>> SOUL_ENERGY =
            register("soul_energy",
                    () -> EntityType.Builder.<EntitySoulEnergy>of(EntitySoulEnergy::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(
            String name, Supplier<EntityType.Builder<T>> builder) {
        return ENTITIES.register(name, () -> builder.get().build(name));
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    private FREntities() {
    }
}
