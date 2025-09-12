package cn.charlotte.pit.event;

import lombok.Getter;
import net.mizukilab.pit.config.ConfigManager;
import net.mizukilab.pit.config.PitWorldConfig;

@Getter
public class MapSwitchEvent extends PitEvent{
    PitWorldConfig pitWorldConfig;
    ConfigManager pitConfigManager;
    public MapSwitchEvent(PitWorldConfig pitWorldConfig,ConfigManager pitConfigManager){
        this.pitWorldConfig = pitWorldConfig;
        this.pitConfigManager = pitConfigManager;
    }
    public void setCursor(int cursor){
        this.pitConfigManager.setCursor(cursor);
    }
}
