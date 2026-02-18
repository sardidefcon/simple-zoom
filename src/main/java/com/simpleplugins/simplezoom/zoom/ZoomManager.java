package com.simpleplugins.simplezoom.zoom;

import com.simpleplugins.simplezoom.SimpleZoom;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks players who received a spyglass via /zoom and their previous off-hand item.
 * Restoration logic:
 * - Empty hand: track for removal only (spyglass removed, off-hand set to air)
 * - Free inventory slot: move item to that slot, restore from slot when done
 * - Full inventory: store in memory (full copy), restore when done
 */
public final class ZoomManager {

    private static final String STORED_KEY = "stored";

    private final SimpleZoom plugin;
    private final NamespacedKey storedItemKey;

    /** Players with zoom spyglass who had empty hand (no item to restore) */
    private final Set<UUID> emptyHandZoomed = ConcurrentHashMap.newKeySet();
    /** Slot index when item was moved to inventory; null when stored in memory */
    private final Map<UUID, Integer> itemSlotByPlayer = new ConcurrentHashMap<>();
    /** Item in memory when inventory was full; only set when itemSlot is null */
    private final Map<UUID, ItemStack> itemMemoryByPlayer = new ConcurrentHashMap<>();

    public ZoomManager(SimpleZoom plugin) {
        this.plugin = plugin;
        this.storedItemKey = new NamespacedKey(plugin, STORED_KEY);
    }

    /**
     * Gives spyglass to off hand.
     * Empty hand: no tracking. Free slot: move item there. Full: store in memory.
     */
    public void giveSpyglass(Player player) {
        if (player == null) return;

        ItemStack current = player.getInventory().getItemInOffHand();
        if (current == null || current.getType().isAir() || current.getAmount() <= 0) {
            emptyHandZoomed.add(player.getUniqueId());
            player.getInventory().setItemInOffHand(new ItemStack(Material.SPYGLASS));
            return;
        }

        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot >= 0) {
            ItemStack toMove = copyItemFully(current);
            markAsStored(toMove);
            player.getInventory().setItem(emptySlot, toMove);
            itemSlotByPlayer.put(player.getUniqueId(), emptySlot);
        } else {
            itemMemoryByPlayer.put(player.getUniqueId(), copyItemFully(current));
        }

        player.getInventory().setItemInOffHand(new ItemStack(Material.SPYGLASS));
    }

    /**
     * Removes spyglass and restores the previous item.
     */
    public void removeSpyglass(Player player) {
        if (player == null) return;

        ItemStack current = player.getInventory().getItemInOffHand();
        if (current.getType() != Material.SPYGLASS) return;

        if (emptyHandZoomed.remove(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            return;
        }

        Integer slot = itemSlotByPlayer.remove(player.getUniqueId());
        if (slot != null) {
            StoredItemLocation found = findStoredItem(player);
            if (found != null) {
                player.getInventory().setItem(found.slot, null);
                player.getInventory().setItemInOffHand(unmarkAndGet(found.item));
            } else {
                ItemStack inSlot = player.getInventory().getItem(slot);
                if (inSlot != null && !inSlot.getType().isAir()) {
                    player.getInventory().setItem(slot, null);
                    player.getInventory().setItemInOffHand(unmarkAndGet(inSlot));
                }
            }
            return;
        }

        ItemStack saved = itemMemoryByPlayer.remove(player.getUniqueId());
        if (saved != null) {
            player.getInventory().setItemInOffHand(copyItemFully(saved));
        }
    }

    public boolean hasZoomSpyglass(Player player) {
        if (player == null) return false;
        return emptyHandZoomed.contains(player.getUniqueId())
                || itemSlotByPlayer.containsKey(player.getUniqueId())
                || itemMemoryByPlayer.containsKey(player.getUniqueId());
    }

    /**
     * Cleans up tracking when player quits. Unmarks any stored item left in inventory.
     */
    public void clear(Player player) {
        if (player == null) return;
        emptyHandZoomed.remove(player.getUniqueId());
        if (itemSlotByPlayer.remove(player.getUniqueId()) != null) {
            StoredItemLocation found = findStoredItem(player);
            if (found != null) {
                unmarkAndGet(found.item);
            }
        }
        itemMemoryByPlayer.remove(player.getUniqueId());
    }

    private StoredItemLocation findStoredItem(Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && !item.getType().isAir() && isMarkedStored(item)) {
                return new StoredItemLocation(item, i);
            }
        }
        return null;
    }

    private record StoredItemLocation(ItemStack item, int slot) {}

    private void markAsStored(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(storedItemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
    }

    private boolean isMarkedStored(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(storedItemKey, PersistentDataType.BYTE);
    }

    /**
     * Returns true if the item is our stored item (in a slot while player zooms).
     * Used to block inventory interactions with it.
     */
    public boolean isStoredItem(ItemStack item) {
        return item != null && !item.getType().isAir() && isMarkedStored(item);
    }

    private ItemStack unmarkAndGet(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().remove(storedItemKey);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack copyItemFully(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return new ItemStack(Material.AIR);
        }
        try {
            Class<?> craftClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            java.lang.reflect.Method asCraftCopy = craftClass.getMethod("asCraftCopy", ItemStack.class);
            return (ItemStack) asCraftCopy.invoke(null, item);
        } catch (Throwable ignored) {
            return item.clone();
        }
    }
}
