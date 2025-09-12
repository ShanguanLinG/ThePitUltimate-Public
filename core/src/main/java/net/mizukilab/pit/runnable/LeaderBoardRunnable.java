package net.mizukilab.pit.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.LeaderBoardEntry;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import nya.Skip;
import org.bson.Document;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/3 12:57
 */
@Skip
public class LeaderBoardRunnable extends BukkitRunnable {

    @SneakyThrows
    @Override
    public void run() {

        updateLeaderboardData();
    }

    public static void updateLeaderboardData() {
        FindIterable<Document> documents = ThePit.getInstance()
                .getMongoDB()
                .getCollection()
                .find()
                .sort(Filters.eq("totalExp", -1))
                .filter(Filters.gte("lastLogoutTime", System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));

        List<LeaderBoardEntry> entries = new ObjectArrayList<>();
        int i = 1;
        for (Document document : documents) {
            try {
                String name = document.getString("playerName");
                String uuid = document.getString("uuid");
                final Object expObj = document.get("experience");
                Double experience;
                try {
                    experience = (Double) expObj;
                } catch (Exception e) {
                    experience = Double.valueOf(((Integer) expObj));
                }
                int prestige = document.getInteger("prestige");
                int rank = i;
                entries.add(new LeaderBoardEntry(name, UUID.fromString(uuid), rank, experience, prestige));

                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LeaderBoardEntry.setLeaderBoardEntries(entries);
    }
}
