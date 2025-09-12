package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

@BowOnly
public class UndeadArrowEnchant extends AbstractEnchantment implements IPlayerShootEntity {

    @Override
    public String getEnchantName() {
        return "亡灵之箭";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "judgment_shot_bow";
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
    public String getUsefulnessLore(int enchantLevel) {
        return (new StringBuilder()).insert(0, "&7攻击玩家时若当次攻击使玩家的生命值低于 &c").append(1.5D + enchantLevel * 0.5D).append("❤ &7,/s&7则该次攻击直接致死.").toString();
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        if (targetPlayer.getHealth() - damage * boostDamage.get() < (1.5 + (enchantLevel * 0.5))) {
            cancel.set(true);
            targetPlayer.damage(9999);
            attacker.playSound(attacker.getLocation(), Sound.VILLAGER_DEATH, 1, 1F);

            Location deathLoc = target.getLocation();
            PacketPlayOutWorldEvent packetA = new PacketPlayOutWorldEvent(2001, new BlockPosition(deathLoc.getBlockX(), deathLoc.getBlockY(), deathLoc.getBlockZ()), 152, false);
            PacketPlayOutWorldEvent packetB = new PacketPlayOutWorldEvent(2001, new BlockPosition(deathLoc.getBlockX(), deathLoc.getBlockY() - 1, deathLoc.getBlockZ()), 152, false);

            PlayerConnection connection = ((CraftPlayer) attacker).getHandle().playerConnection;
            connection.sendPacket(packetA);
            connection.sendPacket(packetB);
        }
    }
}
