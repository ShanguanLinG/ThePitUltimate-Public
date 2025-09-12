package net.mizukilab.pit.enchantment.type.normal;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/8 20:01
 */

@WeaponOnly
@Skip
public class CrushEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, Listener {

    private final Map<UUID, Cooldown> COOLDOWN = new HashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        COOLDOWN.remove(event.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "致残";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "crush_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return new Cooldown(1500, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7攻击玩家时施加 &c虚弱 " + RomanUtil.convert(enchantLevel + 4) + " &7(持续" + new DecimalFormat("0.0").format(0.1 + 0.2 * enchantLevel) + "秒)"
                + "/s&7此附魔每 &f1.5 &7秒只能触发一次.";
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        COOLDOWN.putIfAbsent(attacker.getUniqueId(), new Cooldown(0));
        if (COOLDOWN.get(attacker.getUniqueId()).hasExpired()) {
            COOLDOWN.put(attacker.getUniqueId(), getCooldown());
            Player targetPlayer = (Player) target;
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2 + enchantLevel * 4, enchantLevel + 3, true));
        }
    }

    @Override
    public String getText(int level, Player player) {
        return COOLDOWN.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(COOLDOWN.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }
}
