package net.mizukilab.pit.menu.heresy.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.item.type.ChunkOfVileItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DarkPantsCraftButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lines = new ArrayList<>();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        lines.add("&7通过聚集 &5暗聚块 &7与 &e声望 &7,");
        lines.add("&7创造可以有力对抗 &d神话附魔师 &7的护腿.");
        lines.add("");

        int level = PlayerUtil.getPlayerUnlockedPerkLevel(player, "heresy_perk");

        lines.add("&7合成耗费: &5" + (5 - level) + " 暗聚块");
        lines.add("&7同时耗费: &e2 声望");
        lines.add("");
        lines.add("&7合成物品: &5未附魔的暗黑之甲");
        lines.add("");
        lines.add("&7你当前有: &e" + profile.getRenown() + " 声望");
        lines.add("");
        if (profile.getRenown() < 2) {
            lines.add("&c你的声望不足!");
        } else if (InventoryUtil.getAmountOfItem(player, ChunkOfVileItem.getInternalName()) < (5 - level)) {
            lines.add("&5你没有足够的暗聚块!");
        } else if (InventoryUtil.isInvFull(player)) {
            lines.add("&c你的背包已满!");
        } else {
            lines.add("&e点击合成!");
        }
        return new ItemBuilder(Material.LEATHER_LEGGINGS)
                .name("&5暗黑之甲")
                .setLetherColor(MythicColor.DARK.getLeatherColor())
                .lore(lines)
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        int level = PlayerUtil.getPlayerUnlockedPerkLevel(player, "heresy_perk");

        if (profile.getRenown() < 2) {
            return;
        }
        if (InventoryUtil.isInvFull(player)) {
            return;
        }
        if (InventoryUtil.getAmountOfItem(player, ChunkOfVileItem.getInternalName()) < (5 - level)) {
            return;
        }
        if (InventoryUtil.removeItem(player, ChunkOfVileItem.getInternalName(), 5 - level)) {
            profile.setRenown(profile.getRenown() - 2);
            ItemStack itemStack = new MythicLeggingsItem().toItemStack();
            itemStack = new ItemBuilder(itemStack).changeNbt("mythic_color", "dark").build();
            AbstractPitItem mythicItem = new MythicLeggingsItem();
            mythicItem.loadFromItemStack(itemStack);
            player.getInventory().addItem(mythicItem.toItemStack());
            player.sendMessage(CC.translate("&d&l合成! &7成功合成了暗黑之甲."));
        }
    }
}
