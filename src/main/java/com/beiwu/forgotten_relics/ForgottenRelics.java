package com.beiwu.forgottenrelics_re;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.registry.FRCreativeTabs;
import com.beiwu.forgottenrelics_re.registry.FRItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 失落遗物学（Forgotten Relics）非官方 1.21.1 移植版的主类。
 *
 * <p>1.12.2 原版的入口是 {@code com.beiwu.forgottenrelics_re.Main}，使用 Forge 的
 * {@code @Mod} 加 {@code @Mod.EventHandler} 三段式生命周期（preInit / init / postInit），
 * 物品则在 {@code @Mod.EventBusSubscriber} 里监听 {@code RegistryEvent.Register<Item>} 注册。
 *
 * <p>1.21.1 的 NeoForge 把这一切收敛成了「构造器 + DeferredRegister」：注册表在类加载时声明，
 * 由 {@code IEventBus} 在合适的时机统一提交。因此这里不再有 preInit/init/postInit，
 * 各个注册表分别放在 {@code registry} 包下。
 */
@Mod(ForgottenRelics.MOD_ID)
public final class ForgottenRelics {

    /** 模组 ID。沿用 1.12.2 原版的 forgotten_relics，使贴图、模型、语言键、研究路径可直接复用。 */
    public static final String MOD_ID = "forgotten_relics";

    /** 模组名称。 */
    public static final String MOD_NAME = "Forgotten Relics: Unofficial";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public ForgottenRelics(IEventBus modBus, ModContainer container) {
        // 注册表挂到模组事件总线上
        FRItems.register(modBus);
        FRCreativeTabs.register(modBus);

        // 配置文件：与原版 1.12.2 的 RelicsConfigHandler 一一对应
        container.registerConfig(ModConfig.Type.COMMON, FRConfig.SPEC);

        LOGGER.info("失落遗物学（非官方）正在加载……");
    }
}
