package dev.jeschek.maxmod.freecam;

import com.mojang.blaze3d.platform.InputConstants;
import dev.jeschek.maxmod.MaxModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// detaches the camera from the player so it can fly through the world.
// the player stays where it is, the camera overrides live in the mixins
public final class Freecam {

    private static final float SPRINT_MULTIPLIER = 2.5f;
    // share of the target velocity reached per tick, smooths starting and stopping
    private static final double ACCELERATION = 0.5;

    private final MaxModConfig config;
    private KeyMapping toggleKey;

    private boolean active;
    private @Nullable ClientLevel level;
    private CameraType previousCameraType = CameraType.FIRST_PERSON;
    private float previousHealth;
    private Vec3 position = Vec3.ZERO;
    private Vec3 previousPosition = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private float yRot;
    private float xRot;

    public Freecam(MaxModConfig config) {
        this.config = config;
    }

    public void register(KeyMapping.Category category) {
        toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.maxmod.freecam", InputConstants.KEY_F4, category));

        ClientTickEvents.START_CLIENT_TICK.register(this::blockPlayerActions);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> disable(client));
    }

    public boolean isActive() {
        return active;
    }

    public Vec3 position(float partialTicks) {
        return previousPosition.lerp(position, partialTicks);
    }

    public float yRot() {
        return yRot;
    }

    public float xRot() {
        return xRot;
    }

    // same math as Entity.turn, called with the mouse movement of each frame
    public void turn(double dx, double dy) {
        yRot += (float) dx * 0.15f;
        xRot = Mth.clamp(xRot + (float) dy * 0.15f, -90f, 90f);
    }

    // mouse wheel changes the speed while flying
    public void scroll(int direction, Minecraft client) {
        int speed = config.freecam.speed;
        int step = Math.max(1, Math.round(speed * 0.2f));
        config.freecam.speed = Math.clamp(speed + direction * step, FreecamConfig.MIN_SPEED, FreecamConfig.MAX_SPEED);
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable("maxmod.freecam.speed", config.freecam.speed));
        }
    }

    // a miss keeps the player from targeting blocks and hides the block outline
    public HitResult emptyHitResult() {
        return BlockHitResult.miss(position, Direction.UP, BlockPos.containing(position));
    }

    private void toggle(Minecraft client) {
        if (active) {
            disable(client);
        } else {
            enable(client);
        }
    }

    private void enable(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        Camera camera = client.gameRenderer.mainCamera();
        position = camera.position();
        previousPosition = position;
        velocity = Vec3.ZERO;
        yRot = camera.yRot();
        xRot = camera.xRot();
        level = client.level;
        previousHealth = client.player.getHealth();

        // third person renders the player and hides the hand and crosshair
        previousCameraType = client.options.getCameraType();
        client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        client.gameMode.stopDestroyBlock();
        active = true;
        client.player.sendOverlayMessage(Component.translatable("maxmod.freecam.on", config.freecam.speed));
    }

    private void disable(Minecraft client) {
        if (!active) {
            return;
        }
        active = false;
        level = null;
        client.options.setCameraType(previousCameraType);
        // keeps the speed changed with the mouse wheel
        config.save();
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable("maxmod.freecam.off"));
        }
    }

    // runs before vanilla handles the key bindings of this tick
    private void blockPlayerActions(Minecraft client) {
        if (!active) {
            return;
        }
        Options options = client.options;
        for (KeyMapping key : new KeyMapping[] {options.keyAttack, options.keyUse, options.keyPickItem}) {
            while (key.consumeClick()) {
                // drop the click
            }
            key.setDown(false);
        }
        // switching the perspective would bring back the hand and hide the player
        while (options.keyTogglePerspective.consumeClick()) {
            // drop the click
        }
    }

    private void onTick(Minecraft client) {
        while (toggleKey.consumeClick()) {
            toggle(client);
        }
        if (!active) {
            return;
        }

        if (shouldExit(client)) {
            disable(client);
            return;
        }
        previousHealth = client.player.getHealth();

        if (!client.isPaused()) {
            move(client.options);
        }
    }

    private boolean shouldExit(Minecraft client) {
        if (client.player == null || client.level != level || client.player.isDeadOrDying()) {
            return true;
        }
        return config.freecam.exitOnDamage && client.player.getHealth() < previousHealth;
    }

    // moves along the view direction, jump and sneak go straight up and down
    private void move(Options options) {
        double forward = impulse(options.keyUp, options.keyDown);
        double left = impulse(options.keyLeft, options.keyRight);
        double up = impulse(options.keyJump, options.keyShift);

        float yaw = yRot * Mth.DEG_TO_RAD;
        float pitch = xRot * Mth.DEG_TO_RAD;
        Vec3 look = new Vec3(-Mth.sin(yaw) * Mth.cos(pitch), -Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch));
        Vec3 side = new Vec3(Mth.cos(yaw), 0, Mth.sin(yaw));
        Vec3 direction = look.scale(forward).add(side.scale(left)).add(0, up, 0);

        double speed = config.freecam.speed / 20.0;
        if (options.keySprint.isDown()) {
            speed *= SPRINT_MULTIPLIER;
        }
        Vec3 target = direction.lengthSqr() > 0 ? direction.normalize().scale(speed) : Vec3.ZERO;
        velocity = velocity.lerp(target, ACCELERATION);

        previousPosition = position;
        position = position.add(velocity);
    }

    private static double impulse(KeyMapping positive, KeyMapping negative) {
        return (positive.isDown() ? 1 : 0) - (negative.isDown() ? 1 : 0);
    }
}
