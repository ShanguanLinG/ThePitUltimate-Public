package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 水晶钟声音效 - 清脆悦耳的水晶共鸣
 */
@Skip
object CrystalChimeSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "crystal_chime"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 2.0f)
            1 -> player.playSound(player.location, Sound.NOTE_PLING, 0.8f, 1.8f)
            2 -> player.playSound(player.location, Sound.NOTE_PLING, 0.6f, 1.6f)
            4 -> {
                player.playSound(player.location, Sound.NOTE_PLING, 1.2f, 1.9f)
                player.playSound(player.location, Sound.ORB_PICKUP, 0.3f, 2.0f)
            }
            6 -> player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.7f)
            8 -> {
                player.playSound(player.location, Sound.NOTE_PLING, 1.5f, 2.0f)
                player.playSound(player.location, Sound.GLASS, 0.4f, 2.0f)
            }
            10 -> player.playSound(player.location, Sound.NOTE_PLING, 0.9f, 1.5f)
            12 -> player.playSound(player.location, Sound.FIZZ, 0.3f, 2.0f)
        }

        if (tick > 15) {
            end(player)
        }
    }
} 