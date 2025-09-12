package net.mizukilab.pit.menu.warehouse;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.menu.warehouse.button.WarehouseSlotButton;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class WarehouseMainMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "寄存所";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 21,22,23};
        for (int i = 0; i < 10; i++) {
            buttons.put(slots[i], new WarehouseSlotButton(i + 1, profile));
        }

        for (int i = 0; i < 36; i++) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return new ItemBuilder(Material.STAINED_GLASS_PANE)
                                .name(" ")
                                .durability(15)
                                .build();
                    }

                    @Override
                    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                    }
                });
            }
        }
        
        return buttons;
    }

    @Override
    public int getSize() {
        return 4*9;
    }
} 