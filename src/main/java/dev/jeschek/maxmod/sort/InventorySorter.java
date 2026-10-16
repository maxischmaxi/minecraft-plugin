package dev.jeschek.maxmod.sort;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// sorts a group of slots by sending the same clicks a player would do by hand,
// the server only accepts inventory changes that come as clicks
public final class InventorySorter {

    private static final Comparator<ItemStack> ORDER = Comparator
            // registry order keeps related items (e.g. all stone types) together
            .comparingInt((ItemStack stack) -> BuiltInRegistries.ITEM.getId(stack.getItem()))
            .thenComparing(stack -> stack.getHoverName().getString())
            .thenComparing(ItemStack::getCount, Comparator.reverseOrder());

    private final MultiPlayerGameMode gameMode;
    private final Player player;
    private final AbstractContainerMenu menu;
    private final List<Slot> slots;

    private InventorySorter(MultiPlayerGameMode gameMode, Player player, AbstractContainerMenu menu, List<Slot> slots) {
        this.gameMode = gameMode;
        this.player = player;
        this.menu = menu;
        this.slots = slots;
    }

    // sorts the section the hovered slot belongs to, returns false if it can't be sorted
    public static boolean sort(MultiPlayerGameMode gameMode, Player player, AbstractContainerMenu menu, Slot hovered) {
        // an item on the cursor would get mixed into the sorting
        if (!menu.getCarried().isEmpty()) {
            return false;
        }
        List<Slot> slots = sortableSlots(menu, hovered);
        if (slots.size() < 2) {
            return false;
        }

        InventorySorter sorter = new InventorySorter(gameMode, player, menu, slots);
        sorter.mergeStacks();
        sorter.reorder();
        return true;
    }

    // the player's main inventory (without hotbar) or the storage of a chest-like block
    private static List<Slot> sortableSlots(AbstractContainerMenu menu, Slot hovered) {
        List<Slot> result = new ArrayList<>();
        if (hovered.container instanceof Inventory) {
            if (!isMainInventory(hovered)) {
                return result;
            }
            for (Slot slot : menu.slots) {
                if (slot.container == hovered.container && isMainInventory(slot)) {
                    result.add(slot);
                }
            }
            return result;
        }

        boolean storageMenu = menu instanceof ChestMenu
                || menu instanceof ShulkerBoxMenu
                || menu instanceof DispenserMenu
                || menu instanceof HopperMenu;
        if (storageMenu) {
            for (Slot slot : menu.slots) {
                if (slot.container == hovered.container) {
                    result.add(slot);
                }
            }
        }
        return result;
    }

    private static boolean isMainInventory(Slot slot) {
        int index = slot.getContainerSlot();
        return index >= Inventory.SELECTION_SIZE && index < Inventory.INVENTORY_SIZE;
    }

    // fills up partial stacks of the same item so fewer slots are used
    private void mergeStacks() {
        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size() && !isFull(slots.get(i)); j++) {
                ItemStack target = slots.get(i).getItem();
                ItemStack source = slots.get(j).getItem();
                if (target.isEmpty() || !ItemStack.isSameItemSameComponents(target, source)) {
                    continue;
                }
                moveStack(j, i);
            }
        }
    }

    // selection sort: brings the right stack into each slot one after another
    private void reorder() {
        List<ItemStack> sorted = new ArrayList<>();
        for (Slot slot : slots) {
            if (!slot.getItem().isEmpty()) {
                sorted.add(slot.getItem().copy());
            }
        }
        sorted.sort(ORDER);

        for (int i = 0; i < sorted.size(); i++) {
            ItemStack wanted = sorted.get(i);
            if (ItemStack.matches(slots.get(i).getItem(), wanted)) {
                continue;
            }
            int from = find(wanted, i + 1);
            if (from < 0) {
                // the inventory changed in between, stop instead of making a mess
                return;
            }
            moveStack(from, i);
            if (!menu.getCarried().isEmpty()) {
                return;
            }
        }
    }

    private int find(ItemStack wanted, int start) {
        for (int i = start; i < slots.size(); i++) {
            if (ItemStack.matches(slots.get(i).getItem(), wanted)) {
                return i;
            }
        }
        return -1;
    }

    // picks up "from", drops it on "to" and puts whatever is left on the cursor back into "from".
    // that's a swap for different items and a merge for equal ones
    private void moveStack(int from, int to) {
        click(from);
        click(to);
        if (!menu.getCarried().isEmpty()) {
            click(from);
        }
    }

    private boolean isFull(Slot slot) {
        ItemStack stack = slot.getItem();
        return stack.isEmpty() || stack.getCount() >= slot.getMaxStackSize(stack);
    }

    private void click(int slot) {
        gameMode.handleContainerInput(menu.containerId, slots.get(slot).index, 0, ContainerInput.PICKUP, player);
    }
}
