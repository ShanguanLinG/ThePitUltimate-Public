package net.mizukilab.pit.menu.viewer.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class SingleWarehouseViewerMenu extends Menu {
    
    private final PlayerProfile profile;
    private final int warehouseId;
    
    public SingleWarehouseViewerMenu(PlayerProfile profile, int warehouseId) {
        this.profile = profile;
        this.warehouseId = warehouseId;
    }
    
    @Override
    public String getTitle(Player player) {
        return CC.translate("&8" + Bukkit.getOfflinePlayer(profile.getPlayerUuid()).getName() + " 的寄存箱 #" + warehouseId);
    }
    
    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Inventory warehouse = profile.getWarehouse().getWarehouse(warehouseId);

        for (int i = 0; i < 45; i++) {
            final int slot = i;
            buttons.put(i, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    ItemStack item = warehouse.getItem(slot);
                    return item != null ? item : new ItemStack(Material.AIR);
                }
                
                @Override
                public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                }
            });
        }
        for (int i = 45; i < 53; i++) {
            buttons.put(i, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemStack(Material.AIR);
                }
                
                @Override
                public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                }
            });
        }
        buttons.put(53, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.ARROW)
                        .name("&c返回")
                        .lore("&7返回寄存箱列表")
                        .build();
            }
            
            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                new WarehouseViewerMenu(profile).openMenu(player);
            }
        });
        
        return buttons;
    }
    
    @Override
    public int getSize() {
        return 54;
    }
} 