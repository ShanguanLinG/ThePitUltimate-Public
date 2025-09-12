package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/1/3 18:50
 */
@Skip
@WeaponOnly
public class ComboBacktrackEnchant extends AbstractEnchantment implements IAttackEntity {

    //For safety and efficienct
    private Random random = ThreadLocalRandom.current();

    @Override
    public String getEnchantName() {
        return "强力击: 回溯";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "combo_backtrack_enchant";
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
        return "&7攻击玩家时若自身血量低于 &c4❤ &7则有 &9" + enchantLevel * 15 + "% &7的概率回溯至满血状态";
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        double attackerHealth = attacker.getHealth();

        if (attackerHealth < 8 && random.nextDouble() <= (enchantLevel * 15) * 0.01) {
            attacker.setHealth(attacker.getMaxHealth());
            attacker.sendMessage(CC.translate("&9&l回溯! &7你已回复至满血状态!"));
        }
    }

}
