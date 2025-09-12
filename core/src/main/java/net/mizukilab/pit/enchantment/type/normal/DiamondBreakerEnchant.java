package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.server.v1_8_R3.ItemArmor;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/17 18:26
 */
@Skip
@WeaponOnly
@BowOnly
@ArmorOnly
public class DiamondBreakerEnchant extends AbstractEnchantment implements IAttackEntity, IPlayerShootEntity {

    @Override
    public String getEnchantName() {
        return "钻石破坏者";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "diamond_breaker";
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
        return "&7攻击穿着 &b钻石装备 &7的玩家造成的伤害 &c+" + (4 * enchantLevel) + "%";
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player victim = (Player) target;
        for (ItemStack armor : victim.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR && armor.getType().name().contains("DIAMOND")) {
                if (Utils.toNMStackQuick(armor).getItem() instanceof ItemArmor) {
                    boostDamage.set(boostDamage.get() + (0.04 * enchantLevel));
                }
            }
        }
    }

    @Override
    @PlayerOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player victim = (Player) target;
        for (ItemStack armor : victim.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR && armor.getType().name().contains("DIAMOND")) {
                if (Utils.toNMStackQuick(armor).getItem() instanceof ItemArmor) {
                    boostDamage.set(boostDamage.get() + (0.04 * enchantLevel));
                }
            }
        }
    }
}
