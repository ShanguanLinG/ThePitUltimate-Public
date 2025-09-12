package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Starry_Killer
 * @Created_In: 2021/4/9 20:16
 */
@Skip
@BowOnly
public class BreakArmorEnchant extends AbstractEnchantment implements IPlayerShootEntity {

    @Override
    public String getEnchantName() {
        return "破甲";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "break_armor_bow_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7箭矢在造成伤害前首先造成 &c" + (enchantLevel * 0.5) + "❤ &7的必中伤害";
    }

    @Override
    @BowOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            if (targetPlayer.getHealth() > enchantLevel) {
                targetPlayer.setHealth(Math.max(0.1, targetPlayer.getHealth() - enchantLevel));
            } else {
                targetPlayer.damage(targetPlayer.getMaxHealth() * 100);
            }
        }
    }


}
