package com.simpleplugins.simplezoom.command;

import com.simpleplugins.simplezoom.ConfigUpdater;
import com.simpleplugins.simplezoom.SimpleZoom;
import com.simpleplugins.simplezoom.zoom.ZoomManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles /zoom - gives the player a spyglass in their off hand.
 * The native spyglass zoom works when the item is held.
 * Previous off-hand item is saved and restored when spyglass is removed.
 */
public final class ZoomCommand implements CommandExecutor {

    private final SimpleZoom plugin;
    private final ZoomManager zoomManager;

    public ZoomCommand(SimpleZoom plugin, ZoomManager zoomManager) {
        this.plugin = plugin;
        this.zoomManager = zoomManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("szoom.reload")) {
                sendMessage(sender, getMessage("reload-no-permission"));
                return true;
            }
            ConfigUpdater.mergeWithDefaults(plugin);
            plugin.reloadConfig();
            sendMessage(sender, getMessage("reload-success"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sendMessage(sender, getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("szoom.use")) {
            sendMessage(player, getMessage("no-permission"));
            return true;
        }

        zoomManager.giveSpyglass(player);
        return true;
    }

    private String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "&7[" + key + "]");
    }

    private void sendMessage(CommandSender sender, String raw) {
        String prefix = plugin.getConfig().getString("prefix", "");
        String text = (prefix != null && !prefix.isEmpty()) ? prefix + raw : raw;
        text = text.replace('&', '\u00A7');
        Component component = LegacyComponentSerializer.legacySection().deserialize(text);
        sender.sendMessage(component);
    }
}
