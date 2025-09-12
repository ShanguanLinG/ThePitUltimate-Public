package net.mizukilab.pit.sound.impl

import net.mizukilab.pit.util.sound.AbstractPitSound
import nya.Skip
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 暗影低语音效 - 神秘诡异的暗黑之声
 */
@Skip
object ShadowWhisperSound : AbstractPitSound() {
    override fun getMusicInternalName(): String {
        return "shadow_whisper"
    }

    override fun onSoundTick(player: Player, tick: Int) {
        when (tick) {
            0 -> {
                player.playSound(player.location, Sound.GHAST_SCREAM, 0.3f, 0.1f)
                player.playSound(player.location, Sound.AMBIENCE_CAVE, 0.8f, 0.5f)
            }
            3 -> player.playSound(player.location, Sound.ENDERMAN_IDLE, 0.6f, 0.3f)
            6 -> {
                player.playSound(player.location, Sound.ENDERMAN_SCREAM, 0.4f, 0.2f)
                player.playSound(player.location, Sound.PORTAL, 0.3f, 0.1f)
            }
            9 -> player.playSound(player.location, Sound.WITHER_IDLE, 0.5f, 0.4f)
            12 -> {
                player.playSound(player.location, Sound.GHAST_MOAN, 0.7f, 0.3f)
                player.playSound(player.location, Sound.PORTAL_TRIGGER, 0.2f, 2.0f)
            }
            15 -> player.playSound(player.location, Sound.ENDERMAN_TELEPORT, 0.4f, 0.8f)
        }

        if (tick > 18) {
            end(player)
        }
    }
} 