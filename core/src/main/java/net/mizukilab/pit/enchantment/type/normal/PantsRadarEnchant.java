package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;


/**
 * @Author: Misoryan
 * @Created_In: 2021/2/25 14:53
 */
@Skip
@ArmorOnly
@WeaponOnly
@BowOnly
public class PantsRadarEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "神话雷达";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "pants_radar";
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
        return "&7手持或装备此物品时,战斗获得神话物品的概率提升至原来的 &d" + (100 + 30 * enchantLevel) + "%";
    }
}
