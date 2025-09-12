package net.mizukilab.pit.menu.shop.button.type.server;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Araykal
 * @since 2025/3/3
 */
public class CloverPixelShopButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.STORAGE_MINECART).name("&a杂物").lore(Arrays.asList("&8", "&7&o购买你想购买的任何东西", "&7", "&e点击跳转!")).shiny().build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        player.closeInventory();
        player.sendMessage(CC.translate("§c§l正在跳转至杂物商店!"));
        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 0.9F);
        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dm open 商店 " + player.getName());
        }, 1L);
    }
}
