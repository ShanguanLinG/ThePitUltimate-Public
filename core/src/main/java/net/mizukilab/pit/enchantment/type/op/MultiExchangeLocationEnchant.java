package net.mizukilab.pit.enchantment.type.op;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/6 22:52
 */
@Skip
@BowOnly
public class MultiExchangeLocationEnchant extends AbstractEnchantment implements IPlayerShootEntity {

    @Override
    public String getEnchantName() {
        return "阴间游戏Lite！";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "multi_exchange_location";
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
        return "Its dead game";
    }

    @Override
    @PlayerOnly
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        final Collection<Player> players = PlayerUtil.getNearbyPlayers(target.getLocation(), 5);
        if (!players.contains(attacker)) {
            players.add(attacker);
        }
        final List<Location> locations = players.stream()
                .map(Player::getLocation)
                .collect(Collectors.toList());

        Collections.shuffle(locations);
        AtomicInteger atomicInteger = new AtomicInteger();
        players.forEach(player -> {
            final Location location = locations.get(atomicInteger.getAndIncrement());

            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            player.sendMessage(CC.translate("&5你被卷入到了时空漩涡之中...更替了你的位置"));
            player.teleport(location);
        });
    }
}
