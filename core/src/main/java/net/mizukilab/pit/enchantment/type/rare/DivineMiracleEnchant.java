package net.mizukilab.pit.enchantment.type.rare;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;


/**
 * @Author: Misoryan
 * @Created_In: 2021/2/25 22:05
 */

@ArmorOnly
public class DivineMiracleEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "奇迹力场";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "divine_miracle_enchant";
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
        return "&7死亡时有 &d+" + (enchantLevel * 15) + "% &7的几率不损失背包内神话物品生命";
    }
}
