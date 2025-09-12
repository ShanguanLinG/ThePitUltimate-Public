package net.mizukilab.pit.util.nametag;

import io.irina.backports.utils.SWMRHashTable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class NametagHandler {

    private JavaPlugin plugin;

    private NametagAdapter adapter;
    private NametagThread thread;
    private NametagListeners listeners;

    private Map<UUID, NametagBoard> boards;
    public long ticks = 2;
    private boolean hook = false;

    /**
     * Nametag Handler.
     *
     * @param plugin  instance.
     * @param adapter to display nametags.
     */
    public NametagHandler(JavaPlugin plugin, NametagAdapter adapter) {
        if (plugin == null) {
            throw new RuntimeException("Nametag Handler can not be instantiated without a plugin instance!");
        }

        this.plugin = plugin;
        this.adapter = adapter;
        this.boards = new SWMRHashTable<>();

        this.setup();
    }

    /**
     * Setup Library.
     */
    public void setup() {
        // Register Events.
        this.listeners = new NametagListeners(this);
        this.plugin.getServer().getPluginManager().registerEvents(this.listeners, this.plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            getBoards().putIfAbsent(player.getUniqueId(), new NametagBoard(player, this));
        }

        this.thread = new NametagThread(this);
    }

    /**
     * Cleanup Library.
     */
    public void cleanup() {
        // Unregister Thread.
        if (this.thread != null) {
            this.thread.cancel();
            this.thread = null;
        }

        // Unregister Listeners.
        if (this.listeners != null) {
            HandlerList.unregisterAll(this.listeners);
            this.listeners = null;
        }

        // Destroy boards.
        for (UUID uuid : getBoards().keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            getBoards().remove(uuid);
            if (!isHook()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

}
