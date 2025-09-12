package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Starry_Killer
 * @Created_In: 2024/1/30
 */
@ArmorOnly
@AutoRegister
public class DoubleJumpEnchant extends AbstractEnchantment implements Listener, IActionDisplayEnchant, MovementHandler {

    private Map<UUID, Cooldown> cooldown = new Object2ObjectOpenHashMap<>();

    @SneakyThrows
    public DoubleJumpEnchant() {
        try {
            iSpigot.INSTANCE.addMovementHandler(this);
        } catch (NoClassDefFoundError ignore) {
        }
    }

    @Override
    public String getEnchantName() {
        return "二段跳";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "double_jump_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    private static int getCooldownInt(int enchantLevel) {
        return switch (enchantLevel) {
            case 2 -> 10;
            case 3 -> 5;
            default -> 20;
        };
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7在半空中按下跳跃键,你将向上方冲刺一段距离 (" + getCooldownInt(enchantLevel) + "秒冷却)";
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldown.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PlayerProfile playerProfileByUuid = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        int level = this.getItemEnchantLevel(playerProfileByUuid.leggings);
        if (level != -1 && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
                cooldown.put(player.getUniqueId(), new Cooldown(getCooldownInt(level), TimeUnit.SECONDS));
                event.setCancelled(true);
                player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.2));
                player.setAllowFlight(false);
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        GameMode gameMode = player.getGameMode();
        if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR) {
            if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
                PlayerProfile playerProfileByUuid = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                boolean flag = playerProfileByUuid.leggings instanceof MythicLeggingsItem
                        && isItemHasEnchant(playerProfileByUuid.leggings);

                if (player.getAllowFlight() != flag) {
                    player.setAllowFlight(flag);
                }
            }
        } else {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
            }
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
    }
}
