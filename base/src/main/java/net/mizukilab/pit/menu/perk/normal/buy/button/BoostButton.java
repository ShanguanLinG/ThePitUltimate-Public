package net.mizukilab.pit.menu.perk.normal.buy.button;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PerkData;
import cn.charlotte.pit.perk.AbstractPerk;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.level.LevelUtil;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/7 15:47
 */
public class BoostButton extends Button {

    private final AbstractPerk perk;
    private final int page;

    public BoostButton(AbstractPerk perk, int page) {
        this.perk = perk;
        this.page = page;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        int level = 0;
        int[] price = new int[0];
        int[] limit = new int[]{0, 10, 20, 30, 40, 50};
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        for (Map.Entry<Integer, PerkData> entry : profile.getChosePerk().entrySet()) {
            if (entry.getValue().getPerkInternalName().equals(perk.getInternalPerkName())) {
                level = entry.getValue().getLevel();
                lore.add("&7当前等级: &a" + RomanUtil.convert(level));
                lore.add(" ");
                break;
            }
        }
        lore.addAll(perk.getDescription(player));
        lore.add(" ");
        boolean unlocked = PlayerUtil.isPlayerUnlockedPerk(player, "GoingFurther");
        if (level >= 6 && unlocked) {
            lore.add("&c此加成等级已达到上限！");
        } else if (level >= 5 && !unlocked) {
            lore.add("&c此加成等级已达到上限！");
        } else {
            if (perk.getInternalPerkName().equals("XPBoost")) {
                price = new int[]{500, 1000, 2500, 10000, 25000, 40000};
            }
            if (perk.getInternalPerkName().equals("CoinBoost")) {
                price = new int[]{1000, 2500, 10000, 25000, 40000, 50000};
            }
            if (perk.getInternalPerkName().equals("MeleeBoost") || perk.getInternalPerkName().equals("ArrowBoost") || perk.getInternalPerkName().equals("DmgReduceBoost")) {
                price = new int[]{450, 1050, 1500, 2250, 3000, 4000};
            }
            if (perk.getInternalPerkName().equals("BuilderBattleBoost")) {
                price = new int[]{750, 1750, 2750, 3750, 5000, 6500};
            }
            if (perk.getInternalPerkName().equals("ElGatoBoost")) {
                price = new int[]{1000, 2000, 3000, 4000, 5000, 6000};
            }
            if (limit[level] > profile.getLevel()) {
                lore.add("&c等级不足: " + LevelUtil.getLevelTag(profile.getPrestige(), limit[level]));
            }
            if (price.length >= level + 1) {
                lore.add("&7升级价格: &6" + price[level] + " 硬币");
                if (price[level] > profile.getCoins()) {
                    lore.add("&c你的硬币不足!");
                } else {
                    lore.add("&a点击升级此加成!");
                }
            }
        }
        return perk.getIconWithNameAndLore("&f" + perk.getDisplayName(), lore, (int) perk.requireRenown(0), 1);
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        int level = 0;
        int[] price = new int[0];
        int[] limit = new int[]{0, 10, 20, 30, 40, 50};
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        boolean unlocked = PlayerUtil.isPlayerUnlockedPerk(player, "GoingFurther");
        for (Map.Entry<Integer, PerkData> entry : profile.getChosePerk().entrySet()) {
            if (entry.getValue().getPerkInternalName().equals(perk.getInternalPerkName())) {
                level = entry.getValue().getLevel();
                break;
            }
        }
        if (level < 5 && !unlocked) {
            if (perk.getInternalPerkName().equals("XPBoost")) {
                price = new int[]{500, 1000, 2500, 10000, 25000};
            }
            if (perk.getInternalPerkName().equals("CoinBoost")) {
                price = new int[]{1000, 2500, 10000, 25000, 40000};
            }
            if (perk.getInternalPerkName().equals("MeleeBoost") || perk.getInternalPerkName().equals("ArrowBoost") || perk.getInternalPerkName().equals("DmgReduceBoost")) {
                price = new int[]{450, 1050, 1500, 2250, 3000};
            }
            if (perk.getInternalPerkName().equals("BuilderBattleBoost")) {
                price = new int[]{750, 1750, 2750, 3750, 5000};
            }
            if (perk.getInternalPerkName().equals("ElGatoBoost")) {
                price = new int[]{1000, 2000, 3000, 4000, 5000};
            }
            if (limit[level] > profile.getLevel()) {
                return;
            }
            if (price.length >= level + 1) {
                if (price[level] <= profile.getCoins()) {
                    profile.getChosePerk().put(page, new PerkData(perk.getInternalPerkName(), level + 1));
                    profile.setCoins(profile.getCoins() - price[level]);
                    player.sendMessage(CC.translate("&a&l升级成功! &7成功升级加成 &a" + perk.getDisplayName() + " &7至等级 &b" + RomanUtil.convert(level + 1) + " &7."));
                }
            }
        } else if (level < 6 && unlocked) {
            if (perk.getInternalPerkName().equals("XPBoost")) {
                price = new int[]{500, 1000, 2500, 10000, 25000, 40000};
            }
            if (perk.getInternalPerkName().equals("CoinBoost")) {
                price = new int[]{1000, 2500, 10000, 25000, 40000, 50000};
            }
            if (perk.getInternalPerkName().equals("MeleeBoost") || perk.getInternalPerkName().equals("ArrowBoost") || perk.getInternalPerkName().equals("DmgReduceBoost")) {
                price = new int[]{450, 1050, 1500, 2250, 3000, 4000};
            }
            if (perk.getInternalPerkName().equals("BuilderBattleBoost")) {
                price = new int[]{750, 1750, 2750, 3750, 5000, 6500};
            }
            if (perk.getInternalPerkName().equals("ElGatoBoost")) {
                price = new int[]{1000, 2000, 3000, 4000, 5000, 6000};
            }
            if (limit[level] > profile.getLevel()) {
                return;
            }
            if (price.length >= level + 1) {
                if (price[level] <= profile.getCoins()) {
                    profile.getChosePerk().put(page, new PerkData(perk.getInternalPerkName(), level + 1));
                    profile.setCoins(profile.getCoins() - price[level]);
                    player.sendMessage(CC.translate("&a&l升级成功！ &7成功升级加成 &a" + perk.getDisplayName() + " &7至等级 &b" + RomanUtil.convert(level + 1) + " &7."));
                }
            }
        }
    }

    @Override
    public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
        return true;
    }
}
