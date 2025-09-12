package net.mizukilab.pit.enchantment.type.normal;

import cn.charlotte.pit.data.PlayerProfile;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

/**
 * @Author: Starry_Killer
 * @Created_In: 2023/11/22 18:15
 */

@ArmorOnly
@Skip
public class TrotEnchant extends AbstractEnchantment implements MovementHandler {

    @SneakyThrows
    public TrotEnchant() {
        try {
            iSpigot.INSTANCE.addMovementHandler(this);
        } catch (NoClassDefFoundError ignore) {
        }
    }

    @Override
    public String getEnchantName() {
        return "疾走";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "Trot";
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
        return "&7穿戴时行走速度提升 &b" + (enchantLevel == 3 ? "20%" : (enchantLevel == 2 ? "10%" : "5%"));
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        IMythicItem leggings = (IMythicItem) PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).leggings;
        float walkSpeed = player.getWalkSpeed();
        boolean b = PlayerUtil.shouldIgnoreEnchant(player);
        if (leggings != null && !b) {
            int level = this.getItemEnchantLevel(leggings);
            if (level == 1) {
                if (walkSpeed != 0.21F) {
                    player.setWalkSpeed(0.21F);
                }
            } else if (level == 2) {
                if (walkSpeed != 0.22F) {
                    player.setWalkSpeed(0.22F);
                }
            } else if (level == 3) {

                if (walkSpeed != 0.24F) {
                    player.setWalkSpeed(0.24F);
                }
            } else {
                if (walkSpeed != 0.2F) {
                    player.setWalkSpeed(0.2F);
                }
            }

        } else {

            if (walkSpeed != 0.2F) {
                player.setWalkSpeed(0.2F);
            }
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
