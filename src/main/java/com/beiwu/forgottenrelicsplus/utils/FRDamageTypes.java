package com.beiwu.forgottenrelicsplus.utils;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;

public final class FRDamageTypes {

    public static final ResourceKey<DamageType> OBLIVION = key("oblivion");

    public static final ResourceKey<DamageType> SUPERPOSITION = key("superposition");

    public static final ResourceKey<DamageType> SUPERPOSITION_DEFINED = key("superposition_defined");

    public static final ResourceKey<DamageType> SOUL_DRAIN = key("soul_drain");

    public static final ResourceKey<DamageType> TRUE_DAMAGE = key("true_damage");

    public static final ResourceKey<DamageType> TRUE_DAMAGE_UNDEF = key("true_damage_undef");

    public static final ResourceKey<DamageType> TRUE_LIGHTNING = key("true_lightning");

    public static final ResourceKey<DamageType> DARK_MATTER = key("dark_matter");

    public static final ResourceKey<DamageType> FORGOTTEN_MAGIC = key("forgotten_magic");

    public static final ResourceKey<DamageType> FATE = key("fate");

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, path));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().holderOrThrow(type));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, net.minecraft.world.entity.Entity owner) {
        return new DamageSource(level.registryAccess().holderOrThrow(type), owner);
    }

    public static boolean isAbsolute(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.STARVE)
                || source.is(OBLIVION)
                || source.is(SUPERPOSITION)
                || source.is(SUPERPOSITION_DEFINED)
                || source.is(SOUL_DRAIN)
                || source.is(TRUE_DAMAGE)
                || source.is(TRUE_DAMAGE_UNDEF)
                || source.is(FATE);
    }

    private FRDamageTypes() {
    }
}
