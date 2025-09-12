package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
@Skip
@WeaponOnly
@BowOnly
public class SanctionEnchant extends AbstractEnchantment implements IAttackEntity {

    @Override
    public String getEnchantName() {
        return "制裁";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "sanction_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return (new StringBuilder()).insert(0, "&7攻击附带造成 &f").append(0.01D * ((enchantLevel + 2) * 5)).append("❤ &7的&c必中&7伤害,/s但是你攻击生命值大于其上限 &c50% &7的敌人造成的伤害 &c-").append(60 - enchantLevel * 10).append("%./s&c(必中伤害无法被抵抗或免疫)").toString();
    }


    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player player1 = (Player) target;
        attacker.setHealth(Math.max(0.1D, player1.getHealth() - 0.02D * (enchantLevel + 2) * 5.0D));
        if (attacker.getHealth() >= player1.getMaxHealth() / 2.0D)
            boostDamage.getAndAdd((60 - enchantLevel * 10) * -0.01D);
    }
}
