package net.mizukilab.pit.enchantment.type.dark_normal;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/15 20:14
 */

@AutoRegister
@ArmorOnly
public class MindAssaultEnchant extends AbstractEnchantment implements IAttackEntity, Listener {

    @Override
    public String getEnchantName() {
        return "精神攻击";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "mind_assault_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DARK_NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7你造成的伤害 &9-60% &7,同时周围11格内每有一名穿着 &6神话之甲 &7的玩家,你攻击的基础伤害 &c+5% &7(上限&c100%&7)";
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Collection<Player> nearbyPlayers = PlayerUtil.getNearbyPlayers(attacker.getLocation(), 11.0);
        nearbyPlayers.forEach(i -> {
            PlayerProfile playerProfileByUuid = PlayerProfile.getPlayerProfileByUuid(i.getUniqueId());
            if (playerProfileByUuid != null && playerProfileByUuid.isLoaded()) {
                if (playerProfileByUuid.leggings != null) {
                    boostDamage.getAndAdd(0.05);
                }
            }
        });
        boostDamage.set(Math.max(0, boostDamage.get() - 0.6));
    }
}
