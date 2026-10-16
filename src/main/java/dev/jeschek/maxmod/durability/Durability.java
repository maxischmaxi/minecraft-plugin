package dev.jeschek.maxmod.durability;

import dev.jeschek.maxmod.MaxModClient;
import dev.jeschek.maxmod.MaxModConfig;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

// shows the durability of held items and armor in the hud and in tooltips
public final class Durability {

    private final MaxModConfig config;

    public Durability(MaxModConfig config) {
        this.config = config;
    }

    public void register() {
        // before the chat so visible chat messages stay on top
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MaxModClient.MOD_ID, "durability"),
                new DurabilityHud(config));

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (config.durability.tooltip) {
                DurabilityTooltip.append(stack, flag, lines);
            }
        });
    }
}
