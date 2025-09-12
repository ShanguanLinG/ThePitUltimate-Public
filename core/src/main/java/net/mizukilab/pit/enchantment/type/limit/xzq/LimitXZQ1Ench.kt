package net.mizukilab.pit.enchantment.type.limit.xzq

import cn.charlotte.pit.ThePit
import com.google.common.util.concurrent.AtomicDouble
import net.minecraft.server.v1_8_R3.PacketPlayInFlying
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.enchantment.type.limit.ILimit
import net.mizukilab.pit.parm.listener.IPlayerDamaged
import net.mizukilab.pit.parm.listener.ITickTask
import net.mizukilab.pit.util.chat.RomanUtil
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.music.NBSDecoder
import net.mizukilab.pit.util.music.PositionSongPlayer
import net.mizukilab.pit.util.music.Song
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import spg.lgdev.handler.MovementHandler
import spg.lgdev.iSpigot
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@ArmorOnly
class LimitXZQ1Ench : AbstractEnchantment(), ITickTask, MovementHandler, IPlayerDamaged, ILimit {
    private val playerMap: MutableMap<UUID, PositionSongPlayer> = HashMap()
    private val song: Song =
        NBSDecoder.parse(ThePit.getInstance().javaClass.classLoader.getResourceAsStream("tianwailaiwu.nbs"))

    init {
        object : BukkitRunnable() {
            override fun run() {
                val entries: Set<Map.Entry<UUID, PositionSongPlayer>> = HashSet(playerMap.entries)
                for ((key) in entries) {
                    val player = Bukkit.getPlayer(key)
                    if (player == null || !player.isOnline) {
                        val remove: PositionSongPlayer? = playerMap.remove(key)
                        remove?.isPlaying = false
                        continue
                    }
                    if (player.inventory.leggings == null || getItemEnchantLevel(player.inventory.leggings) == -1) {
                        val remove: PositionSongPlayer? = playerMap.remove(key)
                        remove?.isPlaying = false
                    }
                }
            }
        }.runTaskTimer(ThePit.getInstance(), 20, 20)

        try {
            iSpigot.INSTANCE.addMovementHandler(this)
        } catch (ignore: Exception) {
        }
    }

    override fun getEnchantName() = "§b天外来物"

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String = "xzq_dj_1"

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.OP
    }

    override fun getCooldown(): Cooldown? {
        return null
    }


    override fun handle(enchantLevel: Int, target: Player) {
        val songPlayer: PositionSongPlayer? = playerMap.get(target.uniqueId)
        if (songPlayer == null) {
            val player = PositionSongPlayer(song)
            player.targetLocation = target.location
            player.autoDestroy = false
            player.isLoop = true
            player.isPlaying = true
            player.volume = 0.08.toInt().toByte()
            playerMap[target.uniqueId] = player
        } else {
            target.world.playEffect(target.location.clone().add(0.0, 3.0, 0.0), Effect.NOTE, 1)
        }
    }

    override fun loopTick(enchantLevel: Int): Int {
        return 10
    }

    override fun handleUpdateLocation(
        player: Player, location: Location?, location1: Location?, packetPlayInFlying: PacketPlayInFlying?
    ) {
        val songPlayer: PositionSongPlayer? = playerMap.get(player.uniqueId)
        if (songPlayer != null) {
            songPlayer.targetLocation = player.player.location
        }
    }

    override fun handleUpdateRotation(var1: Player?, var2: Location?, var3: Location?, var4: PacketPlayInFlying?) {
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7向周围的玩家播放音乐: &b天外来物" + "/s&7当在空中受到攻击时, 有 &b${randomNum(enchantLevel)}% &7的概率 清除对方与&c自身&7的虚弱效果" +
                "/s&7同时给予自身 &b速度 ${RomanUtil.convert(speedLevel(enchantLevel) + 1)} &f(${
                    speedDurStr(
                        enchantLevel
                    )
                })&7, 对方 &c缓慢 I &f(00:02)"
    }

    fun randomNum(enchantLevel: Int): Int {
        return when (enchantLevel) {
            1 -> 40
            2 -> 60
            else -> 80
        }
    }

    fun speedLevel(enchantLevel: Int): Int {
        return when (enchantLevel) {
            1 -> 0
            2 -> 1
            else -> 2
        }
    }

    fun speedDuration(enchantLevel: Int): Int {
        return when (enchantLevel) {
            1 -> 4 * 20
            2 -> 6 * 20
            3 -> 8 * 20
            else -> enchantLevel * 20
        }
    }

    fun speedDurStr(enchantLevel: Int): String {
        return when (enchantLevel) {
            1 -> "00:04"
            2 -> "00:06"
            3 -> "00:08"
            else -> "${enchantLevel}s"
        }
    }

    fun random(enchantLevel: Int): Boolean {
        return when (enchantLevel) {
            1 -> Math.random() < 0.3
            2 -> Math.random() < 0.4
            3 -> Math.random() < 0.5
            else -> Math.random() < enchantLevel * 0.5
        }
    }

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
        attacker as Player
        if (!myself.isOnGround && random(enchantLevel)) {
            if (myself.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                myself.removePotionEffect(PotionEffectType.WEAKNESS)
            }
            if (attacker.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                attacker.removePotionEffect(PotionEffectType.WEAKNESS)
            }

            myself.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SPEED,
                    speedDuration(enchantLevel),
                    speedLevel(enchantLevel)
                ), true
            )
            attacker.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2 * 20, 0))
        }
    }

}