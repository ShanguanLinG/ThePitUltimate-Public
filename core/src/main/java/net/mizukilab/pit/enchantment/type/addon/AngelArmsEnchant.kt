package net.mizukilab.pit.enchantment.type.addon

import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.IActionDisplayEnchant
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.listener.IAttackEntity
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.time.TimeUtil
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Araykal
 * @since 2025/5/1
 */
@ArmorOnly
class AngelArmsEnchant : AbstractEnchantment(), IAttackEntity, IActionDisplayEnchant,Listener {
    val cooldown: MutableMap<UUID, Cooldown> = mutableMapOf()
    override fun getEnchantName(): String {
        return "天使军：忠义"
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        cooldown.remove(event.player.uniqueId)
    }
    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "angel_arms"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.RARE
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7当自身生命值低于50%时,攻击敌方将清除敌方&6抗性提升/s" + "&7同时为自身伤害增加 &e${get(enchantLevel)}% &7并恢复自身 &c${
            getHeath(
                enchantLevel
            )
        }❤ &7(5秒冷却)"
    }

    private fun getHeath(enchantLevel: Int): Double {
        return when (enchantLevel) {
            1 -> 1.5
            2 -> 2.0
            3 -> 3.0
            else -> .0
        }
    }

    private fun get(enchantLevel: Int): Int {
        return when (enchantLevel) {
            1 -> 10
            2 -> 15
            3 -> 20
            else -> 0
        }
    }

    override fun handleAttackEntity(
        enchantLevel: Int,
        attacker: Player,
        target: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean
    ) {
        if (cooldown.getOrDefault(attacker.uniqueId, Cooldown(0L)).hasExpired()) {
            if (attacker.health <= (attacker.maxHealth / 2.0)) {
                val player = target as Player
                cooldown[attacker.uniqueId] = Cooldown(5, TimeUnit.SECONDS)
                boostDamage.getAndAdd((get(enchantLevel) / 100).toDouble())
                PlayerUtil.heal(attacker, getHeath(enchantLevel) * 2)
                if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE)
                }
            }
        }
    }

    override fun getText(level: Int, player: Player): String {
        val cooldownData = cooldown[player.uniqueId] ?: Cooldown(0)
        return if (cooldownData.hasExpired()) {
            "&a&l✔"
        } else {
            "&c&l" + TimeUtil.millisToRoundedTime(cooldownData.remaining).replace(" ", "")
        }
    }
}
