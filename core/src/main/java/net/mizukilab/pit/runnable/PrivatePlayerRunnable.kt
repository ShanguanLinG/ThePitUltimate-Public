package net.mizukilab.pit.runnable

import net.mizukilab.pit.util.isPrivate
import nya.Skip
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
@Skip
object PrivatePlayerRunnable : BukkitRunnable() {


    override fun run() {
        val onlinePlayers = Bukkit.getOnlinePlayers()
        val specialPlayers = onlinePlayers.filter { it.isPrivate }
        val normalPlayers = onlinePlayers.filter { !it.isPrivate }


        specialPlayers.forEachIndexed { _, special ->
            normalPlayers.forEach { normal ->
                if (special.canSee(normal)) {
                    special.hidePlayer(normal)
                }
                if (normal.canSee(special)) {
                    if (normal.hasPermission("pit.admin") && !normal.isPrivate) {
                        return@forEach
                    }
                    normal.hidePlayer(special)
                }
            }
        }
    }
}