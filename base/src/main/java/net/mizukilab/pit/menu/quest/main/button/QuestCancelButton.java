package net.mizukilab.pit.menu.quest.main.button;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.QuestData;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/21 13:14
 */
public class QuestCancelButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.BARRIER)
                .name("&c&l点此取消该任务")
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        QuestData currentQuest = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId())
                .getCurrentQuest();
        if (currentQuest != null) {
            ThePit.getInstance()
                    .getQuestFactory()
                    .getQuestMap()
                    .get(currentQuest.getInternalName())
                    .onInactive(player, currentQuest.getLevel());

            player.playSound(player.getLocation(), Sound.WITHER_DEATH, 1, 1);
            player.sendMessage(CC.translate("&c任务已取消!"));
        }
    }
}
