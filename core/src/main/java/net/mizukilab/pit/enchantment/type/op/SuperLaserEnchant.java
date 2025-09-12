package net.mizukilab.pit.enchantment.type.op;

import lombok.RequiredArgsConstructor;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.ITickTask;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.beam.beam.Beam;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/4/17 23:03
 */
@Skip
public class SuperLaserEnchant extends AbstractEnchantment implements ITickTask {

    private final Map<UUID, TargetInfo> targetInfoMap = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "集群镭射光";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "super_laser";
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
        return "&7周期性选中周围8格内所有玩家并对其发射激光,造成 &c0.5❤ &7普通伤害  (视为近战攻击)";
    }

    @Override
    public void handle(int enchantLevel, Player player) {
        final Collection<Player> players = PlayerUtil.getNearbyPlayers(player.getLocation(), 8);
        for (Player target : players) {
            if (target.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            target.damage(1, player);

            final TargetInfo info = targetInfoMap.get(target.getUniqueId());
            if (info == null) {
                final Beam beam = new Beam(player.getLocation(), target.getLocation(), 100, 5);
                beam.start();

                final TargetInfo targetInfo = new TargetInfo(target.getUniqueId(), beam);
                targetInfoMap.put(target.getUniqueId(), targetInfo);
                continue;
            }

            info.beam.setStartingPosition(player.getLocation());
            info.beam.setEndingPosition(target.getLocation());
        }


        for (Map.Entry<UUID, TargetInfo> entry : targetInfoMap.entrySet()) {
            if (players.stream().noneMatch(target -> target.getUniqueId().equals(entry.getKey()))) {
                targetInfoMap.remove(entry.getKey());
                entry.getValue().beam.stop();
            }
        }
    }

    @Override
    public int loopTick(int enchantLevel) {
        return 10;
    }


    @RequiredArgsConstructor
    public static class TargetInfo {

        private final UUID target;
        private final Beam beam;
    }
}
