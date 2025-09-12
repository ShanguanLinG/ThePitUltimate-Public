package net.mizukilab.pit.item.type.sewers

import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author Araykal
 * @since 2025/5/2
 */
class Milk : AbstractPitItem(), Listener {
    override fun getInternalName(): String {
        return "milk"
    }

    override fun getItemDisplayName(): String {
        return "&f牛奶"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.MILK_BUCKET
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(itemDisplayMaterial).internalName(internalName).name(itemDisplayName).lore(
            "&7死亡后保留",
            "",
            "&7从&9下水道&7获取的神秘液体",
            "&7使用后获得",
            "&a生命恢复 I(2:00)",
            "",
            "&9下水道"
        ).build()
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if ("milk" == ItemUtil.getInternalName(item)) {
            event.isCancelled = true
            event.setUseInteractedBlock(Event.Result.DENY)
            event.setUseItemInHand(Event.Result.DENY)
            PlayerUtil.takeOneItemInHand(player)
            val negativeEffects = listOf(
                PotionEffectType.BLINDNESS,
                PotionEffectType.CONFUSION,
                PotionEffectType.HUNGER,
                PotionEffectType.POISON,
                PotionEffectType.SLOW,
                PotionEffectType.WEAKNESS,
                PotionEffectType.WITHER
            )

            for (effect in player.activePotionEffects) {
                if (effect.type in negativeEffects) {
                    player.removePotionEffect(effect.type)
                }
            }
            player.playSound(player.location, Sound.DRINK, 1.0f, 1.0f)
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2400, 1))
        }
    }

    override fun loadFromItemStack(item: ItemStack?) {
    }
}