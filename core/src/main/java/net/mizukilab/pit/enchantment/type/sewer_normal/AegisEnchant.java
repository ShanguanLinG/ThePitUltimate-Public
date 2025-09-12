package net.mizukilab.pit.enchantment.type.sewer_normal;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.parm.listener.ITickTask;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/16 21:43
 */
@ArmorOnly
public class AegisEnchant extends AbstractEnchantment implements ITickTask, IPlayerDamaged, IActionDisplayEnchant, Listener {
    private static final Map<UUID, Boolean> shield = new HashMap<>();
    private static final Map<UUID, Cooldown> cooldownMap = new HashMap<>();
    private static final int COOLDOWN_DURATION = 9;
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldownMap.remove(e.getPlayer().getUniqueId());
        shield.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "宙斯之盾";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "aegis_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.SEWER_NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7每 &e9 &7秒获得一层护盾 (可以抵消1次玩家伤害) (最高1层)";
    }

    @Override
    public void handle(int enchantLevel, Player player) {
        if (PlayerUtil.isVenom(player) || PlayerUtil.isEquippingSomber(player)) return;

        shield.putIfAbsent(player.getUniqueId(), false);
        cooldownMap.putIfAbsent(player.getUniqueId(), new Cooldown(COOLDOWN_DURATION, TimeUnit.SECONDS));

        Cooldown cooldown = cooldownMap.get(player.getUniqueId());
        if (cooldown.hasExpired() && !shield.get(player.getUniqueId())) {
            player.sendMessage(CC.translate("&e&l宙斯之盾! &7恢复一层护盾."));
            shield.put(player.getUniqueId(), true);
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
        }
    }

    @Override
    public int loopTick(int enchantLevel) {
        return 20;
    }

    @Override
    public void handlePlayerDamaged(int enchantLevel, Player myself, Entity attacker, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (shield.getOrDefault(myself.getUniqueId(), false)) {
            cancel.set(true);
            cooldownMap.put(myself.getUniqueId(), new Cooldown(COOLDOWN_DURATION, TimeUnit.SECONDS));
            shield.put(myself.getUniqueId(), false);

            myself.sendMessage(CC.translate("&e&l宙斯之盾! &7抵消一次攻击."));
            myself.playSound(myself.getLocation(), Sound.ANVIL_LAND, 1, 1);
            attacker.sendMessage(CC.translate("&c你的攻击被对方护盾抵消!"));

            if (attacker instanceof Player) {
                ((Player) attacker).playSound(attacker.getLocation(), Sound.ANVIL_LAND, 1, 1);
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        shield.putIfAbsent(player.getUniqueId(), false);
        cooldownMap.putIfAbsent(player.getUniqueId(), new Cooldown(COOLDOWN_DURATION, TimeUnit.SECONDS));

        Cooldown cooldown = cooldownMap.getOrDefault(player.getUniqueId(), new Cooldown(0));
        String duration = cooldown.hasExpired() ? "" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getRemaining()).replace(" ", "");

        return shield.get(player.getUniqueId()) ? "&a&l✔" : duration;
    }
}
