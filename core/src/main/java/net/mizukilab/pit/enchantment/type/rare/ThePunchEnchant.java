package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.ThePit;
import com.google.common.util.concurrent.AtomicDouble;
import net.citizensnpcs.api.CitizensAPI;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Creator Misoryan
 * @Date 2021/5/24 12:29
 */

@WeaponOnly
@BowOnly
public class ThePunchEnchant extends AbstractEnchantment implements Listener,IAttackEntity, IPlayerShootEntity, IActionDisplayEnchant {

    public static double PUNCH_X = 0;
    public static double PUNCH_Y = 0;
    public static double PUNCH_Z = 0;

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "击飞!";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "the_punch";
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
        return "&7攻击可以把目标发射到空中! (" + (35 - 5 * enchantLevel) + "秒冷却)";
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (CitizensAPI.getNPCRegistry().isNPC(target)) {
            return;
        }
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0)).hasExpired()) {
            cooldown.put(attacker.getUniqueId(), new Cooldown(35 - 5L * enchantLevel, TimeUnit.SECONDS));
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                target.setVelocity(new Vector(PUNCH_X, PUNCH_Y, PUNCH_Z));
            });
        }
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (CitizensAPI.getNPCRegistry().isNPC(target)) {
            return;
        }
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0)).hasExpired()) {
            cooldown.put(attacker.getUniqueId(), new Cooldown(35 - 5L * enchantLevel, TimeUnit.SECONDS));
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                target.setVelocity(new Vector(PUNCH_X, PUNCH_Y, PUNCH_Z));
            });
        }
    }

    @Override
    public String getText(int level, Player player) {
        return getCooldownActionText(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)));
    }
}
