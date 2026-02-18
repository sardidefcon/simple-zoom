package com.simpleplugins.simplezoom.listener;

import com.simpleplugins.simplezoom.zoom.ZoomManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Cleans up zoom state when a player leaves.
 */
public final class PlayerQuitListener implements Listener {

    private final ZoomManager zoomManager;

    public PlayerQuitListener(ZoomManager zoomManager) {
        this.zoomManager = zoomManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        zoomManager.clear(event.getPlayer());
    }
}
