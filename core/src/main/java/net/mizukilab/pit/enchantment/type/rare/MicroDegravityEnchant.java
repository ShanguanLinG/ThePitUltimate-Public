package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.parm.listener.IPlayerDamaged;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ArmorOnly
@AutoRegister
public class MicroDegravityEnchant extends AbstractEnchantment implements Listener, IPlayerDamaged, IAttackEntity {

    Map<Entity, Map.Entry<Long, Byte>> entityByteMap = new ConcurrentHashMap<>();

    @Override
    public String getEnchantName() {
        return "微观反重力";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "micro_degravity";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int i) {
        switch (i) {
            case 1:
                return "&7若你在空中连续受到 &f3 &7次攻击 (2秒内)/s" +
                        "&7则恢复 &c2.0❤ &7生命值";
            case 2:
                return "&7若你在空中连续受到 &f3 &7次攻击 (2秒内)/s" +
                        "&7则恢复 &c2.0❤ &7生命值, 并且在接下来的30秒内/s" +
                        "&7增加 &c10% &7的伤害。(可叠加，最高2层)";
            case 3:
                return "&7若你在空中连续受到 &f3 &7次攻击 (2秒内)/s" +
                        "&7则恢复 &c2.0❤ &7生命值, 并且在接下来的30秒内/s" +
                        "&7增加 &c15% &7的伤害。(可叠加，最高2层)";
        }
        return "&c请提交至管理员寻求帮助 (INVALID LEVEL | + " + i + ")";
    }

    @ArmorOnly
    @Override
    public void handlePlayerDamaged(int level, Player player, Entity entity, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        if (!player.isOnGround()) {
            return;
        }
        Map.Entry<Long, Byte> hurtTime1 = entityByteMap.get(player);
        if (hurtTime1 == null || (System.currentTimeMillis() - hurtTime1.getKey()) > 30000) {
            entityByteMap.put(player, Map.entry(System.currentTimeMillis(), (byte) 0));
        } else {
            Byte hurtTime = hurtTime1.getValue();
            if (++hurtTime > 2) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 2));
            }
            int min = Math.min(2, hurtTime);
            entityByteMap.put(player, Map.entry(System.currentTimeMillis(), (byte) min));

        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        entityByteMap.remove(e.getPlayer());
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Map.Entry<Long, Byte> b = entityByteMap.get(attacker);
        if (b != null) {
            if (enchantLevel <= 1) {
                return;
            } else if (enchantLevel >= 3) {
                boostDamage.addAndGet(0.15 * b.getValue());
            } else {
                boostDamage.addAndGet(0.11 * b.getValue());
            }
        }


    }
}
