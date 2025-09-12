package net.mizukilab.pit.item.type.egg

import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.sendMultiMessage
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import net.mizukilab.pit.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Araykal
 * @since 2025/5/2
 */
class SpeedEggs : AbstractPitItem(), Listener {
    private val cooldowns = HashMap<UUID, Cooldown>()
    override fun getInternalName(): String {
        return "speed_eggs"
    }

    override fun getItemDisplayName(): String {
        return "&b速度蛋"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.MONSTER_EGG
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(itemDisplayMaterial).deathDrop(true).internalName(internalName).canSaveToEnderChest(true)
            .durability(94)
            .name(itemDisplayName).lore("&7使用后给予自身 &b速度 II &f(00:20)", "&7(28秒冷却)", "", "&7&o死亡后消失")
            .build()
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if ("speed_eggs" == ItemUtil.getInternalName(item)) {
            if (cooldowns.getOrDefault(player.uniqueId, Cooldown(0L)).hasExpired()) {
                event.isCancelled = true
                event.setUseInteractedBlock(Event.Result.DENY)
                event.setUseItemInHand(Event.Result.DENY)
                player.removePotionEffect(PotionEffectType.SPEED)
                cooldowns[player.uniqueId] = Cooldown(28, TimeUnit.SECONDS)
                player.playSound(player.location, Sound.EAT, 1f, 1f)

                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 400, 1))
            } else {
                player.sendMultiMessage(
                    "&c再次使用需等 ${
                        cooldowns[player.uniqueId]?.let {
                            TimeUtil.millisToRoundedTime(it.remaining).replace(" ", "")
                        }
                    }."
                )
            }
        }
    }

    override fun loadFromItemStack(item: ItemStack?) {

    }
}
