 package cn.charlotte.pit.data.sub;

import cn.charlotte.pit.data.deserializer.WarehouseDeserializer;
import cn.charlotte.pit.data.serializer.WarehouseSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 玩家仓库数据类
 * @Author: Araykal
 * @Date: 2025/6/21
 */
@JsonSerialize(using = WarehouseSerializer.class)
@JsonDeserialize(using = WarehouseDeserializer.class)
public class PlayerWarehouse {
    
    private Map<Integer, Inventory> warehouses;
    
    public PlayerWarehouse() {
        warehouses = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            Inventory inv = Bukkit.createInventory(null, 45, "寄存箱 #" + i);
            // 用黑色玻璃板填充所有空格子
            fillEmptySlots(inv);
            warehouses.put(i, inv);
        }
    }
    
    public static PlayerWarehouse deserialization(String string) {
        PlayerWarehouse warehouse = new PlayerWarehouse();
        
        if (string != null && !string.isEmpty()) {
            String[] warehouseData = string.split("\\|");
            for (int i = 0; i < warehouseData.length && i < 10; i++) {
                if (!warehouseData[i].isEmpty()) {
                    ItemStack[] items = InventoryUtil.stringToItems(warehouseData[i]);
                    Inventory inv = warehouse.getWarehouse(i + 1);
                    if (items != null && items.length > 0) {
                        ItemStack[] limitedItems = new ItemStack[45];
                        int copyLength = Math.min(items.length, 45);
                        System.arraycopy(items, 0, limitedItems, 0, copyLength);
                        inv.setContents(limitedItems);
                    }
                    warehouse.fillEmptySlots(inv);
                }
            }
        }
        
        return warehouse;
    }
    
    public Inventory getWarehouse(int warehouseId) {
        if (warehouseId < 1 || warehouseId > 10) {
            throw new IllegalArgumentException("寄存编号必须在1-10之间");
        }
        return warehouses.get(warehouseId);
    }
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            Inventory inv = warehouses.get(i);
            // 过滤掉屏障方块，只保存真实物品
            ItemStack[] contents = inv.getContents();
            ItemStack[] realItems = new ItemStack[contents.length];
            for (int j = 0; j < contents.length; j++) {
                ItemStack item = contents[j];
                if (item != null && !isBarrierPlaceholder(item)) {
                    realItems[j] = item;
                }
            }
            String warehouseData = InventoryUtil.itemsToString(realItems);
            sb.append(warehouseData);
            if (i < 10) {
                sb.append("|");
            }
        }
        return sb.toString();
    }
    
    public int getItemCount(int warehouseId) {
        Inventory inv = getWarehouse(warehouseId);
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && !isBarrierPlaceholder(item)) {
                count++;
            }
        }
        return count;
    }
    
    public boolean isEmpty(int warehouseId) {
        return getItemCount(warehouseId) == 0;
    }

    private ItemStack createBarrierPlaceholder() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName("§7储存格");
        glass.setItemMeta(meta);
        return glass;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack barrier = createBarrierPlaceholder();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, barrier);
            }
        }
    }
    private boolean isBarrierPlaceholder(ItemStack item) {
        if (item == null || item.getType() != Material.STAINED_GLASS_PANE || item.getDurability() != 15) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && "§7储存格".equals(meta.getDisplayName());
    }
    
    public Map<Integer, Inventory> getWarehouses() {
        return warehouses;
    }
    
    public void setWarehouses(Map<Integer, Inventory> warehouses) {
        this.warehouses = warehouses;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerWarehouse)) return false;
        PlayerWarehouse that = (PlayerWarehouse) o;
        return Objects.equals(warehouses, that.warehouses);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(warehouses);
    }
    
    @Override
    public String toString() {
        return "PlayerWarehouse{warehouses=" + warehouses + "}";
    }
} 