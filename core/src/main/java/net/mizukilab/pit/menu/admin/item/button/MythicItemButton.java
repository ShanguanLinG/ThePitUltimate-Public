package net.mizukilab.pit.menu.admin.item.button;

import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.DyeColor;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.type.*;
import net.mizukilab.pit.item.type.mythic.MagicFishingRod;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/17 22:42
 */
public class MythicItemButton extends Button {

    private final int i;

    public MythicItemButton(int i) {
        this.i = i;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        if (i == 0) {
            return new MythicSwordItem().toItemStack();
        } else if (i == 1) {
            return new MythicBowItem().toItemStack();
        } else if (i == 2) {
            return new MythicLeggingsItem().toItemStack();
        } else if (i == 3) {
            return new MagicFishingRod().toItemStack();
        } else if (i == 4) {
            ItemStack itemStack = new MythicLeggingsItem().toItemStack();
            itemStack = new ItemBuilder(itemStack).changeNbt("mythic_color", "dark").build();
            AbstractPitItem mythicItem = new MythicLeggingsItem();
            mythicItem.loadFromItemStack(itemStack);
            return mythicItem.toItemStack();
        } else if (i == 5) {
            ItemStack itemStack = new AngelChestplate().toItemStack();
            IMythicItem mythicItem = new AngelChestplate();
            mythicItem.loadFromItemStack(itemStack);
            mythicItem.setLive(12);
            return mythicItem.toItemStack();
        } else if (i == 6) {
            ItemStack itemStack = new ArmageddonBoots().toItemStack();
            IMythicItem mythicItem = new ArmageddonBoots();
            mythicItem.loadFromItemStack(itemStack);
            mythicItem.setLive(3);
            return mythicItem.toItemStack();
        } else if (i == 7) {
            return new SpireSword(Material.DIAMOND_SWORD).toItemStack();
        } else if (i == 8) {
            ItemStack itemStack = new MythicLeggingsItem().toItemStack();
            itemStack = new ItemBuilder(itemStack).changeNbt("dyeColor", DyeColor.WHITE.name()).build();
            AbstractPitItem mythicItem = new MythicLeggingsItem();
            mythicItem.loadFromItemStack(itemStack);
            return mythicItem.toItemStack();
        } else if (i == 9) {
            return new LuckyChestplate().toItemStack();
        } else if (i == 10) {
            ItemStack itemStack = new AngelChestplate().toItemStack();
            IMythicItem mythicItem = new AngelChestplate();
            mythicItem.loadFromItemStack(itemStack);
            return mythicItem.toItemStack();
        } else if (i == 11) {
            return MythicBook.toItemStack();
        }
        return new ItemBuilder(Material.AIR).build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        if (clickType.isRightClick()) {
            player.sendMessage("这个物品的NBTName是: " + ItemUtil.getInternalName(currentItem));
            return;
        }
        player.getInventory().addItem(currentItem);
    }


}
