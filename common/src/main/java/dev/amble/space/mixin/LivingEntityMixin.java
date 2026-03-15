package dev.amble.space.mixin;

import dev.amble.space.api.planet.Planet;
import dev.amble.space.api.planet.PlanetRegistry;
import dev.amble.space.common.item.spacesuit.SpaceSuitItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method="tick", at = @At("HEAD"))
    public void space$tick(CallbackInfo ci) {
        Planet planet = PlanetRegistry.INSTANCE.get(this.level());

        if (planet == null || !planet.getFreezing()) return;
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.isSpectator())
            return;

        if (entity instanceof Player player && SpaceSuitItem.checkFullSpaceSuit(player)) {
            entity.setIsInPowderSnow(false);
            if (entity.getTicksFrozen() > 0) entity.setTicksFrozen(entity.getTicksFrozen() - 5);
            return;
        }

        if (entity.getType() == EntityType.BOAT || entity.getType() == EntityType.CHEST_BOAT)
            return;

        entity.setIsInPowderSnow(true);
        entity.setTicksFrozen(entity.getTicksFrozen() + 5);
    }

    @Inject(method="getDefaultGravity", at=@At("HEAD"), cancellable = true)
    private void space$getDefaultGravity(CallbackInfoReturnable<Double> cir) {
        Planet planet = PlanetRegistry.INSTANCE.get(this.level());

        if (planet == null || !planet.getModifiesGravity()) return;
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.isSwimming() || entity.isNoGravity() || entity.isSpectator())
            return;

        if (entity instanceof Player player && player.getAbilities().flying)
            return;

        if (entity.getType() == EntityType.BOAT || entity.getType() == EntityType.CHEST_BOAT)
            return;

        cir.setReturnValue((double) planet.getGameGravity());
    }

    @Inject(method="causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void space$causeFallDamage(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Planet planet = PlanetRegistry.INSTANCE.get(this.level());
        if (planet != null && planet.getNoFallDamage()) {
            cir.setReturnValue(false);
            return;
        }
    }
}
