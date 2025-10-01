package net.mizukilab.pit.menu;

import cn.charlotte.pit.ThePit;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import net.mizukilab.pit.menu.item.SavedMythicItemMenu;
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

    /**
     * @Author ShanguanLinG
     * @Created 2025/09/30 3:53
     */

    private static final int ITEMS_PER_PAGE = 45;

    private final int page;
    private final String playerId; // 添加玩家ID字段
    private List<Document> items;

    public AllSavedMythicItemsMenu() {
        this(1, null);
    }

    public AllSavedMythicItemsMenu(int page) {
        this(page, null);
    }

    public AllSavedMythicItemsMenu(int page, String playerId) {
        this.page = page;
        this.playerId = playerId;
        this.setAutoUpdate(true);
    }

    public AllSavedMythicItemsMenu(List<Document> items, String playerId) {
        this.items = items;
        this.page = 1;
        this.playerId = playerId;
        this.setAutoUpdate(false);
    }

    @Override
    public String getTitle(Player player) {
        loadItems();
        int totalPages = (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if (playerId != null) {
            return CC.translate("&8(" + page + "/" + totalPages + ") " + playerId + " 保存的物品");
        }
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
            buttons.put(slot++, new ItemButton(doc, page, playerId));
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
                    if (playerId != null) {
                        new AllSavedMythicItemsMenu(page - 1, playerId).openMenu(player);
                    } else {
                        new AllSavedMythicItemsMenu(page - 1).openMenu(player);
                    }
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
                    if (playerId != null) {
                        new AllSavedMythicItemsMenu(page + 1, playerId).openMenu(player);
                    } else {
                        new AllSavedMythicItemsMenu(page + 1).openMenu(player);
                    }
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
        
        FindIterable<Document> iterable;
        if (playerId != null) {
            // 如果指定了玩家ID，则只加载该玩家的物品
            iterable = collection.find(new Document("createdByName", playerId)).sort(new Document("createdAt", -1));
        } else {
            // 否则加载所有物品
            iterable = collection.find().sort(new Document("createdAt", -1)); // 按创建时间倒序排序
        }
        
        for (Document doc : iterable) {
            this.items.add(doc);
        }
    }

    private static class ItemButton extends Button {
        private final Document document;
        private final int currentPage;
        private final String playerId;

        public ItemButton(Document document, int currentPage, String playerId) {
            this.document = document;
            this.currentPage = currentPage;
            this.playerId = playerId;
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
            lore.add("");
            lore.add(CC.translate("&e左键点击查看物品"));
            lore.add(CC.translate("&c右键点击删除物品"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
            String uuid = document.getString("uuid");
            String encodedItem = document.getString("item");

            if (clickType == ClickType.RIGHT) {
                MongoCollection<Document> collection = ThePit.getInstance().getMongoDB().getDatabase().getCollection("saved_mythic_items");
                collection.deleteOne(new Document("uuid", uuid));
                player.sendMessage(CC.translate("&a成功删除物品!"));
                if (playerId != null) {
                    new AllSavedMythicItemsMenu(currentPage, playerId).openMenu(player);
                } else {
                    new AllSavedMythicItemsMenu(currentPage).openMenu(player);
                }
                return;
            }

            if (uuid != null && encodedItem != null) {
                new SavedMythicItemMenu(uuid, encodedItem, currentPage, playerId).openMenu(player);
            }
        }
    }
}