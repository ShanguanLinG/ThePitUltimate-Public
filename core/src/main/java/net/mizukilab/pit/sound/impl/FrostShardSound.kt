package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 霜冰碎片音效 - 寒冷的冰晶破碎
 */
@Skip
object FrostShardSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "frost_shard"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> {
                player.playSound(player.location, Sound.GLASS, 1.5f, 1.8f)
                player.playSound(player.location, Sound.FIZZ, 0.8f, 2.0f)
            }
            2 -> player.playSound(player.location, Sound.GLASS, 1.2f, 1.6f)
            4 -> {
                player.playSound(player.location, Sound.GLASS, 1.8f, 1.4f)
                player.playSound(player.location, Sound.NOTE_PLING, 0.6f, 2.0f)
            }
            6 -> player.playSound(player.location, Sound.STEP_SNOW, 2.0f, 1.5f)
            8 -> {
                player.playSound(player.location, Sound.GLASS, 1.0f, 1.2f)
                player.playSound(player.location, Sound.SILVERFISH_HIT, 0.4f, 2.0f)
            }
            10 -> player.playSound(player.location, Sound.GLASS, 0.8f, 1.0f)
            12 -> player.playSound(player.location, Sound.FIZZ, 0.5f, 1.8f)
        }

        if (tick > 15) {
            end(player)
        }
    }
} 