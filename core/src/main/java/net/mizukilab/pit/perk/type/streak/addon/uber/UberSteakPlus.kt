package net.mizukilab.pit.perk.type.prestige

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.PlayerProfile
import cn.charlotte.pit.event.PitStreakKillChangeEvent
import cn.charlotte.pit.event.PotionAddEvent
import cn.charlotte.pit.perk.AbstractPerk
import cn.charlotte.pit.perk.MegaStreak
import cn.charlotte.pit.perk.PerkType
import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.getPitProfile
import net.mizukilab.pit.item.type.UberDrop
import net.mizukilab.pit.item.type.UberPlusDrop
import net.mizukilab.pit.parm.listener.IAttackEntity
import net.mizukilab.pit.parm.listener.IPlayerDamaged
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity
import net.mizukilab.pit.parm.listener.ITickTask
import net.mizukilab.pit.sendMessage
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.chat.MessageType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor

/**
 * @author Araykal
 * @since 2025/5/4
 */
class UberSteakPlus : AbstractPerk(), MegaStreak, Listener, IPlayerKilledEntity, IAttackEntity, ITickTask,
    IPlayerDamaged {
    override fun getInternalPerkName(): String {
        return "uber_steak_plus"
    }

    override fun getDisplayName(): String {
        return "&d&l超级登封造极"
    }

    override fun getIcon(): Material {
        return Material.DIAMOND_SWORD
    }

    override fun requireCoins(): Double {
        return 200000.0
    }

    override fun requireRenown(level: Int): Double {
        return 160.0
    }

    override fun requirePrestige(): Int {
        return 30
    }

    override fun requireLevel(): Int {
        return 110
    }

    override fun getDescription(player: Player): MutableList<String> {
        return mutableListOf(
            "&7激活要求连杀数: &c1000 连杀",
            "",
            "&7当激活时: ",
            "  &d◼ +200% &7获得神话物品的概率",
            "  &a◼ &7击杀时额外获得+ &6175% &7金币",
            "  &a◼ &7击杀时额外获得+ &6375% &7经验",
            "",
            "&7但是: ",
            "  &c◼ &7每1000击杀将额外承受&c +20%&7 的伤害",
            "&7       (从1000连杀开始)",
            "  &c◼ &7赏金将&c无上限增长",
            "  &c◼ &7自身获取的&a药水效果&7无法超越 &cII &7级",
            "",
            "&7连杀期间: ",
            " &6◼ &71000 杀: &7对被悬赏的玩家造成的伤害 &c-60%",
            " &6◼ &72000 杀: &7最大生命值减少 &c4❤",
            " &6◼ &73000 杀: &7药水持续时间 &c-80%",
            " &6◼ &74000 杀: &7生命值恢复 &c-50%",
            " &6◼ &75000 杀: &7每秒减少 &c1❤ &7生命值",
            " &6◼ &76000 杀: &c谢幕",
            "",
            "&7当死亡时: ",
            "  &e◼ &7当死亡时击杀到达 &e6000 &7获得将 &c超级登峰造极掉落物",
        )
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onRegen(event: EntityRegainHealthEvent) {
        val player = event.entity
        if (player is Player) {
            val profile = PlayerProfile.getPlayerProfileByUuid(player.uniqueId)
            if (!profile.isLoaded) {
                return
            }

            if (!hasUberPlus(player)) {
                return
            }

            if (profile.streakKills < 4000) {
                return
            }
            event.amount /= 2
        }
    }

    override fun getMaxLevel(): Int {
        return 0
    }

    private fun hasUberPlus(player: Player) =
        PlayerUtil.isPlayerChosePerk(player, "uber_steak_plus")

    override fun getPerkType(): PerkType {
        return PerkType.MEGA_STREAK
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onStreak(event: PitStreakKillChangeEvent) {
        val player = Bukkit.getPlayer(event.playerProfile.playerUuid) ?: return
        if (!hasUberPlus(player)) return
        if (event.from < 1000 && event.to >= 1000) {
            CC.boardCast(
                MessageType.COMBAT,
                "&c&l超级连杀! ${event.playerProfile.formattedNameWithRoman} &7激活了 ${this.displayName} &7!"
            )
            Bukkit.getOnlinePlayers().forEach { it.playSound(it.location, Sound.WITHER_SPAWN, 0.8f, 1.5f) }
        }
    }

    override fun onPerkActive(player: Player?) {

    }

    override fun onPerkInactive(player: Player?) {

    }

    override fun getStreakNeed(): Int {
        return 1000
    }

    override fun handlePlayerKilled(
        enchantLevel: Int,
        myself: Player,
        target: Entity,
        coins: AtomicDouble,
        experience: AtomicDouble
    ) {
        if (!hasUberPlus(myself)) return
        val profile = ThePit.getInstance().profileOperator.namedIOperator(myself.name).profile()
        if (profile.streakKills >= 1000) {
            coins.addAndGet(1.75)
            experience.addAndGet(1.75)
        }
        if (profile.streakKills >= 2000) {
            myself.maxHealth -= 8
        }

        if (profile.streakKills >= 4000) {
            PlayerUtil.addPotionEffect(myself, PotionEffect(PotionEffectType.CONFUSION, 100000, 0, false))
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPotionAdd(event: PotionAddEvent) {
        val player = event.player

        if (player !is Player) return
        if (!hasUberPlus(player)) return
        val profile = player.getPitProfile()
        if (profile.streakKills < 3000) return

        val effect = event.effect
        val ambient = effect.isAmbient

        if (!ambient) {
            return
        }

        if (effect.amplifier > 1) {
            event.isCancelled = true
            return
        }

        event.isCancelled = true


        val type = effect.type
        val duration = effect.duration / 3
        val amplifier = effect.amplifier


        player.addPotionEffect(PotionEffect(type, duration, amplifier, false))
    }

    @PlayerOnly
    override fun handleAttackEntity(
        enchantLevel: Int,
        attacker: Player,
        target: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble?,
        cancel: AtomicBoolean?
    ) {
        if (!hasUberPlus(attacker)) return
        val profile = ThePit.getInstance().profileOperator.namedIOperator(attacker.name).profile()
        if (profile.streakKills >= 1000) {
            val targetProfile = ThePit.getInstance().profileOperator.namedIOperator(target.name).profile()
            if (targetProfile.bounty >= 1) {
                finalDamage.set(finalDamage.get() - 0.6)
            }
        }


    }

    override fun handle(enchantLevel: Int, player: Player) {
        if (!hasUberPlus(player)) return
        val profile = ThePit.getInstance().profileOperator.namedIOperator(player.name).profile()
        if (profile.streakKills >= 5000) {
            PlayerUtil.damage(player, PlayerUtil.DamageType.TRUE, 2.0, false)
        }
        if (profile.streakKills >= 6000) {
            val itemStack = UberPlusDrop()
            PlayerUtil.damage(player, PlayerUtil.DamageType.TRUE, player.maxHealth * 10, false)
            player.sendMessage(CC.translate("&c谢幕!!!"))
            player.inventory.addItem(itemStack.toItemStack())
        }

    }

    override fun loopTick(enchantLevel: Int): Int {
        return 20
    }

    override fun handlePlayerDamaged(
        enchantLevel: Int,
        myself: Player,
        attacker: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean
    ) {
        val profile = ThePit.getInstance().profileOperator.namedIOperator(myself.name).profile() ?: return
        val streakKills = profile.streakKills

        if (streakKills >= 1000) {
            val extraBoost = floor((streakKills - 1000) / 1000.0)
            finalDamage.addAndGet(extraBoost * 0.2)
        }

    }
}