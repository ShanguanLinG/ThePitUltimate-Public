package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.quest.main.QuestMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/19 18:38
 */

public class QuestNpc extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "quest";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&3&l任务");
        if (profile.getLevel() >= 30 || profile.getPrestige() > 0) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 30) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getQuestNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin("ewogICJ0aW1lc3RhbXAiIDogMTc0NjEyMjU3MzU2NCwKICAicHJvZmlsZUlkIiA6ICJiNTQ1ZDcxNDJkZmM0MWVlYjBjMDcwOTkwOTI4NTE0MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJraXdpNDgxMiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNzU5MGU2ZjdlNWVlNzRhNjJiMmM2Y2ExMDEzZDIxMzI0ODgyYmM3OWMyMzk3YTEwNmU4Yzc2NGQwNWMzZDhkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "et9npP/mAZ8Q3l/s6/Lypc/u/GhuNeovnCIFNzNLwO0toKLmK8dCriSqiuB+JeJpcExUDTb9ccXrj/8N9j84Wb0cIuIFdeT+1myLwLZua49yr+nzQZAcLQl2l7sMdej4zfsU1DKsb29/vEAvsGfnyTjyDLAMHh+JYjWYS/ZBsfHDqQ6c0yBREgUMV6lUwSnFuMGjh1wy9lQK1S/OqeR3k30yC5nZ1oPy2M3YJI2v0tIb+WuUBLnWjvoxkXK4/4CJehFGIE0TjdMdu0x1sECRQRPIPHsoYisvEnvHotEe2UdT9Q8Df93ZoDkWkIl2MU4cQAhLgavUqx3S7c3phUllZRoBOeAUJvTLQnIIVxrMnwDq75uLq07UYtCO3M/sZ5GfYyNOaMoYdHfNaeCFG2mFWp0xC1/2a2hEvggsS6ACPGjs04kxSaTRBsJ63p4Vxeg1KTQSmBj9VqeFj01lEtgi7R0tqyzZqRpf2pi7eW5yfg8LlXej1T7I+/MeQUP5cTC9KCwHtJI1hUg3xa3W56pwxg/6UZ4SeyBGdJeUX/axbeTV9Krlfe47CWNu4YpEmzOAkNXIABwLx2wTUru5LlQenn8sP7lBkB8Z/9UxB8mcuLUQ4aLqfDkvGyZi5l3VkfVy6z070C+Rmj1X+ATyZKnCBSlI1mdp0zwGL9Gn7/e2tsk="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 30) {
            player.sendMessage(CC.translate("&c&l等级不足! &7任务在 " + LevelUtil.getLevelTag(profile.getPrestige(), 30) + " &7时解锁."));
            return;
        }
        new QuestMenu().openMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return new ItemStack(Material.BOOK);
    }
}
