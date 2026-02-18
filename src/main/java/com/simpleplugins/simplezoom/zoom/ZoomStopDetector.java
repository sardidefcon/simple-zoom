package com.simpleplugins.simplezoom.zoom;

import com.simpleplugins.simplezoom.SimpleZoom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when a player stops zooming (releases right-click while using spyglass)
 * via NMS reflection. Runs every 2 ticks.
 */
public final class ZoomStopDetector extends BukkitRunnable {

    private final SimpleZoom plugin;
    private final ZoomManager zoomManager;
    private final Map<UUID, Boolean> wasUsingLastTick = new ConcurrentHashMap<>();

    private static Method getHandleMethod;
    private static Method getUseItemMethod;

    public ZoomStopDetector(SimpleZoom plugin, ZoomManager zoomManager) {
        this.plugin = plugin;
        this.zoomManager = zoomManager;
    }

    private static boolean init(Player player) {
        if (getHandleMethod != null) return true;
        try {
            Class<?> craftPlayerClass = player.getClass();
            getHandleMethod = craftPlayerClass.getMethod("getHandle");
            Object handle = getHandleMethod.invoke(player);
            getUseItemMethod = handle.getClass().getMethod("getUseItem");
        } catch (Throwable ignored) {
            return false;
        }
        return true;
    }

    private static boolean isUsingSpyglass(Player player) {
        if (getHandleMethod == null || getUseItemMethod == null) return false;
        try {
            Object handle = getHandleMethod.invoke(player);
            Object nmsUseItem = getUseItemMethod.invoke(handle);
            if (nmsUseItem == null) return false;

            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            java.lang.reflect.Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            ItemStack bukkitStack = (ItemStack) asBukkitCopy.invoke(null, nmsUseItem);
            return bukkitStack != null && bukkitStack.getType() == Material.SPYGLASS;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("remove-on-stop-zoom", false)) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!zoomManager.hasZoomSpyglass(player)) continue;

            if (!init(player)) continue;

            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() != Material.SPYGLASS) {
                zoomManager.clear(player);
                wasUsingLastTick.remove(player.getUniqueId());
                continue;
            }

            boolean currentlyUsing = isUsingSpyglass(player);
            boolean wasUsing = wasUsingLastTick.getOrDefault(player.getUniqueId(), false);

            if (wasUsing && !currentlyUsing) {
                zoomManager.removeSpyglass(player);
                wasUsingLastTick.remove(player.getUniqueId());
            } else {
                wasUsingLastTick.put(player.getUniqueId(), currentlyUsing);
            }
        }
    }
}
