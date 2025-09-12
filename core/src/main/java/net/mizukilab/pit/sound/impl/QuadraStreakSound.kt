package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

@Skip
object QuadraStreakSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "quadra_streak"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        if (tick == 0) {
            player.playSound(player.location, Sound.ORB_PICKUP, 1f, 1.7f)
        } else if (tick == 2) {
            player.playSound(player.location, Sound.ORB_PICKUP, 1f, 1.8f)
        } else if (tick == 4) {
            player.playSound(player.location, Sound.ORB_PICKUP, 1f, 1.9f)
        } else if (tick == 6) {
            player.playSound(player.location, Sound.ORB_PICKUP, 1f, 2f)
        } else if (tick > 6) {
            end(player)
        }
    }
}