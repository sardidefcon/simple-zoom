package com.simpleplugins.simplezoom;

import com.simpleplugins.simplezoom.command.ZoomCommand;
import com.simpleplugins.simplezoom.listener.InventoryGuardListener;
import com.simpleplugins.simplezoom.listener.PlayerItemHeldListener;
import com.simpleplugins.simplezoom.listener.PlayerMoveListener;
import com.simpleplugins.simplezoom.listener.PlayerQuitListener;
import com.simpleplugins.simplezoom.update.UpdateChecker;
import com.simpleplugins.simplezoom.zoom.ZoomManager;
import com.simpleplugins.simplezoom.zoom.ZoomStopDetector;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Simple Zoom - A lightweight plugin that allows players to zoom.
 * Part of the SimplePlugins family.
 * Gives a spyglass to the player's off hand; the native zoom works when held.
 */
public final class SimpleZoom extends JavaPlugin {

    private ZoomManager zoomManager;
    private ZoomStopDetector zoomStopDetector;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!validateRemovalOptions()) {
            getLogger().severe("At least one removal option must be enabled (remove-on-move, remove-on-hotbar-switch, remove-on-stop-zoom). Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        int pluginId = 29592;
        Metrics metrics = new Metrics(this, pluginId);

        zoomManager = new ZoomManager(this);
        getCommand("zoom").setExecutor(new ZoomCommand(this, zoomManager));
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, zoomManager), this);
        getServer().getPluginManager().registerEvents(new PlayerItemHeldListener(this, zoomManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(zoomManager), this);
        getServer().getPluginManager().registerEvents(new InventoryGuardListener(zoomManager), this);

        zoomStopDetector = new ZoomStopDetector(this, zoomManager);
        zoomStopDetector.runTaskTimer(this, 2L, 2L);

        UpdateChecker.check(this);
    }

    @Override
    public void onDisable() {
        if (zoomStopDetector != null) {
            zoomStopDetector.cancel();
        }
    }

    private boolean validateRemovalOptions() {
        boolean move = getConfig().getBoolean("remove-on-move", true);
        boolean hotbar = getConfig().getBoolean("remove-on-hotbar-switch", true);
        boolean stopZoom = getConfig().getBoolean("remove-on-stop-zoom", false);
        return move || hotbar || stopZoom;
    }
}
