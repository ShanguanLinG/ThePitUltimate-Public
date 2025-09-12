package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.perk.normal.choose.PerkChooseMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/1 13:11
 */

public class PerkNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "perk";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&c&l天赋");
        if (profile.getLevel() >= 10) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getPerkNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTcwNjIxMDIxOTc3MiwKICAicHJvZmlsZUlkIiA6ICJlNTZkYjMyZWVkOGQ0NTY3YWI4YmZjOWMwYmM1YWFlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJraXVnYW1lcjk1NiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iOGRiNGEyNWQ1ODU5MzUzOWEzODgwMzhhMmIyYzk1Yjc0Y2ZjMTczMmEzYjY2MGJiN2E0OWE2ODNiYzBlZTMiCiAgICB9CiAgfQp9",
                "rEMIb6AomWQkfYePDyQ8zIpcVl324A4ozAf7e4UWhvwFMMBCb/q1AUlZ5WlFtvzeA1yDJR9zka/sDF5Ati70mXTUqOQUHbMlCPJO8NR++lhbb8eaGTrWR71iFqlWQObNlLQpwRvlnfFv62a1mWLQJUO6JCdTZhCbWuvSIUUABaGx10VfBIkV7vNqzAbjlO+wzDlkEYZfMj9Y9/m3sxYDaUuk2qKGzafJ4CegU+S+lLDg55Zdl/Gs/KTxXH+LIOORLzIf1ACqfKlN912wY04YHA40vgpqSEMvS2MtM+zeaMh+l9JXjn3l4YmOdjxHjB+0jpZCNz7E0VoIN8o1V5eSxdllb+wGej5jgGuujhHlTsLI9oY/kHAQYlyXJM6fCCPj2L9Eymo6NyCAkAW+gBENzucK1/jjv4CSMxftHuAgnqVCnCr34k4WS0xJzZm4SJQg5eZySaM+cTaDoTJcy/hg6XV5aLeaddKip+TSatCk2kolZ7gI+5HmvVB1cLL06TQGkJpoSy9W08fYSbIPi682U8sfPpBQabBAHy0gMXEcaQ4wO7J8lJVBkFN3VnQ27eYipdgjghE0iyefuCd95h5aG/wPG96WvU/ZXgJfuBBgnq7XjyFRRcsrvdaCSoI5GAJ98mMfAP/cGzRRCs4BcDxkhv4z9kVPo+EaxskouBp1asc="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 10) {
            player.sendMessage(CC.translate("&c&l等级不足! &7天赋在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &7时解锁."));
        } else {
            new PerkChooseMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
