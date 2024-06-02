package com.igrium.videolib.mixins;

import com.igrium.videolib.VideoLib;
import com.igrium.videolib.config.VideoLibConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin extends Overlay {
    @Shadow @Final private ResourceReload reload;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void vl$showToast(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.reload.isComplete() && !VideoLib.getInstance().getVideoManager().hasNatives() && !VideoLib.getInstance().missingNativesWarningShown && VideoLibConfig.showMissingNativesToast) {
            client.getToastManager().add(SystemToast.create(client, SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("videolib.missing_natives.toast.title"),
                    Text.translatable("videolib.missing_natives.toast.description")));
            VideoLib.getInstance().missingNativesWarningShown = true;
        }
    }
}
