package net.mizukilab.pit.events.impl.major;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.ItemLiveDropEvent;
import cn.charlotte.pit.events.trigger.type.IEpicEvent;
import cn.charlotte.pit.events.AbstractEvent;
import cn.charlotte.pit.events.trigger.type.addon.IScoreBoardInsert;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.chat.MessageType;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RespawnFamilyEvent extends AbstractEvent implements IEpicEvent, Listener, IScoreBoardInsert {

    private Cooldown timer;
    private BukkitRunnable runnable;

    @Override
    public String getEventInternalName() {
        return "respawn_family";
    }

    @Override
    public String getEventName() {
        return "庇护";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    @EventHandler
    public void onItemLiveDrop(ItemLiveDropEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void onActive() {
        this.timer = new Cooldown(5, TimeUnit.MINUTES);
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer.hasExpired()) {
                    cancel();
                    if (RespawnFamilyEvent.this.equals(ThePit.getInstance().getEventFactory().getActiveEpicEvent())) {
                        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                            ThePit.getInstance()
                                    .getEventFactory()
                                    .inactiveEvent(RespawnFamilyEvent.this);
                        });
                    }
                }
                ThePit.getInstance()
                        .getBossBar()
                        .getBossBar()
                        .setTitle(CC.translate("&5&l大型事件! &6&l" + getEventName() + " &7将在 &a" + TimeUtil.millisToTimer(timer.getRemaining()) + "&7 后结束!"));
                ThePit.getInstance()
                        .getBossBar()
                        .getBossBar()
                        .setProgress(timer.getRemaining() / (1000 * 60 * 5f));
            }
        };
        this.runnable.runTaskTimer(ThePit.getInstance(), 20, 10);

        CC.boardCast(MessageType.EVENT, "&a&l大型事件! &7庇护，现在所有人都不会掉毛,掉命。");
        Bukkit.getPluginManager().registerEvents(this, ThePit.getInstance());
    }

    @Override
    public void onInactive() {
        HandlerList.unregisterAll(this);

    }

    @Override
    public List<String> insert(Player player) {
        return List.of("&f剩余时间: &a" + TimeUtil.millisToTimer(timer.getRemaining()), "&c不存在掉命掉毛现象");
    }
}
