package dev.jeschek.maxmod.mixin;

import dev.jeschek.maxmod.MaxModClient;
import dev.jeschek.maxmod.freecam.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// moves the camera to the freecam position
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract BlockPos blockPosition();

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void maxmod$applyFreecam(float partialTicks, CallbackInfo ci) {
        Freecam freecam = MaxModClient.freecam();
        if (freecam.isActive()) {
            setRotation(freecam.yRot(), freecam.xRot());
            setPosition(freecam.position(partialTicks));
        }
    }

    // like vanilla does for spectators: occlusion culling hides everything while the camera is inside a block
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void maxmod$disableCullingInBlocks(CameraRenderState cameraState, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (MaxModClient.freecam().isActive() && client.level != null
                && client.level.getBlockState(blockPosition()).isSolidRender()) {
            cameraState.smartCull = false;
        }
    }
}
