package dev.jeschek.xray;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class XraySettingsScreen extends OptionsSubScreen {

    private final XrayConfig config;

    public XraySettingsScreen(Screen parent, XrayConfig config) {
        super(parent, Minecraft.getInstance().options, Component.translatable("xray.options.title"));
        this.config = config;
    }

    @Override
    protected void addOptions() {
        if (list == null) {
            return;
        }

        list.addBig(new OptionInstance<>(
                "xray.options.intensity",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.translatable("options.percent_value", caption, value),
                new OptionInstance.IntRange(XrayConfig.MIN_INTENSITY, XrayConfig.MAX_INTENSITY),
                config.intensity,
                value -> config.intensity = value));

        list.addBig(new OptionInstance<>(
                "xray.options.radius",
                OptionInstance.noTooltip(),
                Options::genericValueLabel,
                new OptionInstance.IntRange(XrayConfig.MIN_RADIUS, XrayConfig.MAX_RADIUS),
                config.radius,
                value -> config.radius = value));

        list.addHeader(Component.translatable("xray.options.ores"));
        OptionInstance<?>[] toggles = new OptionInstance<?>[OreType.values().length];
        for (OreType type : OreType.values()) {
            toggles[type.ordinal()] = OptionInstance.createBoolean(
                    type.translationKey(),
                    config.isEnabled(type),
                    value -> config.setEnabled(type, value));
        }
        // two toggles per row
        list.addSmall(toggles);
    }

    @Override
    public void removed() {
        super.removed();
        config.save();
    }
}
