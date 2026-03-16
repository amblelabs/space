package dev.amble.space.mixin.client;

import dev.amble.space.api.planet.PlanetRegistry;
import dev.amble.space.common.lib.SpaceSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class PlanetMusicMixin {
    @Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
    private void space$replaceQueuedMusicOnPlanets(CallbackInfoReturnable<Music> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        Level level = minecraft.level;
        if (level == null || PlanetRegistry.INSTANCE.get(level) == null) {
            return;
        }

        SoundEvent randomTrack = SpaceSounds.randomSpaceMusic();
        Music replacement = new Music(Holder.direct(randomTrack), 12000, 24000, false);
        cir.setReturnValue(replacement);
    }
}
