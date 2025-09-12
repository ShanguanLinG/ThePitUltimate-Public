package net.mizukilab.pit.medal.impl.challenge;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.PitKillEvent;
import net.mizukilab.pit.medal.AbstractMedal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


/**
 * @Creator Misoryan
 * @Date 2021/6/9 23:02
 */

public class MaxBountyHunterMedal extends AbstractMedal implements Listener {

    @Override
    public String getInternalName() {
        return "MAX_BOUNTY_HUNTER";
    }

    @Override
    public String getDisplayName(int level) {
        return "赏金猎人";
    }

    @Override
    public String getRequirementDescription(int level) {
        return "击杀一名被5,000及以上硬币悬赏的玩家";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getProgressRequirement(int level) {
        return 1;
    }

    @Override
    public int getRarity(int level) {
        return 1;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void handleProfileLoaded(PlayerProfile profile) {

    }

    @EventHandler
    public void onPitKill(PitKillEvent event) {
        Player player = (Player) event.getTarget();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        PlayerProfile pitProfile = PlayerProfile.getPlayerProfileByUuid(event.getKiller().getUniqueId());
        if (profile.getBounty() >= 5000) {
            setProgress(pitProfile, 1);
        }
    }
}
