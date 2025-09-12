package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerBeKilledByEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@ArmorOnly
public class VengeanceSpiritEnchant extends AbstractEnchantment implements IPlayerBeKilledByEntity {

    @Override
    public String getEnchantName() {
        return "复仇之魂";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "vengeance_spirit_enchant";
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
        double damage = enchantLevel * 1.5 + 2.5; // 4, 5.5, 7 hearts
        int radius = enchantLevel + 3; // 4, 5, 6 blocks
        return "&7死亡时对 &c" + radius + " &7格内的敌人造成 &c" + damage + " &7颗心的爆炸伤害";
    }

    @Override
    public void handlePlayerBeKilledByEntity(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        Location deathLocation = myself.getLocation();
        double damage = (enchantLevel * 1.5 + 2.5) * 2;
        int radius = enchantLevel + 3;

        deathLocation.getWorld().playSound(deathLocation, Sound.EXPLODE, 2.0f, 0.8f);

        for (Player nearbyPlayer : PlayerUtil.getNearbyPlayers(myself.getLocation(), radius)) {
            if (nearbyPlayer.equals(myself)) continue;

            nearbyPlayer.damage(damage);
            Vector knockback = nearbyPlayer.getLocation().toVector().subtract(deathLocation.toVector()).normalize();
            knockback.multiply(0.8).setY(0.3);
            nearbyPlayer.setVelocity(knockback);
            nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.HURT_FLESH, 1.0f, 0.8f);
        }
    }
} 