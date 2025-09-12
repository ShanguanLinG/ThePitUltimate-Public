package net.mizukilab.pit.menu.trade;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerInvBackup;
import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.util.menu.Menu;
import net.mizukilab.pit.util.menu.buttons.DisplayButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/4 20:46
 */
public class ShowInvBackupButton extends DisplayButton {

    private final PlayerInvBackup backup;
    private final PlayerProfile profile;
    private final List<PlayerInvBackup> backups;
    private Menu previous;

    public ShowInvBackupButton(List<PlayerInvBackup> inv, ItemStack itemStack, PlayerInvBackup backup,PlayerProfile profile) {
        this(inv,itemStack,backup,null,profile);
    }
    public ShowInvBackupButton(List<PlayerInvBackup> inv, ItemStack itemStack, PlayerInvBackup backup,Menu previous, PlayerProfile profile) {
        super(itemStack, true);
        this.backup = backup;
        this.profile = profile;
        this.backups = inv;
        this.previous = previous;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        ThePit.api.openBackupShowMenu(player, profile, backups, backup,previous, clickType.isRightClick());
    }
}
