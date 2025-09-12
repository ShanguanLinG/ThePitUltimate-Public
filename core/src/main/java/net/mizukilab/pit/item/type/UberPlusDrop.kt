package net.mizukilab.pit.item.type

import cn.charlotte.pit.ThePit
import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.inventory.InventoryUtil
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import net.mizukilab.pit.util.random.RandomUtil
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

/**
 * @author Araykal
 * @since 2025/5/4
 */
class UberPlusDrop : AbstractPitItem(), Listener {

    override fun getInternalName(): String {
        return "uber_drop"
    }

    override fun getItemDisplayName(): String {
        return "&c不朽登峰造极掉落物"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.ENDER_CHEST
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(Material.ENDER_CHEST).lore(
            "&7死亡时保留",
            "",
            "&e拿着并右键获得物品!"
        )
            .dontStack()
            .enchantment(Enchantment.THORNS).flags(ItemFlag.HIDE_ENCHANTS)
            .internalName(internalName).name(itemDisplayName).dontStack().deathDrop(false).build()
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(event: BlockPlaceEvent) {
        val itemInHand = event.itemInHand ?: return
        if ("uber_drop" == ItemUtil.getInternalName(itemInHand)) {
            event.isCancelled = true

            val player = event.player
            if (InventoryUtil.isInvFull(player)) {
                player.sendMessage(CC.translate("&c你的背包是满的, 无法使用物品"))
                return
            }

            val itemStack =
                RandomUtil.helpMeToChooseOne(
                    FunkyFeather.toItemStack().also { it.amount = Random.nextInt(4, 8) },
                    FunkyFeather.toItemStack().also { it.amount = Random.nextInt(4, 8) },
                    PitCactus.toItemStack().also { it.amount = Random.nextInt(16, 64) },
                    JewelSword().toItemStack(),
                    MythicRepairKit.toItemStack0(),
                    TotallyLegitGem().toItemStack(),
                    GlobalAttentionGem().toItemStack(),
                ) as ItemStack


            var ticks = 0.3f
            object : BukkitRunnable() {
                override fun run() {
                    player.playSound(player.location, Sound.CHICKEN_EGG_POP, 1f, ticks)
                    ticks += 0.1f
                    if (ticks >= 2.0) {
                        cancel()
                    }
                }
            }.runTaskTimer(ThePit.getInstance(), 0L, 2L)

            PlayerUtil.takeOneItemInHand(player)
            player.inventory.addItem(itemStack)
            player.sendMessage(CC.translate("&c不朽登峰造极掉落物! &7你获得了 ${itemStack.itemMeta.displayName}"))
        }
    }
    override fun loadFromItemStack(item: ItemStack?) {
    }
}