package net.mizukilab.pit.runnable;

import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * @Author: Starry_Killer
 * @Date: 2024/1/3
 */
@Skip
public class NightVisionRunnable extends BukkitRunnable {

    Iterator<? extends Player> iterator = null;
    PotionEffect cachedPotionEffect = PotionEffectType.NIGHT_VISION.createEffect(114154, 2);

    @Override
    public void run() {
        if (iterator == null || !iterator.hasNext()) {
            if (Bukkit.getOnlinePlayers() == null || Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            iterator = Bukkit.getOnlinePlayers().iterator();
        }
        boolean flag = true;
        while (flag && iterator.hasNext()) {
            Player next = iterator.next();
            if (!next.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                next.addPotionEffect(cachedPotionEffect);
                flag = false;
            }
        }
    }
}
