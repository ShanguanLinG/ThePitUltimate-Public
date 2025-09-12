package net.mizukilab.pit.command

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.CDKData
import cn.charlotte.pit.data.PlayerProfile
import cn.charlotte.pit.data.mail.Mail
import cn.charlotte.pit.data.operator.IOperator
import cn.charlotte.pit.data.sub.KillRecap
import cn.charlotte.pit.data.sub.OfferData
import cn.charlotte.pit.data.temp.TradeRequest
import cn.charlotte.pit.event.PitPlayerSpawnEvent
import cn.charlotte.pit.perk.AbstractPerk
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.RootCommand
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import dev.rollczi.litecommands.annotations.quoted.Quoted
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_8_R3.NBTTagCompound
import net.mizukilab.pit.audience
import net.mizukilab.pit.command.handler.HandHasItem
import net.mizukilab.pit.enchantment.type.custom.VollewyA
import net.mizukilab.pit.enchantment.type.custom.Volley_B
import net.mizukilab.pit.map.kingsquests.ui.CakeBakeUI
import net.mizukilab.pit.map.kingsquests.ui.KingQuestsUI
import net.mizukilab.pit.menu.offer.OfferMenu
import net.mizukilab.pit.menu.option.PlayerOptionMenu
import net.mizukilab.pit.menu.previewer.EventPreviewerMenu
import net.mizukilab.pit.menu.trade.TradeManager
import net.mizukilab.pit.menu.trade.TradeMenu
import net.mizukilab.pit.menu.viewer.StatusViewerMenu
import net.mizukilab.pit.sendMessage
import net.mizukilab.pit.trade.TradeMonitorRunnable
import net.mizukilab.pit.util.*
import net.mizukilab.pit.util.DateCodeUtils.codeToDate
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.chat.ChatComponentBuilder
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.inventory.InventoryUtil
import net.mizukilab.pit.util.item.ItemBuilder
import net.mizukilab.pit.util.item.ItemUtil
import net.mizukilab.pit.util.level.LevelUtil
import net.mizukilab.pit.util.rank.RankUtil
import net.mizukilab.pit.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * 2024/5/15<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */

@RootCommand
class PitCommands {
    private val viewCooldown: Cache<UUID, Long> = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build()
    private val random = Random()
    private val PATTEN_DEFAULT_YMD = "yyyy-MM-dd"
    private val dateFormat = SimpleDateFormat(PATTEN_DEFAULT_YMD)
    private val numFormat = DecimalFormat("0.00")
    private val COOLDOWN_SHOW: Cache<UUID, Cooldown> =
        CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<UUID, Cooldown>()

    @Execute(name = "thepit", aliases = ["天坑", "天坑乱斗", "version", "ver"])
    fun info(@Context player: Player) {
        player.sendMessage(
            CC.translate(
                "&7Currently running &cThePitUltimate Public"
            )
        )
    }

    @Execute(name = "startDate")
    fun startDate(@Context player: Player) {
        val convertedDate: LocalDate = codeToDate(ThePit.getInstance().serverId)
        player.sendMessage(CC.translate("&e最后运行日期: &a${convertedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"));
    }

    @Execute(name = "bar")
    fun getBar(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)

        player.sendMessage(
            "§8[ " + ProgressBar.getProgressBar(
                profile.experience,
                LevelUtil.getLevelTotalExperience(profile.prestige, profile.level),
                LevelUtil.getLevelTotalExperience(profile.prestige, profile.level + 1),
                12
            ) + " §8]"
        )
    }

    @Execute(name = "option", aliases = ["options", "opt", "setting", "settings"])
    fun option(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        if (profile.getPrestige() > 0 || profile.level >= 5) {
            PlayerOptionMenu().openMenu(player)
        } else {
            player.sendMessage(
                CC.translate(
                    "&c&l等级不足! &7此指令在 " + LevelUtil.getLevelTag(
                        profile.getPrestige(),
                        5
                    ) + " &7时解锁."
                )
            )
        }
    }

    @Execute(name = "view")
    fun view(@Context player: Player, @Arg("name") name: String) {
        val present = viewCooldown.getIfPresent(player.uniqueId)
        if (present != null && !player.hasPermission("pit.view-bypass")) {
            player.sendMessage(CC.translate("&c冷却中..."))
            return
        }

        viewCooldown.put(player.uniqueId, System.currentTimeMillis())

        try {
            val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
            if (profile.level < 70 && profile.getPrestige() < 1) {
                player.sendMessage(
                    CC.translate(
                        "&c&l等级不足! &7此指令在 " + LevelUtil.getLevelTag(
                            profile.getPrestige(),
                            70
                        ) + " &7时解锁."
                    )
                )
                return
            }
            Bukkit.getScheduler().runTaskAsynchronously(ThePit.getInstance()) {
                val lookupStrict = ThePit.getInstance()?.profileOperator?.namedIOperator(name)
                if (lookupStrict == null || lookupStrict.profile().playerName == "NotLoadPlayer") {
                    player.sendMessage(CC.translate("&c此玩家的档案不存在,请检查输入是否有误."))
                    return@runTaskAsynchronously
                }
                if (!player.hasPermission("pit.admin") || player.isSpecial) {
                    if (!name.equals(player.name, ignoreCase = true) && lookupStrict.profile().isSpecial) {
                        player.sendMessage(CC.translate("&c此玩家的档案不存在,请检查输入是否有误."))
                        return@runTaskAsynchronously
                    }
                }
                /*                if (lookupStrict.profile().playerUuid == player.uniqueId) {
                                    player.sendMessage(CC.translate("&c疑? 为什么要查看自己档案?"))
                                    return@runTaskAsynchronously
                                }*/
                Bukkit.getScheduler().runTask(ThePit.getInstance()) {
                    StatusViewerMenu(lookupStrict.profile()).openMenu(
                        player
                    )
                }
            }
        } catch (e: Exception) {
            if (player.hasPermission("pit.admin")) {
                CC.printError(player, e)
            }
        }
    }

    @Execute(name = "events")
    fun events(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        if (!profile.isSupporter && !PlayerUtil.isStaff(player)) {
            player.sendMessage(CC.translate("&c你需要购买 &e天坑乱斗会员 &c才可以使用此指令!"))
            return
        }
        EventPreviewerMenu().openMenu(player)
    }

    @Execute(name = "show")
    fun show(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        if (!profile.isSupporter && !PlayerUtil.isStaff(player)) {
            player.sendMessage(CC.translate("&c你需要购买 &e天坑乱斗会员 &c才可以使用此指令!"))
            return
        }
        var ifPresent = COOLDOWN_SHOW.getIfPresent(player.uniqueId)
        if ((ifPresent != null && ifPresent.hasExpired()) && !player.hasPermission("thepit.admin")) {
            player.sendMessage(
                CC.translate(
                    "此指令仍在冷却中: " + TimeUtil.millisToTimer(
                        ifPresent.remaining
                    )
                )
            )
            return
        }
        if (player.itemInHand == null || player.itemInHand.type == Material.AIR) {
            player.sendMessage(CC.translate("&c请先手持要展示的物品!"))
            return
        }
        if (player.itemInHand.itemMeta.displayName == null && !player.hasPermission("pit.admin")) {
            player.sendMessage(CC.translate("&c此物品无法被用于展示!"))
            return
        }
        COOLDOWN_SHOW.put(player.uniqueId, Cooldown(60, TimeUnit.SECONDS))
        val nms = Utils.toNMStackQuick(player.itemInHand)
        val tag = NBTTagCompound()
        nms.save(tag)
        val hoverEventComponents = arrayOf<BaseComponent>(
            TextComponent(tag.toString())
        )
        var showPlayers = Bukkit.getOnlinePlayers()
        if (player.isSpecial) {
            showPlayers = buildSet {
                Bukkit.getOnlinePlayers().forEach {
                    if (it.hasPermission("pit.admin") && !it.isSpecial) {
                        add(it)
                    }
                }
                add(player)
            }
        }
        for (p in showPlayers) {
            p.spigot().sendMessage(
                *ChatComponentBuilder(CC.translate("&a&l物品展示! &7" + profile.formattedName + " &7正在展示物品: &f" + (if (player.itemInHand.itemMeta.displayName == null) player.itemInHand.type.name else player.itemInHand.itemMeta.displayName) + " &e[查看]"))
                    .setCurrentHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents)).create()
            )
        }
    }

    @Execute(name = "tradeLimits")
    fun sendTradeLimits(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        val now = System.currentTimeMillis()
        val date = profile.tradeLimit.lastRefresh

        //获取
        // 今天的日期
        val
                nowDay = dateFormat.format(now)

        //对比的时间
        val day = dateFormat.format(date)


        //daily reset
        if (day != nowDay && Calendar.getInstance()[Calendar.HOUR_OF_DAY] >= 4) {
            profile.tradeLimit.lastRefresh = now
            profile.tradeLimit.amount = 0.0
            profile.tradeLimit.times = 0
        }
        player.sendMessage(CC.translate("&6&l每日交易限制! &7(每日4:00 AM重置)"))
        player.sendMessage(CC.translate("&7每日硬币交易上限: " + (if (profile.tradeLimit.amount >= 50000) "&c" else "&a") + profile.tradeLimit.amount + "&7/&650000g"))
        player.sendMessage(CC.translate("&7每日交易次数上限: &e" + profile.tradeLimit.times + "/25"))
    }

    @Execute(name = "trade")
    fun onRequest(@Context player: Player, @Arg("target") target: Player) {
        if (player.uniqueId == target.uniqueId || target.isSpecial || player.isSpecial) {
            if (!player.hasPermission("pit.admin")) {
                player.sendMessage(CC.translate("&c你无法选择此玩家进行交易!"))
                return
            }
        }
        if (player.name == target.name) {
            player.sendMessage(CC.translate("&c你无法对自己发起交易!"))
            return
        }
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        val targetProfile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
        if (!profile.combatTimer.hasExpired()) {
            player.sendMessage(CC.translate("&c你无法在战斗中使用此功能!"))
            return
        }

        // 当前时间
        val now = System.currentTimeMillis()
        val date = profile.tradeLimit.lastRefresh

        //获取今天的日期
        val nowDay = dateFormat.format(now)

        //对比的时间
        val day = dateFormat.format(date)


        //daily reset
        if (day != nowDay && Calendar.getInstance()[Calendar.HOUR_OF_DAY] >= 4) {
            profile.tradeLimit.lastRefresh = now
            profile.tradeLimit.amount = 0.0
            profile.tradeLimit.times = 0
        }

        if (profile.tradeLimit.times >= 25) {
            player.sendMessage(CC.translate("&c你今天的交易次数已经达到上限! (25/25)"))
            player.sendMessage(CC.translate("&c使用 &e/tradeLimits &c查看你的今日交易上限情况."))
            return
        }

        if (!player.name.equals(player.displayName, ignoreCase = true)) {
            player.sendMessage(CC.translate("&c你无法在匿名模式下使用交易功能!"))
            return
        }

        if (profile.level < 60) {
            player.sendMessage(
                CC.translate(
                    "&c&l等级不足! &7此指令在 " + LevelUtil.getLevelTag(
                        profile.getPrestige(),
                        60
                    ) + " &7时解锁."
                )
            )
            return
        }

        for (tradeRequest in TradeMonitorRunnable.getTradeRequests()) {
            if (tradeRequest.target == player && tradeRequest.player == target) {
                val tradeManager = TradeManager(player, target)
                TradeMenu(tradeManager).openMenu(player)
                TradeMenu(tradeManager).openMenu(target)
                return
            }
            if (tradeRequest.target == target && tradeRequest.player == player) {
                player.sendMessage(CC.translate("&c你已经发送过请求了,请等待对方接受!"))
                return
            }
        }



        TradeMonitorRunnable.getTradeRequests().add(TradeRequest(player, target))
        /*      if (PlusPlayer.on) {
                  if (player.isBlacks || target.isBlacks) {
                      TradeMonitorRunnable.getTradeRequests().remove(TradeRequest(player, target))
                      return
                  }
              }*/
        if (!targetProfile.playerOption.isTradeNotify && !player.hasPermission(PlayerUtil.getStaffPermission())) {
            player.sendMessage(CC.translate("&c对方在游戏选项之后中设置了不接受交易请求,因此无法查看你的请求提示."))
            player.sendMessage(CC.translate("&c但对方仍可以通过使用 &e/trade " + player.name + " &c以同意你的请求."))
            return
        } else {
            player.sendMessage(
                CC.translate(
                    "&a&l交易请求发送! &7成功向 " + LevelUtil.getLevelTag(
                        targetProfile.getPrestige(),
                        targetProfile.level
                    ) + " " + RankUtil.getPlayerColoredName(target.uniqueId) + " &7发送了交易请求!"
                )
            )
        }

        target.sendMessage(
            Component.text(
                CC.translate(
                    "&6&l交易请求! " + LevelUtil.getLevelTag(
                        profile.getPrestige(),
                        profile.level
                    ) + " " + RankUtil.getPlayerColoredName(player.uniqueId) + " &7向你发送了交易请求,请"
                )
            )
                .append(
                    Component.text(CC.translate(" &6&o点击这里"))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/trade " + player.name))
                        .hoverEvent(Component.text("/trade " + player.name))
                )
                .append(Component.text(CC.translate(" &r&7以接受交易请求.")))
        )
    }

    @Execute(name = "respawn", aliases = ["spawn", "home", "back"])
    fun respawn(@Context player: Player) {
        if (player.hasMetadata("backing")) {
            player.sendMessage(CC.translate("&c&l已有一个计划中的回城..."))
            return
        }

        if (!PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
                .combatTimer
                .hasExpired()
        ) {
            player.sendMessage(CC.translate("&c&l您无法在战斗中传送！"))
            return
        }

        if (player.gameMode == GameMode.SPECTATOR) {
            player.sendMessage(CC.translate("&c&l您无法在当前状态下传送！"))
            return
        }


        //player.sendMessage(CC.translate("&c&l即将传送,请保持脱战状态并不要移动位置..."));
        player.setMetadata("backing", FixedMetadataValue(ThePit.getInstance(), true))

        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), {
            if (player.isOnline) {
                if (player.hasMetadata("backing")) {
                    val location = ThePit.getInstance().pitConfig
                        .spawnLocations[random.nextInt(ThePit.getInstance().pitConfig.spawnLocations.size)]

                    player.removeMetadata("backing", ThePit.getInstance())

                    player.teleport(location)

                    for (item in player.inventory) {
                        if (ItemUtil.isRemovedOnJoin(item)) {
                            player.inventory.remove(item)
                        }
                    }

                    if (ItemUtil.isRemovedOnJoin(player.inventory.helmet)) {
                        player.inventory.helmet = ItemStack(Material.AIR)
                    }

                    if (ItemUtil.isRemovedOnJoin(player.inventory.chestplate)) {
                        player.inventory.chestplate = ItemStack(Material.AIR)
                    }

                    if (ItemUtil.isRemovedOnJoin(player.inventory.leggings)) {
                        player.inventory.leggings = ItemStack(Material.AIR)
                    }

                    if (ItemUtil.isRemovedOnJoin(player.inventory.boots)) {
                        player.inventory.boots = ItemStack(Material.AIR)
                    }

                    val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
                    profile.streakKills = 0.0
                    profile.isInArena = false

                    PitPlayerSpawnEvent(player).callEvent()

                    PlayerUtil.resetPlayer(player, true, false)
                }
            }
        }, 1)
    }

    @Execute(name = "iKnowIGotWiped")
    fun iKnowIGotWiped(@Context player: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        if (profile.wipedData != null) {
            profile.wipedData.isKnow = true
        }
    }

    @Execute(name = "killRecap")
    fun killRecap(@Context player: Player) {
        val killRecap = KillRecap.recapMap[player.uniqueId]
        if (killRecap == null || killRecap.killer == null || killRecap.assistData == null) {
            player.sendMessage(CC.translate("&c&l错误! &7未找到有效的近期死亡回放数据,抱歉!"))
            return
        }
        val title = Component.text("KleefuckYou")
        player.audience.openBook(Book.book(title, title, buildList {
            add(
                Component.text(CC.translate("&c&l死亡回放"))
                    .appendNewline().appendNewline()
                    .append(Component.text(CC.translate("&0此系统仍在开发中,")))
                    .appendNewline()
                    .append(Component.text(CC.translate("&0展示数据不保证100%准确!")))
                    .appendNewline().appendNewline()
                    .append(Component.text(CC.translate("&0你: " + RankUtil.getPlayerRealColoredName(player.uniqueId))))
                    .appendNewline().appendNewline()
                    .let {
                        val collect = ThePit.getInstance()
                            .perkFactory
                            .perks
                            .stream()
                            .filter { abstractPerk: AbstractPerk ->
                                killRecap.perk.contains(abstractPerk.internalPerkName)
                            }
                            .collect(Collectors.toList())
                        val killerHover = StringBuilder()
                        val killerProfile = PlayerProfile.getPlayerProfileByUuid(killRecap.killer)
                        killerHover.append(CC.translate(killerProfile.formattedName))
                            .append("\n")
                        for (perk in collect) {
                            killerHover.append(CC.translate("&e"))
                                .append(CC.translate(perk.displayName))
                                .append("\n")
                        }
                        it.append(Component.text(CC.translate("&0击杀者:")))
                            .appendNewline()
                            .append(
                                Component.text(CC.translate(RankUtil.getPlayerColoredName(killRecap.killer)))
                                    .hoverEvent(Component.text(killerHover.toString()))
                            )
                            .appendNewline()
                    }.let {
                        val killerCoin = java.lang.StringBuilder()
                        killerCoin.append(CC.translate("&f基础奖励: &6+" + numFormat.format(killRecap.baseCoin)))
                            .append("\n")
                        if (killRecap.notStreakCoin > 0) {
                            killerCoin.append(CC.translate("&f首杀奖励: &6+" + numFormat.format(killRecap.notStreakCoin)))
                                .append("\n")
                        }
                        if (killRecap.levelDisparityCoin > 0) {
                            killerCoin.append(CC.translate("&f等级/装备差距: &6+" + numFormat.format(killRecap.levelDisparityCoin)))
                                .append("\n")
                        }
                        if (killRecap.otherCoin > 0) {
                            killerCoin.append(CC.translate("&f其他奖励: &6+" + numFormat.format(killRecap.otherCoin)))
                                .append("\n")
                        }

                        val killerExp = java.lang.StringBuilder()
                        killerExp.append(CC.translate("&f基础奖励: &b+" + numFormat.format(killRecap.baseExp)))
                            .append("\n")
                        if (killRecap.notStreakExp > 0) {
                            killerExp.append(CC.translate("&f首杀奖励: &b+" + numFormat.format(killRecap.notStreakExp)))
                                .append("\n")
                        }
                        if (killRecap.levelDisparityExp > 0) {
                            killerExp.append(CC.translate("&f等级/装备差距: &b+" + numFormat.format(killRecap.levelDisparityExp)))
                                .append("\n")
                        }
                        if (killRecap.otherExp > 0) {
                            killerExp.append(CC.translate("&f其他奖励: &b+" + numFormat.format(killRecap.otherExp)))
                                .append("\n")
                        }
                        it.append(
                            Component.text(CC.translate("&6+" + numFormat.format(killRecap.totalCoin) + "硬币"))
                                .hoverEvent(Component.text(killerCoin.toString()))
                        )
                            .append(
                                Component.text(CC.translate(" &b+" + numFormat.format(killRecap.totalExp) + "经验值"))
                                    .hoverEvent(Component.text(killerExp.toString()))
                            )
                    }
            )
            var i = 1
            var component = Component.text(CC.translate("&0助攻 (" + killRecap.assistData.size + "):"))
                .appendNewline()
            for (assistData in killRecap.assistData) {
                val assistCoin = java.lang.StringBuilder()
                assistCoin.append(CC.translate("&f基础奖励: &6+" + numFormat.format(killRecap.baseCoin * assistData.percentage)))
                    .append("\n")
                if (assistData.streakCoin > 0) {
                    assistCoin.append(CC.translate("&f连杀奖励: &6+" + numFormat.format(assistData.streakCoin)))
                        .append("\n")
                }
                if (assistData.levelDisparityCoin > 0) {
                    assistCoin.append(CC.translate("&f等级/装备差距: &6+" + numFormat.format(assistData.levelDisparityCoin)))
                        .append("\n")
                }
                val assistExp = java.lang.StringBuilder()
                assistExp.append(CC.translate("&f基础奖励: &b+" + numFormat.format(killRecap.baseExp * assistData.percentage)))
                    .append("\n")
                if (assistData.streakExp > 0) {
                    assistExp.append(CC.translate("&f连杀奖励: &b+" + numFormat.format(assistData.streakExp)))
                        .append("\n")
                }
                if (assistData.levelDisparityExp > 0) {
                    assistExp.append(CC.translate("&f等级/装备差距: &b+" + numFormat.format(assistData.levelDisparityExp)))
                        .append("\n")
                }
                component = component
                    .append(Component.text(CC.translate("&0" + numFormat.format(assistData.percentage * 100) + "% " + assistData.displayName)))
                    .appendNewline()
                    .append(
                        Component.text(CC.translate("&6+" + numFormat.format(assistData.totalCoin) + "硬币"))
                            .hoverEvent(Component.text(assistCoin.toString()))
                    )
                    .append(
                        Component.text(CC.translate(" &b+" + numFormat.format(assistData.totalExp) + "经验值"))
                            .hoverEvent(Component.text(assistExp.toString()))
                    )
                    .appendNewline()
                    .appendNewline()
                ++i
                if (i >= 5) {
                    add(component)
                    component = Component.text("")
                    i = 0
                }
            }
            if (i > 0) {
                add(component)
            }
            i = 1
            component = Component.text(CC.translate("&0&l伤害日志 (" + killRecap.damageLogs.size + "):"))
            for (damageData in killRecap.damageLogs) {
                component = component
                    .append(
                        Component.text(
                            CC.translate(
                                "&7" + ((killRecap.completeTime - damageData.timer.start) / 1000) + "秒前  &c" + numFormat.format(
                                    damageData.damage
                                ) + " " + (if (damageData.isMelee) "近战" else "远程")
                            )
                        )/*.let {
                        if (damageData.usedItem != null && damageData.usedItem.type != Material.AIR) {
                            val usedItem = damageData.getUsedItem()
                            val nbt = usedItem.nbt
                            val item = net.kyori.adventure.text.event.HoverEvent.ShowItem.of(Key.key(nbt.getString("id")), 1,
                                BinaryTagHolder.binaryTagHolder(nbt.getCompound("tag").toString()) )
                            it.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showItem(item))
                        }
                    }*/
                    )
                    .append(
                        Component.text(CC.translate((if (damageData.isAttack) "&0⚔ " else "&0☬ ") + damageData.displayName))
                            .hoverEvent(
                                Component.text(
                                    CC.translate((if (damageData.isAttack) "&c攻击" else "&c受到攻击")) + "\n" + CC.translate(
                                        """
                                    &7攻击后${if (damageData.isAttack) "此玩家" else "自身"}剩余血量: &c${
                                            numFormat.format(
                                                damageData.afterHealth
                                            )
                                        }
                                    ${CC.translate("&7附魔/天赋 附加伤害: &c" + numFormat.format(damageData.boostDamage))}
                                    """.trimIndent()
                                    )
                                )
                            )
                    ).appendNewline().appendNewline()
                ++i
                if (i >= 5) {
                    add(component)
                    component = Component.text("")
                    i = 0
                }
            }
        }))
        // todo
    }

    @Execute(name = "cdk")
    fun cdk(@Context player: Player, @Arg("cdk") cdk: String) {
        val data = CDKData.getCachedCDK()[cdk]
        if (data == null) {
            player.sendMessage(CC.translate("&r&c错误的CDK,请仔细检查哦"))
            player.sendMessage(CC.translate("&r&c如您是在商店购买的点券卡密请输入/code进行兑换!"))
            return
        }

        if (data.limitPermission != null) {
            if (!player.hasPermission(data.limitPermission)) {
                player.sendMessage(CC.translate("&c这个CDK似乎不适合你!"))
                return
            }
        }
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)

        if (data.limitPrestige <= 0 && profile.getPrestige() == 0 && profile.level < data.limitLevel) {
            player.sendMessage(CC.translate("&c你不满足领取条件!"))
            return
        }
        if (data.limitPrestige > 0 && profile.getPrestige() < data.limitPrestige) {
            player.sendMessage(CC.translate("&c精通等级不满足要求哦,快去升级吧!"))
            return
        }
        if (data.limitPrestige > 0 && data.limitLevel > 0 && profile.getPrestige() == data.limitPrestige && profile.level < data.limitLevel) {
            player.sendMessage("§c似乎还差一点等级就能领取到了...加油!")
            return
        }

        val now = System.currentTimeMillis()
        if (now > data.expireTime) {
            player.sendMessage(CC.translate("&c错误的CDK,请检查大小写是否一致!"))
            player.sendMessage(CC.translate("&r&c如您是在商店购买的卡密请输入/code进行兑换!"))
            return
        }

        if (CDKData.isLoading()) {
            player.sendMessage(CC.translate("&c系统繁忙,请稍后再试!"))
            return
        }

        if (data.limitClaimed != -1 && data.claimedPlayers.size >= data.limitClaimed) {
            player.sendMessage(CC.translate("&c领取已达上限,下次快一点哦!"))
            return
        }

        val added = profile.usedCdk.add(cdk)
        if (added) {
            val mail = Mail()
            mail.expireTime = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L
            mail.coins = data.coins
            mail.exp = data.exp
            mail.renown = data.renown
            mail.item = data.item
            mail.sendTime = System.currentTimeMillis()
            mail.title = "&e【奖励】兑换码兑换奖励"
            mail.content = "&f亲爱的玩家: 请查收通过兑换码获得的奖励"


            data.claimedPlayers.add(player.name)
            data.active()

            profile.getMailData().sendMail(mail)
            player.playSound(player.location, Sound.LEVEL_UP, 1f, 0.8f)
            player.sendMessage(CC.translate("&a领取成功! 请在邮件NPC处领取奖励!"))
        } else {
            player.sendMessage(CC.translate("&c你已经领取过这个CDK了!"))
        }
    }

    @Execute(name = "viewOffer")
    fun viewOffer(@Context player: Player, @Arg("target") target: Player) {
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
        val targetProfile = ThePit.getInstance().profileOperator.ifPresentAndILoaded(
            target
        ) { operator: IOperator ->
            val targetProfile = operator.profile()
            if (player.isSpecial || target.isSpecial) {
                if (!player.hasPermission("pit.admin") || player.isSpecial) {
                    player.sendMessage(CC.translate("&c你无法选择此玩家进行交易报价!"))
                    return@ifPresentAndILoaded
                }
            }


            if (targetProfile.offerData.buyer == null || targetProfile.offerData.buyer != player.uniqueId) {
                player.sendMessage(CC.translate("&c你没有来自此玩家的交易报价!"))
                return@ifPresentAndILoaded
            }

            if (targetProfile.offerData.hasUnclaimedOffer()) {
                player.sendMessage(CC.translate("&c此交易报价已过期!"))
                return@ifPresentAndILoaded
            }

            if (!profile.combatTimer.hasExpired()) {
                player.sendMessage(CC.translate("&c你无法在战斗中使用此功能!"))
                return@ifPresentAndILoaded
            }

            if (profile.tradeLimit.times >= 25) {
                player.sendMessage(CC.translate("&c你今天的交易次数已经达到上限!"))
                player.sendMessage(CC.translate("&c使用 &e/tradeLimits &c查看你的今日交易上限情况."))
                return@ifPresentAndILoaded
            }

            if (profile.tradeLimit.amount + targetProfile.offerData.price >= 50000) {
                player.sendMessage(CC.translate("&c对方的开价加上今日已交易量已超过交易上限,因此你无法接受此交易报价."))
                player.sendMessage(CC.translate("&c使用 &e/tradeLimits &c查看你的今日交易上限情况."))
                return@ifPresentAndILoaded
            }

            if (!player.name.equals(player.displayName, ignoreCase = true)) {
                player.sendMessage(CC.translate("&c你无法在匿名模式下使用交易功能!"))
                return@ifPresentAndILoaded
            }

            if (profile.level < 60) {
                player.sendMessage(
                    CC.translate(
                        "&c&l等级不足! &7此指令在 " + LevelUtil.getLevelTag(
                            profile.getPrestige(),
                            60
                        ) + " &7时解锁."
                    )
                )
                return@ifPresentAndILoaded
            }

            OfferMenu(target).openMenu(player)
        }
    }

    @Execute(name = "offer")
    fun offer(@Context player: Player, @Arg("target") targetPlayer: String, @Arg("price") price: String) {
        if (!player.hasPermission("pit.admin")) {
            player.sendMessage(CC.translate("&c该指令已经弃用, 请使用 /trade"))
            return
        }
        val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)


        if (profile.offerData.hasActiveOffer()) {
            player.sendMessage(CC.translate("&c你当前有一个正在进行中的交易报价!"))
            return
        } else if (profile.offerData.hasUnclaimedOffer()) {
            if (InventoryUtil.isInvFull(player)) {
                player.sendMessage(CC.translate("&c你有一个未结算的交易报价,请将背包腾出空间后重试!"))
            } else {
                InventoryUtil.addInvReverse(player.inventory, profile.offerData.itemStack)
                profile.offerData = OfferData()
                player.sendMessage(CC.translate("&c你有一个未结算的交易报价,相关物品已退还到你的背包.要发起一个新的交易报价,请再次输入此指令."))
            }
            return
        }
        if (targetPlayer == "#null" || price == "#null") {
            player.sendMessage(CC.translate("&c&l错误的使用方法! &7请手持要出售的物品,输入 &e/offer 玩家名 你的出价 &7来向此玩家发送一个报价请求,对方同意后将获得你提供的物品,你获得出价的硬币."))
            return
        }
        try {
            if (price.toInt() <= 0) {
                player.sendMessage("§c请输入正确的价格!")
                return
            }
        } catch (e: java.lang.Exception) {
            player.sendMessage(CC.translate("&c你输入的价格有误!"))
            return
        }
        if (Bukkit.getPlayer(targetPlayer) == null || !Bukkit.getPlayer(targetPlayer).isOnline || PlayerUtil.isVanished(
                Bukkit.getPlayer(targetPlayer)
            )
        ) {
            player.sendMessage(CC.translate("&c你选择的玩家不在线!"))
            return
        }
        val target = Bukkit.getPlayer(targetPlayer)
        ThePit.getInstance().profileOperator.ifPresentAndILoaded(target) { operator: IOperator ->
            val targetProfile = operator.profile()
            if (player.uniqueId == target.uniqueId || player.isSpecial || target.isSpecial) {
                if (!player.hasPermission("pit.admin") || player.isSpecial) {
                    player.sendMessage(CC.translate("&c你无法选择此玩家进行交易!"))
                    return@ifPresentAndILoaded
                }
            }
            if (!profile.combatTimer.hasExpired()) {
                player.sendMessage(CC.translate("&c你无法在战斗中使用此功能!"))
                return@ifPresentAndILoaded
            }


            // 当前时间
            val now = System.currentTimeMillis()
            val date = profile.tradeLimit.lastRefresh

            //获取今天的日期
            val nowDay = dateFormat.format(now)

            //对比的时间
            val day = dateFormat.format(date)


            //daily reset
            if (day != nowDay && Calendar.getInstance()[Calendar.HOUR_OF_DAY] >= 4) {
                profile.tradeLimit.lastRefresh = now
                profile.tradeLimit.amount = 0.0
                profile.tradeLimit.times = 0
            }

            if (profile.tradeLimit.times >= 25) {
                player.sendMessage(CC.translate("&c你今天的交易次数已经达到上限! (25/25)"))
                player.sendMessage(CC.translate("&c使用 &e/tradeLimits &c查看你的今日交易上限情况."))
                return@ifPresentAndILoaded
            }

            if (profile.tradeLimit.amount + price.toInt() >= 50000) {
                player.sendMessage(CC.translate("&c你的开价加上今日已交易量已超过交易上限,因此你无法发起此交易报价."))
                player.sendMessage(CC.translate("&c使用 &e/tradeLimits &c查看你的今日交易上限情况."))
                return@ifPresentAndILoaded
            }

            if (!player.name.equals(player.displayName, ignoreCase = true)) {
                player.sendMessage(CC.translate("&c你无法在匿名模式下使用交易功能!"))
                return@ifPresentAndILoaded
            }

            if (profile.level < 60) {
                player.sendMessage(
                    CC.translate(
                        "&c&l等级不足! &7此指令在 " + LevelUtil.getLevelTag(
                            profile.getPrestige(),
                            60
                        ) + " &7时解锁."
                    )
                )
                return@ifPresentAndILoaded
            }
            if (player.name == targetPlayer) {
                player.sendMessage(CC.translate("&c你无法对自己发起交易!"))
                return@ifPresentAndILoaded
            }

            if (player.itemInHand == null || player.itemInHand.type == Material.AIR) {
                player.sendMessage(CC.translate("&c请手持你要出售的物品再设置出售对象与价格!"))
                return@ifPresentAndILoaded
            }

            if (!ItemUtil.canTrade(player.itemInHand)) {
                player.sendMessage(CC.translate("&c此物品无法用于交易!"))
                return@ifPresentAndILoaded
            }


            //todo: create offer
            profile.offerData.createOffer(target.uniqueId, player.itemInHand, price.toInt().toDouble())
            player.itemInHand = ItemStack(Material.AIR)
            if (!targetProfile.playerOption.isTradeNotify && !player.hasPermission(PlayerUtil.getStaffPermission())) {
                player.sendMessage(CC.translate("&c对方在游戏选项之后中设置了不接受交易请求,因此无法查看你的请求提示."))
                player.sendMessage(CC.translate("&c但对方仍可以通过使用 &e/viewOffer " + player.name + " &c以同意你的请求."))
            } else {
                player.sendMessage(
                    CC.translate(
                        "&e&l交易报价发送! &7成功向 " + LevelUtil.getLevelTag(
                            targetProfile.getPrestige(),
                            targetProfile.level
                        ) + " " + RankUtil.getPlayerColoredName(target.uniqueId) + " &7发送了交易报价!"
                    )
                )
                target.spigot()
                    .sendMessage(
                        *ChatComponentBuilder(
                            CC.translate(
                                "&e&l交易报价! " + LevelUtil.getLevelTag(
                                    profile.getPrestige(),
                                    profile.level
                                ) + " " + RankUtil.getPlayerColoredName(player.uniqueId) + " &7向你发送了交易报价,请"
                            )
                        )
                            .append(
                                ChatComponentBuilder(CC.translate(" &e点击这里")).setCurrentHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        ChatComponentBuilder(CC.translate("&6点击以接受")).create()
                                    )
                                ).setCurrentClickEvent(
                                    ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewOffer " + player.name)
                                ).create()
                            )
                            .append(CC.translate(" &r&7以查看此交易报价."))
                            .create()
                    )
            }
        }
    }

    @Execute(name = "AuctionGui")
    fun auctionGui(@Context player: Player) {
        val eventFactory = ThePit.getInstance().eventFactory

        //check if event is available
        if (eventFactory.activeNormalEvent == null || "auction" != eventFactory.activeNormalEventName) {
            player.sendMessage(CC.translate("&c此指令当前无法使用!"))
            return
        }
        ThePit.getApi().openAuctionMenu(player)
    }

    @Execute(name = "changeVolley")
    @HandHasItem
    fun changeVolley(@Context player: Player) {
        val mythicItem = MythicUtil.getMythicItem(player.itemInHand)

        val volleyALevel = Utils.getEnchantLevel(mythicItem, "volley_enchant")
        val volleyBLevel = Utils.getEnchantLevel(mythicItem, "volley_enchant_B")

        if (volleyALevel < 1 && volleyBLevel < 1) {
            player.sendMessage(CC.translate("&c手中神话武器，并没有任何一类连射附魔！"))
            return
        }

        when {
            volleyALevel >= 1 -> {
                mythicItem.enchantments.apply {
                    put(Volley_B(), volleyALevel)
                    remove(VollewyA())
                }

                player.itemInHand = mythicItem.toItemStack()
                player.sendMessage(CC.translate("&a已将连射附魔切换为 B 类型！"))
            }

            volleyBLevel >= 1 -> {

                mythicItem.enchantments.apply {
                    put(VollewyA(), volleyBLevel)
                    remove(Volley_B())
                }

                player.itemInHand = mythicItem.toItemStack()
                player.sendMessage(CC.translate("&a已将连射附魔切换为 A 类型！"))
            }
        }
    }

    @Execute(name = "cool")
    fun cool(@Context player: Player) {
        if (!PlayerUtil.isPlayerUnlockedPerk(player, "cool_perk")) {
            player.sendMessage(CC.translate("&c你当前无法使用该指令!"))
            return
        }
        val list: MutableList<String> = ArrayList()
        for (target in Bukkit.getOnlinePlayers()) {
            if (PlayerUtil.isVanished(target)) {
                continue
            }
            val profile = PlayerProfile.getPlayerProfileByUuid(target.uniqueId)
            if (profile.isNicked && profile.nickPrestige > 0) {
                list.add(CC.translate(profile.formattedNameWithRoman))
            } else if (!profile.isNicked && profile.getPrestige() > 0) {
                list.add(CC.translate(profile.formattedNameWithRoman))
            }
        }
        if (list.size > 0) {
            player.sendMessage(CC.translate("&b当前在线精通玩家数: &6" + list.size))
            list.forEach { s: String? -> player.sendMessage(s) }
        } else {
            player.sendMessage(CC.translate("&c当前没有精通玩家在线!"))
        }
    }

    @Execute(name = "openKingsQuestUI")
    @Permission("pit.kingquest.ui")
    fun openKingsQuestUI(@Context player: Player) {
        KingQuestsUI.openMenu(player)
    }

    @Execute(name = "openBakeMaster")
    @Permission("pit.kingquest.ui")
    fun openBakeMaster(@Context player: Player) {
        CakeBakeUI.openMenu(player)
    }

    @Execute(name = "rename")
    @Permission("pit.rename")
    @HandHasItem(mythic = true)
    fun rename(@Context player: Player, @Quoted @Arg("name") name: String): String {
        if (!player.hasPermission("pit.rename-bypass")) {
            val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId) ?: return "§c获取玩家信息失败"
            val cdEndTime = profile.lastRenameTime + Duration.ofMinutes(2).toMillis()
            val now = System.currentTimeMillis()
            if (cdEndTime > now) {
                return "§c冷却中!请等待${(cdEndTime - now) / 1000}s"
            }
            profile.lastRenameTime = System.currentTimeMillis()
        }
        val item = player.itemInHand
        val mythicItem = MythicUtil.getMythicItem(item)
        val permission = "pit.rename-color"
        if (player.hasPermission(permission)) {
            val translate = CC.translate(name)
            if (CC.stripColor(translate).isBlank()) {
                return "§c不可以这样哦!"
            }
            mythicItem.customName = translate
        } else if (name.contains("&") && !player.hasPermission(permission)) {
            return CC.translate("&c需要拥有颜色字符权限方可命名颜色名称！")
        } else {
            mythicItem.customName = name
        }
        player.itemInHand = ItemBuilder(mythicItem.toItemStack())
            .forceCanTrade(false)
            .build()
        return "§a已重命名"
    }

    @Execute(name = "unrename")
    @Permission("pit.unrename")
    @HandHasItem(mythic = true)
    fun unRename(@Context player: Player): String {
        val item = player.itemInHand
        val mythicItem = MythicUtil.getMythicItem(item)
        if (mythicItem.customName == null) {
            return "§c咦...这个物品似乎没有被重命名呢!"
        }
        mythicItem.customName = null;
        player.itemInHand = ItemBuilder(mythicItem.toItemStack())
            .customName(null)
            .unsetForceCanTrade()
            .build()
        return "§a成功取消重命名!"
    }


    @Execute(name = "drop")
    fun mythicDrop(@Context player: Player): String {
        val profile = ThePit.getInstance().profileOperator.namedIOperator(player.name).profile()
            ?: return "§c读取你的数据错误，请重进！"

        if (!PlayerUtil.isPlayerUnlockedPerk(player, "MythicDrop")) {
            return "§c你当前无法使用该指令!"
        }

        profile.isNotMythDrop = !profile.isNotMythDrop
        val stateMessage = if (profile.isNotMythDrop) "&c否" else "&a是"
        return "§7切换掉落状态为: $stateMessage"
    }
    /*
            @Execute(name = "nick")
            @Permission("pit.nick")
            fun nick(@Context player: Player,@Arg("name") name: String) {
                val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
                profile.isNicked = true
                profile.nickName = name
                player.displayName = name
                player.sendMessage(CC.translate("&a成功设置名称: ${name}"))
            }

            @Execute(name = "unnick")
            @Permission("pit.nick")
            fun unnick(@Context player: Player ) {
                val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
                player.displayName = player.name
                profile.isNicked = false
                profile.nickName = player.name
                player.sendMessage(CC.translate("&c成功取消Nick"))
            }*/


    @Execute(name = "python")

    fun thepit(): String {
        return "Python 10.0.22631.4037 (tags/v10.0.22631.4037:1414hhs, " + Calendar.getInstance().time.toGMTString() + ") [MSC v.1942 64 bit (AMD64)] on win32\n" +
                "Type \"help\", \"copyright\", \"credits\" or \"license\" for more information."
    }
}