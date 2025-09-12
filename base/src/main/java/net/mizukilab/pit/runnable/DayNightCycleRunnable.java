package net.mizukilab.pit.runnable;

import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class DayNightCycleRunnable extends BukkitRunnable {

    @Override
    public void run() {
        long minecraftTick = TimeUtil.getMinecraftTick();
            Bukkit.getWorlds().forEach(world -> world.setTime(minecraftTick)
        );
    }
}
