package net.mizukilab.pit.enchantment.type.sewer_rare;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.cooldown.Cooldown;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/1/6
 */
@ArmorOnly
public class TrashPandaEnchant extends AbstractEnchantment {

    @Override
    public String getEnchantName() {
        return "垃圾拾荒者";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "trash_panda_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.SEWER_RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7穿戴拥有此附魔的护甲时候,死亡后于&9下水道&7重生";
    }
}
