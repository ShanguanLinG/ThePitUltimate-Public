package net.mizukilab.pit.enchantment.type.aqua

import cn.charlotte.pit.ThePit
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.AutoRegister
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.toMythicItem

import org.bukkit.Bukkit
import org.bukkit.entity.FishHook
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import kotlin.math.max


@ArmorOnly
@AutoRegister
class GrandmasterEnchant : AbstractEnchantment(), Listener {
    @EventHandler
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager is FishHook && event.entity is Player &&
            damager.shooter is Player && (damager.shooter as Player).inventory.leggings?.toMythicItem()?.enchantments?.any {
                it.key.nbtName == "rod_back"
            } == true
        ) {
            Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), {
                val player = event.entity as Player
                player.damage(0.0, (event.damager as FishHook).shooter as Player)
                player.health = max(0.0, player.health - 1.0)
            }, 1L)
        }
    }

    override fun getEnchantName(): String {
        return "特级渔夫"
    }

    override fun getMaxEnchantLevel(): Int {
        return 1
    }

    override fun getNbtName(): String {
        return "grandmaster"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.FISH_NORMAL
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7使用鱼竿命中玩家时额外造成 &c0.5❤ &7的伤害"
    }
}