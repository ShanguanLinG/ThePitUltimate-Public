package net.mizukilab.pit.menu.admin.backpack;

import cn.charlotte.pit.data.PlayerInvBackup;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.menu.admin.backpack.button.ItemShowButton;
import net.mizukilab.pit.menu.admin.backpack.button.RollbackButton;
import net.mizukilab.pit.menu.trade.ShowInvBackupButton;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import net.mizukilab.pit.util.menu.buttons.BackButton;
import net.mizukilab.pit.util.menu.menus.PagedMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/4 20:47
 */
public class BackupShowMenu extends Menu {

    private final DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final PlayerProfile playerProfile;
    private final PlayerInvBackup backup;
    private final List<PlayerInvBackup> backups;
    private final boolean right;
    private Menu fatherMenu;

    public BackupShowMenu(PlayerProfile playerProfile, List<PlayerInvBackup> backups, PlayerInvBackup backup, boolean right) {
        this(playerProfile,backups,backup,right,null);
    }
    public BackupShowMenu(PlayerProfile playerProfile, List<PlayerInvBackup> backups, PlayerInvBackup backup, boolean right,Menu father) {
        this.playerProfile = playerProfile;
        this.backup = backup;
        this.backups = backups;
        this.right = right;
        this.fatherMenu = father;
    }

    @Override
    public String getTitle(Player player) {
        return playerProfile.getPlayerName() + " 的背包备份";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        if (right) {
            int i = 0;
            for (ItemStack itemStack : backup.getChest().getInventory()) {
                buttonMap.put(i, new ItemShowButton(itemStack, true));
                i++;
            }
        } else {
            for (int i = 0; i < backup.getInv().getContents().length; i++) {
                buttonMap.put(i, new ItemShowButton(backup.getInv().getContents()[i], true));
            }

            for (int i = 0; i < backup.getInv().getArmorContents().length; i++) {
                buttonMap.put(i + 27, new ItemShowButton(backup.getInv().getArmorContents()[i], true));
            }
        }

        if (!right) {
            buttonMap.put(36, new RollbackButton(new ItemBuilder(Material.CHEST).name("&a回滚至该背包").shiny().build(), playerProfile, backup));
        }
        if(fatherMenu == null) {
            List<Button> buttons = new ObjectArrayList<>();
            int i = 0;
            for (PlayerInvBackup invBackup : backups) {
                buttons.add(new ShowInvBackupButton(backups,
                        new ItemBuilder(Material.BOOK)
                                .name("&a备份时间: " + format.format(invBackup.getTimeStamp()))
                                .lore(("&e物品数: " + InventoryUtil.getInventoryFilledSlots(invBackup.getInv().getContents())))
                                .amount(Math.min(64, InventoryUtil.getInventoryFilledSlots(invBackup.getInv().getContents())))
                                .build(), invBackup, playerProfile));
                i++;
            }
            Collections.reverse(buttons);
            fatherMenu = new PagedMenu(playerProfile.getPlayerName() + " 的背包备份", buttons);
        }
        buttonMap.put(41, new BackButton(fatherMenu));

        return buttonMap;
    }
}
