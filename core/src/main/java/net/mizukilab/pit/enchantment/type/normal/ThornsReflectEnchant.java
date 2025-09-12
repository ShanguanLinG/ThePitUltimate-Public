package net.mizukilab.pit.enchantment.type.normal;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.random.RandomUtil;
import nya.Skip;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;
@Skip
@ArmorOnly
public class ThornsReflectEnchant extends AbstractEnchantment implements IPlayerDamaged {

    @Override
    public String getEnchantName() {
        return "荆棘反射";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "thorns_reflect_enchant";
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
        int chance = enchantLevel * 10 + 10; // 20%, 30%, 40%
        double reflectPercent = enchantLevel * 5 + 10; // 15%, 20%, 25%
        return "&7受到攻击时有 &e" + chance + "% &7的概率反射 &c" + reflectPercent + "% &7的伤害给攻击者";
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (!(attacker instanceof Player)) return;
        
        int chance = enchantLevel * 10 + 10;
        if (RandomUtil.hasSuccessfullyByChance(chance / 100.0)) {
            double reflectPercent = (enchantLevel * 0.05 + 0.10); // 0.15, 0.20, 0.25
            double reflectDamage = damage * reflectPercent;
            
            Player attackerPlayer = (Player) attacker;
            attackerPlayer.damage(reflectDamage);
            myself.playSound(myself.getLocation(), Sound.ANVIL_LAND, 0.5f, 1.5f);
            attackerPlayer.playSound(attackerPlayer.getLocation(), Sound.HURT_FLESH, 0.8f, 1.2f);
        }
    }
} 