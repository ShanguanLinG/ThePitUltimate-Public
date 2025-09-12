package net.mizukilab.pit.item.type

import net.mizukilab.pit.item.IMythicItem
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*


class ArmageddonBoots : IMythicItem() {

    init {
        maxLive = 66
        live = 66
        uuid = UUID.randomUUID()
    }

    override fun getInternalName(): String {
        return "armageddon_boots"
    }

    override fun getItemDisplayName(): String {
        return "&c终末之靴"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.LEATHER_BOOTS
    }

    override fun isEnchanted(): Boolean {
        return true
    }

    override fun loadFromItemStack(item: ItemStack?) {
        item ?: return

        val nmsItem = Utils.toNMStackQuick(item)
        val tag = nmsItem?.tag ?: return
        val extra = tag.getCompound("extra") ?: return


        this.uuid = ItemUtil.getUUIDObj(item);
        this.maxLive = extra.getInt("maxLive")
        this.live = extra.getInt("live")
        if (extra.hasKey("forceCanTrade")) {
            if (extra.getBoolean("forceCanTrade")) {
                this.forceCanTrade = 1
            } else {
                this.forceCanTrade = 0
            }
        }
        if (extra.hasKey("customName")) {
            this.customName = extra.getString("customName")
        }

    }


    override fun toItemStack(): ItemStack? {
        return ItemBuilder(super.toItemStack())
            .lore(
                "&7生命: " + (if (live / (maxLive * 1.0) <= 0.6) if (live / (maxLive * 1.0) <= 0.3) "&c" else "&e" else "&a") + live + "&7/" + maxLive,
                "",
                "&7攻击其他玩家时免疫 &9无尽黑暗 &7附魔效果.",
                ""
            )
            .internalName(internalName)
            .maxLive(maxLive)
            .live(live)
            .uuid(uuid)
            .setLetherColor(Color.RED)
            .deathDrop(false)
            .canSaveToEnderChest(true)
            .removeOnJoin(false)
            .canDrop(false)
            .canTrade(true)
            .shiny()
            .build()
    }

}