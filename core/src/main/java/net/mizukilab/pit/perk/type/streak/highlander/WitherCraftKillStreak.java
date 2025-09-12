package net.mizukilab.pit.perk.type.streak.highlander;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.PitStreakKillChangeEvent;
import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkType;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.ItemBow;
import net.minecraft.server.v1_8_R3.ItemSword;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.type.ChunkOfVileItem;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Skip
@AutoRegister
public class WitherCraftKillStreak extends AbstractPerk implements Listener {

    @Override
    public String getInternalPerkName() {
        return "wither_craft_kill_streak";
    }

    @Override
    public String getDisplayName() {
        return "凋灵工艺";
    }

    @Override
    public Material getIcon() {
        return Material.SOUL_SAND;
    }

    @Override
    public double requireCoins() {
        return 70000;
    }

    @Override
    public double requireRenown(int level) {
        return 0;
    }

    @Override
    public int requirePrestige() {
        return 0;
    }

    @Override
    public int requireLevel() {
        return 110;
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> list = new ArrayList<>();
        list.add("&7此天赋每 &c25 连杀 &7触发一次.");
        list.add(" ");
        list.add("&7触发时:");
        list.add("  &a▶ &7消耗背包内一件 &5暗聚块 &7,");
        list.add("  &a▶ &7恢复 &d神话之井 &7内物品 &c1 &7生命.");
        return list;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public PerkType getPerkType() {
        return PerkType.KILL_STREAK;
    }

    @Override
    public void onPerkActive(Player player) {

    }

    @Override
    public void onPerkInactive(Player player) {

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStreak(PitStreakKillChangeEvent event) {
        Player myself = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
        PlayerProfile profile = event.getPlayerProfile();
        if (myself == null || !myself.isOnline()) {
            return;
        }
        if (!PlayerUtil.isPlayerChosePerk(myself, getInternalPerkName())) {
            return;
        }
        //trigger check (every X streak)
        int streak = 25;
        if (Math.floor(event.getFrom()) % streak != 0 && Math.floor(event.getTo()) % streak == 0) {
            //effect goes here
            if (profile.getEnchantingItem() == null) {
                return;
            }
            ItemStack item = InventoryUtil.deserializeItemStack(profile.getEnchantingItem());
            IMythicItem mythicItem = null;
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = Utils.toNMStackQuick(item);
            if (item == null) {
                return;
            }

            if (nmsStack.getItem() instanceof ItemSword) {
                mythicItem = new MythicSwordItem();
            } else if (nmsStack.getItem() instanceof ItemBow) {
                mythicItem = new MythicBowItem();
            } else if (nmsStack.getItem() instanceof ItemArmor && ItemUtil.getInternalName(item).equalsIgnoreCase("mythic_leggings")) {
                mythicItem = new MythicLeggingsItem();
            }

            if (mythicItem == null) {
                return;
            }

            mythicItem.loadFromItemStack(item);

            if (mythicItem.getLive() <= 0 || mythicItem.getMaxLive() <= 0 || mythicItem.getMaxLive() == mythicItem.getLive()) {
                return;
            }

            if (!InventoryUtil.removeItem(myself, ChunkOfVileItem.getInternalName(), 1)) {
                return;
            }

            mythicItem.setLive(Math.min(mythicItem.getLive() + 1, mythicItem.getMaxLive()));
            profile.setEnchantingItem(InventoryUtil.serializeItemStack(mythicItem.toItemStack()));
            myself.sendMessage(CC.translate("&5&l凋灵工艺! &7成功修复了神话之井中放入的神话物品."));
        }
    }

}
