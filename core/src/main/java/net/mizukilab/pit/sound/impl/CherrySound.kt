package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author Araykal
 * @since 2025/5/16
 */
@Skip
object CherrySound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "cherry_sound"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        val pitch = when (tick) {
            0 -> 1.0f
            2 -> 1.5f
            4 -> 1.4f
            6 -> 1.7f
            else -> null
        }

        pitch?.let {
            val sound = Sound.NOTE_STICKS
            player.playSound(player.location, sound, 2.5f, it)
        }

        if (tick > 10) {
            end(player)
        }
    }
}
