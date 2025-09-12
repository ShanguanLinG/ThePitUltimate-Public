package net.mizukilab.pit.npc.runnable;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.npc.AbstractPitNPC;
import net.mizukilab.pit.npc.NpcFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author EmptyIrony
 * @date 2021/1/1 21:09
 */
public class NpcRunnable extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (AbstractPitNPC pitNpc : ThePit.getInstance().getNpcFactory().getPitNpc()) {
                pitNpc.getNpc().setText(player, pitNpc.getNpcTextLine(player));
            }
        }
    }

}
