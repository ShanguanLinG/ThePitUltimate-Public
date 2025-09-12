package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 天籁和声音效 - 优美的天堂旋律
 */

@Skip
object CelestialHarmonySound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "celestial_harmony"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> player.playSound(player.location, Sound.NOTE_PIANO, 1.2f, 1.0f)
            2 -> player.playSound(player.location, Sound.NOTE_PIANO, 1.0f, 1.2f)
            4 -> player.playSound(player.location, Sound.NOTE_PIANO, 0.8f, 1.5f)
            6 -> {
                player.playSound(player.location, Sound.NOTE_PIANO, 1.5f, 1.8f)
                player.playSound(player.location, Sound.NOTE_BASS, 0.6f, 2.0f)
            }
            8 -> player.playSound(player.location, Sound.NOTE_BASS, 1.0f, 1.6f)
            10 -> player.playSound(player.location, Sound.NOTE_BASS, 0.8f, 1.9f)
            12 -> {
                player.playSound(player.location, Sound.NOTE_PIANO, 1.2f, 2.0f)
                player.playSound(player.location, Sound.ORB_PICKUP, 0.4f, 2.0f)
            }
            14 -> player.playSound(player.location, Sound.LEVEL_UP, 0.3f, 2.0f)
        }

        if (tick > 18) {
            end(player)
        }
    }
} 