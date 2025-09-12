package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class HyperOxygenEnchant extends AbstractEnchantment implements IPlayerDamaged {

    private static PotionEffect potionEffectLevel1 = new PotionEffect(PotionEffectType.REGENERATION, 80, 0);

    private static PotionEffect potionEffectLevel2 = new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 0);


    private static PotionEffect potionEffectLevel3 = new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 1);

    @Override
    public String getEnchantName() {
        return "超氧化物";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "hyper_oxygen";
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
    public String getUsefulnessLore(int i) {
        switch (i) {
            case 1:
                return "受到攻击时如自身没有 &c生命恢复 &7效果/s则为自身添加 &c生命恢复 I &f(00：04) &7效果";
            case 2:
                return "受到攻击时如自身没有 &c生命恢复 &7效果/s则为自身添加 &c生命恢复 I &f(00：08) &7效果";
            case 3:
                return "受到攻击时如自身没有 &c生命恢复 &7效果/s则为自身添加 &c生命恢复 II &f(00：08) &7效果";
            default:
                return "&c请提交至管理员寻求帮助 (INVALID LEVEL | + " + i + ")";
        }
    }

    @Override
    public void handlePlayerDamaged(int level, Player player, Entity entity, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            switch (level) {
                case 1:
                    player.addPotionEffect(potionEffectLevel1);
                    break;
                case 2:
                    player.addPotionEffect(potionEffectLevel2);
                    break;
                case 3:
                    player.addPotionEffect(potionEffectLevel3);
                    break;
            }
        }
    }
}
