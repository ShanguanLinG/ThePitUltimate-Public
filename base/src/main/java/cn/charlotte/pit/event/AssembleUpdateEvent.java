package cn.charlotte.pit.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class AssembleUpdateEvent extends PitEvent implements Cancellable {

    boolean cancel = false;
    @Getter
    Player player;

    public AssembleUpdateEvent(Player player) {
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}
