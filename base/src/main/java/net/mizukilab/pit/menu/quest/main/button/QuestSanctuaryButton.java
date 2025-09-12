package net.mizukilab.pit.menu.quest.main.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.menu.quest.sanctuary.QuestSanctuaryMenu;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/31 20:04
 */
public class QuestSanctuaryButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lore = new ArrayList<>();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lore.add("&7行动赏金可在兑换所内购买物资与加成!");
        lore.add("&7行动赏金: &e" + profile.getActionBounty());
        lore.add(" ");
        lore.add("&e点击访问兑换所!");
        return new ItemBuilder(Material.GOLD_BLOCK).name("&e兑换所").lore(lore).build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        new QuestSanctuaryMenu().openMenu(player);
    }
}
