package net.mizukilab.pit.actionbar;

import org.bukkit.entity.Player;

public interface IActionBarManager {

    void tick();

    void addActionBarOnQueue(Player player, String arg, String val, int repeat);

    void addActionBarOnQueue(Player player, String arg, String val, int repeat,boolean flush);
}
