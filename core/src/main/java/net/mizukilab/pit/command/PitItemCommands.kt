package net.mizukilab.pit.command

import cn.charlotte.pit.ThePit
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import net.md_5.bungee.api.chat.ClickEvent
import net.mizukilab.pit.command.handler.HandHasItem
import net.mizukilab.pit.menu.item.SavedMythicItemMenu
import net.mizukilab.pit.menu.item.AllSavedMythicItemsMenu
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.chat.ChatComponentBuilder
import net.mizukilab.pit.util.inventory.InventoryUtil
import net.mizukilab.pit.util.item.ItemUtil
import org.bson.Document
import org.bukkit.Material
import org.bukkit.entity.Player

@Command(name = "pit")
class PitItemCommands {

    @Execute(name = "saveItem")
    @HandHasItem(mythic = true)
    fun saveItem(@Context player: Player) {
        val item = player.itemInHand
        if (item == null || item.type == Material.AIR) {
            player.sendMessage(CC.translate("&c&l错误! &c请手持需要保存的神话物品."))
            return
        }
        if (!ItemUtil.isMythicItem(item)) {
            player.sendMessage(CC.translate("&c&l错误! &7这不是一件有效的神话物品."))
            return
        }
        val uuid = ItemUtil.getUUID(item)
        if (uuid == null) {
            player.sendMessage(CC.translate("&c&l错误! &c该物品缺少唯一UUID."))
            return
        }
        val internal = ItemUtil.getInternalName(item)
        val encoded = InventoryUtil.serializeItemStack(item)
        if (encoded == null) {
            player.sendMessage(CC.translate("&c&l错误! &c序列化物品失败."))
            return
        }
        val collection = ThePit.getInstance().mongoDB.database.getCollection("saved_mythic_items")
        val doc = Document()
            .append("uuid", uuid)
            .append("internal", internal)
            .append("item", encoded)
            .append("createdBy", player.uniqueId.toString())
            .append("createdByName", player.name)
            .append("createdAt", System.currentTimeMillis())
        collection.replaceOne(Filters.eq("uuid", uuid), doc, ReplaceOptions().upsert(true))
        val chat = ChatComponentBuilder(CC.translate("&a&l保存成功! &7已保存UUID为 &f$uuid &7的神话物品. &e[复制UUID]"))
            .setCurrentClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid))
            .create()
        player.spigot().sendMessage(*chat)
    }

    @Execute(name = "searchItem")
    fun searchItem(@Context player: Player, @Arg("uuid") uuid: String) {
        val collection = ThePit.getInstance().mongoDB.database.getCollection("saved_mythic_items")
        val found = collection.find(Filters.eq("uuid", uuid)).first()
        if (found == null) {
            player.sendMessage(CC.translate("&c&l错误! &7未找到该UUID对应的神话物品."))
            return
        }
        val encoded = found.getString("item")
        if (encoded == null) {
            player.sendMessage(CC.translate("&c&l错误! &7数据库中的物品数据不完整."))
            return
        }
        SavedMythicItemMenu(uuid, encoded).openMenu(player)
    }

    @Execute(name = "savedItems")
    @Permission("pit.admin")
    fun showSavedItems(@Context player: Player) {
        AllSavedMythicItemsMenu().openMenu(player)
    }

    @Execute(name = "removeItem")
    @Permission("pit.admin")
    fun removeItem(@Context player: Player, @Arg("uuid") uuid: String) {
        val collection = ThePit.getInstance().mongoDB.database.getCollection("saved_mythic_items")
        val result = collection.deleteOne(Filters.eq("uuid", uuid))
        if (result.deletedCount > 0) {
            player.sendMessage(CC.translate("&a&l删除成功! &7已删除UUID为 &e$uuid &a的神话物品记录"))
        } else {
            player.sendMessage(CC.translate("&c&l错误! &c未找到可删除的记录: &e$uuid"))
        }
    }
}