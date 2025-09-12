package net.mizukilab.pit.runnable.dupe

import net.mizukilab.pit.item.IMythicItem
import net.mizukilab.pit.util.MythicUtil
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object CleanupDupeEnchTheKMGodRunnable : BukkitRunnable() {
    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val size = player.inventory.size
            for (i in 0 until size) {
                val itemStack = player.inventory.getItem(i)
                if (checkDupe(itemStack)) {
                    val mythicItem = MythicUtil.getMythicItem(itemStack)
                    if (mythicItem != null) {
                        player.sendMessage("§c检测到您的背包中存在于8/13时的大无语神经附魔，以为您清除。")
                        player.inventory.setItem(i, rollback(mythicItem))
                    }
                }
            }
        }
    }

    fun checkDupe(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val mythicItem = MythicUtil.getMythicItem(itemStack) ?: return false
        mythicItem.enchantmentRecords?.forEach { record ->
            if (record.enchanter == "outingOF") {
                return true
            }
        }
        return false
    }

    fun rollback(item: IMythicItem): ItemStack {
        item.enchantments.clear()
        item.enchantmentRecords.clear()
        item.tier = 0
        item.maxLive = 0
        item.live = 0
        item.prefix = null
        item.customName = null
        return item.toItemStack()
    }

    fun auto(itemStack: ItemStack?): Pair<Boolean, ItemStack?> {
        if (checkDupe(itemStack)) {
            val mythicItem = MythicUtil.getMythicItem(itemStack) ?: return false to null
            return true to rollback(mythicItem)
        }
        return false to itemStack
    }
}