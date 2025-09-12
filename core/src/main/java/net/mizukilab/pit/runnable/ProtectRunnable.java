package net.mizukilab.pit.runnable;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.util.BlockUtil;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/27 15:16
 */
@Skip
public class ProtectRunnable extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            if (profile.isLoaded() && (!profile.isEditingMode() || !PlayerUtil.isStaffSpectating(player))) {
                GameMode gameMode = player.getGameMode();
                if (!profile.isInArena()) {
                    final boolean inArena = Utils.isInArena(player);
                    if (gameMode == GameMode.ADVENTURE) {
                        if (BlockUtil.isBlockNearby(player.getLocation(), 5)) {
                            player.setGameMode(GameMode.SURVIVAL);
                            return;
                        }
                    }
                    if (inArena) {
                        profile.setInArena(true);
                        continue;
                    }

                    if (gameMode == GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.ADVENTURE);
                    }
                } else {
                    if (gameMode == GameMode.ADVENTURE) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }
            }
        }
    }

}
