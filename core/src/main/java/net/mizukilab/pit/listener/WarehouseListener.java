package net.mizukilab.pit.listener;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
@AutoRegister
public class WarehouseListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if (!isWarehouseInventory(inventory)) return;
        event.setCancelled(true);
        if (event.getClick() == ClickType.NUMBER_KEY ||
                event.getAction().name().contains("HOTBAR") ||
                event.getClick() == ClickType.DOUBLE_CLICK ||
                event.getClick() == ClickType.SHIFT_LEFT ||
                event.getClick() == ClickType.SHIFT_RIGHT) {
            return;
        }
        if (event.getClick() != ClickType.LEFT) {
            return;
        }

        org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().name().equals("AIR")) {
            return;
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            moveItemToWarehouse(player, inventory, clickedItem, event.getSlot());
        } else if (event.getClickedInventory() != null && isWarehouseInventory(event.getClickedInventory())) {
            // 如果点击的是屏障占位符，则不做任何操作
            if (isBarrierPlaceholder(clickedItem)) {
                return;
            }
            moveItemToPlayerInventory(player, inventory, clickedItem, event.getSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if (isWarehouseInventory(inventory)) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            profile.save(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory inventory = event.getInventory();
        if (!isWarehouseInventory(inventory)) return;
        event.setCancelled(true);
    }

    private boolean isWarehouseInventory(Inventory inventory) {
        if (inventory == null) return false;
        String title = inventory.getTitle();
        return title != null && title.startsWith("寄存箱 #");
    }

    private void moveItemToWarehouse(Player player, Inventory warehouseInventory, org.bukkit.inventory.ItemStack item, int playerSlot) {
        if (!canSaveToWarehouse(item)) {
            player.sendMessage("§c该物品不能寄存!");
            return;
        }

        for (int i = 0; i < warehouseInventory.getSize(); i++) {
            org.bukkit.inventory.ItemStack existingItem = warehouseInventory.getItem(i);
            if (existingItem != null && !isBarrierPlaceholder(existingItem) && existingItem.isSimilar(item)) {
                int maxStackSize = item.getMaxStackSize();
                int existingAmount = existingItem.getAmount();
                int itemAmount = item.getAmount();

                if (existingAmount < maxStackSize) {
                    int canAdd = Math.min(itemAmount, maxStackSize - existingAmount);
                    existingItem.setAmount(existingAmount + canAdd);

                    if (canAdd == itemAmount) {
                        player.getInventory().setItem(playerSlot, null);
                    } else {
                        item.setAmount(itemAmount - canAdd);
                        player.getInventory().setItem(playerSlot, item);
                    }
                    player.playSound(player.getLocation(), Sound.LAVA_POP, 1.0f, 1.0f);
                    player.updateInventory();
                    return;
                }
            }
        }
        // 寻找空格子或屏障占位符
        int emptySlot = -1;
        for (int i = 0; i < warehouseInventory.getSize(); i++) {
            ItemStack slotItem = warehouseInventory.getItem(i);
            if (slotItem == null || isBarrierPlaceholder(slotItem)) {
                emptySlot = i;
                break;
            }
        }
        
        if (emptySlot == -1) {
            player.sendMessage("§c寄存箱已满!");
            return;
        }
        warehouseInventory.setItem(emptySlot, item.clone());
        player.getInventory().setItem(playerSlot, null);
        player.playSound(player.getLocation(), Sound.LAVA_POP, 1.0f, 1.0f);
        player.updateInventory();
    }

    private void moveItemToPlayerInventory(Player player, Inventory warehouseInventory, org.bukkit.inventory.ItemStack item, int warehouseSlot) {
        org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            org.bukkit.inventory.ItemStack existingItem = contents[i];
            if (existingItem != null && existingItem.isSimilar(item)) {
                int maxStackSize = item.getMaxStackSize();
                int existingAmount = existingItem.getAmount();
                int itemAmount = item.getAmount();

                if (existingAmount < maxStackSize) {
                    int canAdd = Math.min(itemAmount, maxStackSize - existingAmount);
                    existingItem.setAmount(existingAmount + canAdd);

                    if (canAdd == itemAmount) {
                        warehouseInventory.setItem(warehouseSlot, createBarrierPlaceholder());
                    } else {
                        item.setAmount(itemAmount - canAdd);
                        warehouseInventory.setItem(warehouseSlot, item);
                    }
                    player.playSound(player.getLocation(), Sound.LAVA_POP, 1.0f, 1.0f);
                    player.updateInventory();
                    return;
                }
            }
        }
        if (InventoryUtil.isInvFull(player)) {
            player.sendMessage("§c背包已满 无法取出!");
            return;
        }
        player.getInventory().addItem(item.clone());
        warehouseInventory.setItem(warehouseSlot, createBarrierPlaceholder());
        player.playSound(player.getLocation(), Sound.LAVA_POP, 1.0f, 1.0f);
        player.updateInventory();
    }

    private boolean canSaveToWarehouse(org.bukkit.inventory.ItemStack item) {
        if (item == null) return true;

        return ItemUtil.canSaveEnderChest(item);
    }

    private ItemStack createBarrierPlaceholder() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName("§7储存格");
        glass.setItemMeta(meta);
        return glass;
    }

    private boolean isBarrierPlaceholder(ItemStack item) {
        if (item == null || item.getType() != Material.STAINED_GLASS_PANE || item.getDurability() != 15) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && "§7储存格".equals(meta.getDisplayName());
    }

    private void updateBarrierPlaceholders(Inventory warehouseInventory) {
        for (int i = 0; i < warehouseInventory.getSize(); i++) {
            ItemStack item = warehouseInventory.getItem(i);
            if (item == null) {
                warehouseInventory.setItem(i, createBarrierPlaceholder());
            }
        }
    }
} 