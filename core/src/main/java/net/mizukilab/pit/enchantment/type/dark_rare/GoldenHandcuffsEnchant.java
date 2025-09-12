package net.mizukilab.pit.enchantment.type.dark_rare;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/15 20:43
 */
@ArmorOnly
public class GoldenHandcuffsEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "金手铐";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "golden_handcuffs_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DISABLED;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7获得的赏金数量 &6+400% &7,额外获得的部分由当前房间内的所有玩家平分.";
    }
}
