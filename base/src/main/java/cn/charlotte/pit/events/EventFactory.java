package cn.charlotte.pit.events;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.events.trigger.type.IEpicEvent;
import cn.charlotte.pit.events.trigger.type.INormalEvent;
import cn.charlotte.pit.events.trigger.type.addon.IPreparative;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.mizukilab.pit.config.PitGlobalConfig;
import net.mizukilab.pit.util.bossbar.BossBar;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Sound.BURP;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/9 12:59
 */
public class EventFactory {

    private final List<INormalEvent> normalEvents;
    private final List<IEpicEvent> epicEvents;
    private INormalEvent activeNormalEvent;
    private IEpicEvent activeEpicEvent;
    private Cooldown normalEnd;
    private long lastNormalEvent;

    private IEpicEvent nextEpicEvent;
    private Cooldown nextEpicEventTimer;

    EventTimer eventTimer;

    public EventFactory() {
        this.normalEvents = new ObjectArrayList<>();
        this.epicEvents = new ObjectArrayList<>();
        this.normalEnd = new Cooldown(0);
    }
    public EventTimer getEventTimer(){
        return eventTimer;
    }

    public void pushEvent(IEpicEvent event) {
        pushEvent(event, false);
    }

    public void pushEvent(IEpicEvent event, boolean force) {
        if (force || Bukkit.getOnlinePlayers().size() >= ((AbstractEvent) event).requireOnline()) {
            readyEpicEvent(event);
        }
    }

    public void pushEvent(INormalEvent event) {
        pushEvent(event, false);
    }

    public void pushEvent(INormalEvent event, boolean force) {
        if (Bukkit.getOnlinePlayers().size() >= ((AbstractEvent) event).requireOnline()) {
            activeEvent(event);
        }
    }

    @SneakyThrows
    public void init(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (IEpicEvent.class.isAssignableFrom(clazz)) {
                try {
                    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    this.epicEvents.add((IEpicEvent) declaredConstructor.newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (INormalEvent.class.isAssignableFrom(clazz)) {
                try {
                    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    this.normalEvents.add((INormalEvent) declaredConstructor.newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        Bukkit.getScheduler()
                .runTaskTimerAsynchronously(ThePit.getInstance(), eventTimer = new EventTimer(), 20, 20);

    }

    public String getActiveEpicEventName() {
        if (activeEpicEvent == null) {
            return null;
        }
        return ((AbstractEvent) activeEpicEvent).getEventInternalName();
    }

    public String getActiveNormalEventName() {
        if (activeNormalEvent == null) {
            return null;
        }
        return ((AbstractEvent) activeNormalEvent).getEventInternalName();
    }

    public void activeEvent(INormalEvent event) {
        AbstractEvent iEvent = (AbstractEvent) event;
        iEvent.onActive();
        iEvent.setActive(true);
        this.lastNormalEvent = System.currentTimeMillis();
        this.activeNormalEvent = event;
        this.normalEnd = new Cooldown(5, TimeUnit.MINUTES);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!event.equals(activeNormalEvent)) {
                    ThePit.getInstance()
                            .getBossBar()
                            .getBossBar()
                            .setTitle("");
                    cancel();
                    return;
                }

                if (normalEnd.hasExpired()) {
                    ThePit.getInstance()
                            .getBossBar()
                            .getBossBar()
                            .setTitle("");
                    cancel();
                    return;
                }

                if (tick >= 15) {
                    tick = 0;
                }
                String start;
                if (tick % 2 == 0) {
                    start = "&a&l小型事件! ";
                } else {
                    start = "&2&l小型事件! ";
                }

                final String title = CC.translate(start + "&6&l" + iEvent.getEventName() + " &7将在 &e" + TimeUtil.millisToTimer(normalEnd.getRemaining()) + " &7后结束!");

                BossBar bossBar = ThePit.getInstance()
                        .getBossBar()
                        .getBossBar();
                bossBar
                        .setTitle(title);
                bossBar.setProgress(normalEnd.getRemaining() / (5 * 1000 * 60f));

                tick++;
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(), 10, 10);
    }

    public void inactiveEvent(INormalEvent event) {
        if (activeNormalEvent != event) return;

        this.activeNormalEvent = null;
        AbstractEvent iEvent = (AbstractEvent) event;
        iEvent.setActive(false);
        iEvent.onInactive();
    }

    public void safeInactiveEvent(INormalEvent event) {
        if (activeNormalEvent != event) return;
        this.normalEnd.fastExpired();
        eventTimer.getCooldown().fastExpired();
    }

    public void cooldown() {
        eventTimer.setCooldown(new Cooldown(1, TimeUnit.MINUTES));
    }

    public void readyEpicEvent(IEpicEvent event) {
        this.nextEpicEvent = event;
        this.nextEpicEventTimer = new Cooldown(5, TimeUnit.MINUTES);
        AbstractEvent iEvent = (AbstractEvent) event;

        if (event instanceof IPreparative) {
            ((IPreparative) event).onPreActive();
        }
        PitGlobalConfig pitWorldConfig = ThePit.getInstance().getGlobalConfig();
        List<String> animationForEpicEvent = pitWorldConfig.animationForEpicEvent;
        int periodForEpicEvent = pitWorldConfig.periodForEpicEvent;
        new BukkitRunnable() {
            Iterator<String> iterator = animationForEpicEvent.iterator();
            @Override
            public void run() {
                if (nextEpicEventTimer.hasExpired()) {
                    ThePit.getInstance()
                            .getBossBar()
                            .getBossBar()
                            .setTitle("");
                    Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> activeEvent(event));
                    cancel();
                    return;
                }
                if(!iterator.hasNext()){
                    iterator = animationForEpicEvent.iterator();
                }
                String start = "Epic Event Bossbar";
                if(iterator.hasNext()) {
                     start = iterator.next(); //check
                }
                final String title = CC.translate(
                        start + "&6&l" + iEvent.getEventName() + " &7将在 &e" + TimeUtil.millisToTimer(nextEpicEventTimer.getRemaining()) + " &7后开始!");

                ThePit.getInstance()
                        .getBossBar()
                        .getBossBar()
                        .setTitle(title);
                ThePit.getInstance()
                        .getBossBar()
                        .getBossBar()
                        .setProgress(nextEpicEventTimer.getRemaining() / (5 * 1000 * 60f));
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(), 10, periodForEpicEvent);
    }

    public void activeEvent(IEpicEvent event) {
        AbstractEvent iEvent = (AbstractEvent) event;
        iEvent.onActive();
        iEvent.setActive(true);
        this.nextEpicEvent = null;
        this.activeEpicEvent = event;
        this.normalEnd = new Cooldown(5, TimeUnit.MINUTES);
    }

    public void inactiveEvent(IEpicEvent event) {
        if (activeEpicEvent != event) return;

        this.activeEpicEvent = null;
        AbstractEvent iEvent = (AbstractEvent) event;
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), BURP, 1, 0.72F));
        iEvent.setActive(false);
        iEvent.onInactive();
    }

    public List<INormalEvent> getNormalEvents() {
        return normalEvents;
    }

    public List<IEpicEvent> getEpicEvents() {
        return epicEvents;
    }

    public INormalEvent getActiveNormalEvent() {
        return activeNormalEvent;
    }

    public IEpicEvent getActiveEpicEvent() {
        return activeEpicEvent;
    }

    public Cooldown getNormalEnd() {
        return normalEnd;
    }

    public void setNormalEnd(Cooldown cooldown) {
        this.normalEnd = cooldown;
    }

    public long getLastNormalEvent() {
        return lastNormalEvent;
    }

    public IEpicEvent getNextEpicEvent() {
        return nextEpicEvent;
    }

    public Cooldown getNextEpicEventTimer() {
        return nextEpicEventTimer;
    }
}
