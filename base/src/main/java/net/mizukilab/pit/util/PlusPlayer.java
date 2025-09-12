package net.mizukilab.pit.util;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlusPlayer {

    public static List<String> PlusPlayer = new ArrayList<>();

    public static boolean on = true;

    public static double probability = 0.50;

    public static boolean isPlusPlayer(Player player) {
        return getPlusPlayer().contains(player.getName());
    }

    public static List<String> getPlusPlayer() {
        return PlusPlayer;
    }
}
