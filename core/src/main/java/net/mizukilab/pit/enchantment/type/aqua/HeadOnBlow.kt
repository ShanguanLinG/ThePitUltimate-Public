package net.mizukilab.pit.enchantment.type.aqua

import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.toMythicItem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent


class HeadOnBlow : AbstractEnchantment(), Listener {
    override fun getEnchantName(): String {
        return "当头一棒"
    }

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "head-on_blow"
    }

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        if (event.player.inventory.itemInHand.toMythicItem()?.enchantments?.any {
                enchantName == "head-on_blow"
            } == true) {
            if (event.state == PlayerFishEvent.State.CAUGHT_ENTITY && event.caught is Player) {
                val attacker = event.player
                val victim = event.caught as Player

                val knockback = victim.location.toVector().subtract(attacker.location.toVector()).normalize()


                knockback.multiply(1.2)
                victim.velocity = knockback
            }
        }
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.FISH_NORMAL
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "甩出鱼钩击退玩家力度增加20%"
    }
}