package net.mizukilab.pit.impl

import cn.charlotte.pit.api.PointsAPI
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerPointsAPIImpl : PointsAPI {
    private val pointsAPI by lazy {
        (Bukkit.getPluginManager()
            .getPlugin("PlayerPoints") as? PlayerPoints)
            ?.api
    }

    override fun hasPoints(player: Player, points: Int): Boolean =
        (pointsAPI?.look(player.uniqueId) ?: 0) >= points

    override fun getPoints(player: Player): Int =
        pointsAPI?.look(player.uniqueId) ?: 0

    override fun costPoints(player: Player, points: Int) {
        pointsAPI?.take(player.uniqueId, points)
    }
}