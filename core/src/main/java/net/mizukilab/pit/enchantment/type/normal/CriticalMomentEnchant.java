package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import cn.charlotte.pit.event.PitDamagePlayerEvent;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import com.google.common.util.concurrent.AtomicDouble;
import io.irina.backports.utils.SWMRHashTable;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
@Skip
@AutoRegister
public class CriticalMomentEnchant extends AbstractEnchantment implements IPlayerDamaged, Listener, IActionDisplayEnchant {

    Map<UUID, Integer> record = new SWMRHashTable<>();
    Map<UUID, Cooldown> cooldownMap = new SWMRHashTable<>();

    @Override
    public String getEnchantName() {
        return "危急时刻";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "critical_moment";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return new Cooldown(10, TimeUnit.SECONDS);
    }

    //&9危急时刻
    //&7当你还剩 &c3.0❤ &7时，获得 &b速度 III &f(00:03)&7 (10秒冷却)
    @Override
    public String getUsefulnessLore(int enchantLevel) {
        switch (enchantLevel) {
            case 1 -> {
                return "&7当你还剩 &c3.0❤ &7时，获得 &b速度 III &f(00:03)&7 (10秒冷却)";
            }
            case 2 -> {
                return "&7当你还剩 &c4.0❤ &7时，获得 &b速度 III &f(00:06)&7 (10秒冷却)";
            }
            case 3 -> {
                return "&7当你还剩 &c4.0❤ &7时，获得 &b速度 III &f(00:09)&7 (10秒冷却)";
            }
        }
        return null;
    }

    static PotionEffect L1 = PotionEffectType.SPEED.createEffect(80, 2);

    static PotionEffect L2 = PotionEffectType.SPEED.createEffect(150, 2);

    static PotionEffect L3 = PotionEffectType.SPEED.createEffect(200, 2);

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        UUID uniqueId = myself.getUniqueId();
        Cooldown cooldown = this.cooldownMap.get(uniqueId);
        if (cooldown == null || cooldown.hasExpired()) {
            record.put(uniqueId, enchantLevel);
        }
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        record.remove(uniqueId);
        cooldownMap.remove(uniqueId);
    }

    @EventHandler
    public void handleRegen(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (player.getHealth() + e.getAmount() > 4) {
                record.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void handlePlayerDamagedFinal(PitDamagePlayerEvent e) {
        Player victim = e.getVictim();
        UUID uniqueId = victim.getUniqueId();
        Integer integer = record.get(uniqueId);
        double v = victim.getHealth() - e.getFinalDamage();
        if (v < 0) {
            record.remove(uniqueId);
            return;
        }
        if (integer == null) {
            return;
        }

        switch (integer) {
            case 1 -> {
                if (v <= 6.0) {
                    victim.addPotionEffect(L1, true);

                    cooldownMap.put(uniqueId, new Cooldown(10, TimeUnit.SECONDS));
                }
            }
            case 2 -> {

                if (v <= 8.0) {
                    victim.addPotionEffect(L2, true);

                    cooldownMap.put(uniqueId, new Cooldown(10, TimeUnit.SECONDS));
                }
            }
            case 3 -> {

                if (v <= 8.0) {
                    victim.addPotionEffect(L3, true);

                    cooldownMap.put(uniqueId, new Cooldown(10, TimeUnit.SECONDS));
                }
            }
        }
        record.remove(uniqueId);
    }

    @Override
    public String getText(int level, Player player) {
        return cooldownMap.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔"
                : "&c&l" + TimeUtil.millisToRoundedTime(cooldownMap.get(player.getUniqueId()).getRemaining())
                .replace(" ", "");
    }
}
