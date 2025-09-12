package net.mizukilab.pit.medal.impl.challenge;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.PitStreakKillChangeEvent;
import net.mizukilab.pit.medal.AbstractMedal;
import net.mizukilab.pit.parm.AutoRegister;
import nya.Skip;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @Creator Misoryan
 * @Date 2021/6/9 23:00
 */
@Skip
@AutoRegister
public class FiftyStreaksMedal extends AbstractMedal implements Listener {

    @Override
    public String getInternalName() {
        return "FIFTY_STREAKS";
    }

    @Override
    public String getDisplayName(int level) {
        return "完杀";
    }

    @Override
    public String getRequirementDescription(int level) {
        return "完成一次50连杀";
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
        return true;
    }

    @Override
    public void handleProfileLoaded(PlayerProfile profile) {

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStreak(PitStreakKillChangeEvent event) {
        if (event.getTo() >= 50) {
            addProgress(event.getPlayerProfile(), 1);
        }
    }
}
