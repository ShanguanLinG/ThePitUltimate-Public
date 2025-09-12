package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.status.StatusMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/2 21:31
 */

public class StatusNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "status";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&9&l统计信息");
        if (profile.getLevel() >= 50 || profile.getPrestige() > 0) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 50) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getStatusNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTYzODAyOTc3ODEzMSwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc3Nzg3MGI4YjRhODAxODY0NjcwZGRkZjhlOTJhNjhlZTY5ZGEzZGNiNDNjNDA5MmYxNzk0NDMyZjNlNWY3NyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                "QgC/tgLb/rKB0RN0I1ColRyZyio9vP5CrvevlfMC4Uaj/0UhD1zgaW7eGQ0Jzo0EQKk8xNRMDnfG5dGSV9DQ/V3kFHw7SRZSnqraRiolP/ZKyJpldUmWaWJlbmQ99oqvQTDAXbAeGWR/kvpCKEH2hgNjSi6NHbzEAFuDNwU0Hzh8SG8BtBp5t4oXBfH9ms96ZiBp9qj12eNAuSgCwZsoqs247kdpiE4BKG87BCmBHKw3Gfzw1V9Y2wWEjkqoUiSN6RxIWa1kt56PoDxzbosxSMCvPhMMUkQcKKU2cKHIVdXoqu57EHhbjkft1VkV0dJtSv2P/EZzlo5dlOEaqiZnXyFxOfvj2JIKTRAvkjeV9DHi0eezMBkwIYFoYAvXqZR5sAWB1jFEACUoBPcxLWTSfDVdIk2shKwGgFBRRO1qlLBvifdcj3b5dfEdpzKBiADjCGQa6zVFMkGe+h5bvDRNIy7+Qs2Hd2cHm6LY0Bi9/VSPfYfOpU+Mn2EIXsyQFxloe52bO7qqQWD8/kklI7r2DbHgrfPmeP9ROSttw2i6xFlsvKOwUS/OGy82LYY5c0pcp/nBMnn8ZhFlZBQWH9YDx3C2IfxdHljY+nLHqSz4hzFzlmG4QPcxVKqEePwRaw0k9ySGocrBmc+ArDY1Dj0t3kObjw/h0dl7X75BOk7FvPI="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 50 && profile.getPrestige() == 0) {
            player.sendMessage(CC.translate("&c&l等级不足! &7统计在 " + LevelUtil.getLevelTag(profile.getPrestige(), 50) + " &7时解锁."));
        } else {
            new StatusMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
