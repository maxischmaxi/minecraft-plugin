package dev.jeschek.maxmod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.jeschek.maxmod.MaxModClient;
import dev.jeschek.maxmod.freecam.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// sends mouse movement and the mouse wheel to the freecam instead of the player
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void maxmod$turnFreecam(LocalPlayer player, double dx, double dy, Operation<Void> original) {
        Freecam freecam = MaxModClient.freecam();
        if (freecam.isActive()) {
            freecam.turn(dx, dy);
        } else {
            original.call(player, dx, dy);
        }
    }

    // a zero result makes vanilla skip the hotbar scrolling
    @WrapOperation(method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"))
    private Vector2i maxmod$scrollFreecam(ScrollWheelHandler handler, double x, double y, Operation<Vector2i> original) {
        Vector2i wheel = original.call(handler, x, y);
        Freecam freecam = MaxModClient.freecam();
        if (!freecam.isActive()) {
            return wheel;
        }
        if (wheel.y != 0) {
            freecam.scroll(Integer.signum(wheel.y), minecraft);
        }
        return new Vector2i();
    }
}
