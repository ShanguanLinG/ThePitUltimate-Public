package net.mizukilab.pit.enchantment.type.normal;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;
@Skip
@ArmorOnly
public class EnduringWillEnchant extends AbstractEnchantment implements IPlayerDamaged {

    @Override
    public String getEnchantName() {
        return "坚韧意志";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "enduring_will_enchant";
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
        int threshold = 35 - enchantLevel * 5;
        int reduction = enchantLevel * 5 + 10;
        return "&7当生命值低于 &c" + threshold + "% &7时，受到的伤害 &9-" + reduction + "%";
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        double healthPercentage = myself.getHealth() / myself.getMaxHealth();
        double threshold = (35 - enchantLevel * 5) / 100.0;
        
        if (healthPercentage <= threshold) {
            double reduction = (enchantLevel * 0.05 + 0.10);
            boostDamage.getAndAdd(-reduction);
        }
    }
} 