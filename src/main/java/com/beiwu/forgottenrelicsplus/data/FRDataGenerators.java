package com.beiwu.forgottenrelicsplus.data;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.conditions.OmegaCoreEnabledCondition;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ForgottenRelics.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class FRDataGenerators {
    private FRDataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.createDatapackRegistryObjects(new RegistrySetBuilder()
                    .add(IAspect.REGISTRY_KEY, UpstreamResearchRegistries::aspects)
                    .add(IResearchCategory.REGISTRY_KEY, UpstreamResearchRegistries::categories)
                    .add(IResearchEntry.REGISTRY_KEY, FRResearchBootstrap::entries),
                    Map.of(ResourceKey.create(IResearchEntry.REGISTRY_KEY,
                                    ResourceLocation.fromNamespaceAndPath(ForgottenRelics.MOD_ID, "omega_core")),
                            List.of(new ModLoadedCondition("avaritia"), new OmegaCoreEnabledCondition())));
            event.createProvider(FRResearchNoteValidation::new);
        }
    }
}
