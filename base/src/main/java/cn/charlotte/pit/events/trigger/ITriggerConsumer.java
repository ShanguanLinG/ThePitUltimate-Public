package cn.charlotte.pit.events.trigger;

import org.bukkit.entity.Player;

public interface ITriggerConsumer {
    default boolean processTrigger(TrigAction action, Player player, Object... args){
        return true;
    }
    enum TrigAction{
        CLEAR,
    }
}
