package com.simpleplugins.simplezoom.listener;

import com.simpleplugins.simplezoom.zoom.ZoomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents players from taking or moving the stored item while zooming.
 * The item is in a slot temporarily and must stay there until zoom ends.
 */
public final class InventoryGuardListener implements Listener {

    private final ZoomManager zoomManager;

    public InventoryGuardListener(ZoomManager zoomManager) {
        this.zoomManager = zoomManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!zoomManager.hasZoomSpyglass(player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (zoomManager.isStoredItem(cursor) || zoomManager.isStoredItem(clicked)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!zoomManager.hasZoomSpyglass(player)) return;

        if (zoomManager.isStoredItem(event.getOldCursor())) {
            event.setCancelled(true);
            return;
        }
        for (int slot : event.getRawSlots()) {
            ItemStack item = event.getView().getItem(slot);
            if (zoomManager.isStoredItem(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
