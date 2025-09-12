package cn.charlotte.pit.util.hologram.packet

import cn.hutool.core.collection.ConcurrentHashSet
import org.bukkit.scheduler.BukkitRunnable

class PacketHologramRunnable : BukkitRunnable() {
    override fun run() {
        holograms.removeIf {
            !it.spawned
        }
        holograms.forEach {
            it.update()
        }
    }

    companion object {
        val holograms: MutableSet<PacketHologram> = ConcurrentHashSet()

        @JvmStatic
        fun deSpawnAll() {
            holograms.forEach {
                it.hologram.entity.remove()
            }
            holograms.clear()
        }
    }
}