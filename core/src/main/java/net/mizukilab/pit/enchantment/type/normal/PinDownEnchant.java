package net.mizukilab.pit.enchantment.type.normal;


import cn.charlotte.pit.buff.impl.SiltedUpBuff;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Skip
@BowOnly
public class PinDownEnchant extends AbstractEnchantment implements Listener, IActionDisplayEnchant, IPlayerShootEntity, IMagicLicense {

    private static final SiltedUpBuff debuff = new SiltedUpBuff();

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "阻滞战术";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "pin_down_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7箭矢命中玩家可对其施加 &2阻滞 &f(" + TimeUtil.millisToTimer(getBuffTime(enchantLevel) * 1000L) + ") &7效果."
                + "/s&7效果 &2阻滞 &7: 无法受到与被施加 &b速度 &7与 &a跳跃提升 &7效果 (3秒冷却)";
    }

    @Override
    @PlayerOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0L)).hasExpired()) {
            Player player = (Player) target;
            cooldown.put(attacker.getUniqueId(), new Cooldown(3, TimeUnit.SECONDS));
            debuff.stackBuff(player, getBuffTime(enchantLevel) * 20L);
            player.sendMessage(CC.translate("&2&l阻滞! &7你在 &e" + getBuffTime(enchantLevel) + "秒 &7内无法被施加 &b速度 &7与 &a跳跃 &7效果."));
        }
    }

    public int getBuffTime(int enchantLevel) {
        return switch (enchantLevel) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 10;
            default -> 0;
        };
    }


    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }
}
