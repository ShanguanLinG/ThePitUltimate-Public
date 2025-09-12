package net.mizukilab.pit.runnable


import nya.Skip
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

@Skip
object AnnouncementRunnable : BukkitRunnable() {
    private var index = 0;
    val announcement = listOf(
        "&e&l公告! &fNyacho天坑乱斗官方交流群: 697126758 &f欢迎您的加入~",
        "&e&l公告! &f发现BUG找管理员举报可以领取奖励哦~",
        "&c&l警告! &f请仔细甄别诈骗, 并且本服禁止私下 &cRMB " +
                "&f交易, 若被骗管理员不负责"
    ).map { it.replace("&", "§") }

    override fun run() {
        Bukkit.broadcastMessage(announcement[index])
        index = (index + 1) % announcement.size
    }
}