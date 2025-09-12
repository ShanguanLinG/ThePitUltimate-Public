package net.mizukilab.pit.command

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.PlayerProfile
import cn.charlotte.pit.data.TradeData
import com.mongodb.client.model.Filters
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.async.Async
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.flag.Flag
import dev.rollczi.litecommands.annotations.optional.OptionalArg
import dev.rollczi.litecommands.annotations.permission.Permission
import dev.rollczi.litecommands.annotations.shortcut.Shortcut
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.mizukilab.pit.PitHook
import net.mizukilab.pit.command.handler.HandHasItem
import net.mizukilab.pit.events.impl.QuickMathEvent
import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.item.MythicColor
import net.mizukilab.pit.medal.impl.challenge.hidden.KaboomMedal
import net.mizukilab.pit.runnable.RebootRunnable.RebootTask
import net.mizukilab.pit.sendMessage
import net.mizukilab.pit.util.Log
import net.mizukilab.pit.util.MythicUtil
import net.mizukilab.pit.util.PlusPlayer
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.level.LevelUtil
import net.mizukilab.pit.util.rank.RankUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.min


/**
 * 2024/5/15<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */

@Command(name = "pit")
@Permission("pit.admin")
class PitAdminCommands {
    @Execute(name = "dbg")
    fun dbg(@Context player: Player){
        Log.log = !Log.log;
        player.sendMessage("done")
    }
    @Execute(name = "switchMap")
    @Shortcut("swm")
    fun switchMap(@Context player: Player, @OptionalArg("cursor") cursor: Integer) {
        player.sendMessage("成功切换")
        var mapSelector = ThePit.getInstance().mapSelector
        if (cursor == null) {
            mapSelector.switchMap();
            return;
        }
        mapSelector.switchMapIndexed(cursor.toInt());
    }

    @Execute(name = "lookupAndSwitchMap")
    @Shortcut("lookupSWM")
    fun lookupMap(@Context player: Player, @OptionalArg("cursorName") cursor: String) {
        var mapSelector = ThePit.getInstance().mapSelector
        if (cursor == null) {
            var cursorInt: Int = -1;
            ThePit.getInstance().configManager.pitConfigs.values.forEach {
                if (it.worldName.equals(cursor)) {
                    cursorInt = it.id
                }
            };
            if (cursorInt == -1) {
                player.sendMessage("error")
                return;
            }
            mapSelector.switchMapIndexed(cursorInt)
            return;
        }
        player.sendMessage("error")
    }

    @Execute(name = "createEquation")
    @Shortcut("eq")
    fun eqEvent(@Context player: Player, @Arg("eqQuest") eq: String, @Arg("eqAns") ans: String) {
        ThePit.getApi().openEvent(QuickMathEvent(eq, ans), null)
    }

    @Execute(name = "giveItemInHand")
    @Shortcut("give")
    fun giveItemInHand(@Context player: Player, @Arg("target") target: Player) {
        if (player.itemInHand == null || player.itemInHand.type == Material.AIR) {
            player.sendMessage(CC.translate("&c请手持要给予的物品!"))
            return
        }
        target.inventory.addItem(player.itemInHand)
        target.sendMessage(CC.translate("&a一位管理员给予了你一些物品..."))
        player.sendMessage(CC.translate("&a成功给予物品至 " + RankUtil.getPlayerColoredName(target.uniqueId)))
    }

    @Execute(name = "giveAll")
    @Shortcut("giveAll")
    fun giveItemInHandAll(@Context player: Player, @Flag("pvp") pvp: Boolean) {
        if (player.itemInHand == null || player.itemInHand.type == Material.AIR) {
            player.sendMessage(CC.translate("&c请手持要给予的物品!"))
            return
        }
        Bukkit.getOnlinePlayers().forEach { target ->
            if (pvp) {
                if (PlayerProfile.getPlayerProfileByUuid(target.uniqueId).combatTimer.hasExpired()) {
                    return@forEach
                }
            }
            target.inventory.addItem(player.itemInHand)
            target.sendMessage(CC.translate("&a一位管理员给予了你一些物品..."))
            player.sendMessage(CC.translate("&a成功给予物品至 " + RankUtil.getPlayerColoredName(target.uniqueId)))
        }
    }

    @Execute(name = "addSpawn")
    @Async
    fun addSpawn(@Context player: Player): String {
        ThePit.getInstance().pitConfig.spawnLocations.add(player.location)

        ThePit.getInstance().pitConfig.save()

        val num = ThePit.getInstance().pitConfig.spawnLocations.size

        return CC.translate("&a成功!添加第" + num + "个出生点")
    }

    @Execute(name = "readyEpic")
    @Async
    fun readyEpic(@Context player: Player) {
        var nextEpicEvent = ThePit.getInstance().eventFactory.nextEpicEvent
        if (nextEpicEvent != null) {
            CC.boardCast("&c&l跳过! &f管理员已经跳过事件延迟")
            ThePit.getInstance().eventFactory.nextEpicEventTimer.fastExpired()
        } else {
            player.sendMessage("There is no epic event currently available")
        }
    }

    @Execute(name = "loc")
    @Async
    fun dumpLocation(@Context player: Player) {
        val location = player.location
        player.sendMessage(
            Component.text(location.toString()).clickEvent(
                ClickEvent.suggestCommand(location.toString())
            )
        )
    }

    @Execute(name = "hologramLoc")
    @Async
    fun setHologramLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.hologramLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置全息位置!")
    }

    @Execute(name = "statics")
    @Async
    fun statics(@Context player: Player): String {
        player.sendMessage("IS REMOVED")
        return "LevelUtils: Cache|Raw:${LevelUtil.fromCache}|${LevelUtil.fromRaw}"
    }

    @Execute(name = "keeperLoc")
    @Async
    fun setKeeperLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.keeperNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置看门人NPC位置!")
    }

    @Execute(name = "warehouseLoc")
    fun setWarehouseLocLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.warehouseNpcLocation = player.location
        ThePit.getInstance().pitConfig.save()
        ThePit.getInstance().customEntityNPCFactory.reload()
        return CC.translate("&a成功设置寄存所NPC位置!")
    }
    @Execute(name = "mail")
    @Async
    fun setMailLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.mailNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置邮件NPC位置!")
    }

    @Execute(name = "genesisDemonLoc")
    @Async
    fun setGenesisDemonLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.genesisDemonNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置恶魔阵营NPC位置!")
    }

    @Execute(name = "genesisAngelLoc")
    @Async
    fun setGenesisAngelLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.genesisAngelNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置天使阵营NPC位置!")
    }

    @Execute(name = "sewersFishLoc")
    @Async
    fun setSewersFishLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.sewersFishNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置下水道鱼NPC位置!")
    }

    @Execute(name = "shopNpc")
    @Async
    fun setShopNpcLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.shopNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置商店NPC位置!")
    }

    @Execute(name = "perkNpc")
    @Async
    fun setPerkNpcLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.perkNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置增益NPC位置!")
    }

    @Execute(name = "prestigeNpc")
    @Async
    fun setPrestigeNpcLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.prestigeNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置精通NPC位置!")
    }

    @Execute(name = "debug")
    @Async
    fun debug(@Context player: Player, @Arg("type") type: String): String {
        val pitConfig = ThePit.getInstance().pitConfig
        if (type == "infNpc") {
            pitConfig.infinityNpcLocation = player.location
            pitConfig.save()

            return CC.translate("&a设置成功!")
        }
        val pitGlobal = ThePit.getInstance().globalConfig
        if (type.equals("toggle", ignoreCase = true)) {
            pitGlobal.isDebugServer = !pitGlobal.isDebugServer
            pitGlobal.save()

            ThePit.getInstance().rebootRunnable.addRebootTask(
                RebootTask(
                    "服务器配置切换",
                    System.currentTimeMillis() + 10 * 1000
                )
            )
            if (pitGlobal.isDebugServer) {
                return CC.translate("&a现在开启了，重启以生效")
            } else {
                return CC.translate("&c现在关闭了，重启以生效")
            }
        }

        if (type == "enchNpc") {
            pitConfig.enchantNpcLocation = player.location
            pitGlobal.save()

            return CC.translate("&a设置成功!")
        }

        if (type == "toPublic") {
            pitGlobal.isDebugServerPublic = true
            pitGlobal.save()

            return CC.translate("&a现在开启了")
        }

        if (type == "toPrivate") {
            pitGlobal.isDebugServerPublic = false
            pitGlobal.save()

            return CC.translate("&c现在关闭了")
        }
        return "§c未知type"
    }

    @Execute(name = "statusNpc")
    @Async
    fun setStatusNpcLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.statusNpcLocation = player.location

        ThePit.getInstance().pitConfig.save()

        return CC.translate("&a成功设置统计NPC位置!")
    }

    @Execute(name = "leaderHologram")
    fun setLeaderHologramLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.leaderBoardHologram = player.location

        ThePit.getInstance().pitConfig.save()
        return CC.translate("&a成功设置排行榜全息位置!")
    }

    @Execute(name = "helperHolo")
    fun setHelperHologramLocation(@Context player: Player): String {
        ThePit.getInstance().pitConfig.helperHologramLocation = player.location

        ThePit.getInstance().pitConfig.save()
        return CC.translate("&a成功设置指令帮助全息位置!")
    }


    @Execute(name = "dragonLoc")
    fun setDragonLoc(@Context player: Player): String {
        ThePit.getInstance().pitConfig.dragonEggLoc = player.location
        ThePit.getInstance().pitConfig.save()
        return CC.translate("成功设置龙蛋事件位置！")
    }

    @Execute(name = "pitLoc")
    fun setPitLoc(@Context player: Player, @Arg("type") type: String): String {
        if (type.equals("a", ignoreCase = true)) {
            ThePit.getInstance().pitConfig.pitLocA = player.location
        } else if (type.equals("b", ignoreCase = true)) {
            ThePit.getInstance().pitConfig.pitLocB = player.location
        } else {
            return "§c未知type: a, b"
        }
        ThePit.getInstance().pitConfig.save()
        return "§a已保存"
    }

    @Execute(name = "quest")
    @Async
    fun setQuestNpc(@Context player: Player): String {
        ThePit.getInstance().pitConfig.questNpcLocation = player.location
        ThePit.getInstance().pitConfig.save()
        return CC.translate("&a成功设置任务NPC位置!")
    }

    @Execute(name = "table")
    @Async
    fun setTableNpc(@Context player: Player): String {
        ThePit.getInstance()
            .pitConfig.enchantLocation = player.getTargetBlock(setOf(Material.ENCHANTMENT_TABLE), 100).location
        ThePit.getInstance().pitConfig.save()
        return CC.translate("&a成功设置附魔方块位置: ${ThePit.getInstance().pitConfig.enchantLocation}")
    }


    @Execute(name = "sewers")
    @Async
    fun setSewers(@Context player: Player): String {
        ThePit.getInstance().pitConfig.sewersLocation = player.location
        ThePit.getInstance().pitConfig.save()
        return CC.translate("&a成功设置下水道出生点")
    }

    @Execute(name = "ham")
    fun hamNpc(@Context player: Player): String {
        val config = ThePit.getInstance()
            .pitConfig
        config.hamburgerNpcLocA
            .add(player.location)
        config.save()
        return "§a添加成功: " + config.hamburgerNpcLocA.size
    }

    @Execute(name = "ham clear")
    fun hamClear(@Context player: Player): String {
        val config = ThePit.getInstance()
            .pitConfig
        config.hamburgerNpcLocA.clear()
        config.save()
        return "§a已清空"
    }

    @Execute(name = "spire floor")
    fun spireFloor(@Context player: Player): String {
        val config = ThePit.getInstance()
            .pitConfig
        config.spireFloorLoc
            .add(player.location)
        config.save()

        return "§aNow: " + config.spireFloorLoc.size
    }

    @Execute(name = "spire spawn")
    fun spireSpawn(@Context player: Player) {
        ThePit.getInstance()
            .pitConfig.spireLoc = player.location

        ThePit.getInstance()
            .pitConfig
            .save()
    }

    @Execute(name = "reload")
    fun reloadConfig(@Context sender: CommandSender) {
        sender.sendMessage(CC.translate("&7 重载中..."))
        ThePit.getInstance().configManager.reload()
        PitHook.loadConfig()
        ThePit.getInstance().mapSelector.reload()
        ThePit.getInstance().customEntityNPCFactory.reload()
        for (npc in ThePit.getInstance().npcFactory.pitNpc) {
            npc.npc.setLocation(npc.getNpcSpawnLocation())
        }
        sender.sendMessage(CC.translate("&7 重载完成!"))
    }


    @Execute(name = "edit")
    fun changeEdit(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        profile.isEditingMode = !profile.isEditingMode
        if (profile.isEditingMode) {
            player.sendMessage(CC.translate("&a你现在可以自由破坏方块"))
        } else {
            player.sendMessage(CC.translate("&c你关闭了自由破坏方块"))
        }
    }

    @Execute(name = "kaboom")
    @Shortcut("kaboom")
    fun kaboom(@Context player: Player) {
        for (target in Bukkit.getOnlinePlayers()) {
            target.velocity = Vector(0, 2, 0)
            target.world.strikeLightningEffect(target.location)
            target.sendMessage(CC.translate("&a&lKaboom!!! " + RankUtil.getPlayerColoredName(player.name) + " &7把你击飞了!"))
            KaboomMedal().addProgress(PlayerProfile.getPlayerProfileByUuid(player.uniqueId), 1)
        }
    }

    @Execute(name = "event")
    @Shortcut("event")
    fun event(@Context player: Player, @Arg("action") /*todo*/action: String): String? {
        val success = ThePit.getApi().openEvent(player, action)
        return if (success) {
            CC.translate("&a成功!")
        } else {
            CC.translate("&c失败, 错误的参数")
        }
    }

    @Execute(name = "testSound")
    fun testSound(@Context player: Player, @Arg("sound") sound: String) {
        ThePit.getInstance().soundFactory
            .playSound(sound, player)
    }

    @Execute(name = "changeItemInHand lives")
    @HandHasItem(mythic = true)
    fun changeLives(@Context player: Player, @Arg("lives") lives: Int) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.live = lives
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "changeItemInHand nolive")
    @HandHasItem(mythic = true)
    fun nolive(@Context player: Player) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.live = 0
                it.maxLive = 0
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "nocache")
    fun executeBrokeCache(@Context player: Player) {
        ThePit.getInstance().itemFactory.clientSide = !ThePit.getInstance().itemFactory.clientSide
        player.sendMessage("§a成功设置客户端主导服务端 值为: " + ThePit.getInstance().itemFactory.clientSide)
    }

    @Execute(name = "changeItemInHand color")
    @HandHasItem(mythic = true)
    fun changeColor(@Context player: Player, @Arg("color") color: String) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.color = MythicColor.valueOf(color.uppercase(Locale.getDefault()))
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "changeItemInHand randomUUID")
    @HandHasItem(mythic = true)
    fun randomUUID(@Context player: Player) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.uuid = UUID.randomUUID()
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "changeItemInHand uuid")
    @HandHasItem(mythic = true)
    fun uuid(@Context player: Player, @Arg("uuid") uuid: String) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.uuid = UUID.fromString(uuid)
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "changeItemInHand resetEnch")
    @HandHasItem(mythic = true)
    fun rstEnch(@Context player: Player) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.resetEnch()
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "changeItemInHand ench")
    @HandHasItem(mythic = true)
    fun ench(@Context player: Player, @Arg("enchName") ench: String, @Arg("level") level: String) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                val enchObj = ThePit.getInstance().enchantmentFactor.enchantmentMap.get(ench)
                val levelInt = Integer.valueOf(level)
                if (levelInt == 0) {
                    it.enchantments.remove(enchObj)
                } else {
                    it.enchantments[enchObj] = enchObj?.let { it1 -> min(it1.maxEnchantLevel, levelInt) }
                }
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "changeItemInHand maxLive")
    @HandHasItem(mythic = true)
    fun changeMaxLive(@Context player: Player, @Arg("maxLive") maxLive: Int) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.maxLive = maxLive
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "listavailablePerks")
    @Async
    fun listavailablePerks(@Context player: Player) {
        val perkMap = ThePit.getInstance().perkFactory.perkMap
        player.sendMessage("当前Perk数量为 " + perkMap.size)

        perkMap.values.forEachIndexed { a, it ->
            player.sendMessage(CC.translate("NUM: " + a + " NBT: " + it.internalPerkName + " DISPLAYNAME: " + it.displayName))
        }
    }

    @Execute(name = "listitems")
    @Async
    fun listitems(@Context player: Player) {
        val itemMap = ThePit.getInstance().itemFactor
        player.sendMessage("当前Perk数量为 " + itemMap.itemMap)

        itemMap.itemMap.keys.forEachIndexed() { a, it ->
            player.sendMessage(CC.translate("NUM: $a NBT: $it"))
        }
    }

    @Execute(name = "enchantments")
    @Async
    fun enchantments(@Context player: Player) {
        val enchs = ThePit.getInstance().getEnchantmentFactor()
        player.sendMessage("当前Ench数量为 " + enchs.enchantmentMap.size)

        enchs.enchantmentMap.values.forEachIndexed { index, it ->
            player.sendMessage(
                CC.translate("NUM: " + index + " NBT: " + it.nbtName + " DISPLAYNAME: " + it.enchantName)
            )
        }
    }

    @Execute(name = "itemNbtDump")
    fun nbtDump(@Context player: Player) {
        try {
            val stack = player.itemInHand
            player.sendMessage(Utils.dumpNBTOnString(stack))
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "flushMythicItems")
    fun flushPlayerItem(@Context player: Player, @Arg("player_name") playerName: String) {
        try {
            val player1 = Bukkit.getPlayer(playerName)
            val inventory = player1.inventory
            inventory.forEachIndexed { index, itemStack ->
                val mmItem = ThePit.getInstance().itemFactory.getItemFromStack(itemStack)
                if (mmItem != null) {
                    inventory.remove(index)
                    inventory.setItem(index, mmItem.toItemStack())
                }
            }
            player1.updateInventory()
            player.sendMessage("成功刷新.")
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "give")

    fun give(@Context player: Player, @Arg("nbtName") nbtName: String, @Arg("amount") amount: String) {
        try {
            val clazz = ThePit.getInstance().itemFactor.itemMap[nbtName]
            for (i in 0..Integer.valueOf(amount)) {
                val abstractPitItem = clazz?.newInstance() as AbstractPitItem
                player.inventory.addItem(abstractPitItem.toItemStack())
            }
            val instance = clazz?.newInstance()
            player.sendMessage("成功给予x" + amount + " " + (instance as AbstractPitItem).itemDisplayName)
        } catch (ignored: Exception) {
            player.sendMessage("Error, 神话系物品并没有注册进factor")
        }
    }

    @Execute(name = "changeItemInHand tier")
    @HandHasItem(mythic = true)
    fun changeTier(@Context player: Player, @Arg("tier") tier: Int) {
        try {
            val stack = player.itemInHand
            player.itemInHand = MythicUtil.getMythicItem(stack).also {
                it.tier = tier
            }.toItemStack()
        } catch (ignored: Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "trade")
    @Async
    fun trade(@Context player: Player, @Arg("target") target: String) {
        val profile = ThePit.getInstance().profileOperator
            .namedIOperator(target).profile()
        if (profile == null) {
            player.sendMessage("§c玩家不存在!")
            return
        }
        val tradeA = ThePit.getInstance()
            .mongoDB
            .tradeCollection
            .find(Filters.eq("playerA", profile.uuid))

        val tradeB = ThePit.getInstance()
            .mongoDB
            .tradeCollection
            .find(Filters.eq("playerB", profile.uuid))

        val data: MutableList<TradeData> = ArrayList()
        for (tradeData in tradeA) {
            data.add(tradeData)
        }
        for (tradeData in tradeB) {
            data.add(tradeData)
        }

        ThePit.api.openTradeTrackMenu(player, profile, data)
    }

    @Execute(name = "change")
    fun change(
        @Context player: Player,
        @Arg("target") target: Player,
        @Arg("type") type: String,
        @Arg("value") amount: Int,
        @Flag("save") save: Boolean
    ) {
        val profile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
        if ("coin".equals(type, ignoreCase = true)) {
            profile.coins = amount.toDouble()
            player.sendMessage("§a已修改玩家硬币")
        }
        if ("prestige".equals(type, ignoreCase = true)) {
            profile.setPrestige(amount)
            player.sendMessage("§a已修改玩家精通")
        }
        if ("renown".equals(type, ignoreCase = true)) {
            profile.setRenown(amount)
            player.sendMessage("§a已修改玩家声望")
        }
        if ("streak".equals(type, ignoreCase = true)) {
            profile.streakKills = amount.toDouble()
            player.sendMessage("§a已修改玩家连杀")
        }
        if ("abounty".equals(type, ignoreCase = true)) {
            profile.setActionBounty(amount)
            player.sendMessage("§a已修改玩家行动赏金")
        }
        if ("level".equals(type, ignoreCase = true)) {
            val levelExpRequired = LevelUtil.getLevelTotalExperience(profile.getPrestige(), amount)
            profile.experience = levelExpRequired
            profile.applyExperienceToPlayer(player)
            player.sendMessage("§a已修改玩家等级")
        }
        if ("bounty".equals(type, ignoreCase = true)) {
            profile.setBounty(amount)
            player.sendMessage("§a已修改玩家赏金")
        }
        if ("maxhealth".equals(type, ignoreCase = true)) {
            target.maxHealth = 20.0 + profile.extraMaxHealthValue
            player.sendMessage("§a已更新玩家血量")
        }
        if (save) {
            profile.saveData(null)
            player.sendMessage("§a已保存玩家数据")
        }
    }

    @Execute(name = "internalName")
    fun internalName(@Context player: Player, @Arg("name") internalName: String) {
        try {
            player.itemInHand = ItemBuilder(player.itemInHand)
                .internalName(internalName)
                .build()
            player.sendMessage("§a已修改手持物品的内部名为: §f$internalName")
        } catch (ignored: java.lang.Exception) {
            player.sendMessage("Error")
        }
    }

    @Execute(name = "skipNormalEvent")
    fun skipNormalEvent(@Context player: Player) {
        var eventFactory = ThePit.getInstance().eventFactory
        eventFactory.activeNormalEvent?.let {

            player.sendMessage(CC.translate("已经跳过事件 $it"))
            CC.boardCast("&c&l事件跳过! &c管理员已经跳过该普通事件")
            eventFactory.safeInactiveEvent(it)
        }
        eventFactory.eventTimer.cooldown.fastExpired()
    }


    @Execute(name = "skipEpicEvent")
    fun skipEpicEvent(@Context player: Player) {
        var eventFactory = ThePit.getInstance().eventFactory
        eventFactory.activeEpicEvent?.let {

            player.sendMessage(CC.translate("已经跳过事件 $it"))
            CC.boardCast("&c&l事件跳过! &c管理员已经跳过该Epic事件")
            eventFactory.inactiveEvent(it)

        }
        eventFactory.eventTimer.cooldown.fastExpired()
    }

    @Execute(name = "rareplus")
    fun rareplus(@Context player: Player) {
        if (PlusPlayer.on) {
            PlusPlayer.on = false
            player.sendMessage("§a平衡模式开启！")
        } else {
            PlusPlayer.on = true
            player.sendMessage("§c平衡模式关闭！")
        }
    }

    @Execute(name = "saveAll")
    fun saveAll(@Context player: Player, @Arg("announce") shouldAnnounce: String) {
        try {
            PlayerProfile.saveAllSync(!shouldAnnounce.toBoolean());
        } catch (t: Throwable) {
            player.sendMessage("失败 $shouldAnnounce")
        }
    }
}