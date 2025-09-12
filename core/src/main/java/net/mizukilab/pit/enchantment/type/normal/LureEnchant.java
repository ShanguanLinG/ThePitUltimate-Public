package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.RodOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;


/**
 * @Author: EmptyIrony
 * @Date: 2021/2/7 1:16
 */
@Skip
@RodOnly
public class LureEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "饵钓";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "lure";
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
        return "&7使你钓鱼速度提升 &c" + enchantLevel * 50 + "%";
    }
}
