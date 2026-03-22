package com.github.noamm9.nvgrenderer.mixin;


import com.github.noamm9.nvgrenderer.batchers.NVGBatcher;
import com.github.noamm9.nvgrenderer.batchers.MultiPassBatcher;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderDeferredSubtitles()V"))
    private void onRenderEnd(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci, @Local GuiGraphics context) {
        NVGBatcher.endRenderHook(context, minecraft);
        MultiPassBatcher.endRenderHook(context, minecraft);
    }
}
