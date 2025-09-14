package net.mizukilab.pit.config;

import lombok.Getter;
import lombok.Setter;
import net.mizukilab.pit.util.configuration.Configuration;
import net.mizukilab.pit.util.configuration.annotations.ConfigData;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PitGlobalConfig extends Configuration {
    public PitGlobalConfig(JavaPlugin plugin) {
        super(plugin,"config.yml");
    }
    @Setter
    @Getter
    @ConfigData(
            path = "genesis-start-date"
    )

    private long genesisStartDate = 1675339795842L;

    @Setter
    @Getter
    @ConfigData(
            path = "debug.debugServer"
    )
    private boolean debugServer;
    @Setter
    @Getter
    @ConfigData(
            path = "debug.public"
    )
    private boolean debugServerPublic;

    @Setter
    @Getter
    @ConfigData(
            path = "curfew.enable"
    )
    private boolean curfewEnable;
    @Setter
    @Getter
    @ConfigData(
            path = "curfew.start"
    )
    private int curfewStart;
    @Setter
    @Getter
    @ConfigData(
            path = "curfew.end"
    )
    private int curfewEnd;
    @Setter
    @Getter
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public int maxLevel = 130; //Mirror
    @Setter
    @Getter
    private boolean tradeEnable = true;
    @Setter
    @Getter
    private boolean PVPEnable = true;
    public List<String> animationForEpicEvent;

    public int periodForEpicEvent;
    @Getter
    @ConfigData(
            path = "eigenToken"
    )
    private String token;
    @Setter
    @Getter
    @ConfigData(
            path = "service.mongodb.ip"
    )
    private String mongoDBAddress;
    @Setter
    @Getter
    @ConfigData(
            path = "service.mongodb.port"
    )
    private int mongoDBPort;

    @Setter
    @Getter
    @ConfigData(
            path = "service.mongodb.database"
    )
    private String databaseName;

    @Setter
    @Getter
    @ConfigData(
            path = "service.mongodb.user"
    )
    private String mongoUser;

    @Setter
    @Getter
    @ConfigData(
            path = "service.mongodb.password"
    )
    private String mongoPassword;

    @Setter
    @Getter
    @ConfigData(
            path = "scoreboard.viewSwitchMapCooldownPre30s"
    )
    private boolean cooldownView;
    @Setter
    @Getter
    @ConfigData(path = "server-name")
    private String serverName = "&e天坑乱斗";

    @Setter
    @Getter
    @ConfigData(path = "bot-name")
    private String bot_name = "&cBot";

    @Setter
    @Getter
    @ConfigData(path = "currentMapId")
    private long currentMapId = 0;

    @Setter
    @Getter
    @ConfigData(path = "duration")
    private long duration = 60000;
    @Setter
    @Getter
    @ConfigData(path = "startDate")
    private long startDate = System.currentTimeMillis();

    public boolean isGenesisEnable() {
        try {
            return System.currentTimeMillis() >= getGenesisStartTime() && System.currentTimeMillis() < getGenesisEndTime();
        } catch (Exception ignored) {
            return false;
        }
    }

    @Setter
    @Getter
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");

    public long getGenesisStartTime() {
        return genesisStartDate;
    }

    public long getGenesisOriginalEndTime() {
        return getGenesisStartTime() + 16 * 24 * 60 * 60 * 1000;
    }

    public long getGenesisEndTime() {
        long endTime = getGenesisOriginalEndTime();
        while (endTime < System.currentTimeMillis()) {
            endTime += 56 * 24 * 60 * 60 * 1000L;
        }
        return endTime;
    }

    //Season X: From Season X-1 End To Season X End
    public int getGenesisSeason() {
        int season = 1;
        long endTime = getGenesisOriginalEndTime();
        while (endTime < System.currentTimeMillis()) {
            endTime += 56L * 24 * 60 * 60 * 1000;
            season++;
        }
        return season;
    }

}
