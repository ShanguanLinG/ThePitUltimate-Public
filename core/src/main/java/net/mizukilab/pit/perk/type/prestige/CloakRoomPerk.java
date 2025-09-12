package net.mizukilab.pit.perk.type.prestige;

import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Araykal
 * @since 2025/6/21
 */
public class CloakRoomPerk extends AbstractPerk {
    @Override
    public String getInternalPerkName() {
        return "cloak_room";
    }

    @Override
    public String getDisplayName() {
        return "寄存所产业";
    }

    @Override
    public Material getIcon() {
        return Material.ENDER_PORTAL_FRAME;
    }

    @Override
    public double requireCoins() {
        return 50000;
    }

    @Override
    public double requireRenown(int level) {
        return 10;
    }

    @Override
    public int requirePrestige() {
        return 1;
    }

    @Override
    public int requireLevel() {
        return 0;
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("&7解锁额外业务寄存.");
        lines.add("");
        lines.add("&7前往寄存所会为你提供 &e10 &7个寄存箱");
        lines.add("&7可往寄存箱内,存储物品和取出物品");
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
