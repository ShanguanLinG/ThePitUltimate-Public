package net.mizukilab.pit.command

import cn.charlotte.pit.ThePit
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import dev.rollczi.litecommands.annotations.async.Async
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import net.mizukilab.pit.runnable.dupe.CleanupDupeEnch0525Runnable
import net.mizukilab.pit.runnable.dupe.CleanupDupeEnchTheKMGodRunnable
import net.mizukilab.pit.util.inventory.InventoryUtil
import net.mizukilab.pit.util.item.ItemUtil
import org.bukkit.Material
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil

/**
 * 2024/5/26<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */

@Command(name = "df")
@Permission("pit.admin")
class PitAdminDupeFixCommands {
    @Execute(name = "standard")
    @Async
    fun standardSearch(@Context sender: CommandSender) {
        val count = AtomicInteger(0)
        val visited = Collections.synchronizedSet(HashSet<String>())
        val searchedPlayer = Collections.synchronizedSet(HashSet<String>())

        val size = ThePit.getInstance().mongoDB.profileCollection.countDocuments()
        sender.sendMessage("发现玩家总数: $size")

        val perDetect = ceil(size / 64.0).toInt()

        val scanned = AtomicInteger(0)

        for (index in 0 until 64) {

            val builder = StringBuilder()
            builder.append("Ids: ")

            ThePit.getInstance().mongoDB.profileCollection.find()
                .skip(count.getAndAdd(perDetect))
                .limit(perDetect)
                .forEach { profile ->
                    try {
                        if (!searchedPlayer.add(profile.uuid)) {
                            return@forEach
                        }

                        var coalCounts = 0
                        var featherCounts = 0
                        var gemCounts = 0

                        var dupe = false

                        profile.inventory.contents.toList().forEachIndexed { index, itemStack ->
                            if (itemStack != null) {
                                if (itemStack.type == Material.GOLD_SWORD || itemStack.type == Material.BOW || itemStack.type == Material.LEATHER_LEGGINGS) {
                                    val uuid = ItemUtil.getUUID(itemStack) ?: return@forEachIndexed
                                    val success = visited.add(uuid)
                                    if (!success) {
                                        profile.inventory.contents[index] = null
                                        dupe = true
                                    }
                                }

                                if (itemStack.type == Material.COAL) {
                                    coalCounts += itemStack.amount
                                }
                                if (itemStack.type == Material.FEATHER) {
                                    featherCounts += itemStack.amount
                                }

                                if (itemStack.type == Material.EMERALD) {
                                    gemCounts += itemStack.amount
                                }
                            }
                        }

                        profile.inventory.armorContents.toList().forEachIndexed { index, itemStack ->
                            if (itemStack != null) {
                                if (itemStack.type == Material.GOLD_SWORD || itemStack.type == Material.BOW || itemStack.type == Material.LEATHER_LEGGINGS) {
                                    val uuid = ItemUtil.getUUID(itemStack) ?: return@forEachIndexed
                                    val success = visited.add(uuid)
                                    if (!success) {
                                        profile.inventory.armorContents[index] = null
                                        dupe = true
                                    }
                                }

                                if (itemStack.type == Material.COAL) {
                                    coalCounts += itemStack.amount
                                }
                                if (itemStack.type == Material.FEATHER) {
                                    featherCounts += itemStack.amount
                                }

                                if (itemStack.type == Material.EMERALD) {
                                    gemCounts += itemStack.amount
                                }
                            }
                        }

                        profile.enderChest.inventory.toList().forEachIndexed { index, itemStack ->
                            if (itemStack != null) {
                                if (itemStack.type == Material.GOLD_SWORD || itemStack.type == Material.BOW || itemStack.type == Material.LEATHER_LEGGINGS) {
                                    val uuid = ItemUtil.getUUID(itemStack) ?: return@forEachIndexed
                                    val success = visited.add(uuid)
                                    if (!success) {
                                        profile.enderChest.inventory.setItem(index, null)
                                        dupe = true
                                    }
                                }

                                if (itemStack.type == Material.COAL) {
                                    coalCounts += itemStack.amount
                                }
                                if (itemStack.type == Material.FEATHER) {
                                    featherCounts += itemStack.amount
                                }
                                if (itemStack.type == Material.EMERALD) {
                                    gemCounts += itemStack.amount
                                }
                            }
                        }

                        val enchantingItem = profile.enchantingItem
                        if (enchantingItem != null) {
                            val item = InventoryUtil.deserializeItemStack(profile.enchantingItem)
                            if (item != null) {
                                ItemUtil.getUUID(item)?.let { uuid ->
                                    val success = visited.add(uuid)
                                    if (!success) {
                                        profile.enchantingItem = null
                                        dupe = true
                                    }
                                }
                                if (item.type == Material.COAL) {
                                    coalCounts += item.amount
                                }
                                if (item.type == Material.FEATHER) {
                                    featherCounts += item.amount
                                }
                                if (item.type == Material.EMERALD) {
                                    gemCounts += item.amount
                                }
                            }
                        }

                        if (dupe) {
                            sender.sendMessage("已清理玩家: " + profile.playerName + " 的重复物品")
                        }

                        if (coalCounts >= 32 || featherCounts >= 32 || gemCounts >= 8) {
                            builder.append("玩家: ${profile.playerName} 羽毛: $featherCounts, 煤炭: $coalCounts, 宝石: ${gemCounts}\n")
                        }
                    } catch (e: Throwable) {
                        println("异常玩家: ${profile.playerName}")
                        e.printStackTrace()
                    }

                    profile.isLoaded = true
                    profile.save(null)
                    scanned.addAndGet(1)
                }
            sender.sendMessage("Completed (${index + 1}/64)")
            sender.sendMessage("detected: $builder")
            sender.sendMessage("scanned: ${scanned.get()}")
        }
    }

    @Execute(name = "0526")
    @Async
    fun fix0526(@Context sender: CommandSender) {
        val profileCollection = ThePit.getInstance().mongoDB.profileCollection
        profileCollection.find().forEach { profile ->
            var count = 0
            profile.inventory.apply {
                contents = contents.map { itemStack ->
                    CleanupDupeEnch0525Runnable.auto(itemStack).also {
                        if (it.first) {
                            ++count;
                        }
                    }.second
                }.toTypedArray()
                armorContents = armorContents.map { itemStack ->
                    CleanupDupeEnch0525Runnable.auto(itemStack).also {
                        if (it.first) {
                            ++count;
                        }
                    }.second
                }.toTypedArray()
            }

            for ((index, itemStack) in profile.enderChest.inventory.withIndex()) {
                profile.enderChest.inventory.setItem(index, CleanupDupeEnch0525Runnable.auto(itemStack).also {
                    if (it.first) {
                        ++count;
                    }
                }.second)
            }
            profileCollection.replaceOne(
                Filters.eq("uuid", profile.uuid),
                profile,
                ReplaceOptions().upsert(true)
            )
            if (count > 0) {
                sender.sendMessage("已处理玩家${profile.playerName} 的 $count 个物品")
            }
        }
    }

    @Execute(name = "kamiOnlinePlayer")
    @Async
    fun kamiExecute(@Context sender: CommandSender) {
        CleanupDupeEnchTheKMGodRunnable.run();
    }

    @Execute(name = "kami")
    @Async
    fun kami(@Context sender: CommandSender) {
        val profileCollection = ThePit.getInstance().mongoDB.profileCollection
        profileCollection.find().forEach { profile ->
            var count = 0
            profile.inventory.apply {
                contents = contents.map { itemStack ->
                    CleanupDupeEnchTheKMGodRunnable.auto(itemStack).also {
                        if (it.first) {
                            ++count;
                        }
                    }.second
                }.toTypedArray()
                armorContents = armorContents.map { itemStack ->
                    CleanupDupeEnchTheKMGodRunnable.auto(itemStack).also {
                        if (it.first) {
                            ++count;
                        }
                    }.second
                }.toTypedArray()
            }

            for ((index, itemStack) in profile.enderChest.inventory.withIndex()) {
                profile.enderChest.inventory.setItem(index, CleanupDupeEnch0525Runnable.auto(itemStack).also {
                    if (it.first) {
                        ++count;
                    }
                }.second)
            }
            profileCollection.replaceOne(
                Filters.eq("uuid", profile.uuid),
                profile,
                ReplaceOptions().upsert(true)
            )
            if (count > 0) {
                sender.sendMessage("已处理玩家${profile.playerName} 的 $count 个物品")
            }
        }
    }
}