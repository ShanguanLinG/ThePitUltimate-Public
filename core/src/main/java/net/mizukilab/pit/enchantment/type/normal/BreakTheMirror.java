package net.mizukilab.pit.enchantment.type.normal;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Araykal
 * @since 2025/4/19
 */
@ArmorOnly
@Skip
@BowOnly
@WeaponOnly
public class BreakTheMirror extends AbstractEnchantment implements IAttackEntity {
    @Override
    public String getEnchantName() {
        return "破镜";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "break_the_mirror_enchant";
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
        return "&7攻击穿戴带有 &f平面镜 &7附魔 &6神话之甲 &7的玩家造成的伤害 &c+" + (enchantLevel * 6) + "%";
    }


    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (target instanceof Player) {
            ItemStack itemStack = attacker.getInventory().getLeggings();
            if (itemStack != null && new MirrorEnchant().isItemHasEnchant(itemStack)) {
                boostDamage.getAndAdd(enchantLevel * 0.06);
            }
        }
    }
}
