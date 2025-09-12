package net.mizukilab.pit.util;

import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Araykal
 * @since 2025/1/17
 */
public class VisibleApi {
    private static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void hideArmor(Player player) {
        ItemStack nmsCopy = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));
        hideNameTag(player);
        for(int i = 1; i <= 4; ++i) {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), i, nmsCopy);

            for(Player target : Bukkit.getOnlinePlayers()) {
                if (!target.equals(player)) {
                    sendPacket(target, packet);
                }
            }
        }

    }

    public static void hidePlayerFromAll(Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(target)) {
                player.hidePlayer(target);
            }
        }
    }

    public static void showPlayerToAll(Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(target)) {
                player.showPlayer(target);
            }
        }
    }
    private static void hideNameTag(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("hiddenNameTags");

        if (team == null) {
            team = scoreboard.registerNewTeam("hiddenNameTags");
            team.setNameTagVisibility(NameTagVisibility.NEVER);
        }

        team.addEntry(player.getName());
    }


    private static void showNameTag(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("hiddenNameTags");

        if (team != null && team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
        }
    }
    public static void showArmor(Player player) {
        List<PacketPlayOutEntityEquipment> packets = new ArrayList();
        packets.add(new PacketPlayOutEntityEquipment(player.getEntityId(), 1, CraftItemStack.asNMSCopy(player.getEquipment().getBoots())));
        packets.add(new PacketPlayOutEntityEquipment(player.getEntityId(), 2, CraftItemStack.asNMSCopy(player.getEquipment().getLeggings())));
        packets.add(new PacketPlayOutEntityEquipment(player.getEntityId(), 3, CraftItemStack.asNMSCopy(player.getEquipment().getChestplate())));
        packets.add(new PacketPlayOutEntityEquipment(player.getEntityId(), 4, CraftItemStack.asNMSCopy(player.getEquipment().getHelmet())));
        showNameTag(player);
        for(Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player)) {
                for(PacketPlayOutEntityEquipment packet : packets) {
                    sendPacket(target, packet);
                }
            }
        }

    }

}
