package net.mizukilab.pit.menu.sewers.button

import net.mizukilab.pit.item.MythicColor
import net.mizukilab.pit.item.type.ChunkOfVileItem
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem
import net.mizukilab.pit.item.type.sewers.Rubbish
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.inventory.InventoryUtil
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.menu.Button
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * @author Araykal
 * @since 2025/5/2
 */
class RedeemButton : Button() {
    override fun getButtonItem(player: Player): ItemStack {
        val list: MutableList<String> = mutableListOf()
        list.add("&7使用 &2下水道废弃物 &7兑换")
        list.add("&7拥有独特附魔,极具属性的裤子")
        list.add("")
        list.add("&7兑换所需: &e64 &2下水道废弃物")
        list.add("")
        list.add("&7兑换物品: &2下水道之甲")
        list.add("")
        if (InventoryUtil.getAmountOfItem(player, Rubbish().internalName) < 64) {
            list.add("&2你没有足够的下水道废弃物!")
        } else if (InventoryUtil.isInvFull(player)) {
            list.add("&c你的背包已满!")
        } else {
            list.add("&e点击合成!")
        }
        return ItemBuilder(Material.LEATHER_LEGGINGS).name("&2下水道之甲").setLetherColor(MythicColor.DARK_GREEN.leatherColor).lore(list)
            .build()
    }

    override fun clicked(
        player: Player,
        slot: Int,
        clickType: ClickType?,
        hotbarButton: Int,
        currentItem: ItemStack?
    ) {
        if (InventoryUtil.isInvFull(player)) {
            return
        }
        if (InventoryUtil.getAmountOfItem(player, Rubbish().internalName) < 64) {
            return
        }
        if (InventoryUtil.removeItem(player, Rubbish().internalName, 64)) {
            val item = MythicLeggingsItem()
            item.setColor(MythicColor.DARK_GREEN)
            player.inventory.addItem(item.toItemStack())
            player.sendMessage(CC.translate("&9&l下水道! &7成功兑换 &2下水道之甲."))
        }
    }


}