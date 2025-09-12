package net.mizukilab.pit.enchantment.type.rare;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.ITickTask;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ArmorOnly
public class SacredShieldEnchant extends AbstractEnchantment implements ITickTask, Listener, IActionDisplayEnchant {

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "神圣护盾";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "sacred_shield_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.OP;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        int shieldAmount = enchantLevel * 2; // 2, 4, 6 hearts
        int cooldownSeconds = 35 - enchantLevel * 5; // 30s, 25s, 20s
        return "&7每 &e" + cooldownSeconds + " &7秒获得 &6" + shieldAmount + " &7颗心的伤害吸收护盾";
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? 
               "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).getRemaining()).replace(" ", "");
    }

    @Override
    public void handle(int enchantLevel, Player player) {
        if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
            int shieldAmount = enchantLevel * 2;
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, shieldAmount - 1), true);

            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 0.8f, 1.5f);

            int cooldownSeconds = 35 - enchantLevel * 5;
            cooldown.put(player.getUniqueId(), new Cooldown(cooldownSeconds, TimeUnit.SECONDS));
        }
    }

    @Override
    public int loopTick(int enchantLevel) {
        return 20;
    }
} 