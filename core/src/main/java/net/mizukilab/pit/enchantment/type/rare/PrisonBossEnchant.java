package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/15 20:59
 */

@WeaponOnly
public class PrisonBossEnchant extends AbstractEnchantment implements IAttackEntity {

    @Override
    public String getEnchantName() {
        return "牢大";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 4;
    }

    @Override
    public String getNbtName() {
        return "prison_boss";
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
        String enchJump = switch (enchantLevel) {
            case 1:
                yield "0.5";
            case 2:
                yield "0.4";
            case 3:
                yield "0.3";
            case 4:
                yield "0.2";
            default:
                yield "0.6";
        };
        return "&7使用时允许你不跳跃也能造成暴击伤害, 但是当被攻击后 1 秒后该附魔不生效, 当在地上时会自动跳跃 " + enchJump + " 格" + ", 当不在则快速下若同格";
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (attacker.getNoDamageTicks() <= 1) {
            if (attacker.isOnGround()) {
                boostDamage.addAndGet(0.5);
                if (attacker.getVelocity().getY() <= 0.1) {
                    attacker.setVelocity(new Vector(0, (0.6 - ((double) enchantLevel) / 10), 0));
                }
            } else {
                attacker.setVelocity(new Vector(0, (-0.6 + ((double) enchantLevel) / 10), 0));
            }
        }
    }
}
