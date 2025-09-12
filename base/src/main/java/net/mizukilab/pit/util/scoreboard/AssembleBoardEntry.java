package net.mizukilab.pit.util.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ConcurrentModificationException;

public class AssembleBoardEntry {

    private final AssembleBoard board;
    private final int position;
    private String text, identifier;
    private Team team;

    /**
     * Assemble Board Entry
     *
     * @param board    that entry belongs to.
     * @param text     of entry.
     * @param position of entry.
     */
    public AssembleBoardEntry(AssembleBoard board, String text, int position) {
        this.board = board;
        this.text = text;
        this.position = position;
        this.identifier = this.board.getUniqueIdentifier(position);

        this.setup();
    }

    /**
     * Setup Board Entry.
     */
    public void setup() {
        final Scoreboard scoreboard = this.board.getScoreboard();

        if (scoreboard == null) {
            return;
        }

        String teamName = this.identifier;

        // This shouldn't happen, but just in case.
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        Team team = scoreboard.getTeam(teamName);

        // Register the team if it does not exist.
        if (team == null) {
            try {
                team = scoreboard.registerNewTeam(teamName);
            } catch (IllegalArgumentException e) {
                team = scoreboard.getTeam(teamName);
            }
        }

        // Add the entry to the team.
        if (team.getEntries() == null || team.getEntries().isEmpty() || !team.getEntries().contains(this.identifier)) {
            team.addEntry(this.identifier);
        }

        // Add the entry if it does not exist.
        if (!this.board.getEntries().contains(this)) {
            this.board.getEntries().add(this);
        }

        this.team = team;
    }

    /**
     * Send Board Entry Update.
     *
     * @param position of entry.
     */
    public void send(int position) {
        if (this.text.length() > 16) {
            String prefix = this.text.substring(0, 16);
            String suffix;

            if (prefix.charAt(15) == ChatColor.COLOR_CHAR) {
                prefix = prefix.substring(0, 15);
                suffix = this.text.substring(15);
            } else if (prefix.charAt(14) == ChatColor.COLOR_CHAR) {
                prefix = prefix.substring(0, 14);
                suffix = this.text.substring(14);
            } else {
                if (ChatColor.getLastColors(prefix).equalsIgnoreCase(ChatColor.getLastColors(this.identifier))) {
                    suffix = this.text.substring(16);
                } else {
                    suffix = ChatColor.getLastColors(prefix) + this.text.substring(16);
                }
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }
            setPrefixCheckEqual(team, prefix);
            setSuffixCheckEqual(team, suffix);
        } else {
            setPrefixCheckEqual(team, text);
            setSuffixCheckEqual(team, "");
        }

        Score score = this.board.getObjective().getScore(this.identifier);
        int score1 = score.getScore();
        if (score1 != position) {
            try {
                score.setScore(position);
            } catch (ConcurrentModificationException e){
                return;
            }
        }
    }

    public void setPrefixCheckEqual(Team team, String prefix) {
        if (!team.getPrefix().equals(prefix)) {
            team.setPrefix(prefix);
        }
    }

    public void setSuffixCheckEqual(Team team, String suffix) {
        if (!team.getSuffix().equals(suffix)) {
            team.setSuffix(suffix);
        }
    }

    /**
     * Remove Board Entry from Board.
     */
    public void remove() {
        this.board.getIdentifiers().remove(this.identifier);
        this.board.getScoreboard().resetScores(this.identifier);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
