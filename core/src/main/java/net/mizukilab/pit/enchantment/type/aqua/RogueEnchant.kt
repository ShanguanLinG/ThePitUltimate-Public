package net.mizukilab.pit.enchantment.type.aqua

import com.google.common.util.concurrent.AtomicDouble
import net.mizukilab.pit.enchantment.AbstractEnchantment
import net.mizukilab.pit.enchantment.param.event.PlayerOnly
import net.mizukilab.pit.enchantment.param.item.ArmorOnly
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity
import net.mizukilab.pit.item.IMythicItem
import net.mizukilab.pit.item.MythicColor
import net.mizukilab.pit.parm.listener.IAttackEntity
import net.mizukilab.pit.parm.listener.IPlayerDamaged
import net.mizukilab.pit.util.cooldown.Cooldown
import net.mizukilab.pit.util.toMythicItem
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicBoolean


@ArmorOnly
class RogueEnchant : AbstractEnchantment(), IAttackEntity, IPlayerDamaged {
    override fun getEnchantName(): String {
        return "无赖"
    }

    override fun getMaxEnchantLevel(): Int {
        return 1
    }

    override fun getNbtName(): String {
        return "rogue"
    }

    override fun getRarity(): EnchantmentRarity {
        return EnchantmentRarity.FISH_NORMAL
    }

    override fun getCooldown(): Cooldown? {
        return null
    }

    override fun getUsefulnessLore(enchantLevel: Int): String {
        return "&7攻击穿着青色神话之甲的玩家时造成的伤害 &c+25% &7且自身受到其的伤害 &9-10%"
    }

    @PlayerOnly
    override fun handleAttackEntity(
        enchantLevel: Int,
        attacker: Player?,
        target: Entity,
        damage: Double,
        finalDamage: AtomicDouble?,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean?
    ) {
        if (target is Player) {
            var toMythicItem = target.inventory.leggings?.toMythicItem() as IMythicItem
            if (toMythicItem.color == MythicColor.AQUA) {
                boostDamage.addAndGet(0.25)
            }
        }
    }

    @PlayerOnly
    override fun handlePlayerDamaged(
        enchantLevel: Int,
        myself: Player,
        attacker: Entity?,
        damage: Double,
        finalDamage: AtomicDouble?,
        boostDamage: AtomicDouble,
        cancel: AtomicBoolean?
    ) {
        if (attacker is Player) {
            val toMythicItem = attacker.inventory.leggings?.toMythicItem() as IMythicItem
            if (toMythicItem.color == MythicColor.AQUA) {
                boostDamage.set(boostDamage.get() - 0.1)
            }
        }
    }
}