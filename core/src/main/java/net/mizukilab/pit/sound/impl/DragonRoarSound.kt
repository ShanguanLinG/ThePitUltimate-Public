package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 龙吼音效 - 威严的巨龙咆哮
 */
@Skip
object DragonRoarSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "dragon_roar"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> {
                player.playSound(player.location, Sound.ENDERDRAGON_GROWL, 2.0f, 0.6f)
            }
            4 -> {
                player.playSound(player.location, Sound.ENDERDRAGON_GROWL, 1.8f, 0.8f)
                player.playSound(player.location, Sound.GHAST_SCREAM, 0.5f, 0.4f)
            }
            8 -> {
                player.playSound(player.location, Sound.ENDERDRAGON_HIT, 1.2f, 0.7f)
            }
            12 -> {
                player.playSound(player.location, Sound.ENDERDRAGON_WINGS, 1.5f, 1.0f)
            }
            16 -> {
                player.playSound(player.location, Sound.ENDERDRAGON_DEATH, 0.8f, 1.5f)
            }
        }

        if (tick > 20) {
            end(player)
        }
    }
} 