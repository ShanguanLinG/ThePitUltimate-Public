package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/8 13:58
 */
@Skip
@WeaponOnly
public class CounterJanitorEnchant extends AbstractEnchantment implements IPlayerKilledEntity {

    @Override
    public String getEnchantName() {
        return "反侦察";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "counter_janitor_enchant";
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
        return "&7击杀获得 &3抗性提升 I &f(00:0" + (enchantLevel + (enchantLevel >= 3 ? 2 : 1) + ")");
    }

    @Override
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        myself.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        myself.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * (enchantLevel + (enchantLevel >= 3 ? 2 : 1)), 0), true);
    }
}
