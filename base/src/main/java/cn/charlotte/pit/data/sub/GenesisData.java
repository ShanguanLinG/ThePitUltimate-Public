package cn.charlotte.pit.data.sub;

import cn.charlotte.pit.events.genesis.GenesisTeam;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/29 22:05
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GenesisData {

    private int tier = 0;
    private int points = 0;
    private int boostTier = 0;
    private GenesisTeam team = GenesisTeam.NONE;
    private int season = 0;

    public int getTier() {
        return tier;
    }

    public int getPoints() {
        return points;
    }

    public int getBoostTier() {
        return boostTier;
    }

    public GenesisTeam getTeam() {
        return team;
    }

    public int getSeason() {
        return season;
    }
}
