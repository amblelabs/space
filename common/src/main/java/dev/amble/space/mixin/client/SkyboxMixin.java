package dev.amble.space.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.amble.space.api.planet.Planet;
import dev.amble.space.api.planet.PlanetRegistry;
import dev.amble.space.client.renderer.space.SkyboxRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class SkyboxMixin {
    @Inject(method="renderSky", at=@At("HEAD"), cancellable = true)
    private void space$renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Planet planet = PlanetRegistry.INSTANCE.get(level);

        if (planet == null) return;

        // On non-overworld planetary dimensions, fully replace vanilla sky to suppress vanilla sun/moon.
        if (!level.dimension().equals(Level.OVERWORLD)) {
            float fade = space$solarSkyFade(planet, level, camera);
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(frustumMatrix);
            SkyboxRenderer.INSTANCE.drawSpace(poseStack, Tesselator.getInstance(), fade);

            ci.cancel();
            return;
        }

        float fade = space$solarSkyFade(planet, level, camera);

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(frustumMatrix);
        SkyboxRenderer.INSTANCE.drawSpace(poseStack, Tesselator.getInstance(), fade);
    }

    @Inject(method="renderClouds", at=@At("HEAD"),cancellable = true)
    private void space$renderClouds(CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Planet planet = PlanetRegistry.INSTANCE.get(level);

        if (planet == null) return;

        if (!planet.getClouds()) ci.cancel();
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true, require = 0)
    private void space$hideVanillaSunMoonOnPlanets(CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Planet planet = PlanetRegistry.INSTANCE.get(level);
        if (planet == null) return;

        if (!level.dimension().equals(Level.OVERWORLD)) {
            ci.cancel();
        }
    }

    private static float space$solarSkyFade(Planet planet, Level level, Camera camera) {
        float density = Mth.clamp(planet.getAtmosphereDensity(), 0.0f, 1.0f);
        // Very thin/no atmosphere: always show space sky.
        if (density < 0.05f) {
            return 1.0f;
        }

        if (space$isGroundBeneathVisible(level, camera, density)) {
            return 0.0f;
        }

        double altitude = camera.getPosition().y - level.getSeaLevel();
        float fadeStart = 16.0f;
        float fadeSpan = 256.0f + (density * 768.0f);
        return Mth.clamp((float) ((altitude - fadeStart) / fadeSpan), 0.0f, 1.0f);
    }

    private static boolean space$isGroundBeneathVisible(Level level, Camera camera, float density) {
        Vec3 start = camera.getPosition();
        double maxProbeDepth = 384.0 + (density * 1024.0);
        Vec3 end = start.subtract(0.0, maxProbeDepth, 0.0);

        HitResult hit = level.clip(new ClipContext(
            start,
            end,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            camera.getEntity()
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        double verticalDistanceToGround = start.y - hit.getLocation().y;
        double visibilityThreshold = 220.0 + (density * 580.0);
        return verticalDistanceToGround <= visibilityThreshold;
    }
}
