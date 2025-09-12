package net.mizukilab.pit.enchantment.type.rare

import cn.charlotte.pit.ThePit
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.IActionDisplayEnchant
import net.mizukilab.pit.enchantment.param.item.WeaponOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.AutoRegister
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.chat.RomanUtil
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.time.TimeUtil
import net.mizukilab.pit.util.toMythicItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*


@AutoRegister
@WeaponOnly
class LastShadowLeapForward : AbstractEnchantment(), Listener, IActionDisplayEnchant {
    private val cooldownMap = HashMap<UUID, Cooldown>()

    override fun getEnchantName(): String {
        return "末影跃进"
    }

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "leapfrog"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.RARE
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7右键触发技能后向前突进一段距离,/s&7并获得 §b速度 ${RomanUtil.convert(enchantLevel)} §f(00:03) &7(${
            getDuration(
                enchantLevel
            )
        }秒冷却)"
    }

    private fun getDuration(enchantLevel: Int): Int {
        return when (enchantLevel) {
            1 -> 30
            2 -> 28
            else -> 20
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val action = event.action
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            val player = event.player

            val ignore = PlayerUtil.shouldIgnoreEnchant(player)
            if (ignore) return

            val item = player.itemInHand
            val mythicItem = item.toMythicItem() ?: return
            val level = mythicItem.enchantments[this] ?: return

            cooldownMap[player.uniqueId]?.also {
                if (!it.hasExpired()) {
                    return
                }
            }

            cooldownMap[player.uniqueId] = Cooldown(getDuration(level) * 1000L)

            val vector = player.location.direction.clone().multiply(1.6).apply {
                this.setY(0.5)
            }

            Bukkit.getScheduler().runTask(ThePit.getInstance()) {
                player.velocity = vector
            }
            player.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SPEED,
                    3 * 20,
                    level - 1,
                    true
                )
            )
        }
    }

    override fun getText(level: Int, player: Player): String {
        return (if (cooldownMap.getOrDefault(player.uniqueId, Cooldown(0))
                .hasExpired()
        ) "&a&l✔" else "&c&l" + TimeUtil.millisToRoundedTime(
            cooldownMap.getOrDefault(
                player.uniqueId,
                Cooldown(0)
            ).remaining
        ).replace(" ", ""))
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        cooldownMap.remove(e.player.uniqueId)
    }

}