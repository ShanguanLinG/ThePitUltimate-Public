package net.mizukilab.pit.enchantment.type.normal

import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity
import net.mizukilab.pit.util.cooldown.Cooldown
import com.google.common.util.concurrent.AtomicDouble
import nya.Skip
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/25 15:11
 */
@Skip
@ArmorOnly
class ElectrolytesEnchant : AbstractEnchantment(), IPlayerKilledEntity {
    override fun getEnchantName(): String {
        return "电解质"
    }

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "electrolytes_enchant"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.NORMAL
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return ("&7击杀时如自身存在 &b速度 &7效果,延长效果时间 &e" + (enchantLevel * 2) + " 秒"
                + "/s&7(如效果等级大于II则延长时间减半,上限" + ((enchantLevel + 2) * 6) + "秒)")
    }

    @PlayerOnly
    override fun handlePlayerKilled(
        enchantLevel: Int,
        myself: Player,
        target: Entity?,
        coins: AtomicDouble?,
        experience: AtomicDouble?
    ) {
        myself.activePotionEffects.stream()
            .filter { effect: PotionEffect -> effect.type === PotionEffectType.SPEED }
            .findFirst()
            .ifPresent { potionEffect: PotionEffect ->
                val duration = if (potionEffect.amplifier > 1) {
                    potionEffect.duration + ((enchantLevel * 20) / 2)
                } else {
                    potionEffect.duration + enchantLevel * 20
                }
                myself.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SPEED,
                        duration,
                        potionEffect.amplifier
                    ), true
                )
            }
    }
}