package net.mizukilab.pit.menu.warehouse.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.menu.warehouse.SingleWarehouseMenu;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class WarehouseSlotButton extends Button {

    private final int warehouseId;
    private final PlayerProfile profile;

    public WarehouseSlotButton(int warehouseId, PlayerProfile profile) {
        this.warehouseId = warehouseId;
        this.profile = profile;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        int itemCount = profile.getWarehouse().getItemCount(warehouseId);
        boolean isEmpty = profile.getWarehouse().isEmpty(warehouseId);

        Material material = isEmpty ? Material.CHEST : Material.ENDER_PORTAL_FRAME;
        String statusColor = isEmpty ? "§7" : "§a";

        return new ItemBuilder(material)
                .name("§6寄存箱 #" + warehouseId)
                .lore(Arrays.asList(
                        "",
                        statusColor + "物品数量: " + itemCount + "/45",
                        "",
                        "&e点击打开!"
                ))
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        playNeutral(player);
        new SingleWarehouseMenu(warehouseId).openMenu(player);
    }
} 