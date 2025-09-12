package net.mizukilab.pit.perk.type.streak.king

import cn.charlotte.pit.perk.AbstractPerk
import cn.charlotte.pit.perk.PerkType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener

/**
 * @author Araykal
 * @since 2025/4/28
 */
class Leader : AbstractPerk(), Listener {
    override fun getInternalPerkName(): String {
        return "leader_perk"
    }

    override fun getDisplayName(): String {
        return "冠冕"
    }

    override fun getIcon(): Material {
        return Material.GOLD_HELMET
    }

    override fun requireCoins(): Double {
        return 10000.0
    }

    override fun requireRenown(level: Int): Double {
        return .0
    }

    override fun requirePrestige(): Int {
        return 15
    }

    override fun requireLevel(): Int {
        return 70
    }

    override fun getDescription(player: Player?): MutableList<String> {
        return mutableListOf("")
    }

    override fun getMaxLevel(): Int {
        return 1
    }

    override fun getPerkType(): PerkType {
        return PerkType.KILL_STREAK
    }

    override fun onPerkActive(player: Player) {
    }

    override fun onPerkInactive(player: Player) {
    }
}