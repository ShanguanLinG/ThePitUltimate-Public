package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Log {
    public static boolean log = false;
    public static void WriteLine(String arg){
        if(!log){
            return;
        }
        Player player = Bukkit.getPlayer("KleeLoveLife");
        if(player != null){
            player.sendMessage(CC.translate("&cDEBUG: " + arg));
        }
    }
}
