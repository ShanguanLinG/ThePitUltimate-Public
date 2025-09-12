package net.mizukilab.pit.menu.heresy;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.menu.heresy.button.DarkPantsCraftButton;
import net.mizukilab.pit.menu.heresy.button.NightQuestSwitchButton;
import net.mizukilab.pit.menu.heresy.button.RagePantsCraftButton;
import net.mizukilab.pit.menu.perk.prestige.button.PrestigePerkBuyButton;
import net.mizukilab.pit.menu.quest.main.QuestMenu;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HeresyMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "邪术";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> button = new HashMap<>();

        button.put(11, new DarkPantsCraftButton());
        if (PlayerUtil.isPlayerUnlockedPerk(player, "pure_rage")) {
            button.put(13, new RagePantsCraftButton());
        }
        button.put(15, new PrestigePerkBuyButton(ThePit.getInstance().getPerkFactory().getPerkMap().get("heresy_perk")));
        //return button
        button.put(31, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.BOOK)
                        .name("&a任务")
                        .lore("&7前往任务界面")
                        .build();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                new QuestMenu().openMenu(player);
            }
        });
        button.put(32, new NightQuestSwitchButton());
        return button;
    }

    @Override
    public int getSize() {
        return 4 * 9;
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
