package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 地狱爆炸音效 - 炽热的火焰爆发
 */
@Skip
object InfernoBlastSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "inferno_blast"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> {
                player.playSound(player.location, Sound.FIRE_IGNITE, 2.0f, 0.5f)
                player.playSound(player.location, Sound.GHAST_FIREBALL, 1.0f, 0.8f)
            }
            2 -> player.playSound(player.location, Sound.FIRE, 1.5f, 1.2f)
            4 -> {
                player.playSound(player.location, Sound.EXPLODE, 2.0f, 0.7f)
                player.playSound(player.location, Sound.BLAZE_BREATH, 1.2f, 1.0f)
            }
            6 -> player.playSound(player.location, Sound.LAVA_POP, 1.8f, 1.5f)
            8 -> {
                player.playSound(player.location, Sound.BLAZE_HIT, 1.0f, 0.8f)
                player.playSound(player.location, Sound.FIZZ, 2.0f, 0.5f)
            }
            10 -> player.playSound(player.location, Sound.LAVA, 1.5f, 1.0f)
            12 -> player.playSound(player.location, Sound.FIRE_IGNITE, 1.0f, 1.8f)
        }

        if (tick > 16) {
            end(player)
        }
    }
} 