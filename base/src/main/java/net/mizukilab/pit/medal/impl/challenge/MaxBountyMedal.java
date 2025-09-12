package net.mizukilab.pit.medal.impl.challenge;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.PitKillEvent;
import net.mizukilab.pit.medal.AbstractMedal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


/**
 * @Creator Misoryan
 * @Date 2021/6/9 22:48
 */

public class MaxBountyMedal extends AbstractMedal implements Listener {

    @Override
    public String getInternalName() {
        return "MAX_BOUNTY";
    }

    @Override
    public String getDisplayName(int level) {
        return "焦点";
    }

    @Override
    public String getRequirementDescription(int level) {
        return "被超过5,000硬币的赏金悬赏";
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
        return 3;
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
        Player player = event.getKiller();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getBounty() >= 5000) {
            setProgress(profile, 1);
        }
    }
}
