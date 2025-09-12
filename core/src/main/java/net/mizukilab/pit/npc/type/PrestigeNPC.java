package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.prestige.PrestigeMainMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/1 13:44
 */

public class PrestigeNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "prestige";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&6&l精通");
        if (profile.getLevel() >= 120 || profile.getPrestige() > 0) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 120) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getPrestigeNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc0NDIwMzQxOTE5MCwKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q3OTQ0MTgwNGRlZDNhYjBmMDQ2OThkODJkYjhhNDlmMWIwZTIzNTc1MTgyYjgwODdkMGI5ZmZlZjczYjQyYjYiCiAgICB9CiAgfQp9",
                "ixquBaDwzAydQwHHnG+QejwtTocDg+mqXXgUmy0gehbgNMwFdJf7hMjh6Ca8Ufh5hZ5dVDm9iswqTsdqxWRk1fSfsrkjKPlteJ//DExsPBWGZuQBpgUY3U+fxW0sBXx2D8Pzt/zU3O3kpDpKV8dx0ECSwH9Gc4iqliSLqGhN0yktX2VeDTn/eaIyIkppvjmJkDe8NcK+f0QMgy0BFli1kxQna29qGYwvDFbKhj6JEF6lIdvbra5WvFMxePBrzL1tMlwp3qhTjNFdIx23zo7X0S4IYTy3zjQ/vhCP0+Y/sVsDJV92Gah2/Gq0bweZOxQBTG7efiO6IYPfu/Foi9HOYL3dAjuZP66EDkAZPYoecFUb5Sz+tFrQhd/t5fEps8zSGTvfnjmQDyeHqQvKUIm/sr7NYc7UD71zIY1DAGYLvwwNBryjpCSHfETR9y4+WXsPAoJtIuq3pQ0ODtENPRsFPVRs0Lq6IgEvs/j3DVuK7LKnjYli95lUHZ0cJiEdmNDYAZM6382YZThAHRAx72hid48Q2bor1THCPiUpD5P0crrop1AL0Xjq+gG2DV+iVE2QMIcaSkDAaJ+V+AH+2caFrqyJuJxhUnBO8b07AlqLdZ06FA96bm14J1Xy2OmfW1Ab9K6VZrURI1YtBHLhU1b5gFO+Egxa3Hw8zc3lDOyf75M="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 120 && profile.getPrestige() == 0) {
            player.sendMessage(CC.translate("&c&l等级不足! &7精通在 " + LevelUtil.getLevelTag(profile.getPrestige(), 120) + " &7时解锁."));
        } else {
            new PrestigeMainMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
