package net.mizukilab.pit.impl

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.api.PitInternalHook
import cn.charlotte.pit.data.PlayerInvBackup
import cn.charlotte.pit.data.PlayerProfile
import cn.charlotte.pit.data.TradeData
import cn.charlotte.pit.events.AbstractEvent
import cn.charlotte.pit.events.genesis.GenesisTeam
import cn.charlotte.pit.events.trigger.type.IEpicEvent
import cn.charlotte.pit.events.trigger.type.INormalEvent
import cn.charlotte.pit.util.hologram.Hologram
import cn.charlotte.pit.util.hologram.packet.PacketHologram
import net.mizukilab.pit.config.NewConfiguration
import net.mizukilab.pit.enchantment.menu.MythicWellMenu
import net.mizukilab.pit.events.impl.*
import net.mizukilab.pit.events.impl.major.*
import net.mizukilab.pit.item.AbstractPitItem
import net.mizukilab.pit.item.type.AngelChestplate
import net.mizukilab.pit.item.type.ArmageddonBoots
import net.mizukilab.pit.item.type.ChunkOfVileItem
import net.mizukilab.pit.item.type.mythic.MythicBowItem
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem
import net.mizukilab.pit.item.type.mythic.MythicSwordItem
import net.mizukilab.pit.menu.admin.backpack.BackupShowMenu
import net.mizukilab.pit.menu.admin.item.AdminEnchantMenu
import net.mizukilab.pit.menu.admin.item.AdminItemMenu
import net.mizukilab.pit.menu.admin.trade.TradeTrackerMenu
import net.mizukilab.pit.menu.genesis.GenesisMenu
import net.mizukilab.pit.menu.heresy.HeresyMenu
import net.mizukilab.pit.menu.mail.MailMenu
import net.mizukilab.pit.menu.main.AuctionMenu
import net.mizukilab.pit.menu.prestige.PrestigeMainMenu
import net.mizukilab.pit.menu.quest.main.QuestMenu
import net.mizukilab.pit.menu.sewers.SewersMenu
import net.mizukilab.pit.menu.shop.ShopMenu
import net.mizukilab.pit.menu.warehouse.WarehouseMainMenu
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.item.ItemUtil
import net.mizukilab.pit.util.menu.Menu
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object PitInternalImpl : PitInternalHook {
    var loaded = false
    override fun openMythicWellMenu(player: Player?) {
        MythicWellMenu(player!!).openMenu(player)
    }

    override fun openAuctionMenu(player: Player?) {
        AuctionMenu().openMenu(player)
    }

    override fun openAngelMenu(player: Player) {
        GenesisMenu(GenesisTeam.ANGEL).openMenu(player)
    }

    override fun openDemonMenu(player: Player) {
        GenesisMenu(GenesisTeam.DEMON).openMenu(player)
    }

    override fun openTradeTrackMenu(player: Player?, profile: PlayerProfile?, data: MutableList<TradeData>?) {
        TradeTrackerMenu(profile, data).openMenu(player)
    }

    override fun openBackupShowMenu(
        player: Player?,
        profile: PlayerProfile?,
        backups: List<PlayerInvBackup>?,
        backup: PlayerInvBackup?,
        enderChest: Boolean
    ) {
        BackupShowMenu(profile, backups, backup, enderChest).openMenu(player)
    }

    override fun openBackupShowMenu(
        player: Player?,
        profile: PlayerProfile?,
        backups: MutableList<PlayerInvBackup>?,
        backup: PlayerInvBackup?,
        previousMenu: Menu?,
        enderChest: Boolean
    ) {
        BackupShowMenu(profile, backups, backup, enderChest,previousMenu).openMenu(player)
    }

    override fun openMenu(player: Player, menuName: String) {
        when (menuName) {
            "shop" -> {
                ShopMenu().openMenu(player)
            }
            "warehouse" -> {
                WarehouseMainMenu().openMenu(player)
            }
            "admin_enchant" -> {
                AdminEnchantMenu().openMenu(player)
            }

            "admin_item" -> {
                AdminItemMenu().openMenu(player)
            }

            "sewers" -> {
                SewersMenu().openMenu(player)
            }
            "heresy" -> {
                HeresyMenu().openMenu(player)
            }
        }
    }

    override fun openEvent(player: Player, eventName: String?): Boolean {
        eventName ?: return false
        val event = when (eventName.lowercase()) {
            "rspf" -> {
                RespawnFamilyEvent()
            }

            "rage" -> {
                RagePitEvent()
            }

            "rvb" -> {
                RedVSBlueEvent()
            }

            "qm" -> {
                QuickMathEvent()
            }
            "hunt" -> {
                HuntEvent()
            }

            "package" -> {
                CarePackageEvent()
            }

            "100g" -> {
                EveOneBountyEvent()
            }

            "cake" -> {
                CakeEvent()
            }

            "dragon_egg" -> {
                DragonEggsEvent()
            }

            "hamburger" -> {
                HamburgerEvent()
            }

            "spire" -> {
                SpireEvent()
            }

            "squads" -> {
                SquadsEvent()
            }

            "auction" -> {
                AuctionEvent()
            }

            "bh" -> {
                BlockHeadEvent()
            }

            "cancel" -> {
                val factory = ThePit.getInstance().eventFactory
                if (factory.activeEpicEvent != null) {
                    factory.inactiveEvent(factory.activeEpicEvent)
                }
                if (factory.activeNormalEvent != null) {
                    factory.inactiveEvent(factory.activeNormalEvent)
                }
                return true
            }

            else -> return false
        }

        return openEvent(event, player)
    }

    override fun openEvent(event: AbstractEvent, player: Player?): Boolean {

        val factory = ThePit.getInstance().eventFactory
        if (event is IEpicEvent) {
            if (player != null) {
                val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
                if (profile.playerOption.isDebugDamageMessage) {
                    factory.activeEvent(event)
                } else {
                    factory.pushEvent(event, true)
                }
            } else {
                factory.pushEvent(event, true)
            }
        } else {
            if (factory.activeNormalEvent != null) {
                factory.inactiveEvent(factory.activeNormalEvent)
            }
            if (event is INormalEvent) {
                factory.activeEvent(event)
            } else {
                return false
            }
        }
        return true;
    }

    override fun createHologram(location: Location, text: String): Hologram {
        return PacketHologram(text, location)
//        val plugin = Bukkit.getPluginManager().getPlugin("DecentHolograms")
//        return if (plugin == null) {
//            DefaultHologram(location, text)
//        } else {
//            DecentHologramImpl(location, text)
//        }
    }

    override fun getRunningKingsQuestsUuid(): UUID {
        return NewConfiguration.kingsQuestsMarker
    }

    override fun getPitSupportPermission(): String {
        return NewConfiguration.pitSupportPermission
    }

    override fun getRemoveSupportWhenNoPermission(): Boolean {
        return NewConfiguration.removeSupportWhenNoPermission
    }

    override fun reformatPitItem(itemStack: ItemStack?): ItemStack? {
        val internalName = ItemUtil.getInternalName(itemStack)
        val item = if ("mythic_sword" == internalName) {
            MythicSwordItem()
        } else if ("mythic_bow" == internalName) {
            MythicBowItem()
        } else if ("mythic_leggings" == internalName) {
            MythicLeggingsItem()
        } else if ("angel_chestplate" == internalName) {
            AngelChestplate()
        } else if ("armageddon_boots" == internalName) {
            ArmageddonBoots()
        } else {
            return itemStack
        }

        item.loadFromItemStack(itemStack)
        if (item.isEnchanted && (item.maxLive <= 0 || item.live <= 0)) {
            return null
        }


        return item.toItemStack()
    }

    override fun generateItem(item: String): ItemStack? {
        return when (item) {
            "ChunkOfVileItem" -> ChunkOfVileItem.toItemStack()
            "Leggings" -> MythicLeggingsItem().toItemStack()
            else -> null
        }
    }

    override fun getItemEnchantLevel(item: ItemStack?, enchantName: String?): Int {
        return Utils.getEnchantLevel(item, enchantName)
    }

    override fun getWatermark(): String {
        return NewConfiguration.watermarks
    }


    override fun addItemInHandEnchant(player: Player?, enchantName: String?, enchantLevel: Int): Int {
        val item = player?.inventory?.itemInHand ?: return 1

        if (item.type == Material.AIR) {
            return 0
        }

        val enchant = ThePit.getInstance().enchantmentFactor.enchantmentMap[enchantName] ?: return 1

        val mythicItem = Utils.getMythicItem(item) ?: return 2
        mythicItem.enchantments[enchant] = enchantLevel

        player.inventory.itemInHand = mythicItem.toItemStack()
        return 3
    }


    override fun getMythicItemItemStack(itemName: String?): ItemStack {
        val itemClass: Class<out AbstractPitItem> =
            ThePit.getInstance().itemFactor.itemMap[itemName] ?: return ItemStack(Material.AIR)
        val item = itemClass.getDeclaredConstructor().newInstance()
        val itemStack = item.toItemStack()
        return itemStack
    }
    override fun isLoaded(): Boolean {
        return loaded
    }
}