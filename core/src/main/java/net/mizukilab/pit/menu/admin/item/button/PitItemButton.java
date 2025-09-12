package net.mizukilab.pit.menu.admin.item.button;

import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/28 18:56
 */
public class PitItemButton extends Button {

    private final ItemStack itemStack;

    public PitItemButton(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return CraftItemStack.asCraftCopy(itemStack).clone();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        ItemStack buttonItem = getButtonItem(player);
        if (clickType.isRightClick()) {
            player.sendMessage("这个物品的NBTName为: " + ItemUtil.getInternalName(buttonItem));
            return;
        }
        player.getInventory().addItem(buttonItem);
    }
}
