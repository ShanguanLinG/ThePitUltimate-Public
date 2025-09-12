package cn.charlotte.pit.api;

import cn.charlotte.pit.data.PlayerInvBackup;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.TradeData;
import cn.charlotte.pit.events.AbstractEvent;
import cn.charlotte.pit.util.hologram.Hologram;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface PitInternalHook {

    void openMythicWellMenu(Player player);

    void openAuctionMenu(Player player);

    void openAngelMenu(Player player);

    void openDemonMenu(Player player);

    void openTradeTrackMenu(Player player, PlayerProfile profile, List<TradeData> data);

    void openBackupShowMenu(Player player, PlayerProfile profile, List<PlayerInvBackup> backups, PlayerInvBackup backup, boolean enderChest);

    void openBackupShowMenu(Player player, PlayerProfile profile, List<PlayerInvBackup> backups, PlayerInvBackup backup, Menu previousMenu, boolean enderChest);

    void openMenu(Player player, String menuName);

    boolean openEvent(Player player, String event);

    Hologram createHologram(Location location, String text);

    UUID getRunningKingsQuestsUuid();


    String getPitSupportPermission();

    boolean getRemoveSupportWhenNoPermission();

    ItemStack reformatPitItem(ItemStack item);

    ItemStack generateItem(String item);

    int getItemEnchantLevel(ItemStack item, String enchantName);

    boolean openEvent(AbstractEvent event, Player player);

    String getWatermark();

    int addItemInHandEnchant(Player player, String enchantName, int enchantLevel);


    ItemStack getMythicItemItemStack(String itemName);

    default boolean isLoaded() {
        return false;
    }
}
