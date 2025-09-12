package net.mizukilab.pit.trade;

import cn.charlotte.pit.data.temp.TradeRequest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/5 0:30
 */
public class TradeMonitorRunnable extends BukkitRunnable {

    private final static List<TradeRequest> tradeRequests = new ObjectArrayList<>();

    @Override
    public void run() {
        tradeRequests.removeIf(tradeRequest -> tradeRequest.getCooldown().hasExpired());
    }

    public static List<TradeRequest> getTradeRequests() {
        return tradeRequests;
    }
}
