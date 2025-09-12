package net.mizukilab.pit.map.kingsquests.item

import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.item.ItemBuilder

import org.bukkit.Material
import org.bukkit.inventory.ItemStack


object Cherry : AbstractPitItem() {
    override fun getInternalName(): String {
        return "cherry"
    }

    override fun getItemDisplayName(): String {
        return "&d樱桃"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.APPLE
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(itemDisplayMaterial)
            .internalName(internalName)
            .canDrop(true)
            .canTrade(true)
            .deathDrop(false)
            .canSaveToEnderChest(true)
            .name(itemDisplayName)
            .lore(
                "&7死亡时保留"
            )
            .build()
    }

    override fun loadFromItemStack(item: ItemStack?) {

    }
}