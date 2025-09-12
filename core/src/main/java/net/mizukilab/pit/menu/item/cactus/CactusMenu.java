package net.mizukilab.pit.menu.item.cactus;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/21 17:16
 */
public class CactusMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "哲学仙人掌 (选择其一)";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> button = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            button.put(i, getPantsButton());
        }
        return button;
    }

    public Button getPantsButton() {
        return new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return ThePit.getApi().generateItem("Leggings");
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                if (InventoryUtil.isInvFull(player)) {
                    player.sendMessage(CC.translate("&c你的背包已满!"));
                    return;
                }
                if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR || !"cactus".equalsIgnoreCase(ItemUtil.getInternalName(player.getItemInHand()) == null ? "" : ItemUtil.getInternalName(player.getItemInHand()))) {
                    player.sendMessage(CC.translate("&c请手持哲学仙人掌后重试!"));
                    return;
                }
                if (currentItem == null) return;


                PlayerUtil.takeOneItemInHand(player);
                player.getInventory().addItem(currentItem);
                player.closeInventory();
                player.sendMessage(CC.translate("&a&l奖励已领取! " + currentItem.getItemMeta().getDisplayName()));
            }
        };
    }
}
