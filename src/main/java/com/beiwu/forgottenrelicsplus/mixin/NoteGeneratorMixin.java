package com.beiwu.forgottenrelicsplus.mixin;

import com.beiwu.forgottenrelicsplus.ForgottenRelics;
import com.beiwu.forgottenrelicsplus.research.FRResearchNoteGenerator;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NoteGenerator.class, remap = false)
public abstract class NoteGeneratorMixin {
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private static void forgottenRelicsPlus$generate(
            ResourceLocation entry,
            int index,
            AspectList anchors,
            int complexity,
            RandomSource random,
            CallbackInfoReturnable<ResearchNoteData> callback) {
        if (entry.getNamespace().equals(ForgottenRelics.MOD_ID) && complexity >= 2) {
            callback.setReturnValue(FRResearchNoteGenerator.generate(entry, index, anchors, complexity, random));
        }
    }
}
