package net.mizukilab.pit.item.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MythicEnchantingTable extends AbstractPitItem implements Listener {

    @Override
    public String getInternalName() {
        return "enchant_table_mobile";
    }

    @Override
    public String getItemDisplayName() {
        return "&d神话之井";
    }

    @Override
    public Material getItemDisplayMaterial() {
        return Material.ENCHANTMENT_TABLE;
    }

    ItemBuilder builder = new ItemBuilder(getItemDisplayMaterial()).internalName(getInternalName()).name(getItemDisplayName())
            .lore("&7通过击杀玩家来获得", "&e神话之剑&7, &b神话之弓 &7以及", "&c神&6话&9之&a甲 &7等物品.", " ", "&7在神话之井中为这些物品附魔", "&7可以赋予其大量的强大增益.", " ", "&d放入一件神话物品到左侧空格中以开始!");

    @Override
    public ItemStack toItemStack() {
        return builder.build();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack itemInHand = e.getPlayer().getItemInHand();
        if (itemInHand.getType() == getItemDisplayMaterial()) {
            if (getInternalName().equals(ItemUtil.getInternalName(itemInHand))) {
                if (e.getPlayer().hasPermission("pit.enchbypass") || PlayerProfile.getPlayerProfileByUuid(e.getPlayer().getUniqueId()).getCombatTimer().hasExpired()) {
                    ThePit.getApi().openMythicWellMenu(e.getPlayer());
                } else {
                    e.getPlayer().sendMessage("&c当前在战斗中, 不允许打开附魔台");
                }
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void loadFromItemStack(ItemStack item) {

    }
}
