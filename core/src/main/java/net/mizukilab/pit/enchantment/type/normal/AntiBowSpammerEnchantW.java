package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/17 22:47
 */
@Skip
@WeaponOnly
public class AntiBowSpammerEnchantW extends AbstractEnchantment implements IAttackEntity, IPlayerShootEntity {

    @Override
    public String getEnchantName() {
        return "神射手反制";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "AntiBowSpammerW";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击手持弓的玩家造成的伤害 &c+" + (10 + enchantLevel * 10) + "%";
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player victim = (Player) target;
        if (victim.getItemInHand().getType().equals(Material.BOW)) {
            boostDamage.set(boostDamage.get() + (0.1 + enchantLevel * 0.1));
        }
    }

    @Override
    @PlayerOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player victim = (Player) target;
        if (victim.getItemInHand().getType().equals(Material.BOW)) {
            boostDamage.set(boostDamage.get() + (0.1 + enchantLevel * 0.1));
        }
    }
}
