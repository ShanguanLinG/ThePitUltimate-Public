package net.mizukilab.pit.util.scoreboard;

import lombok.Getter;
import net.mizukilab.pit.util.scoreboard.events.AssembleBoardCreateEvent;
import net.mizukilab.pit.util.scoreboard.events.AssembleBoardDestroyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Getter
public class AssembleListener implements Listener {

    private final Assemble assemble;

    /**
     * Assemble Listener.
     *
     * @param assemble instance.
     */
    public AssembleListener(Assemble assemble) {
        this.assemble = assemble;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AssembleBoardCreateEvent createEvent = new AssembleBoardCreateEvent(player);

        Bukkit.getPluginManager().callEvent(createEvent);
        if (createEvent.isCancelled()) {
            return;
        }
        getAssemble().getBoards().put(player.getUniqueId(), new AssembleBoard(player, getAssemble()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        AssembleBoardDestroyEvent destroyEvent = new AssembleBoardDestroyEvent(event.getPlayer());

        Bukkit.getPluginManager().callEvent(destroyEvent);
        if (destroyEvent.isCancelled()) {
            return;
        }

        getAssemble().getBoards().remove(event.getPlayer().getUniqueId());
        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

}
