package dev.jeschek.maxmod.sort;

import com.mojang.blaze3d.platform.InputConstants;
import dev.jeschek.maxmod.mixin.AbstractContainerScreenAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;

// sorts the inventory section under the mouse when the sort key is pressed
public final class InventorySorting {

    private KeyMapping sortKey;

    public void register(KeyMapping.Category category) {
        sortKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.maxmod.sort", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE, category));

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            // the creative inventory uses its own fake slots, sorting there would break
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)
                    || screen instanceof CreativeModeInventoryScreen) {
                return;
            }
            ScreenMouseEvents.allowMouseClick(screen).register((s, event) ->
                    !(sortKey.matchesMouse(event) && trySort(client, containerScreen)));
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) ->
                    !(sortKey.matches(event) && trySort(client, containerScreen)));
        });

        // the key is only used inside screens, drop presses made in the world
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sortKey.consumeClick()) {
                // nothing to do
            }
        });
    }

    // returns true when the input was used for sorting
    private boolean trySort(Minecraft client, AbstractContainerScreen<?> screen) {
        Slot hovered = ((AbstractContainerScreenAccessor) screen).maxmod$getHoveredSlot();
        if (hovered == null || client.player == null || client.gameMode == null) {
            return false;
        }
        return InventorySorter.sort(client.gameMode, client.player, screen.getMenu(), hovered);
    }
}
