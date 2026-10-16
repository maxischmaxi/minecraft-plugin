package dev.jeschek.maxmod;

import dev.jeschek.maxmod.durability.DurabilityConfig;
import dev.jeschek.maxmod.freecam.FreecamConfig;
import dev.jeschek.maxmod.xray.OreType;
import dev.jeschek.maxmod.xray.XrayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class MaxModSettingsScreen extends OptionsSubScreen {

    private final MaxModConfig config;

    public MaxModSettingsScreen(Screen parent, MaxModConfig config) {
        super(parent, Minecraft.getInstance().options, Component.translatable("maxmod.options.title"));
        this.config = config;
    }

    @Override
    protected void addOptions() {
        if (list == null) {
            return;
        }
        XrayConfig xray = config.xray;

        list.addHeader(Component.translatable("maxmod.options.xray"));

        list.addBig(new OptionInstance<>(
                "maxmod.options.xray.intensity",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.translatable("options.percent_value", caption, value),
                new OptionInstance.IntRange(XrayConfig.MIN_INTENSITY, XrayConfig.MAX_INTENSITY),
                xray.intensity,
                value -> xray.intensity = value));

        list.addBig(new OptionInstance<>(
                "maxmod.options.xray.radius",
                OptionInstance.noTooltip(),
                Options::genericValueLabel,
                new OptionInstance.IntRange(XrayConfig.MIN_RADIUS, XrayConfig.MAX_RADIUS),
                xray.radius,
                value -> xray.radius = value));

        list.addHeader(Component.translatable("maxmod.options.xray.ores"));
        OptionInstance<?>[] toggles = new OptionInstance<?>[OreType.values().length];
        for (OreType type : OreType.values()) {
            toggles[type.ordinal()] = OptionInstance.createBoolean(
                    type.translationKey(),
                    xray.isEnabled(type),
                    value -> xray.setEnabled(type, value));
        }
        // two toggles per row
        list.addSmall(toggles);

        DurabilityConfig durability = config.durability;
        list.addHeader(Component.translatable("maxmod.options.durability"));
        list.addSmall(
                OptionInstance.createBoolean("maxmod.options.durability.hud", durability.hud,
                        value -> durability.hud = value),
                OptionInstance.createBoolean("maxmod.options.durability.tooltip", durability.tooltip,
                        value -> durability.tooltip = value));

        FreecamConfig freecam = config.freecam;
        list.addHeader(Component.translatable("maxmod.options.freecam"));
        list.addBig(new OptionInstance<>(
                "maxmod.options.freecam.speed",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.translatable("maxmod.options.freecam.speed_value", caption, value),
                new OptionInstance.IntRange(FreecamConfig.MIN_SPEED, FreecamConfig.MAX_SPEED),
                freecam.speed,
                value -> freecam.speed = value));
        list.addBig(OptionInstance.createBoolean("maxmod.options.freecam.exit_on_damage", freecam.exitOnDamage,
                value -> freecam.exitOnDamage = value));
    }

    @Override
    public void removed() {
        super.removed();
        config.save();
    }
}
