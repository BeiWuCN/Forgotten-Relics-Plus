package com.beiwu.forgottenrelics_plus;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRArmorMaterials;
import com.beiwu.forgottenrelics_plus.registry.FRCreativeTabs;
import com.beiwu.forgottenrelics_plus.registry.FREntities;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.registry.FRParticleTypes;
import com.beiwu.forgottenrelics_plus.registry.FRRecipeSerializers;
import com.beiwu.forgottenrelics_plus.registry.FRSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 失落遗物学（Forgotten Relics）非官方 1.21.1 移植版的主类。
 *
 * <p>1.12.2 原版的入口是 {@code com.beiwu.forgottenrelics_plus.Main}，使用 Forge 的
 * {@code @Mod} 加 {@code @Mod.EventHandler} 三段式生命周期（preInit / init / postInit），
 * 物品则在 {@code @Mod.EventBusSubscriber} 里监听 {@code RegistryEvent.Register<Item>} 注册。
 *
 * <p>1.21.1 的 NeoForge 把这一切收敛成了「构造器 + DeferredRegister」：注册表在类加载时声明，
 * 由 {@code IEventBus} 在合适的时机统一提交。因此这里不再有 preInit/init/postInit，
 * 各个注册表分别放在 {@code registry} 包下。
 */
@Mod(ForgottenRelics.MOD_ID)
public final class ForgottenRelics {

    /**
     * 模组 ID，也就是资源与数据包的命名空间。
     *
     * <p>1.12.2 原版与移植版用的都是 {@code forgotten_relics}；本项目更名后改为
     * {@code forgotten_relics_plus}，让注册 ID 与项目名（Forgotten Relics +）、
     * 显示名（Forgotten Relics Plus）一致。物品的注册键（{@code mining_charm} 等）没有变，
     * 变的是命名空间这一层。
     */
    public static final String MOD_ID = "forgotten_relics_plus";

    /** 模组名称。 */
    public static final String MOD_NAME = "Forgotten Relics Plus";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public ForgottenRelics(IEventBus modBus, ModContainer container) {
        // 注册表挂到模组事件总线上
        FRItems.register(modBus);
        FREntities.register(modBus);
        FRArmorMaterials.register(modBus);
        FRDataComponents.register(modBus);
        // 闪电弧的粒子类型（1.7.10 LightningMessage 的现代替身，见 FRBoltParticleData）。
        FRParticleTypes.register(modBus);
        FRRecipeSerializers.register(modBus);
        FRCreativeTabs.register(modBus);
        // 模组自带的音效（1.7.10 assets/forgottenrelics/sounds.json 的 4 个可用条目）。
        FRSounds.register(modBus);

        // 配置文件：与原版 1.12.2 的 RelicsConfigHandler 一一对应
        container.registerConfig(ModConfig.Type.COMMON, FRConfig.SPEC);

        modBus.addListener(this::onCommonSetup);

        LOGGER.info("失落遗物学（非官方）正在加载……");
    }

    /**
     * 通用初始化：报告本模组注册了多少件物品。
     *
     * <p>物品注册表在 common setup 之前就已经填好，所以这里能安全地数。
     * 这条日志是给「迁移进度」做硬校验用的——加了新物品却在日志里看不到数量上涨，
     * 就说明 DeferredRegister 没登记上，比只看编译通过可靠。
     */
    private void onCommonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        long count = net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().stream()
                .filter(key -> key.getNamespace().equals(MOD_ID))
                .count();
        LOGGER.info("失落遗物学：已注册 {} 件物品", count);
    }
}
