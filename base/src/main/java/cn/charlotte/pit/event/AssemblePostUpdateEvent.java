package cn.charlotte.pit.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.List;

public class AssemblePostUpdateEvent extends PitEvent implements Cancellable {

    boolean cancel = false;
    List<String> strList;
    @Getter
    Player player;

    public AssemblePostUpdateEvent(Player player, List<String> strList) {
        this.strList = strList;
        this.player = player;
    }

    public List<String> getTexts() {
        return strList;
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
