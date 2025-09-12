package net.mizukilab.pit.park;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.park.IParker;
import net.mizukilab.pit.parm.listener.ITickTask;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
@Skip
public class Parker implements IParker {
    Set<Player> players = new HashSet<>();
    public void tick(){
        players.removeIf(i -> {
            boolean online = i.isOnline();
            if(online){
                hide(i);
            } else {
                show(i);
            }
            return !online;
        });
    }

    private static void hide(Player i) {
        Bukkit.getOnlinePlayers().forEach(b -> {
            if(b != i){
                b.hidePlayer(i);
            }
        });
    }

    public void showAlways(Player p){
        if(Bukkit.isPrimaryThread()) {
            players.remove(p);
            show(p);
            return;
        }
        Bukkit.getScheduler().runTask(ThePit.getInstance(),() -> showAlways(p));
    };
    private static void show(Player i) {
        Bukkit.getOnlinePlayers().forEach(b -> b.showPlayer(i));
    }

    public void hideAlways(Player p){
        if(Bukkit.isPrimaryThread()) {
            players.add(p);
            hide(p);
            return;
        }
        Bukkit.getScheduler().runTask(ThePit.getInstance(),() -> hideAlways(p));
    }
}
