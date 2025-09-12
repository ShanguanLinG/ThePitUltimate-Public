package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoRegister
@BowOnly
public class SeriousSituationEnchant extends AbstractEnchantment implements Listener, IActionDisplayEnchant, IPlayerShootEntity {

    Object2LongOpenHashMap<UUID> shoot = new Object2LongOpenHashMap<>();

    @Override
    public String getEnchantName() {
        return "事态严重";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "serious_sit";
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

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.shoot.removeLong(e.getPlayer().getUniqueId());
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return switch (enchantLevel) {
            case 1 -> "&7每 &e2 &7次击中玩家将使第三支箭拥有 &b冲击 II &7效果";
            case 2 -> "&7每 &e2 &7次击中玩家将使第三支箭拥有 &b冲击 III &7效果, 并额外造成 &c0.5❤ &7伤害";
            case 3 -> "&7每 &e2 &7次击中玩家将使第三支箭拥有 &b冲击 IV &7效果, 并额外造成 &c1.0❤ &7伤害";
            default -> "ERROR";
        };
    }


    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player player) {
            UUID uniqueId = e.getEntity().getUniqueId();
            long l = shoot.getLong(uniqueId);
            if (l == 2) {
                final int level = this.getItemEnchantLevel(player.getItemInHand());
                if (level <= 0) {
                    return;
                }
                Entity handle = ((CraftEntity) e.getProjectile()).getHandle();
                if (handle instanceof EntityArrow arrow) {
                    arrow.setKnockbackStrength(getPunch(level)); //set punch III
                    arrow.b(arrow.j() + getAddonDamage(level)); //2 H = 1 Heart
                }
                shoot.removeLong(uniqueId);
            }
        }
    }

    public int getPunch(int ench) {
        return switch (ench) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            default -> 0;
        };
    }

    public double getAddonDamage(int ench) {
        return switch (ench) {
            case 2 -> 1D;
            case 3 -> 2D;
            default -> 0D;
        };
    }

    @Override
    public String getText(int level, Player player) {
        long remain = (3L - shoot.getLong(player.getUniqueId()));
        if (remain == 1) {
            return "&a&l✔";
        }
        return "&e&l" + remain + "/3";
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player attacker, org.bukkit.entity.Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        UUID uniqueId = attacker.getUniqueId();
        long aLong = shoot.getLong(uniqueId);
        shoot.put(uniqueId, ++aLong);
    }
}
