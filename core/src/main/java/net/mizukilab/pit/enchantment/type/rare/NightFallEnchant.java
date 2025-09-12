package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@BowOnly
public class NightFallEnchant extends AbstractEnchantment implements IPlayerShootEntity, IActionDisplayEnchant, Listener {

    private final Map<UUID, Cooldown> Cooldown = new HashMap();
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getText(int level, Player player) {
        return Cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0L)).hasExpired()
                ? "&a&l✔"
                : new StringBuilder()
                .insert(0, "&c&l")
                .append(TimeUtil.millisToRoundedTime((Cooldown.get(player.getUniqueId())).getRemaining()).replace(" ", ""))
                .toString();
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Cooldown.putIfAbsent(attacker.getUniqueId(), new Cooldown(0L));
        if ((Cooldown.get(attacker.getUniqueId())).hasExpired()) {
            Cooldown.put(attacker.getUniqueId(), getCooldown());
            Player targetplayer = (Player) target;
            targetplayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * enchantLevel, 9));
        }

    }

    public String getEnchantName() {
        return "夜幕降临";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "nightfall_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return new Cooldown(10L, TimeUnit.SECONDS);

    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return new StringBuilder().insert(0, "&7箭矢命中时为目标施加 &8失明 X &f(00:0").append(enchantLevel).append("). &7(10秒冷却)").toString();

    }
}
