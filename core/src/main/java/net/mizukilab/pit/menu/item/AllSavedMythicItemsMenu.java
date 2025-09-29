package net.mizukilab.pit.menu.item;

import cn.charlotte.pit.ThePit;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class AllSavedMythicItemsMenu extends Menu {

    private static final int ITEMS_PER_PAGE = 45;

    private final int page;
    private List<Document> items;

    public AllSavedMythicItemsMenu() {
        this(1);
    }

    public AllSavedMythicItemsMenu(int page) {
        this.page = page;
        this.setAutoUpdate(true);
    }

    @Override
    public String getTitle(Player player) {
        // 先加载物品以计算总页数
        loadItems();
        int totalPages = (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        return CC.translate("&8(" + page + "/" + totalPages + ") 保存的物品");
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        loadItems();
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Document doc = items.get(i);
            buttons.put(slot++, new ItemButton(doc));
        }

        // 上一页
        if (page > 1) {
            buttons.put(45, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    ItemStack item = new ItemStack(Material.ARROW);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(CC.translate("&f上一页"));
                    item.setItemMeta(meta);
                    return item;
                }

                @Override
                public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                    new AllSavedMythicItemsMenu(page - 1).openMenu(player);
                }
            });
        }
        
        // 下一页
        if (endIndex < items.size()) {
            buttons.put(53, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    ItemStack item = new ItemStack(Material.ARROW);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(CC.translate("&f下一页"));
                    item.setItemMeta(meta);
                    return item;
                }

                @Override
                public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
                    new AllSavedMythicItemsMenu(page + 1).openMenu(player);
                }
            });
        }

//        buttons.put(49, new Button() {
//            @Override
//            public ItemStack getButtonItem(Player player) {
//                ItemStack item = new ItemStack(Material.PAPER);
//                ItemMeta meta = item.getItemMeta();
//                meta.setDisplayName(CC.translate("&e第 " + page + " 页"));
//                List<String> lore = new ArrayList<>();
//                lore.add(CC.translate("&7总共 " + ((items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE) + " 页"));
//                lore.add(CC.translate("&7共 " + items.size() + " 个物品"));
//                meta.setLore(lore);
//                item.setItemMeta(meta);
//                return item;
//            }
//
//            @Override
//            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
//            }
//        });

        return buttons;
    }

    private void loadItems() {
        if (this.items != null) {
            return;
        }
        this.items = new ArrayList<>();
        MongoCollection<Document> collection = ThePit.getInstance().getMongoDB().getDatabase().getCollection("saved_mythic_items");
        FindIterable<Document> iterable = collection.find().sort(new Document("createdAt", -1)); // 按创建时间倒序排序
        for (Document doc : iterable) {
            this.items.add(doc);
        }
    }

    private static class ItemButton extends Button {
        private final Document document;

        public ItemButton(Document document) {
            this.document = document;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            String encodedItem = document.getString("item");
            ItemStack item = InventoryUtil.deserializeItemStack(encodedItem);
            ItemMeta meta = item.getItemMeta();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(CC.translate("&7UUID: &f" + document.getString("uuid")));
            lore.add(CC.translate("&7创建者: &f" + document.getString("createdByName")));
            long createdAt = document.getLong("createdAt");
            lore.add(CC.translate("&7创建时间: &f" + sdf.format(new Date(createdAt))));
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
            String uuid = document.getString("uuid");
            String encodedItem = document.getString("item");
            if (uuid != null && encodedItem != null) {
                new SavedMythicItemMenu(uuid, encodedItem).openMenu(player);
            }
        }
    }
}