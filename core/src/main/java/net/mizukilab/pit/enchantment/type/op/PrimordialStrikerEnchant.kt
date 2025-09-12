package net.mizukilab.pit.enchantment.type.op

import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.IActionDisplayEnchant
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.enchantment.param.item.WeaponOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.parm.listener.IAttackEntity
import net.mizukilab.pit.parm.listener.IPlayerDamaged
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity
import net.mizukilab.pit.parm.listener.ITickTask
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.chat.CC
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.time.TimeUtil
import net.mizukilab.pit.util.toMythicItem
import nya.Skip
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Araykal
 * @since 2025/5/17
 */
@Skip
@WeaponOnly
class PrimordialStrikerEnchant : AbstractEnchantment(), IPlayerDamaged, IPlayerKilledEntity,Listener, IAttackEntity, ITickTask,
    IActionDisplayEnchant {

    private val shields = mutableMapOf<UUID, Int>()
    private val cooldowns = mutableMapOf<UUID, Cooldown>()
    @EventHandler
    fun onQuit(pq: PlayerQuitEvent){
        val key = pq.player.uniqueId
        shields.remove(key);
        cooldowns.remove(key);
    }
    override fun getEnchantName() = "原初圣伊"

    override fun getMaxEnchantLevel() = 1

    override fun getNbtName() = "primordial_striker"

    override fun getRarity() = EnchantmentRarity.OP

    override fun getCooldown(): Cooldown? = null

    override fun getUsefulnessLore(enchantLevel: Int) =
        listOf(
            "&7攻击敌人时额外造成 &c233% &7真实伤害 与 &c233% &7普通伤害",
            "&7自身对真实伤害与普通伤害减伤 &c300%",
            "每击杀一名玩家 将对周围 &e10 &7格所有玩家造成 &d天雷之罚 &7(5秒冷却)",
            "&7如周围玩家内有穿戴 &c危险集群 &7则直接对其致死",
            "每秒恢复 &c20❤ &7并给自身施加 &e圣盾 &7(无限叠加)",
            "",
            "属性 &d天雷之罚&7: 对受到此属性攻击的玩家造成 &c15❤ &7普通伤害并引导闪电劈下",
            "属性 &e圣盾&7: 免疫一切玩家攻击造成的伤害,并给予自身额外 &61❤ 生命吸收"
        ).joinToString("/s")

    @PlayerOnly
    override fun handlePlayerDamaged(
        enchantLevel: Int,
        myself: Player,
        attacker: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean
    ) {
        val player = attacker as Player
        shields[myself.uniqueId]?.takeIf { it > 0 }?.let {
            cancel.set(true)
            finalDamage.set(0.0)
            boostDamage.set(.0)
            shields[myself.uniqueId] = it - 1
            val shieldLeft = shields[myself.uniqueId] ?: 0

            myself.sendMessage(CC.translate("&e&l原初圣伊! &b抵消一层攻击!"))
            myself.playSound(myself.location, Sound.DRINK, 1.0f, 1.0f)
            player.playSound(myself.location, Sound.DRINK, 1.0f, 1.0f)
            player.sendMessage(CC.translate("&e&l原初圣伊! &b你的攻击被抵消,对方剩余圣盾数: $shieldLeft"))
        }
        finalDamage.set(finalDamage.get() - 1.33)
        boostDamage.set(boostDamage.get() - 1.33)
    }

    @PlayerOnly
    override fun handlePlayerKilled(
        enchantLevel: Int,
        myself: Player,
        target: Entity,
        coins: AtomicDouble,
        experience: AtomicDouble
    ) {
        if (cooldowns.getOrDefault(myself.uniqueId, Cooldown(0L)).hasExpired()) {
            PlayerUtil.getNearbyPlayers(myself.location, 10.0).filterNot { it.uniqueId == myself.uniqueId }
                .forEach { player ->
                    player.inventory.leggings?.takeIf { it.type != Material.AIR }?.toMythicItem()?.let { item ->
                        if (item.getEnchantmentLevel("emergency_cluster") >= 1) {
                            PlayerUtil.playThunderEffect(player.location)
                            PlayerUtil.deadPlayer(player)
                        }
                    }

                    PlayerUtil.playThunderEffect(player.location)
                    player.damage(30.0, myself)
                }
            cooldowns[myself.uniqueId] = Cooldown(5L, TimeUnit.SECONDS)
        }
    }

    @PlayerOnly
    override fun handleAttackEntity(
        enchantLevel: Int,
        attacker: Player,
        target: Entity,
        damage: Double,
        finalDamage: AtomicDouble,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean
    ) {
        finalDamage.addAndGet(2.33)
        boostDamage.addAndGet(2.33)
        PlayerUtil.playThunderEffect(target.location)
    }

    override fun handle(enchantLevel: Int, player: Player?) {
        player?.let {
            shields[it.uniqueId] = shields.getOrDefault(it.uniqueId, 0) + 1
            PlayerUtil.addAbsorptionHearts(player, 2.0f)
            PlayerUtil.heal(it, player.maxHealth)
        }
    }

    override fun loopTick(enchantLevel: Int) = 20

    override fun getText(level: Int, player: Player): String {
        val shieldCount = shields.getOrDefault(player.uniqueId, 0)
        val cooldownStatus = cooldowns.getOrDefault(player.uniqueId, Cooldown(0L)).let {
            if (it.hasExpired()) "&a&l✔" else "&c&l" + TimeUtil.millisToRoundedTime(it.remaining)
        }
        return "&e圣盾 &f| &a$shieldCount&7/&c* &d天雷 &f| $cooldownStatus"
    }
}
