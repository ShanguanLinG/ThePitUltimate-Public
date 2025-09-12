package net.mizukilab.pit.perk.type.prestige;

import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkType;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @Author: Starry_Killer
 * @Created_In: 2023/12/12
 */
public class GoingFurtherPerk extends AbstractPerk {
    @Override
    public String getInternalPerkName() {
        return "GoingFurther";
    }

    @Override
    public String getDisplayName() {
        return "更进一步";
    }

    @Override
    public Material getIcon() {
        return Material.PAINTING;
    }

    @Override
    public double requireCoins() {
        return 0;
    }

    @Override
    public double requireRenown(int level) {
        return 50;
    }

    @Override
    public int requirePrestige() {
        return 5;
    }

    @Override
    public int requireLevel() {
        return 0;
    }

    @Override
    public PerkType getPerkType() {
        return PerkType.PERK;
    }

    @Override
    public List<String> getDescription(Player player) {
        return Collections.singletonList("&7所有 &c被动天赋提升 &7的最大等级拓展至 &eVI &7级");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public void onPerkActive(Player player) {

    }

    @Override
    public void onPerkInactive(Player player) {

    }
}
