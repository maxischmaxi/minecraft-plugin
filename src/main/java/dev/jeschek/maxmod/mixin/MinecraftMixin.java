package dev.jeschek.maxmod.mixin;

import dev.jeschek.maxmod.MaxModClient;
import dev.jeschek.maxmod.freecam.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// the player targets nothing while in freecam
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public @Nullable HitResult hitResult;

    @Shadow
    public @Nullable Entity crosshairPickEntity;

    @Inject(method = "pick", at = @At("TAIL"))
    private void maxmod$clearTarget(float partialTicks, CallbackInfo ci) {
        Freecam freecam = MaxModClient.freecam();
        if (freecam.isActive()) {
            hitResult = freecam.emptyHitResult();
            crosshairPickEntity = null;
        }
    }
}
