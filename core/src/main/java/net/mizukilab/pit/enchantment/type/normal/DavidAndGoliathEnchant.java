package net.mizukilab.pit.enchantment.type.normal;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/26 12:15
 */

@ArmorOnly
@Skip
public class DavidAndGoliathEnchant extends AbstractEnchantment implements IPlayerDamaged {

    @Override
    public String getEnchantName() {
        return "以弱战强";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "david_and_goliath_enchant";
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
        return "&7受到来自持有赏金的玩家攻击时受到的伤害 &9-" + (10 + enchantLevel * 10) + "%";
    }

    @Override
    @PlayerOnly
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player attackerPlayer = (Player) attacker;
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(attackerPlayer.getUniqueId());
        if (profile.getBounty() > 0) {
            boostDamage.getAndAdd(-0.1 - 0.1 * enchantLevel);
        }
    }
}
