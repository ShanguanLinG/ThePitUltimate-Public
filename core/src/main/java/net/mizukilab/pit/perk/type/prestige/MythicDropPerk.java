package net.mizukilab.pit.perk.type.prestige;

import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Araykal
 * @since 2025/4/21
 */
public class MythicDropPerk extends AbstractPerk {
    @Override
    public String getInternalPerkName() {
        return "MythicDrop";
    }

    @Override
    public String getDisplayName() {
        return "炼金术士";
    }

    @Override
    public Material getIcon() {
        return Material.GOLDEN_APPLE;
    }

    @Override
    public double requireCoins() {
        return 0;
    }

    @Override
    public double requireRenown(int level) {
        return 10;
    }

    @Override
    public int requirePrestige() {
        return 3;
    }

    @Override
    public int requireLevel() {
        return 0;
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("&7允许你使用 &f/drop &7命令开关神话物品的掉落.");
        lines.add("");
        lines.add("&7当关闭了神话物品掉落时");
        lines.add("&7原本应获得的神话物品将立即转化为 &62❤ 生命吸收");
        return lines;
    }
    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public PerkType getPerkType() {
        return PerkType.PERK;
    }


    @Override
    public void onPerkActive(Player player) {

    }

    @Override
    public void onPerkInactive(Player player) {

    }
}
