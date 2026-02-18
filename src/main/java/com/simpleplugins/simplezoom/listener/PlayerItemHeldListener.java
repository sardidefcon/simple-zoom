package com.simpleplugins.simplezoom.listener;

import com.simpleplugins.simplezoom.SimpleZoom;
import com.simpleplugins.simplezoom.zoom.ZoomManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * Removes spyglass from off hand when the player switches hotbar slot (if remove-on-hotbar-switch is enabled).
 */
public final class PlayerItemHeldListener implements Listener {

    private final SimpleZoom plugin;
    private final ZoomManager zoomManager;

    public PlayerItemHeldListener(SimpleZoom plugin, ZoomManager zoomManager) {
        this.plugin = plugin;
        this.zoomManager = zoomManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!plugin.getConfig().getBoolean("remove-on-hotbar-switch", true)) return;
        if (!zoomManager.hasZoomSpyglass(event.getPlayer())) return;

        zoomManager.removeSpyglass(event.getPlayer());
    }
}
