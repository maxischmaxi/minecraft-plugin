package dev.jeschek.maxmod.durability;

import dev.jeschek.maxmod.MaxModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// lists armor and held items with their remaining durability in the bottom left corner
public final class DurabilityHud implements HudElement {

    // top to bottom, the main hand ends up in the lowest row
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND
    };
    private static final int MARGIN = 2;
    private static final int ROW_HEIGHT = 17;
    private static final int ICON_SIZE = 16;

    private final MaxModConfig config;

    public DurabilityHud(MaxModConfig config) {
        this.config = config;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (!config.durability.hud || player == null || player.isSpectator() || client.gui.hud.isHidden()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isDamageableItem()) {
                stacks.add(stack);
            }
        }

        int y = graphics.guiHeight() - MARGIN - ICON_SIZE - (stacks.size() - 1) * ROW_HEIGHT;
        for (ItemStack stack : stacks) {
            graphics.item(stack, MARGIN, y);
            graphics.text(client.font, DurabilityTooltip.remaining(stack), MARGIN + ICON_SIZE + 4,
                    y + (ICON_SIZE - client.font.lineHeight) / 2 + 1, ARGB.opaque(stack.getBarColor()));
            y += ROW_HEIGHT;
        }
    }
}
