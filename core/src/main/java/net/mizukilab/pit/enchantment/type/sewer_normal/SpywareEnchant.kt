package net.mizukilab.pit.enchantment.type.sewer_normal

import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity
import net.mizukilab.pit.parm.listener.ITickTask
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.cooldown.Cooldown
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author Araykal
 * @since 2025/5/2
 */
@ArmorOnly
class SpywareEnchant : AbstractEnchantment(), IPlayerKilledEntity, ITickTask {
    override fun getEnchantName(): String {
        return "蠕虫"
    }

    override fun getMaxEnchantLevel(): Int {
        return 1
    }

    override fun getNbtName(): String {
        return "spyware_enchant"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.SEWER_NORMAL
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7穿戴此下水道之甲时, 你将获得 &b速度I &7同时每次击杀 &b经验 &7额外加成 &e20%"
    }

    override fun handlePlayerKilled(
        enchantLevel: Int,
        myself: Player?,
        target: Entity?,
        coins: AtomicDouble,
        experience: AtomicDouble
    ) {
        experience.addAndGet(0.2)
    }

    override fun handle(enchantLevel: Int, player: Player) {
        PlayerUtil.addPotionEffect(player, PotionEffect(PotionEffectType.SPEED, 30, 0))
    }

    override fun loopTick(enchantLevel: Int): Int {
        return 20
    }
}