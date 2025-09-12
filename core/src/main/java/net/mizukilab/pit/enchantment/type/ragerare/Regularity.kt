package net.mizukilab.pit.enchantment.type.ragerare

import cn.charlotte.pit.ThePit
import net.mizukilab.pit.data.operator.ProfileOperator
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.AutoRegister
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.cooldown.Cooldown
import nya.Skip
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.LazyMetadataValue
import org.bukkit.scheduler.BukkitRunnable


@AutoRegister
@Skip
@ArmorOnly
class Regularity : AbstractEnchantment(), Listener {
    override fun getEnchantName(): String {
        return "狂暴连击"
    }

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String {
        return "regularity"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.RAGE_RARE
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7当近战伤害低于&c${
            when (enchantLevel) {
                1 -> 1.1
                2 -> 1.3
                else -> 1.5
            }
        }❤ &7时/ s&7, 将会自动再次攻击./s&7第二次攻击的伤害为第一次攻击的&c${
            when (enchantLevel) {
                1 -> 45
                2 -> 60
                else -> 75
            }
        }%&7. &7(最多三次)"
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun damage(event: EntityDamageByEntityEvent) {
        val attacker = event.damager
        if (attacker !is Player) return

        val victim = event.entity
        if (victim !is LivingEntity) return

        var level = -1
        val operator = (ThePit.getInstance().profileOperator as ProfileOperator).getOperator(attacker)

        if (operator != null) {
            if (operator.profile().leggings != null) {
                if (operator.isLoaded) {
                    level = operator.profile().leggings.getEnchantmentLevel(this.nbtName)
                }
            }
        }


        if (level < 1) return

        if (PlayerUtil.shouldIgnoreEnchant(attacker, victim)) {
            return
        }
        if (event.finalDamage < when (level) {
                1 -> 1.1
                2 -> 1.3
                else -> 1.5
            }
        ) {
            val metadata = victim.getMetadata("regularity")
            metadata.firstOrNull()?.asLong()?.let {
                if (System.currentTimeMillis() < it) {
                    return
                } else {
                    victim.removeMetadata("regularity", ThePit.getInstance())
                }
            }

            if (!victim.isDead) {
                val boost = when (level) {
                    1 -> 45
                    2 -> 60
                    else -> 75
                } * 0.01

                val operator_vic = (ThePit.getInstance().profileOperator as ProfileOperator).getOperator(attacker)
                object : BukkitRunnable() {
                    override fun run() {
                        if(operator_vic.profile() != null){
                            if (!operator_vic.profile().isInArena) {
                                return
                            }
                        }
                        victim.noDamageTicks = 0;
                        victim.damage(event.damage * boost, attacker)

                        victim.setMetadata(
                            "regularity",
                            FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + 700L)
                        )
                    }
                }.runTaskLater(ThePit.getInstance(), 5L)
            }
        }
    }
}