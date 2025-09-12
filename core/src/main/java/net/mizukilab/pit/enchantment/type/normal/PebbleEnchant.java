package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;


/**
 * @Author: Misoryan
 * @Created_In: 2021/2/25 14:48
 */
@Skip
@ArmorOnly
public class PebbleEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "鹅卵石";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "pebble_enchant";
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
        return "&7拾起金锭时获得 &6" + (enchantLevel * 10) + " 硬币" + (enchantLevel >= 3 ?
                "/s&7并恢复自身 &c1❤ &7生命值" : "");
    }
}
