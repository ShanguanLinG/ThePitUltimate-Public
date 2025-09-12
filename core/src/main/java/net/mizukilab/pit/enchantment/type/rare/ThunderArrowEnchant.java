package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Araykal
 * @since 2025/1/17
 */
@BowOnly
public class ThunderArrowEnchant extends AbstractEnchantment implements Listener,IPlayerShootEntity, IActionDisplayEnchant {
    private Map<UUID, Cooldown> cooldown = new Object2ObjectOpenHashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "雷箭";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "thunder_arrow_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return new Cooldown(0L);
    }

    @Override
    public String getUsefulnessLore(int level) {
        if (level < 1 || level > 3) return "?";

        String[] damage = {"0.5❤", "1.0❤", "1.0❤"};
        String[] extraDamage = {"", "", "&f(真实)"};
        String[] blindness = {"00:02", "00:04", "00:08"};
        String[] cooldown = {"10秒", "10秒", "12秒"};

        return String.format("&7箭矢命中玩家造成 &c%s &b雷电%s &7伤害 并造成 &d失明 I &f(%s) &7(%s冷却)",
                damage[level - 1], extraDamage[level - 1], blindness[level - 1], cooldown[level - 1]);
    }


    private void applyEffect(Player player, LivingEntity entity, int level) {

        if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
            switch (level) {
                case 1:
                    cooldown.put(player.getUniqueId(), new Cooldown(10, TimeUnit.SECONDS));
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, true), true);
                    break;
                case 2:
                    cooldown.put(player.getUniqueId(), new Cooldown(10, TimeUnit.SECONDS));
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, true), true);
                    break;
                case 3:
                    cooldown.put(player.getUniqueId(), new Cooldown(12, TimeUnit.SECONDS));
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 160, 0, true), true);
                    break;
                default:
                    return;
            }
            double damage = (level == 1) ? 0.5 : 1.0;
            entity.setHealth(Math.max(0.0, entity.getHealth() - damage));
            strikeLightningEffect(entity);
        }
    }

    private void strikeLightningEffect(LivingEntity entity) {
        WorldServer worldServer = ((CraftWorld) entity.getWorld()).getHandle();
        Location location = entity.getLocation();
        EntityLightning entityLightning = new EntityLightning(worldServer, location.getX(), location.getY(), location.getZ());
        PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(entityLightning);

        for (Player p : entity.getWorld().getPlayers()) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
        }

        entity.getWorld().strikeLightningEffect(location);
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.get(player.getUniqueId()).getRemaining()).replace(" ", "");
    }
    @Override
    public void handleShootEntity(int i, Player player, Entity entity, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        applyEffect(player, (LivingEntity) entity,i);
    }
}
