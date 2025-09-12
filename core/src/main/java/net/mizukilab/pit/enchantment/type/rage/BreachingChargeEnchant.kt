package net.mizukilab.pit.enchantment.type.rage

import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.IActionDisplayEnchant
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.listener.IAttackEntity
import net.mizukilab.pit.parm.listener.IPlayerShootEntity
import net.mizukilab.pit.util.cooldown.Cooldown
import nya.Skip
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
 * @Creator Misoryan
 * @Date 2021/5/8 19:13
 */
@Skip
@ArmorOnly
class BreachingChargeEnchant : AbstractEnchantment(), Listener,IAttackEntity, IPlayerShootEntity,
    IActionDisplayEnchant {
    override fun getEnchantName(): String {
        return "强力击: 违约金"
    }

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "breaching_charge"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.RAGE
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7攻击清空命中玩家的 &3抗性提升 &7效果 (" + (4 - enchantLevel) + "秒冷却)"
    }

    @PlayerOnly
    override fun handleAttackEntity(
        enchantLevel: Int,
        attacker: Player,
        target: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean
    ) {
        val targetPlayer = target as Player
        if (Companion.cooldown.getOrDefault(attacker.uniqueId, Cooldown(0)).hasExpired()) {
            Companion.cooldown[attacker.uniqueId] =
                Cooldown((4 - enchantLevel).toLong(), TimeUnit.SECONDS)
            targetPlayer.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE)
        }
    }

    @PlayerOnly
    override fun handleShootEntity(
        enchantLevel: Int,
        attacker: Player,
        target: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean
    ) {
        val targetPlayer = target as Player
        if (Companion.cooldown.getOrDefault(attacker.uniqueId, Cooldown(0)).hasExpired()) {
            Companion.cooldown[attacker.uniqueId] =
                Cooldown((4 - enchantLevel).toLong(), TimeUnit.SECONDS)
            targetPlayer.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE)
        }
    }

    override fun getText(level: Int, player: Player): String {
        return getCooldownActionText(Companion.cooldown.getOrDefault(player.uniqueId, Cooldown(0)))
    }
    @EventHandler
    fun quit(pq :PlayerQuitEvent){
        Companion.cooldown.remove(pq.player.uniqueId)
    }
    companion object {
        private val cooldown = HashMap<UUID, Cooldown>()
    }
}