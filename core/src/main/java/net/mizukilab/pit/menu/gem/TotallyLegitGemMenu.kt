package net.mizukilab.pit.menu.gem

import net.mizukilab.pit.menu.gem.button.ItemGemButton
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.menu.Button
import net.mizukilab.pit.util.menu.Menu
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class TotallyLegitGemMenu : Menu() {
    override fun getTitle(player: Player): String {
        return "宝石点缀"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val map = HashMap<Int, Button>()
        var index = 0
        for ((slot, itemStack) in player.inventory.withIndex()) {
            if (Utils.canUseGen(itemStack)) {
                map[index] = ItemGemButton(itemStack, slot)
                index++
            }
        }

        return map
    }

    override fun onClickEvent(event: InventoryClickEvent?) {
        event?.isCancelled = true
    }
}