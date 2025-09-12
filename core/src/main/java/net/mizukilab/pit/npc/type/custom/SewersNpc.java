package net.mizukilab.pit.npc.type.custom;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.menu.sewers.SewersMenu;
import net.mizukilab.pit.npc.AbstractCustomEntityNPC;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Araykal
 * @since 2025/5/2
 */
public class SewersNpc extends AbstractCustomEntityNPC {
    @Override
    public String getNpcInternalName() {
        return "sewers";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        lines.add("&9&l下水道鱼");
        lines.add("&e&l右键查看");
        return lines;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getSewersFishNpcLocation();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SILVERFISH;
    }


    @Override
    public void handlePlayerInteract(Player player) {
        new SewersMenu().openMenu(player);
        player.playSound(player.getLocation(), Sound.SILVERFISH_HIT, 1, 1);
    }

    @Override
    public void customizeEntity(Entity entity) {

    }
}
