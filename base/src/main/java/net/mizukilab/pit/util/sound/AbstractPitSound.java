package net.mizukilab.pit.util.sound;

import io.irina.backports.utils.SWMRHashTable;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/26 13:32
 */
public abstract class AbstractPitSound {

    private final Map<Player, Integer> playersTick;

    public AbstractPitSound() {
        this.playersTick = new SWMRHashTable<>();
    }

    public abstract String getMusicInternalName();

    public abstract void onSoundTick(Player player, int tick);

    public void tick() {
        for (Map.Entry<Player, Integer> entry : playersTick.entrySet()) {
            final Player player = entry.getKey();
            if (player == null || !player.isOnline()) {
                playersTick.remove(player);
                continue;
            }
            Integer tick = entry.getValue();
            this.onSoundTick(player, tick);
            tick++;
            playersTick.put(player, tick);

        }
    }

    public void play(Player player) {
        this.playersTick.put(player, 0);
    }

    public void end(Player player) {
        this.playersTick.remove(player);
    }

}
