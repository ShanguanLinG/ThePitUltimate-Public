package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 神话升华音效 - 史诗般的升级之声
 */
@Skip
object MythicAscensionSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "mythic_ascension"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> {
                player.playSound(player.location, Sound.LEVEL_UP, 1.0f, 0.5f)
                player.playSound(player.location, Sound.NOTE_PIANO, 0.8f, 1.0f)
            }
            3 -> player.playSound(player.location, Sound.NOTE_PIANO, 1.0f, 1.2f)
            6 -> {
                player.playSound(player.location, Sound.LEVEL_UP, 1.2f, 0.8f)
                player.playSound(player.location, Sound.NOTE_PIANO, 1.2f, 1.5f)
            }
            9 -> player.playSound(player.location, Sound.NOTE_PIANO, 1.5f, 1.8f)
            12 -> {
                player.playSound(player.location, Sound.LEVEL_UP, 1.5f, 1.0f)
                player.playSound(player.location, Sound.ENDERDRAGON_WINGS, 0.8f, 2.0f)
            }
            15 -> player.playSound(player.location, Sound.NOTE_PIANO, 1.8f, 2.0f)
            18 -> {
                player.playSound(player.location, Sound.LEVEL_UP, 2.0f, 1.5f)
                player.playSound(player.location, Sound.ORB_PICKUP, 1.0f, 2.0f)
            }
            21 -> player.playSound(player.location, Sound.FIREWORK_BLAST, 1.5f, 1.8f)
            24 -> player.playSound(player.location, Sound.FIREWORK_LARGE_BLAST, 1.0f, 2.0f)
        }

        if (tick > 28) {
            end(player)
        }
    }
} 