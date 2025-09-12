package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author Araykal
 * @since 2025/5/13
 */
@Skip
object GemsSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "gems_sound"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        val pitch = when (tick) {
            0 -> 1.7f
            2 -> 1.8f
            4 -> 1.9f
            6 -> 2.0f
            8 -> 2.1f
            else -> null
        }

        pitch?.let {
            val sound = if (tick == 8) Sound.CHICKEN_EGG_POP else Sound.LAVA_POP
            player.playSound(player.location, sound, 2.5f, it)
        }

        if (tick > 10) {
            end(player)
        }
    }
}
