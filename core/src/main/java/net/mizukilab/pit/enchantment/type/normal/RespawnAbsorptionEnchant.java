package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerRespawn;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/24 18:58
 */
@Skip
@ArmorOnly
public class RespawnAbsorptionEnchant extends AbstractEnchantment implements IPlayerRespawn {

    @Override
    public String getEnchantName() {
        return "复生: 生命吸收";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "respawn_absorption";
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
        return "&7每次死亡后复活立刻获得 &6" + (enchantLevel * 5) + "❤ 伤害吸收";
    }

    @Override
    public void handleRespawn(int enchantLevel, Player myself) {
        ((CraftPlayer) myself).getHandle().setAbsorptionHearts(enchantLevel * 10);
    }
}
