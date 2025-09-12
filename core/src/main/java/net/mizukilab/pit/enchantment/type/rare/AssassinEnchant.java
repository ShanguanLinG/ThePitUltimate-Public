package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/2/25 21:49
 */
@Skip
@ArmorOnly
@AutoRegister
public class AssassinEnchant extends AbstractEnchantment implements Listener, IPlayerDamaged, IActionDisplayEnchant {

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "暗影步伐";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "assassin_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    public int getCooldownInt(int enchantLevel) {
        return switch (enchantLevel) {
            case 2 -> 5;
            case 3 -> 3;
            default -> 10;
        };
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) { //10 5 3
        return "&7蹲下状态时受到攻击会将你传送至攻击者处 (" + getCooldownInt(enchantLevel) + "秒冷却)";
    }

    @Override
    @PlayerOnly
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (myself == attacker) return;

        if (myself.isSneaking() && cooldown.getOrDefault(myself.getUniqueId(),new Cooldown(0L)).hasExpired()) {
            Location location = attacker.getLocation();
            Location newLoc = location.add(attacker.getLocation().getDirection().clone().multiply(-1.5));

            myself.teleport(newLoc);
            cooldown.put(myself.getUniqueId(), new Cooldown(getCooldownInt(enchantLevel), TimeUnit.SECONDS));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldown.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }
}
