package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.beiwu.forgottenrelics_plus.entity.EntityThunderpealOrb;
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
 * <p>1.12.2 原版把实体挂在 {@code EntityRegistry.registerModEntity(...)} 上，1.21.1 换成
 * {@link DeferredRegister}。尺寸一律取 0.25×0.25：这些弹射物在 1.7.10 里就是「零尺寸 + 目标判定」，
 * 碰撞由 {@code onHit} 自己处理，不靠实体体积。
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
                            .updateInterval(10));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(
            String name, java.util.function.Supplier<EntityType.Builder<T>> builder) {
        return ENTITIES.register(name, () -> builder.get().build(name));
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    private FREntities() {
    }
}
