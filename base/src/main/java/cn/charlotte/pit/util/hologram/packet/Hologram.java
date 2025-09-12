package cn.charlotte.pit.util.hologram.packet;

import org.bukkit.Location;

/**
 * 2022/11/14<br>
 * LimeCode<br>
 *
 * @author huanmeng_qwq
 */
public class Hologram extends PacketArmorStand {

    public Hologram(String text, Location location) {
        super(text, location);
    }

    public void init() {
        super.init();
        invisible();
        small();
        entity.setMarker(true);
        entity.setBasePlate(false);
    }
}
