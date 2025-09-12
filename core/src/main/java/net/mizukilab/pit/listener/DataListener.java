package net.mizukilab.pit.listener;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.FixedRewardData;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PlayerInv;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.mizukilab.pit.data.operator.PackedOperator;
import net.mizukilab.pit.data.operator.ProfileOperator;
import net.mizukilab.pit.util.PitProfileUpdater;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @Author: KleeLoveLife
 * @Date: 2025/4/12 10:54
 */
//@AutoRegister
public class DataListener implements Listener {

    public DataListener() {

    }
}
