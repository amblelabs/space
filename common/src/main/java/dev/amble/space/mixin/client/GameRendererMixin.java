package dev.amble.space.mixin.client;

import dev.amble.space.client.renderer.space.PlanetRenderContext;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getDepthFar", at = @At("RETURN"), cancellable = true)
    private void space$extendDepthFarForPlanets(CallbackInfoReturnable<Float> cir) {
        if (!PlanetRenderContext.getExtendingFarPlane()) {
            return;
        }

        float currentFar = cir.getReturnValueF();
        cir.setReturnValue(currentFar * 40.0f);
    }
}



