package net.mizukilab.pit.menu.workshop;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.item.DyeColor;
import net.mizukilab.pit.menu.workshop.button.DyeButton;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DyeWorkshopMenu extends Menu {

    private final int[] dyeSlot = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public String getTitle(Player player) {
        return "染色工坊";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        List<DyeColor> dyeColorList = Arrays.asList(DyeColor.values());
        for (int i = 0; i < DyeColor.values().length; i++) {
            buttons.put(dyeSlot[i], new DyeButton(dyeColorList.get(i)));
        }

        for (int i = 0; i < 4; i++) {
            if (buttons.get(9 * i + 10) == null) {
                buttons.put(9 * i + 22, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return InventoryUtil.deserializeItemStack(profile.getEnchantingItem());
                    }

                    @Override
                    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {

                    }
                });
                break;
            }
        }
        return buttons;
    }

    @Override
    public int getSize() {
        return 6 * 9;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    @Override
    public boolean isUpdateAfterClick() {
        return true;
    }
}
