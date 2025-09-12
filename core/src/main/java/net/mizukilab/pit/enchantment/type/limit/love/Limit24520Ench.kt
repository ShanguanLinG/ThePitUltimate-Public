package net.mizukilab.pit.enchantment.type.limit.love

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.PlayerProfile
import com.google.common.util.concurrent.AtomicDouble
import net.minecraft.server.v1_8_R3.PacketPlayInFlying
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.enchantment.type.limit.ILimit
import net.mizukilab.pit.parm.listener.IAttackEntity
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity
import net.mizukilab.pit.parm.listener.ITickTask
import net.mizukilab.pit.util.PlayerUtil
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.music.NBSDecoder
import net.mizukilab.pit.util.music.PositionSongPlayer
import net.mizukilab.pit.util.music.Song
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import spg.lgdev.handler.MovementHandler
import spg.lgdev.iSpigot
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 2024/5/19<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */
@ArmorOnly

class Limit24520Ench : AbstractEnchantment(), ITickTask, MovementHandler, IPlayerKilledEntity, IAttackEntity, ILimit {
    private val playerMap: MutableMap<UUID, PositionSongPlayer> = HashMap()
    private val song: Song =
        NBSDecoder.parse(ThePit.getInstance().javaClass.classLoader.getResourceAsStream("fenshoukuaile.nbs"))

    init {
        instance = this
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

    override fun getEnchantName() = "§d520 §9| §f2024"

    override fun getMaxEnchantLevel(): Int {
        return 3
    }

    override fun getNbtName(): String = "24_5_20"

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
        player: Player,
        location: Location?,
        location1: Location?,
        packetPlayInFlying: PacketPlayInFlying?
    ) {
        val songPlayer: PositionSongPlayer? = playerMap.get(player.uniqueId)
        if (songPlayer != null) {
            songPlayer.targetLocation = player.player.location
        }
    }

    override fun handleUpdateRotation(var1: Player?, var2: Location?, var3: Location?, var4: PacketPlayInFlying?) {
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7向周围的玩家播放音乐: &d我爱你" +
                "/s&7击杀额外 &6+13.14 硬币" +
                "/s&7每 &e5 &7次击中恢复 &c2.0❤"
    }

    @PlayerOnly
    override fun handlePlayerKilled(
        enchantLevel: Int,
        myself: Player,
        target: Entity,
        coins: AtomicDouble,
        experience: AtomicDouble
    ) {
        experience.getAndAdd(5.20)
        coins.getAndAdd(13.14)
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
        val profile = PlayerProfile.getPlayerProfileByUuid(attacker.uniqueId)
        if (profile.meleeHit % 5 == 0) {
            PlayerUtil.heal(attacker, 4.0)
        }
    }

    companion object {
        lateinit var instance: Limit24520Ench
    }
}