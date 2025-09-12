package net.mizukilab.pit.enchantment.type.op;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.ITickTask;
import net.mizukilab.pit.util.BatUtil;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import real.nanoneko.register.IMagicLicense;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Araykal
 * @since 2025/4/11
 */
@Skip
@WeaponOnly
public class VerminEnchant extends AbstractEnchantment implements Listener,ITickTask, IActionDisplayEnchant {

    private static final HashMap<UUID, Cooldown> COOLDOWN = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "吸血鬼";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "vermin_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.OP;
    }

    @Override
    public Cooldown getCooldown() {
        return new Cooldown(15, TimeUnit.SECONDS);
    }
    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return String.format(
                "&7右键化身蝙蝠, 向视角方向进行突进/s&7与此同时, 在 &e3 &7秒内无法受到攻击, 且恢复 &c3❤ &7生命值/s&7并且, 对 &e5 &7格范围内的目标每 &e0.5 &7秒造成 &c4❤ &7伤害/s&7(15秒冷却)",
                enchantLevel, enchantLevel, 4
        );
    }
    @Override
    public void handle(int enchantLevel, Player player) {
        if (player.isBlocking() && COOLDOWN.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() && !PlayerUtil.isVenom(player) && !PlayerUtil.isEquippingSomber(player)) {
            BatUtil.attachPlayerToBatsAndMove(player, enchantLevel, enchantLevel * 2);
            COOLDOWN.put(player.getUniqueId(), getCooldown());
        }
    }


    @Override
    public int loopTick(int enchantLevel) {
        return 3;
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        COOLDOWN.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getText(int level, Player player) {
        return COOLDOWN.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(COOLDOWN.get(player.getUniqueId()).getRemaining()).replace(" ", "") + " ";
    }
}