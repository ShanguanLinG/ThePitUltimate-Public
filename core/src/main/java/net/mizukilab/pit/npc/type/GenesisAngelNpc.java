package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Yurinan
 * @since 2022/3/5 16:53
 */

public class GenesisAngelNpc extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "GenesisAngel";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        lines.add("&e限时活动: 光暗派系");
        lines.add("&f&l天使");
        lines.add("&e&l右键查看");
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getGenesisAngelNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc0Mzk4Nzc4NjYyNiwKICAicHJvZmlsZUlkIiA6ICJhYzY1NDYwOWVkZjM0ODhmOTM0ZWNhMDRmNjlkNGIwMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFjZUd1cmxTa3kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjJmNzA5NDExMzE4MjY0NTMxODQzMTI1YjE2MTQyMzMwZTIyZDNlNDQzOTdhN2ZhZjBjMjIzZDZkMDUxNDc3OCIKICAgIH0KICB9Cn0=",
                "UXxDcyWO/KOk0VIE8C57190lsl515w3U/sZ790mB67OEDYUycDLUu0Igaqs+CaNdiQ9UnZGspZXcQfdZWVssLaO6XTx7/qLSPclB8L6dDc/A0p68LfIEz8oTJppwk0s732x+OiJUI78fEw4e0foIkpaSk3/H9Ci1HhexshTcKo1DLGjByLEjIQMgqKTKya4fO4ZdsUZLYwnSOzuE4ZY15A/sEAvvsFyBopXOMDZMAPHTYkzZxFak0EZ6JOMzH0r8rasODJYBdljvC1N6YLZ1z0B3626k924uTp8wnxALhOWQOXua4U4dmZCdWh5t05n8ukfzI69+aN0XyOyqDb+N10SK/DinlL2rAV0N4q43TZ2x3RBFvM/sBQZVf8VVZnRiI9uZaSNOQ6T/H4ZqDL4ZOupI3/Yg6CqptFM+6VhYzDM81nReXsK69y7V8RdC04jRCEb/lbv2fhsfHUZQAK4t1cjtgOFeDIBYmj5mT387qWlyzSW6L5WYOK48NGL96V1MM5ahlxgKvlOOLuhnFZZBy3Deij9pddzJoIJwANKTmzEacpZjxuU3ZAn2ThMm4uuJRW82/ZdW4gCNrO9dVj1uBmuikxxIFaXkOZKbzn3JBjZWjw4gXLbumZsbqbWpG5eLJie2zVQe7M7/SH/QMqTLKIzufgfwjvBMtAKM90UDChc="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        ThePit.getApi().openAngelMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }

}
