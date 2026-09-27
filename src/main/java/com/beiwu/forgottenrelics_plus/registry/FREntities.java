package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.entity.EntityBabylonWeapon;
import com.beiwu.forgottenrelics_plus.entity.EntityChaoticOrb;
import com.beiwu.forgottenrelics_plus.entity.EntityCrimsonOrb;
import com.beiwu.forgottenrelics_plus.entity.EntityDarkMatterOrb;
import com.beiwu.forgottenrelics_plus.entity.EntityLunarFlare;
import com.beiwu.forgottenrelics_plus.entity.EntityRageousMissile;
import com.beiwu.forgottenrelics_plus.entity.EntityShinyEnergy;
import com.beiwu.forgottenrelics_plus.entity.EntitySoulEnergy;
import com.beiwu.forgottenrelics_plus.entity.EntityThunderpealOrb;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册表。
 *
 * <p>RE 把实体挂在 {@code EntityRegistry.registerModEntity(...)} 上，1.21.1 换成
 * {@link DeferredRegister}。尺寸一律取 0.25×0.25：这些弹射物在 1.7.10 里就是「零尺寸 + 目标判定」，
 * 碰撞由 {@code onHit} 自己处理，不靠实体体积。
 *
 * <p><b>updateInterval 取 5</b>（服务器友好）：这些弹射物的运动在两端都算
 * （{@code FRHomingProjectile#applyHoming} 与 {@code EntityRageousMissile#tick}），
 * 客户端能自己推出接近一致的轨迹，位置包只剩纠偏作用，没有必要每 tick 都发。间隔不要再压到 1：
 * 那是为「客户端完全不参与运动、只被动跟随位置包」准备的，画面会一顿一顿。取 5 之后位置包降到 1/5，
 * 纠偏间隔仍明显密于原版投射物（原版普遍 10~20）。
 * 唯一例外是 {@link EntityShinyEnergy}：两端各自确定性推算，连纠偏都不需要，保持 10。
 */
public final class FREntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ForgottenRelics.MOD_ID);

    /** 霹雳咒书的雷电球（原版 {@code EntityThunderpealOrb}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityThunderpealOrb>> THUNDERPEAL_ORB =
            register("thunderpeal_orb",
                    () -> EntityType.Builder.<EntityThunderpealOrb>of(EntityThunderpealOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 邪术之咒的暗物质法球（原版 {@code EntityDarkMatterOrb}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityDarkMatterOrb>> DARK_MATTER_ORB =
            register("dark_matter_orb",
                    () -> EntityType.Builder.<EntityDarkMatterOrb>of(EntityDarkMatterOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 腥红之咒的猩红法球（原版 {@code EntityCrimsonOrb}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityCrimsonOrb>> CRIMSON_ORB =
            register("crimson_orb",
                    () -> EntityType.Builder.<EntityCrimsonOrb>of(EntityCrimsonOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 原初混沌之典的原初能量法球（原版 {@code EntityChaoticOrb}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityChaoticOrb>> PRIMAL_ORB =
            register("primal_orb",
                    () -> EntityType.Builder.<EntityChaoticOrb>of(EntityChaoticOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 核子之怒的追踪导弹（原版 {@code EntityRageousMissile}，注册名沿用 RE 的 {@code rageous_missile}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityRageousMissile>> RAGEOUS_MISSILE =
            register("rageous_missile",
                    () -> EntityType.Builder.<EntityRageousMissile>of(EntityRageousMissile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 月耀咒书的耀月之辉（原版 {@code EntityLunarFlare}，注册名沿用 lang 里现成的 {@code lunar_flare}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityLunarFlare>> LUNAR_FLARE =
            register("lunar_flare",
                    () -> EntityType.Builder.<EntityLunarFlare>of(EntityLunarFlare::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 神化召唤的巴比伦武器（原版 {@code EntityBabylonWeaponSS}）。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBabylonWeapon>> BABYLON_WEAPON =
            register("babylon_weapon",
                    () -> EntityType.Builder.<EntityBabylonWeapon>of(EntityBabylonWeapon::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(5));

    /** 日耀石的能量体（原版 {@code EntityShinyEnergy}）：出生点与速度都由物品侧给，目标用同步数据下发。 */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityShinyEnergy>> SHINY_ENERGY =
            register("shiny_energy",
                    () -> EntityType.Builder.<EntityShinyEnergy>of(EntityShinyEnergy::new, MobCategory.MISC)
                            // 原版 setSize(0,0)；这里给 0.1 只是避免零尺寸包围盒的边界情况，它不参与任何碰撞。
                            .sized(0.1F, 0.1F)
                            // 客户端会照同步过来的目标自己算 sparkle 尺寸并本地移动，不需要每 tick 校正位置。
                            .clientTrackingRange(8)
                            .updateInterval(10));

    /** 千咒之诫的灵魂能量（原版 {@code EntitySoulEnergy}）。 */
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
