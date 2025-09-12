package net.mizukilab.pit.enchantment.type.normal;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/25 14:23
 */

@AutoRegister
@BowOnly
@Skip
public class BowComboEnchant extends AbstractEnchantment implements IPlayerShootEntity, Listener, IActionDisplayEnchant {

    public final HashMap<UUID, Long> hitCheck = new HashMap<>();
    public final HashMap<UUID, Integer> combo = new HashMap<>();

    @Override
    public String getEnchantName() {
        return "神射手";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "bow_combo_enchant";
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
        return "&7连续 &e" + (enchantLevel >= 2 ? 2 : 3) + " &7次箭矢射出并命中为自身添加 &b速度 " + RomanUtil.convert(enchantLevel + 1) + " &f(" + TimeUtil.millisToTimer(4000) + ")";
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (combo.getOrDefault(attacker.getUniqueId(), 0) >= (enchantLevel >= 2 ? 2 : 3)) {
                    combo.put(attacker.getUniqueId(), 0);
                    attacker.removePotionEffect(PotionEffectType.SPEED);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 4, enchantLevel, true));
                }
            }
        }.runTaskLater(ThePit.getInstance(), 2L);
    }

    @EventHandler
    public void onBowShot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        final Player player = (Player) event.getEntity();
        final org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) return;
        final int level = this.getItemEnchantLevel(itemInHand);
        if (level == -1) {
            return;
        }
        event.getProjectile().setMetadata("bow_combo_enchant_uuid", new FixedMetadataValue(ThePit.getInstance(), UUID.randomUUID().toString()));
        Utils.pointMetadataAndRemove(event.getProjectile(), 500, "bow_combo_enchant_uuid");
    }

    @EventHandler
    public void onBowHit(ProjectileHitEvent event) {
        List<MetadataValue> bowComboEnchantUuid = event.getEntity().getMetadata("bow_combo_enchant_uuid");
        if (!bowComboEnchantUuid.isEmpty() && event.getEntity().getShooter() != null) {
            if (event.getEntity().getShooter() instanceof Player player) {
                UUID uuid = UUID.fromString(bowComboEnchantUuid.get(0).asString());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (hitCheck.get(uuid) == null) {
                            combo.put(player.getUniqueId(), 0);
                        } else {
                            combo.put(player.getUniqueId(), combo.getOrDefault(player.getUniqueId(), 0) + 1);
                            hitCheck.remove(uuid);
                        }
                    }
                }.runTaskLater(ThePit.getInstance(), 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            List<MetadataValue> bowComboEnchantUuid = event.getDamager().getMetadata("bow_combo_enchant_uuid");
            if (!bowComboEnchantUuid.isEmpty()) {
                UUID uuid = UUID.fromString(bowComboEnchantUuid.get(0).asString());
                event.getEntity().removeMetadata("bow_combo_enchant_uuid", ThePit.getInstance());
                hitCheck.put(uuid, System.currentTimeMillis());
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        return "&e&l" + combo.getOrDefault(player.getUniqueId(), 0) + "/" + (level >= 2 ? 2 : 3);
    }
}
