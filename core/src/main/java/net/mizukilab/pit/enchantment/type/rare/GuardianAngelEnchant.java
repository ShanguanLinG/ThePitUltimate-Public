package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
public class GuardianAngelEnchant extends AbstractEnchantment implements Listener,IPlayerDamaged, IActionDisplayEnchant {

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "守护天使";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "guardian_angel_enchant";
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
        int threshold = 15 + enchantLevel * 5; // 20%, 25%, 30%
        int cooldownSeconds = 45 - enchantLevel * 5; // 40s, 35s, 30s
        return "&7当生命值低于 &c" + threshold + "% &7时，获得以下效果:" +
               "/s&f▶ &3抗性提升 III &f(00:05)" +
               "/s&f▶ &c生命恢复 II &f(00:08)" +
               "/s&f▶ &b速度 II &f(00:05) &7(" + cooldownSeconds + "秒冷却)";
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? 
               "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).getRemaining()).replace(" ", "");
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (cooldown.getOrDefault(myself.getUniqueId(), new Cooldown(0)).hasExpired()) {
            double healthPercentage = myself.getHealth() / myself.getMaxHealth();
            double threshold = (15 + enchantLevel * 5) / 100.0;
            
            if (healthPercentage <= threshold) {
                myself.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 2), true);
                myself.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 1), true);
                myself.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1), true);
                PlayerUtil.heal(myself, enchantLevel * 1.0);

                myself.playSound(myself.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);

                int cooldownSeconds = 45 - enchantLevel * 5;
                cooldown.put(myself.getUniqueId(), new Cooldown(cooldownSeconds, TimeUnit.SECONDS));
            }
        }
    }
} 