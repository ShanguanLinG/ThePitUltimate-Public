package net.mizukilab.pit.item.type.streng

import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/**
 * @author Araykal
 * @since 2025/5/31
 */
class StrengtHenI : AbstractPitItem(),Listener {
    override fun getInternalName(): String {
        return "strengt_hen_I"
    }

    override fun getItemDisplayName(): String {
        return "§aI 强化石"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.DIAMOND
    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(Material.DIAMOND).internalName(internalName).lore("&7死亡后保留","","&aI &7级强化石,可用来提示神话物品属性","&7当一个神话物品使用了 &aI &7级强化石下次升级需要 &eII &7级强化石","","&e强化道具").deathDrop(false).itemDamage(9.0).build()
    }

    override fun loadFromItemStack(item: ItemStack?) {
    }
}