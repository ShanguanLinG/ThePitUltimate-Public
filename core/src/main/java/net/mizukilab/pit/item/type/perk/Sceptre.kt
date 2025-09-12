package net.mizukilab.pit.item.type.perk

import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


/**
 * @author Araykal
 * @since 2025/4/27
 */
class Sceptre : AbstractPitItem(), Listener {
    override fun getInternalName(): String {
        return "despot_sceptre"
    }

    override fun getItemDisplayName(): String {
        return "&e暴君权杖"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.BLAZE_ROD
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(Material.BLAZE_ROD).name("&e暴君权杖").internalName(this.internalName).lore(
            "&7&o死亡后消耗",
            "",
            "&7腐朽无光，暴君的铁蹄曾踏碎繁华，如今独守荒芜",
            "&7阴影吞噬旧梦，无人再歌颂它的名字",
            "",
            "&8右键挖掘黑曜石",
            "",
            "&6超级连杀道具"
        ).deathDrop(true).canTrade(false).canSaveToEnderChest(true).build()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.player.itemInHand ?: return
        if (this.internalName == ItemUtil.getInternalName(item)) {
            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                val block: Block? = event.clickedBlock
                if (block != null && block.getType() === Material.OBSIDIAN) {
                    event.getPlayer().playSound(block.getLocation(), Sound.STEP_STONE, 1.0f, 1.0f);
                    block.breakNaturally()
                }
            }
        }
    }

    override fun loadFromItemStack(item: ItemStack?) {
        TODO("Not yet implemented")
    }
}