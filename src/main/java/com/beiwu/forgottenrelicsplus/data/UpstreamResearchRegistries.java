package com.beiwu.forgottenrelicsplus.data;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.research.CategoryBootstrap;
import net.minecraft.data.worldgen.BootstrapContext;

public final class UpstreamResearchRegistries {
    private UpstreamResearchRegistries() {}

    public static void aspects(BootstrapContext<IAspect> context) {
        AspectBootstrap.bootstrap(context);
    }

    public static void categories(BootstrapContext<IResearchCategory> context) {
        CategoryBootstrap.bootstrap(context);
        FRResearchBootstrap.categories(context);
    }
}
