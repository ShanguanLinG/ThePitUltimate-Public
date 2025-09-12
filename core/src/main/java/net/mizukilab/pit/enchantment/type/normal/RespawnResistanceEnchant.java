package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerRespawn;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import nya.Skip;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/24 18:53
 */
@Skip
@ArmorOnly
public class RespawnResistanceEnchant extends AbstractEnchantment implements IPlayerRespawn {

    @Override
    public String getEnchantName() {
        return "复生: 抗性";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "respawn_resistance";
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
        return "&7每次死亡后复活立刻获得 &3抗性提升 " + (enchantLevel >= 3 ? "II" : "I") + " &f(" + TimeUtil.millisToTimer((enchantLevel + 1) * 10 * 1000L) + ")";
    }

    @Override
    public void handleRespawn(int enchantLevel, Player myself) {
        myself.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (enchantLevel + 1) * 10 * 20, (enchantLevel >= 3 ? 1 : 0)), true);
    }
}
