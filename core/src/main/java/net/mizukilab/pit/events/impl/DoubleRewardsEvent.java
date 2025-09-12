package net.mizukilab.pit.events.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.events.AbstractEvent;
import cn.charlotte.pit.events.trigger.type.INormalEvent;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/1/6
 */
public class DoubleRewardsEvent extends AbstractEvent implements INormalEvent, IPlayerKilledEntity {

    private boolean isActive = false;

    @Override
    public String getEventInternalName() {
        return "double_rewards_event";
    }

    @Override
    public String getEventName() {
        return "&a&l双倍奖励";
    }

    @Override
    public int requireOnline() {
        return ThePit.getInstance().getConfig().getInt("min-event");
    }

    @Override
    public void onActive() {
        isActive = true;
        CC.boardCast("&a&l双倍奖励! &7现在你可以获得双倍的击杀&b经验&7与&6硬币&7加成!");
    }

    @Override
    public void onInactive() {
        isActive = false;
    }

    @Override
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        if (isActive) {
            coins.addAndGet(2 * coins.get());
            experience.addAndGet(2 * experience.get());
        }
    }
}
