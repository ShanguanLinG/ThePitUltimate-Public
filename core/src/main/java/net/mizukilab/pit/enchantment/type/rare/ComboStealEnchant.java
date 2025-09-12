package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
@Skip
@AutoRegister
public class ComboStealEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant {

    private final DecimalFormat iiiIiI = new DecimalFormat("0.0");

    @Override
    public String getEnchantName() {
        return "强力击: 窃取";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "combosteal_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return (new StringBuilder()).insert(0, "&7每 &e5 &7次击中使目标受到 &c").append(this.iiiIiI.format(0.5D * (enchantLevel + 1))).append("❤ &7的&c必中&7伤害,/s同时恢复自身 &c").append(this.iiiIiI.format(0.5D * (enchantLevel + 1))).append("❤ &7生命值./s&c(必中伤害无法被免疫与抵抗)").toString();

    }

    @Override
    public String getText(int level, Player player) {
        level = (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW) ? PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getBowHit() : PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getMeleeHit();
        return (level % 5 == 0) ? "&a&l✔" : (new StringBuilder()).insert(0, "&e&l").append(5 - level % 5).toString();

    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit() % 5 == 0) {
            ((Player) target).setHealth(Math.max(0.1D, ((Player) target).getHealth() - (2 * (enchantLevel + 1))));
            PlayerUtil.heal(attacker, (enchantLevel + 1));
        }
    }
}
