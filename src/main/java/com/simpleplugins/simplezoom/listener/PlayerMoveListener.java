package com.simpleplugins.simplezoom.listener;

import com.simpleplugins.simplezoom.SimpleZoom;
import com.simpleplugins.simplezoom.zoom.ZoomManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Removes spyglass from off hand when the player moves (if remove-on-move is enabled).
 */
public final class PlayerMoveListener implements Listener {

    private final SimpleZoom plugin;
    private final ZoomManager zoomManager;

    public PlayerMoveListener(SimpleZoom plugin, ZoomManager zoomManager) {
        this.plugin = plugin;
        this.zoomManager = zoomManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("remove-on-move", true)) return;
        if (!zoomManager.hasZoomSpyglass(event.getPlayer())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            zoomManager.removeSpyglass(event.getPlayer());
        }
    }
}
