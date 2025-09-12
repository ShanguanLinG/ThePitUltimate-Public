package net.mizukilab.pit.tab

import cn.charlotte.pit.ThePit
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.WrappedChatComponent
import me.clip.placeholderapi.PlaceholderAPI
import net.mizukilab.pit.config.TabConfiguration.animation
import net.mizukilab.pit.config.TabConfiguration.delay
import net.mizukilab.pit.config.TabConfiguration.head
import net.mizukilab.pit.config.TabConfiguration.part
import net.mizukilab.pit.config.TabConfiguration.tick
import nya.Skip
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author Araykal
 * @since 2025/5/12
 */
@Skip
class TabHandle {
    var counter = 0

    fun fetchTab() {
        object : BukkitRunnable() {
            override fun run() {
                updateTabListForAllPlayers()
                counter++
            }
        }.runTaskTimerAsynchronously(
            ThePit.getInstance(),
            delay.toLong(),
            tick.toLong()
        )
    }

    private fun updateTabListForAllPlayers() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return
        }
        for (player in Bukkit.getOnlinePlayers()) {
            updatePlayerTabList(player)
        }
    }

    private fun updatePlayerTabList(player: Player) {
        val currentValues = mutableMapOf<String, String>()

        for ((key, values) in animation) {
            if (values.isNotEmpty()) {
                currentValues["{$key}"] = values[counter % values.size]
            }
        }

        val headerText = replaceVariables(head, currentValues)
        val footerText = replaceVariables(part, currentValues)

        try {
            val packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)
            packet.chatComponents.write(
                0,
                WrappedChatComponent.fromText(
                    PlaceholderAPI.setPlaceholders(player, headerText.replace("&", "§").replace("/s", "§r"))
                )
            )
            packet.chatComponents.write(
                1,
                WrappedChatComponent.fromText(
                    PlaceholderAPI.setPlaceholders(player, footerText.replace("&", "§").replace("/s", "§r"))
                )
            )
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
        } catch (e: Exception) {
            player.sendMessage("Error: §${e.message}")
            e.printStackTrace()
        }
    }

    private fun replaceVariables(lines: List<String>, variables: Map<String, String>): String {
        val builder = StringBuilder()
        for (line in lines) {
            var modifiedLine = line
            for ((key, value) in variables) {
                modifiedLine = modifiedLine.replace(key, "$value&r")
            }
            builder.append(modifiedLine).append("\n")
        }
        return builder.toString().trim()
    }
}
