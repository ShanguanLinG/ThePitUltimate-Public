package net.mizukilab.pit.util


import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function

/**
 * @author Araykal
 * @since 2025/4/23
 */
object LeadBoardUtil {

    private fun getPlayerRankingString(rank: Int, statExtractor: (PlayerProfile) -> Int): String {
        val priorityQueue = PriorityQueue(
            Comparator.comparing<Map.Entry<Player, Int>, Int> { it.value }.reversed()
        )

        Bukkit.getOnlinePlayers().forEach { player ->
            val profile = ThePit.getInstance().profileOperator.namedIOperator(player.name).profile()
            priorityQueue.offer(AbstractMap.SimpleEntry(player, statExtractor(profile)))
        }

        if (rank !in 1..priorityQueue.size) return "无"

        repeat(rank - 1) { priorityQueue.poll() }
        val rankedEntry = priorityQueue.poll() ?: return "无"

        return "第 $rank 名 &f- ${ThePit.getInstance().profileOperator.namedIOperator(rankedEntry.key.name).profile().formattedName} &f: &r&e${rankedEntry.value}"
    }

    fun getBountyPlayerString(rank: Int) = getPlayerRankingString(rank) { it.bounty }

    fun getKillsPlayerString(rank: Int) = getPlayerRankingString(rank) { it.kills }

    fun getStreakKillsPlayerString(rank: Int) = getPlayerRankingString(rank) { it.streakKills.toInt() }
}
