package net.mizukilab.pit.util.scoreboard;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.AssemblePostUpdateEvent;
import cn.charlotte.pit.event.AssembleUpdateEvent;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;

public class AssembleThread implements Runnable {

    private final Assemble assemble;

    protected int taskId;

    /**
     * Assemble Thread.
     *
     * @param assemble instance.
     */
    AssembleThread(Assemble assemble) {
        this.assemble = assemble;
//        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
//        if (protocolLib.getDescription().getVersion().startsWith("5")) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(ThePit.getInstance(), this, 0, assemble.getTicks()).getTaskId();
//        } else {
//            taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(ThePit.getInstance(), this, assemble.getTicks(), assemble.getTicks()).getTaskId();
//        } useless check
    }

    @Override
    @SneakyThrows
    public void run() {
        tick();
    }

    /**
     * Tick logic for thread.
     */
    private void tick() {
        this.assemble.getBoards().forEach((uuid, board) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || board == null) {
                return;
            }

            try {
                // This shouldn't happen, but just in case.
                Scoreboard scoreboard = board.getScoreboard();
                Objective objective = board.getObjective();

                if (scoreboard == null || objective == null) {
                    return;
                }

                AssembleUpdateEvent assembleUpdateEvent = new AssembleUpdateEvent(player);
                assembleUpdateEvent.callEvent();
                if (assembleUpdateEvent.isCancelled()) {
                    return;
                }
                // Just make a variable so we don't have to
                // process the same thing twice.
                String title = ChatColor.translateAlternateColorCodes('&', this.assemble.getAdapter().getTitle(player));

                // Update the title if needed.
                if (!objective.getDisplayName().equals(title)) {
                    objective.setDisplayName(title);
                }
                List<String> newLines = this.assemble.getAdapter().getLines(player);

                AssemblePostUpdateEvent assemblePostUpdateEvent = new AssemblePostUpdateEvent(player, newLines);
                assemblePostUpdateEvent.callEvent();
                if (assemblePostUpdateEvent.isCancelled()) {
                    return;
                }
                // Allow adapter to return null/empty list to display nothing.
                if (newLines == null || newLines.isEmpty()) {
                    board.getEntries().removeIf(i -> {
                        i.remove();
                        return true;
                    });
                } else {
                    int size1 = newLines.size();
                    if (size1 > 16) {
                        newLines = newLines.subList(0, 15); //EmptyIrony u sucks
                    }
                    size1 = newLines.size();

                    // Reverse the lines because scoreboard scores are in descending order.
                    boolean descending = this.assemble.getAssembleStyle().isDescending();

                    // Remove excessive amount of board entries.
                    int size = board.getEntries().size();
                    boolean b = size > size1;
                    if (b) {
                        for (int i = size1; i < size; i++) {
                            AssembleBoardEntry entry = board.getEntryAtPosition(i);
                            if (entry != null) {
                                entry.remove();
                            }
                        }
                    }

                    // Update existing entries / add new entries.
                    int cache = descending ? this.assemble.getAssembleStyle().getStartNumber() : size1;
                    for (int i = 0; i < size1; i++) {
                        AssembleBoardEntry entry = board.getEntryAtPosition(i);

                        // Translate any colors.
                        String textToTranslate;
                        try {
                            textToTranslate = newLines.get(i);
                            if (textToTranslate == null) {
                                continue;
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                            continue;
                        }
                        String line = ChatColor.
                                translateAlternateColorCodes('&', textToTranslate);

                        // If the entry is null, just create a new one.
                        // Creating a new AssembleBoardEntry instance will add
                        // itself to the provided board's entries list.
                        if (entry == null) {
                            entry = new AssembleBoardEntry(board, line, i);
                        }

                        // Update text, setup the team, and update the display values.
                        entry.setText(line);
                        entry.setup();
                        entry.send(descending ? cache++ : cache--);
                    }
                }

                if (player.getScoreboard() != scoreboard && !assemble.isHook()) {
                    player.setScoreboard(scoreboard);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssembleException("There was an error updating " + player.getName() + "'s scoreboard.");
            }
        });

    }

}
