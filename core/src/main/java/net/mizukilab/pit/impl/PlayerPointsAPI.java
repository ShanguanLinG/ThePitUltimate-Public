package net.mizukilab.pit.impl;

import cn.charlotte.pit.api.PointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PlayerPointsAPI {
    public static PointsAPI API;
    public static void init(){
        Plugin playerPoints = Bukkit.getPluginManager()
                .getPlugin("PlayerPoints");
        if(playerPoints != null){
            API = PlayerPointsAPIImpl.INSTANCE;
        } else{
            API = new NullPlayerPointsAPIImpl();
        }
    }
}
