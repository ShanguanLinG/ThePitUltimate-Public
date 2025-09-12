package cn.charlotte.pit.event;

import cn.hutool.core.lang.func.Consumer3;
import lombok.Getter;
import lombok.Setter;
import net.mizukilab.pit.item.AbstractPitItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/**
 * 此Event可以用来替换附魔逻辑, 默认consumer为 null, 有拓展需求请替换当前逻辑
 */
@Getter
@Setter
public class StartEnchantLogicEvent extends PitEvent implements Cancellable {
    Player player;
    boolean cancelled;

    boolean allowEnchant = true;
    Consumer3<ItemStack, AbstractPitItem, Player> consumer;
    public StartEnchantLogicEvent(Player player){
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
