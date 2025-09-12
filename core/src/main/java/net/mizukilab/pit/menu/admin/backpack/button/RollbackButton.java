package net.mizukilab.pit.menu.admin.backpack.button;

import cn.charlotte.pit.data.PlayerInvBackup;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PlayerInv;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.menu.buttons.DisplayButton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/4 20:54
 */
public class RollbackButton extends DisplayButton {

    private final PlayerProfile profile;
    private final PlayerInvBackup backup;


    public RollbackButton(ItemStack itemStack, PlayerProfile profile, PlayerInvBackup backup) {
        super(itemStack, true);
        this.profile = profile;
        this.backup = backup;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        PlayerInv inv = backup.getInv();
        Player target = Bukkit.getPlayer(profile.getPlayerUuid());
        if (target != null && target.isOnline()) {
            inv.applyItemToPlayer(target);
        }
        profile.setInventoryUnsafe(inv);
        profile.saveData(null);
        player.closeInventory();
        player.sendMessage(CC.translate("&a回滚完成"));
    }
}
