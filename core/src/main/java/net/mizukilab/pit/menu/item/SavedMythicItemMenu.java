package net.mizukilab.pit.menu.item;

import cn.charlotte.pit.data.sub.EnchantmentRecord;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedMythicItemMenu extends Menu {

    private final String uuid;
    private final String encodedItem;

    public SavedMythicItemMenu(String uuid, String encodedItem) {
        this.uuid = uuid;
        this.encodedItem = encodedItem;
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("&8查看物品");
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();
        ItemStack item = InventoryUtil.deserializeItemStack(encodedItem);
        if (item == null || item.getType() == Material.AIR) {
            return map;
        }
        map.put(11, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return item;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                if (!player.hasPermission("pit.admin")) return;
                player.getInventory().addItem(InventoryUtil.deserializeItemStack(encodedItem));
            }
        });
        ItemStack paper = new ItemStack(Material.PAPER);
        map.put(15, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemBuilder builder = new ItemBuilder(paper);
                IMythicItem mythicItem = Utils.getMythicItem(item);
                builder.name("&a附魔记录:");
                buildLoreForEnchantmentRecords(builder, mythicItem);
                return builder.build();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
            }
        });

        map.put(26, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemStack item = new ItemStack(Material.ARROW);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(CC.translate("&c返回"));
                item.setItemMeta(meta);
                return item;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                new AllSavedMythicItemsMenu().openMenu(player);
            }
        });
        
        return map;
    }

    private void buildLoreForEnchantmentRecords(ItemBuilder builder, IMythicItem mythicItem) {
        List<EnchantmentRecord> enchantmentRecords = mythicItem.getEnchantmentRecords();
        if(enchantmentRecords.isEmpty()){
            builder.lore("&c此物品没有附魔记录.");
            return;
        }
        for (EnchantmentRecord enchantmentRecord : enchantmentRecords) {
            builder.lore(getEnchantRecords(enchantmentRecord));
        }
    }

    private String getEnchantRecords(EnchantmentRecord enchantmentRecord) {
        String enchanter = enchantmentRecord.getEnchanter();
        String description = enchantmentRecord.getDescription();
        long timestamp = enchantmentRecord.getTimestamp();
        return CC.translate("  &e" + enchanter + " &7- &a" + description + " &7- &a" + DateFormatUtils.format(timestamp, "yyyy-MM-dd HH:mm:ss"));
    }
}