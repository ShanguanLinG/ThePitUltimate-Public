package net.mizukilab.pit.hook

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.events.genesis.GenesisTeam
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.mizukilab.pit.getPitProfile
import net.mizukilab.pit.listener.CombatListener
import net.mizukilab.pit.util.chat.CC
import org.bukkit.entity.Player

object PitPapiHook : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "pit"
    }

    override fun getAuthor(): String {
        return "ThePitUltimate"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onPlaceholderRequest(p: Player, params: String): String? {
        val profile = run {
            val profile = p.getPitProfile()
            if (profile.isLoaded) {
                profile
            } else {
                null
            }
        }

        when (params) {
            "boost" -> {
                return CombatListener.eventBoost.toString()
            }

            "next_map" -> {
                val map =
                    ThePit.getInstance().configManager.getPitWorldConfigSpecific(ThePit.getInstance().configManager.cursor + 1L)
                if (map != null) {
                    return map.worldName
                }
                return "NO"
            }

            "cooldown_switchmap" -> {
                ThePit.getInstance().mapSelector.remainTime.toString()
            }

            "level_tag_roman" -> {
                return CC.translate(profile?.formattedLevelTagWithRoman ?: "&7[0]")
            }

            "level_tag" -> {
                return CC.translate(profile?.formattedLevelTag ?: "&7[0]")
            }

            "genesis_tag" -> {
                if (ThePit.getInstance().pitConfig.isGenesisEnable) {
                    if (profile?.genesisData?.team == GenesisTeam.ANGEL) {
                        return "&b♆"
                    }
                    if (profile?.genesisData?.team == GenesisTeam.DEMON) {
                        return "&c♨"
                    }
                }
                return ""
            }

            "prestige" -> {
                return profile?.prestige.toString()
            }

            "coins_int" -> {
                return profile?.coins?.toInt().toString()
            }

            "coins" -> {
                return profile?.coins?.toString() ?: "0.0"
            }

            "exp" -> {
                return profile?.experience?.toString() ?: "0.0"
            }

            "bounty" -> {
                return profile?.bounty?.toString() ?: "0"
            }

        }


        return null
    }
}