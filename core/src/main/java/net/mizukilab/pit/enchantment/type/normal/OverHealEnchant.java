package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;


/**
 * @Author: Misoryan
 * @Created_In: 2021/2/25 21:44
 */
@Skip
@ArmorOnly
public class OverHealEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "过度医疗";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "over_heal_enchant";
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
        return "&7持有的治疗物品数量上限 &e+" + enchantLevel;
    }
}
