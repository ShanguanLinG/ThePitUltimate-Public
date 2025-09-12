package net.mizukilab.pit.item.type

import net.mizukilab.pit.item.IMythicItem
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*


class AngelChestplate : IMythicItem() {
    init {
        this.maxLive = 100
        this.live = 100
        this.uuid = UUID.randomUUID()
    }

    override fun getInternalName(): String {
        return "angel_chestplate"
    }

    override fun getItemDisplayName(): String {
        return "&f天使之甲"
    }

    override fun getItemDisplayMaterial(): Material {
        return Material.DIAMOND_CHESTPLATE
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
                this.forceCanTrade = 1;
            } else {
                this.forceCanTrade = 0;
            }
        }
        if (extra.hasKey("customName")) {
            this.customName = extra.getString("customName")
        }

    }

    override fun toItemStack(): ItemStack {
        return ItemBuilder(super.toItemStack())
            .also {
                if (this.customName != null) {
                    it.name(customName)
                } else {
                    it.name(itemDisplayName)
                }
            }
            .lore(
                "&7生命: " + (if (live / (maxLive * 1.0) <= 0.6) if (live / (maxLive * 1.0) <= 0.3) "&c" else "&e" else "&a") + live + "&7/" + maxLive,
                "",
                "&7装备时,自身受到的伤害 &9-10% &7.",
                ""
            )
            .canTrade(true)
            .canSaveToEnderChest(true)
            .deathDrop(false)
            .removeOnJoin(false)
            .internalName(internalName)
            .maxLive(this.maxLive)
            .uuid(uuid)
            .live(this.live)
            .deathDrop(false)
            .canSaveToEnderChest(true)
            .removeOnJoin(false)
            .canDrop(false)
            .canTrade(true)
            .shiny()
            .build()

    }
}