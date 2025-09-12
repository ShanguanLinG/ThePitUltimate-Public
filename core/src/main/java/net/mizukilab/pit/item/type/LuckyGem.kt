package net.mizukilab.pit.item.type

import cn.charlotte.pit.ThePit

import net.minecraft.server.v1_8_R3.EnumParticle
import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.parm.AutoRegister
import net.mizukilab.pit.util.ParticleBuilder
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask


/**
 * @author Araykal
 * @since 2025/2/6
 */
@AutoRegister
class LuckyGem : AbstractPitItem(), Listener {
    override fun getInternalName(): String {
        return "lucky_gem"
    }

    override fun getItemDisplayName(): String {
        return "§e幸运宝石"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.NETHER_STAR
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(itemDisplayMaterial).name(itemDisplayName).lore(
            mutableListOf(
                "&7死亡时保留",
                "",
                "&7为自身添加&e幸运,",
                "&7持续60秒,",
                "&e&l幸运！&7大幅度提升附魔稀有概率",
                "",
                "&e右键使用"
            )
        ).internalName(
            internalName
        ).canSaveToEnderChest(true).canTrade(false).deathDrop(false).shiny().build()
    }
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        // 检查玩家手中是否有物品，避免空指针异常
        val item = event.item ?: return

        if ("lucky_gem" == ItemUtil.getInternalName(item)) {
            // 取消默认事件行为
            event.isCancelled = true
            event.setUseInteractedBlock(Event.Result.DENY)
            event.setUseItemInHand(Event.Result.DENY)

            player.sendMessage(CC.translate("&e&l幸运! &7自身运气已大幅度提升。"))
            PlayerUtil.takeOneItemInHand(player)
            player.setMetadata("lucky_gem", FixedMetadataValue(ThePit.getInstance(), true))
            CC.boardCast("&e&l幸运宝石! &f${player.name} &7使用了幸运宝石,个人运气提升!")

            var scheduledTask: BukkitTask? = null

            val task = object : BukkitRunnable() {
                var tick = 0
                override fun run() {

                    if (tick >= 1200) {
                        player.sendMessage(CC.translate("&e&l幸运! &7自身运气已降低。"))
                        player.removeMetadata("lucky_gem", ThePit.getInstance())
                        scheduledTask?.cancel()
                        return
                    }

                    if (!player.isOnline || player == null) {
                        scheduledTask?.cancel()
                        return
                    }

                    ParticleBuilder(
                        player.location.clone().add(0.0, 2.0, 0.0),
                        EnumParticle.VILLAGER_HAPPY
                    ).apply {
                        setVelocity(0.5f)
                        setCount(3)
                        play()
                    }
                    tick++
                }
            }
            scheduledTask = task.runTaskTimer(ThePit.getInstance(), 0L, 20L)
        }
    }


    override fun loadFromItemStack(item: ItemStack) {
    }
}
