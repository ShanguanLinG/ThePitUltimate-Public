package net.mizukilab.pit.hook

import cn.charlotte.pit.ThePit
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.mizukilab.pit.util.inventory.InventoryUtil
import org.bukkit.entity.Player

/**
 * @author Araykal
 * @since 2025/4/15
 */
object ItemPapiHook : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "item"
    }

    override fun getAuthor(): String {
        return "Araykal"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        val split = params.split("_")

        if (split.isEmpty()) {
            return "Parameters are missing"
        }

        return when (split[0]) {
            "amount" -> {
                if (split.size > 1) {
                    val id = split.subList(1, split.size).joinToString("_")
                    InventoryUtil.getAmountOfItem(player!!, id).toString()
                } else {
                    "Not enough parameters"
                }
            }

            "iscn" -> {
                if (split.size > 2) {
                    val id = split.subList(2, split.size).joinToString("_")
                    val itemAmount = InventoryUtil.getAmountOfItem(player!!, id)
                    if (itemAmount >= split[1].toInt()) "是" else "否"
                } else {
                    "Not enough parameters"
                }
            }

            "is" -> {
                if (split.size > 2) {
                    val id = split.subList(2, split.size).joinToString("_")
                    val itemAmount = InventoryUtil.getAmountOfItem(player!!, id)
                    if (itemAmount >= split[1].toInt()) "true" else "false"
                } else {
                    "Not enough parameters"
                }
            }

            else -> "Unknown placeholder"
        }
    }

}