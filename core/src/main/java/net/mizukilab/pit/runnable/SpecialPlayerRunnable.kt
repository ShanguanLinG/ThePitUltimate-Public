package net.mizukilab.pit.runnable

import net.mizukilab.pit.util.isSpecial
import nya.Skip
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * 2024/5/28<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */
@Skip
object SpecialPlayerRunnable : BukkitRunnable() {
    override fun run() {
        val onlinePlayers = Bukkit.getOnlinePlayers()
        val specialPlayers = onlinePlayers.filter { it.isSpecial }
        val normalPlayers = onlinePlayers.filter { !it.isSpecial }
        specialPlayers.forEachIndexed { _, special ->
            normalPlayers.forEach { normal ->
                if (special.canSee(normal)) {
                    special.hidePlayer(normal)
                }
                if (normal.canSee(special)) {
                    if (normal.hasPermission("pit.admin") && !normal.isSpecial) {
                        return@forEach
                    }

                    normal.hidePlayer(special)
                }
            }
        }
    }
}