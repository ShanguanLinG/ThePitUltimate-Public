package net.mizukilab.pit.runnable;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.util.chat.CC;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @Author: Starry_Killer
 * @Date: 2024/1/1
 */
@Skip
public class FreeExpRunnable extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = PlayerProfile.getRawCache(player.getUniqueId());
            profile.setExperience(profile.getExperience() + 10);
            player.sendMessage(CC.translate("&a&l免费经验！ &7参与游戏&b获得10经验"));
            player.sendMessage(CC.translate("&e&l注意！ &7天坑乱斗中允许组队！"));
        }
    }
}
