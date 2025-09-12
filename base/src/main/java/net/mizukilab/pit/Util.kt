package net.mizukilab.pit

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.PlayerProfile
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.util.chat.CC
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player

val music: MutableList<AbstractEnchantment> = mutableListOf()
val musicIndex = listOf(11, 12, 13, 14, 15, 4, 22)

fun Player.getPitProfile(): PlayerProfile {
    return PlayerProfile.getPlayerProfileByUuid(this.uniqueId)
}

fun Player.releaseItem() {
    val craftPlayer = this as CraftPlayer
    craftPlayer.handle.bU()
}

fun Player.hasRealMan(): Boolean { //hasRealMan 不存在realMan附魔 //TODO
    return false//(player.getMetadata("real_man").firstOrNull()?.asLong() ?: Long.MIN_VALUE) >= System.currentTimeMillis()
}

fun Player.sendMessage(message: Component) {
    ThePit.getInstance().audiences.player(this).sendMessage(message)
}

val Player.audience: Audience
    get() = ThePit.getInstance().audiences.player(this)

fun Player.sendMultiMessage(message: String) {
    message.split("/s").forEach { this.sendMessage(CC.translate(it)) }
}
class Util {
    companion object {
        @JvmStatic
        val chatColorToColor: Map<ChatColor, Color> = hashMapOf(
            ChatColor.BLACK to Color.BLACK,
            ChatColor.RED to Color.RED,
            ChatColor.DARK_GREEN to Color.GREEN,
            ChatColor.DARK_BLUE to Color.BLUE,
            ChatColor.DARK_PURPLE to Color.MAROON,
            ChatColor.DARK_AQUA to Color.fromRGB(0, 170, 170),
            ChatColor.DARK_GRAY to Color.fromRGB(85, 85, 85),
            ChatColor.LIGHT_PURPLE to Color.fromRGB(255, 85, 255),
            ChatColor.GREEN to Color.LIME,
            ChatColor.YELLOW to Color.YELLOW,
            ChatColor.AQUA to Color.AQUA,
            ChatColor.GOLD to Color.ORANGE
        )

        @JvmStatic
        val chatColorToData: Map<ChatColor, Int> = hashMapOf(
            ChatColor.BLACK to 0,
            ChatColor.RED to 1,
            ChatColor.DARK_GREEN to 2,
            ChatColor.DARK_BLUE to 4,
            ChatColor.DARK_PURPLE to 5,
            ChatColor.DARK_AQUA to 6,
            ChatColor.DARK_GRAY to 8,
            ChatColor.LIGHT_PURPLE to 9,
            ChatColor.GREEN to 10,
            ChatColor.YELLOW to 11,
            ChatColor.AQUA to 12,
            ChatColor.GOLD to 14
        )
    }
}
