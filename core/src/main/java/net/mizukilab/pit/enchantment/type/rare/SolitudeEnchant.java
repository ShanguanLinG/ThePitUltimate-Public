package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/31 18:07
 */

@ArmorOnly
public class SolitudeEnchant extends AbstractEnchantment implements IPlayerDamaged {

    @Override
    public String getEnchantName() {
        return "独处";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "solitude_enchant";
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
        return "&7在你周围7格范围内的玩家数低于或等于" + (enchantLevel >= 2 ? 2 : 1) + "时(不包括你),"
                + "/s&7你受到的玩家伤害 &9-" + (enchantLevel * 10 + 30) + "%";
    }

    @Override
    @PlayerOnly
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        int size = PlayerUtil.getNearbyPlayers(myself.getLocation(), 7).size();

        int sybilLevel = Utils.getEnchantLevel(myself.getInventory().getLeggings(), "sybil");
        if (sybilLevel > 0) {
            size += sybilLevel + 1;
        }

        if (size <= (enchantLevel >= 2 ? 3 : 2)) {
            boostDamage.getAndAdd(-0.1 * enchantLevel - 0.3);
        }
    }
}
