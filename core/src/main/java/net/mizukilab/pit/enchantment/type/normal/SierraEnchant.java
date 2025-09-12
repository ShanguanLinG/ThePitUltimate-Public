package net.mizukilab.pit.enchantment.type.normal;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/24 19:23
 */
@Skip
@WeaponOnly
@ArmorOnly
public class SierraEnchant extends AbstractEnchantment implements IPlayerKilledEntity {

    @Override
    public String getEnchantName() {
        return "钻石回收者";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "sierra_enchant";
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
        return "&7击杀的玩家每穿着一件 &b钻石装备 &7,击杀奖励 &6+" + (enchantLevel * 30) + " 硬币";
    }

    @Override
    @PlayerOnly
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        Player targetPlayer = (Player) target;
        for (ItemStack itemStack : targetPlayer.getInventory().getArmorContents()) {
            if (itemStack.getType().name().contains("DIAMOND")) {
                coins.getAndAdd(30 * enchantLevel);
            }
        }
    }

}
