package net.mizukilab.pit.enchantment.type.sewer_normal

import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.listener.IPlayerDamaged
import net.mizukilab.pit.util.cooldown.Cooldown
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Araykal
 * @since 2025/5/2
 */
@ArmorOnly
class EliminatePowerEnchant : AbstractEnchantment(), IPlayerDamaged {
    override fun getEnchantName(): String {
        return "消力"
    }

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "eliminate_power"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.SEWER_NORMAL
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7被攻击时减伤 &e${getDamage(enchantLevel)}%"
    }

    private fun getDamage(enchantLevel: Int): Int {
        return when (enchantLevel) {
            1 -> 15
            2 -> 20
            3 -> 25
            else -> 0
        }
    }

    override fun handlePlayerDamaged(
        enchantLevel: Int,
        myself: Player,
        attacker: Entity,
        damage: Double,
        finalDamage: AtomicDouble?,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean?
    ) {
        boostDamage.set(boostDamage.get() - (getDamage(enchantLevel) / 100).toDouble())
    }
}