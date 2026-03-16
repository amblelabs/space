package dev.amble.space.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.amble.space.api.planet.Planet;
import dev.amble.space.client.renderer.space.AtmosphereRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void space$modifyFogColor(
            Camera camera,
            float partialTick,
            ClientLevel level,
            int renderDistanceChunks,
            float darkenWorldAmount,
            CallbackInfo ci
    ) {
        Planet planet = AtmosphereRenderer.INSTANCE.currentPlanet();
        if (planet == null || !planet.getHasAtmosphereGlow()) return;

        Vector3f color = planet.getAtmosphereColor();
        float density = Math.min(Math.max(planet.getAtmosphereDensity(), 0f), 1f);

        float[] existing = RenderSystem.getShaderFogColor();
        float r = Mth.lerp(existing[0], color.x(), density);
        float g = Mth.lerp(existing[1], color.y(), density);
        float b = Mth.lerp(existing[2], color.z(), density);

        RenderSystem.setShaderFogColor(r, g, b, existing[3]);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void space$modifyFogRange(
            Camera camera,
            FogRenderer.FogMode fogMode,
            float maxDistance,
            boolean useBlindness,
            float partialTick,
            CallbackInfo ci
    ) {
        Planet planet = AtmosphereRenderer.INSTANCE.currentPlanet();
        if (planet == null || !planet.getHasAtmosphereGlow()) return;

        float density = Math.min(Math.max(planet.getAtmosphereDensity(), 0.001f), 1f);
        float start = Math.min(Math.max(50f / density, 50f), 10000f);
        float end   = Math.min(Math.max(200f / density, 200f), 20000f);

        RenderSystem.setShaderFogStart(start);
        RenderSystem.setShaderFogEnd(end);
    }
}