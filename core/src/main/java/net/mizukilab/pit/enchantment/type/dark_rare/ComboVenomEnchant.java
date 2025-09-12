package net.mizukilab.pit.enchantment.type.dark_rare;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.parm.type.BowOnly;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/15 20:44
 */

@ArmorOnly
@AutoRegister
@Skip
public class ComboVenomEnchant extends AbstractEnchantment implements IAttackEntity, IPlayerShootEntity, IActionDisplayEnchant {

    @Override
    public String getEnchantName() {
        return "强力击: 毒液";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "combo_venom_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DARK_RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7每 &e3 &7次攻击对目标玩家施加 &c沉默 &f(00:12) &7与 &c阴影 &f(00:12) 状态," +
                "/s&7同时, 为自身施加 &c沉默 &f(00:20) &7与 &c阴影 &f(00:20) 状态"
                + "/s&7状态 &c沉默 &7: 装备的所有神话物品失效"
                + "/s&7状态 &c阴影 &7: 攻击/受到攻击时,装备/使用的神话物品失效";
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit() % 3 == 0) {
            Player targetPlayer = (Player) target;
            attacker.setMetadata("combo_venom", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + 20 * 1000L));
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 20, 0), true);

            if (PlayerUtil.isNPC(target)) {
                return;
            }
            targetPlayer.setMetadata("combo_venom", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + 12 * 1000L));
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 12 * 20, 0), true);

        }
    }

    @Override
    @PlayerOnly
    @BowOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getBowHit() % 3 == 0) {
            Player targetPlayer = (Player) target;
            attacker.setMetadata("combo_venom", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + 20 * 1000L));
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 12 * 20, 0), true);

            if (PlayerUtil.isNPC(target)) {
                return;
            }
            targetPlayer.setMetadata("combo_venom", new FixedMetadataValue(ThePit.getInstance(), System.currentTimeMillis() + 12 * 1000L));
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 12 * 20, 0), true);

        }
    }

    @Override
    public String getText(int level, Player player) {
        int hit = (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW ? PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getBowHit() : PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getMeleeHit());
        return (hit % 3 == 0 ? "&a&l✔" : "&e&l" + (3 - hit % 3));
    }
}
