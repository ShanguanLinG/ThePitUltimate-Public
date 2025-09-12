package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 虚空回音音效 - 空灵的虚无之声
 */
@Skip
object VoidEchoSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "void_echo"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> {
                player.playSound(player.location, Sound.PORTAL, 1.0f, 0.1f)
                player.playSound(player.location, Sound.ENDERMAN_TELEPORT, 0.6f, 0.5f)
            }
            3 -> player.playSound(player.location, Sound.PORTAL, 0.8f, 0.3f)
            6 -> {
                player.playSound(player.location, Sound.ENDERMAN_IDLE, 0.5f, 0.2f)
                player.playSound(player.location, Sound.PORTAL, 1.2f, 0.2f)
            }
            9 -> player.playSound(player.location, Sound.PORTAL, 0.9f, 0.4f)
            12 -> {
                player.playSound(player.location, Sound.ENDERMAN_TELEPORT, 0.8f, 0.8f)
                player.playSound(player.location, Sound.PORTAL_TRIGGER, 0.3f, 1.5f)
            }
            15 -> player.playSound(player.location, Sound.PORTAL, 0.6f, 0.6f)
            18 -> player.playSound(player.location, Sound.PORTAL, 0.4f, 0.8f)
            21 -> player.playSound(player.location, Sound.PORTAL, 0.2f, 1.0f)
        }

        if (tick > 25) {
            end(player)
        }
    }
} 