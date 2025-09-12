package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.admin.item.AdminItemMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/4/21 21:58
 */

public class InfinityItemNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "infinity_item";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        return Arrays.asList(
                "&b大物品制造者",
                "&e右键查看"
        );
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getInfinityNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return null;
    }

    @Override
    public void handlePlayerInteract(Player player) {
        if (ThePit.isDEBUG_SERVER()) {
            new AdminItemMenu(false).openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return new ItemStack(Material.DIAMOND_BLOCK);
    }
}
