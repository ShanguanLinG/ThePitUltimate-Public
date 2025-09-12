package net.mizukilab.pit.menu.gem.button


import cn.charlotte.pit.data.sub.EnchantmentRecord
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import net.mizukilab.pit.util.menu.Button
import net.mizukilab.pit.util.toMythicItem
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2022/6/5
 * @ Author KleeLoveLife
 * @ Time 0:29
 */
class EnchantChoseButtonGlobalGem(
    private val item: ItemStack,
    private val index: Int,
    private val slot: Int,
    private val enchantment: AbstractEnchantment,
    private val enchantLevel: Int
) : Button() {
    override fun getButtonItem(player: Player): ItemStack {
        return ItemBuilder(item.type)
            .name("&e升级第${slot + 1}号附魔")
            .lore(
                "",
                "&7附魔属性: &d${enchantment.enchantName}",
                "&7等级: &e${enchantLevel}",
                "",
                "&7消耗: &b举世瞩目的宝石",
                "",
                if (Utils.canUseGlobalAttGem(item)) {
                    if (enchantLevel >= 3) "&c已达最大等级" else
                        if (enchantment.rarity.parentType == EnchantmentRarity.RarityType.RARE) "&c稀有附魔" else
                            "&a点击使用!"
                } else "&c无法作用于此附魔上"
            )
            .build()
    }

    override fun clicked(
        player: Player,
        slot: Int,
        clickType: ClickType,
        hotbarButton: Int,
        currentItem: ItemStack?
    ) {
        if (!Utils.canUseGlobalAttGem(item)) {
            player.closeInventory()
            return
        }

        val mythicItem = item.toMythicItem() ?: return

        val indexedItem = player.inventory.getItem(index)
        val indexedMythicItem = indexedItem.toMythicItem()
        if (indexedMythicItem != null && isSameEnchant(mythicItem, indexedMythicItem)) {
            val enchantLevel = indexedMythicItem.enchantments[enchantment] ?: return
            if (enchantLevel >= 3) {
                return
            }

            if (enchantment.rarity.parentType != EnchantmentRarity.RarityType.RARE) {
                return
            }

            var maxedLevel = 0
            indexedMythicItem.enchantments.forEach { (_, level) ->
                if (level >= 3) {
                    maxedLevel++
                }
            }

            //不能从332 => 333
            if (maxedLevel >= 2 && enchantLevel >= 2) {
                player.sendMessage(CC.translate("&c不合法"))
                player.closeInventory()
                return
            }

            indexedMythicItem.enchantments[enchantment] = enchantLevel + 1

            indexedMythicItem.enchantmentRecords += EnchantmentRecord(
                player.name,
                "GlobalAttGem",
                System.currentTimeMillis()
            )

            indexedMythicItem.boostedByGlobalGem = true
            player.sendMessage(CC.translate("&b&l宝石！ &7成功为你的神话之裤升级了${enchantment.enchantName}附魔"))
            player.playSound(player.location, Sound.ANVIL_USE, 1f, 99f)
            player.inventory.setItem(index, indexedMythicItem.toItemStack())
        } else {
            return
        }

        player.closeInventory()

        player.inventory.forEachIndexed { index, itemStack ->
            val internalName = ItemUtil.getInternalName(itemStack)
            if ("global_attention_gem" == internalName) {
                if (itemStack.amount == 1) {
                    player.inventory.setItem(index, null)
                } else {
                    itemStack.amount--
                    player.inventory.setItem(index, itemStack)
                }
                return
            }
        }

        player.playSound(player.location, Sound.ANVIL_USE, 1.5f, 1.5f)
    }

    private fun isSameEnchant(
        itemA: AbstractPitItem,
        itemB: AbstractPitItem
    ): Boolean {
        loop@ for (enchantment in itemA.enchantments) {
            for (enchantmentB in itemB.enchantments) {
                if (enchantment.key.nbtName == enchantmentB.key.nbtName && enchantment.value == enchantmentB.value) {
                    continue@loop
                }
            }

            return false
        }

        return true
    }
}