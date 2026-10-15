package dev.jeschek.xray;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class XrayClient implements ClientModInitializer {

    private static final int SCAN_INTERVAL_TICKS = 10;

    private static XrayConfig config;

    private boolean enabled;
    private int ticksUntilScan;
    private List<OreScanner.Hit> ores = List.of();
    private KeyMapping toggleKey;

    public static XrayConfig config() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        config = XrayConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("xray", "xray"));
        toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.xray.toggle", InputConstants.KEY_X, category));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> setEnabled(false));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
                ClientCommands.literal("xray")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(toggle());
                            return 1;
                        })
                        .then(ClientCommands.literal("radius")
                                .then(ClientCommands.argument("blocks",
                                                IntegerArgumentType.integer(XrayConfig.MIN_RADIUS, XrayConfig.MAX_RADIUS))
                                        .executes(ctx -> {
                                            config.radius = IntegerArgumentType.getInteger(ctx, "blocks");
                                            config.save();
                                            setEnabled(true);
                                            ctx.getSource().sendFeedback(status());
                                            return 1;
                                        })))
                        .then(ClientCommands.literal("settings")
                                .executes(ctx -> {
                                    Minecraft client = ctx.getSource().getClient();
                                    // the chat screen closes after the command, so open the settings afterwards
                                    client.schedule(() -> client.gui.setScreen(new XraySettingsScreen(null, config)));
                                    return 1;
                                }))));
    }

    private void onTick(Minecraft client) {
        while (toggleKey.consumeClick()) {
            if (client.player != null) {
                client.player.sendOverlayMessage(toggle());
            }
        }

        if (!enabled || client.level == null || client.player == null) {
            return;
        }

        if (--ticksUntilScan <= 0) {
            ticksUntilScan = SCAN_INTERVAL_TICKS;
            ores = OreScanner.scan(client.level, client.player.blockPosition(), config.radius, config.enabledOres());
        }

        // gizmos only live for one tick, so they are re-emitted every tick
        OreRenderer.draw(ores, config.intensity);
    }

    private Component toggle() {
        setEnabled(!enabled);
        return status();
    }

    private void setEnabled(boolean value) {
        enabled = value;
        ores = List.of();
        ticksUntilScan = 0;
    }

    private Component status() {
        return enabled
                ? Component.literal("X-ray on, radius " + config.radius)
                : Component.literal("X-ray off");
    }
}
