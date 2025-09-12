package net.mizukilab.pit.util;

import cn.charlotte.pit.data.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * 2024/5/28<br>
 * ThePitPlus<br>
 *
 * @author huanmeng_qwq
 */
public class SpecialUtil {

    public static Set<String> PRIVATE = new HashSet<>();

    private static Set<String> SPECIALS = new HashSet<String>();
    private static Set<String> BLACKS = new HashSet<>();

    static { 
/*        SPECIALS.add("Love_E");
        SPECIALS.add("114514211");*/
    }

    public static String addPlayer(Player player) {
        if (!PRIVATE.contains(player.getName())) {
            PRIVATE.add(player.getName());
            return "§a进入单人模式状态！";
        }
        return "§c已处于单人模式！";
    }

    public static String removePlayer(Player player) {
        if (PRIVATE.contains(player.getName())) {
            PRIVATE.remove(player.getName());
            return "§a进入多人模式状态，请重进游戏！";
        }
        return "§c已处于多人模式！";
    }

    public static boolean isSpecial(String str) {
        return SPECIALS.contains(str);
    }

    public static boolean isSpecial(Player player) {
        return isSpecial(player.getName());
    }

    public static boolean isSpecial(PlayerProfile profile) {
        return isSpecial(profile.getPlayerName());
    }

    public static boolean isPrivate(String str) {
        return PRIVATE.contains(str);
    }

    public static boolean isPrivate(Player player) {
        return isPrivate(player.getName());
    }

    public static boolean isPrivate(PlayerProfile profile) {
        return isPrivate(profile.getPlayerName());
    }

    public static boolean isBlacks(String str) {
        return BLACKS.contains(str);
    }

    public static boolean isBlacks(Player player) {
        return isBlacks(player.getName());
    }

    public static boolean isBlacks(PlayerProfile profile) {
        return isBlacks(profile.getPlayerName());
    }
}
