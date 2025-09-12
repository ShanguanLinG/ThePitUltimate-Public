package net.mizukilab.pit.map.kingsquests.ui

import net.mizukilab.pit.map.kingsquests.ui.button.KingsQuestsButton
import net.mizukilab.pit.util.menu.Button
import net.mizukilab.pit.util.menu.Menu

import org.bukkit.entity.Player


object KingQuestsUI : Menu() {
    override fun getTitle(player: Player?): String {
        return "国王任务"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {
        val map = HashMap<Int, Button>()

        map[13] = KingsQuestsButton

        return map
    }

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 3 * 9
    }
}