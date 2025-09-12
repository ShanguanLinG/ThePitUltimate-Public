package cn.charlotte.pit.buff.impl;

import cn.charlotte.pit.buff.AbstractPitBuff;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.PitRegainHealthEvent;
import net.mizukilab.pit.parm.AutoRegister;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Collections;
import java.util.List;

@AutoRegister
public class CoagulationBuff extends AbstractPitBuff implements Listener {

    @EventHandler
    public void onRegain(PitRegainHealthEvent event) {
        PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(event.getPlayer().getUniqueId());
        event.setAmount(event.getAmount() * Math.max(0.0D, 1.0D - 1.0D * playerProfile.getBuffData().getBuff(getInternalBuffName()).getTier()));
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {

        if (event.getEntity() instanceof Player) {
            PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(event.getEntity().getUniqueId());
            event.setAmount(event.getAmount() * Math.max(0.0D, 1.0D - 1.0D * playerProfile.getBuffData().getBuff(getInternalBuffName()).getTier()));
        }
    }

    @Override
    public String getInternalBuffName() {
        return "rotten_de_buff";
    }

    @Override
    public String getDisplayName() {
        return "&4凝血";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList("&7丧失血量恢复能力, 不可叠加");
    }
}
