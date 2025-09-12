package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.buff.impl.HemorrhageDeBuff;
import com.google.common.util.concurrent.AtomicDouble;
import io.irina.backports.utils.SWMRHashTable;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/27 16:34
 */

@WeaponOnly
@AutoRegister
public class HemorrhageEnchant extends AbstractEnchantment implements Listener, IAttackEntity, IActionDisplayEnchant {

    private static final Map<UUID, Cooldown> cooldown = new SWMRHashTable<>();
    private static final Map<UUID, Cooldown> immune = new SWMRHashTable<>();
    private static final HemorrhageDeBuff buff = new HemorrhageDeBuff();


    @Override
    public String getEnchantName() {
        return "嗜血";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Hemorrhage";
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
        return "&7攻击对玩家施加 &c流血 &f(" + TimeUtil.millisToTimer((enchantLevel >= 3 ? 5 : 4) * 1000) + ") &7与 &c缓慢 I &f(" + TimeUtil.millisToTimer((enchantLevel >= 3 ? 5 : 4) * 1000) + ") &7效果. (" + (8 - enchantLevel * 2) + "秒冷却)"
                + "/s&7效果 &c流血 &7: 无法受到与被施加 &6生命吸收 &7效果";
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        cooldown.remove(uniqueId);
        immune.remove(uniqueId);
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        cooldown.putIfAbsent(attacker.getUniqueId(), new Cooldown(0));
        if (cooldown.get(attacker.getUniqueId()).hasExpired()) {
            Player targetPlayer = (Player) target;
            if (immune.getOrDefault(targetPlayer.getUniqueId(), new Cooldown(0)).hasExpired()) {
                cooldown.put(attacker.getUniqueId(), new Cooldown(8 - enchantLevel * 2L, TimeUnit.SECONDS));
                immune.put(targetPlayer.getUniqueId(), new Cooldown(enchantLevel >= 3 ? 5 : 4, TimeUnit.SECONDS));
                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (enchantLevel >= 3 ? 5 : 4) * 20, 0), false);
                buff.stackBuff(targetPlayer, (enchantLevel >= 3 ? 5 : 4) * 20);
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }
}
