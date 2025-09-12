package net.mizukilab.pit.menu.viewer.button;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PlayerWarehouse;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class WarehouseViewerMenu extends Menu {

    private final PlayerProfile profile;

    public WarehouseViewerMenu(PlayerProfile profile) {
        this.profile = profile;
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("&8" + Bukkit.getOfflinePlayer(profile.getPlayerUuid()).getName() + " 的寄存箱");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        PlayerWarehouse warehouse = profile.getWarehouse();
        for (int i = 1; i <= 10; i++) {
            final int warehouseId = i;

            buttons.put(getSlotForWarehouse(i), new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    int itemCount = warehouse.getItemCount(warehouseId);
                    boolean isEmpty = warehouse.isEmpty(warehouseId);

                    List<String> lore = new ArrayList<>();
                    lore.add("§7物品数量: &a" + itemCount + "&7/&a45");
                    lore.add("");
                    if (isEmpty) {
                        lore.add("§c这个寄存箱为空");
                    } else {
                        lore.add("&e点击查看!");
                    }

                    Material material = isEmpty ? Material.CHEST : Material.ENDER_CHEST;

                    return new ItemBuilder(material)
                            .name("§6寄存箱 #" + warehouseId)
                            .lore(lore)
                            .amount(warehouseId)
                            .build();
                }

                @Override
                public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                    if (warehouse.isEmpty(warehouseId)) {
                        return;
                    }
                    new SingleWarehouseViewerMenu(profile, warehouseId).openMenu(player);
                }
            });
        }
        return buttons;
    }


    private int getSlotForWarehouse(int warehouseId) {
        if (warehouseId <= 7) {
            return 10 + (warehouseId - 1);
        } else {
            return 21 + (warehouseId - 8);
        }
    }

    @Override
    public int getSize() {
        return 4 * 9;
    }
} 