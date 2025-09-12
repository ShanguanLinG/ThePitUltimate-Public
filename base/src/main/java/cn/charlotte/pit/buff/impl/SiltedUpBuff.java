package cn.charlotte.pit.buff.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.buff.AbstractPitBuff;
import cn.charlotte.pit.event.PitStackBuffEvent;
import net.mizukilab.pit.parm.AutoRegister;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

@AutoRegister
public class SiltedUpBuff extends AbstractPitBuff implements Listener {

    @Override
    public String getInternalBuffName() {
        return "pin_down_de_buff";
    }

    @Override
    public String getDisplayName() {
        return "&2阻滞";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList("&7无法受到与被施加 &b速度 &7与 &a跳跃提升 &7效果");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBuffStack(PitStackBuffEvent event) {
        if (event.isCancel() || !getInternalBuffName().equalsIgnoreCase(event.getBuff().getInternalBuffName())) {
            return;
        }

        if (getPlayerBuffData(event.getPlayer()).getTier() < 1) {
            new BuffDebuffTask(event.getPlayer()).runTaskTimer(ThePit.getInstance(), 0L, 10L);
        }
    }

    private class BuffDebuffTask extends BukkitRunnable {

        private final Player player;

        BuffDebuffTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP);

            if (getPlayerBuffData(player).getTier() < 1) {
                cancel();
            }
        }
    }
}
