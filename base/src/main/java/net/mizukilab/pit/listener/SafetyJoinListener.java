package net.mizukilab.pit.listener;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class SafetyJoinListener implements Listener {

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (ThePit.getApi() == null || !ThePit.getApi().isLoaded()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, CC.translate("&c天坑仍然在启动中...如长时间提示该消息则无法连接至天坑验证服务器"));
        }
    }

}
