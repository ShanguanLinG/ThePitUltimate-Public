package net.mizukilab.pit.item.type.sewers

import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @author Araykal
 * @since 2025/5/2
 */
class Rubbish : AbstractPitItem() {
    override fun getInternalName(): String {
        return "rubbish"
    }

    override fun getItemDisplayName(): String {
        return "&2下水道废弃物"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.INK_SACK
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(Material.INK_SACK).durability(15)
            .deathDrop(false)
            .canSaveToEnderChest(true)
            .canTrade(true)
            .lore(
                "&7死亡后保留",
                "",
                "&7使用 64 个废弃物找 &9下水道鱼",
                "&7换取稀有护甲",
                "",
                "&9下水道"
            )
            .internalName(internalName).name(itemDisplayName)
            .build()
    }

    override fun loadFromItemStack(item: ItemStack?) {
    }
}