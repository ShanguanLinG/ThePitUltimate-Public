package net.mizukilab.pit.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2021/4/2 23:40
 */
public class ProfileLoadRunnable extends BukkitRunnable { //这是什么用的? 没啥用(个人猜测AntiDupe

    @Getter
    private static ProfileLoadRunnable instance;
    @Getter
    private final Map<UUID, Cooldown> cooldownMap = new ConcurrentHashMap<>();

    public ProfileLoadRunnable(ThePit plugin) {
        instance = this;
        this.runTaskTimerAsynchronously(plugin, 20, 20);
    }


    @Override
    public void run() {
        Iterator<Map.Entry<UUID, Cooldown>> iterator = cooldownMap.entrySet().iterator();
        while(iterator.hasNext()){
            tickLoad(iterator);
        }
    }

    private void tickLoad(Iterator<Map.Entry<UUID, Cooldown>> iterator) {
        Map.Entry<UUID, Cooldown> entry = iterator.next();
        final Player player = Bukkit.getPlayer(entry.getKey());
        if (player == null || !player.isOnline()) {
            iterator.remove();
            return;
        }

        final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        if (profile.isLoaded()) {
            iterator.remove();
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP);
            return;
        }

        if (entry.getValue().hasExpired()) {
            player.sendMessage(CC.translate("&c档案加载异常,请尝试重新进入!"));
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(player.getName());
            out.writeUTF("L_MainLobby#1"); //大唐王朝大厅。
            Objects.requireNonNull(Iterables.getFirst(Bukkit.getOnlinePlayers(), null)).sendPluginMessage(ThePit.getInstance(), "BungeeCord", out.toByteArray());
            iterator.remove();
        }
    }

    public void handleJoin(Player player) {
        cooldownMap.put(player.getUniqueId(), new Cooldown(1, TimeUnit.MINUTES));
    }

    public void handleQuit(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }
}
