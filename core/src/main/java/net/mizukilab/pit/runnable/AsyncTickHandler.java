package net.mizukilab.pit.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.PitHook;
import net.mizukilab.pit.actionbar.ActionBarManager;
import net.mizukilab.pit.data.operator.ProfileOperator;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.util.PublicUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Async tick handler
 */
@Skip
public class AsyncTickHandler extends BukkitRunnable {


    ThePit instance = ThePit.getInstance();

    private long tick = 0;

    public void flushIds() {
        PublicUtil.itemVersion = PitHook.getItemVersion();
        PublicUtil.signVer = PitHook.getGitVersion();
    }

    public AsyncTickHandler() {
        flushIds();
    }

    @Override
    public void run() {
        //trade
        ActionBarManager actionBarManager = (ActionBarManager) instance.getActionBarManager();
        if (actionBarManager != null) {
            actionBarManager.tick();
        }
        if (tick % 10 == 0) {
            //Async Lru Detector
            ItemFactory itemFactory = (ItemFactory) instance.getItemFactory();
            itemFactory.lru();
            flushIds();
        }
        if (++tick == Long.MIN_VALUE) {
            tick = 0; //从头开始
        }
        if (tick > 1200 && tick % 6000 == 0) {
            //AutoSave
            doAutoSave();
            return;
        }
        //Async Io Tracker
        ((ProfileOperator) instance.getProfileOperator()).tick();
    }

    public void doAutoSave() {
        final long last = System.currentTimeMillis();
        instance.getProfileOperator().doSaveProfiles();


        final long now = System.currentTimeMillis();
        Bukkit.getLogger().info("Auto saved player backups, time: " + (now - last) + "ms");
        Bukkit.getOnlinePlayers().forEach(player -> {

            if (player.hasPermission("pit.admin")) return;
            ((ProfileOperator) instance.getProfileOperator()).operatorStrict(player).ifPresent(operator -> {
                PlayerProfile playerProfileByUuid = operator.profile();
                if (playerProfileByUuid.getCombatTimer().hasExpired()) {
                    if (player.getLastDamageCause() != null) {
                        player.setLastDamageCause(null); //fix memory leak
                    }
                }
                final long lastActionTimestamp = playerProfileByUuid
                        .getLastActionTimestamp();
                //AntiAFK
                if (now - lastActionTimestamp >= 10 * 60 * 1000) {
                    // 意义不明
                    // player.sendMessage("...", true);
                    operator.pending(i -> {
                        playerProfileByUuid.setLastActionTimestamp(now);
                    });
                }
            });

        });
    }
}
