package net.mizukilab.pit.menu.cdk.generate.button;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.util.callback.Callback;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import net.mizukilab.pit.util.menu.buttons.DisplayButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/26 17:16
 */
public class CDKEditItemsButton extends Button {

    private final ItemStack displayItem;
    private final Menu menu;
    private final List<ItemStack> items;
    private final Callback<List<ItemStack>> callback;

    public CDKEditItemsButton(ItemStack displayItem, Menu menu, List<ItemStack> items, Callback<List<ItemStack>> callback) {
        this.displayItem = displayItem;
        this.menu = menu;
        this.items = items;
        this.callback = callback;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return displayItem;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {

            new Menu() {
                @Override
                public String getTitle(Player player) {
                    return "&f放入物品";
                }

                @Override
                public Map<Integer, Button> getButtons(Player player) {
                    final Map<Integer, Button> map = new HashMap<>(18);
                    final DisplayButton button = new DisplayButton(new ItemStack(Material.AIR), false);
                    for (int i = 0; i < 18; i++) {
                        map.put(i, button);
                    }

                    return map;
                }

                @Override
                public void onClickEvent(InventoryClickEvent event) {
                    event.setCancelled(false);
                }

                @Override
                public void onClose(Player player) {
                    for (int i = 0; i < 18; i++) {
                        if (player.getOpenInventory().getItem(i) != null && player.getOpenInventory().getItem(i).getType() != Material.AIR) {
                            ItemStack item = player.getOpenInventory().getItem(i);
                            String internalName = ItemUtil.getInternalName(item);
                            if (internalName != null) {
                                String uuid = ItemUtil.getUUID(item);
                                if (uuid != null) {
                                    ItemUtil.setUUID(item, "00000000-0000-0000-0000-000000000001");
                                }
                            }
                            items.set(i, item);
                        }
                    }
                    callback.call(items);
                    Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> menu.openMenu(player), 1L);
                }

                @Override
                public int getSize() {
                    return 2 * 9;
                }
            }.openMenu(player);


        }, 1L);
    }

    @Override
    public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
        return true;
    }
}
