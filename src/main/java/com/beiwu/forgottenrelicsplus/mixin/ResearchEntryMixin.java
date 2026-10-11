package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.leclowndu93150.thaumaturge.content.research.ResearchEntry;
import com.mojang.serialization.DataResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResearchEntry.class, remap = false)
public abstract class ResearchEntryMixin {
    @ModifyArg(
            method = "lambda$static$5",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lcom/mojang/serialization/Codec;intRange(II)Lcom/mojang/serialization/Codec;"),
            index = 1)
    private static int forgottenRelicsPlus$complexityMaximum(int original) {
        return 8;
    }

    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void forgottenRelicsPlus$validateComplexity(
            ResearchEntry entry, CallbackInfoReturnable<DataResult<ResearchEntry>> callback) {
        boolean ours = entry.category()
                .unwrapKey()
                .map(key -> key.location().getNamespace().equals(ForgottenRelics.MOD_ID))
                .orElse(false);
        if (!ours && entry.complexity() > 3) {
            callback.setReturnValue(DataResult.error(() -> "Research complexity exceeds the upstream maximum of 3"));
        }
    }
}
