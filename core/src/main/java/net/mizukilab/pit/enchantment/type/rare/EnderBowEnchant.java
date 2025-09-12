package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.ThePit;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/8 19:58
 */

@BowOnly
@AutoRegister
public class EnderBowEnchant extends AbstractEnchantment implements IPlayerShootEntity, Listener, IActionDisplayEnchant {

    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    private static int getCooldownInt(int enchantLevel) {
        switch (enchantLevel) {
            case 2:
                return 30;
            case 3:
                return 15;
            default:
                return 60;
        }
    }

    @Override
    public String getEnchantName() {
        return "末影弓";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "ender_bow_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7潜行射击发射的箭矢落地时会将自身传送至落地点 &7(" + getCooldownInt(enchantLevel) + "秒冷却)"
                + "/s&7(每次箭矢命中目标减少3秒此附魔冷却时间)";
    }

    @Override
    public void handleShootEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        cooldown.putIfAbsent(attacker.getUniqueId(), new Cooldown(0));
        if (!cooldown.get(attacker.getUniqueId()).hasExpired()) {
            cooldown.put(attacker.getUniqueId(), new Cooldown(Math.max(0, cooldown.get(attacker.getUniqueId()).getRemaining() - 3000)));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldown.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBowShot(EntityShootBowEvent event) {
        if (ThePit.getInstance().getEventFactory().getActiveEpicEvent() != null) return;

        if (!(event.getEntity() instanceof Player)) return;
        final Player player = (Player) event.getEntity();
        if (PlayerUtil.isVenom(player) || PlayerUtil.isEquippingSomber(player)) return;
        final org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) return;
        final int level = this.getItemEnchantLevel(itemInHand);
        if (level == -1) {
            return;
        }
        if (!player.isSneaking()) return;
        if (itemInHand.getType() == Material.BOW) {
            if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {
                cooldown.put(player.getUniqueId(), new Cooldown(getCooldownInt(level), TimeUnit.SECONDS));
                Entity projectile = event.getProjectile();
                event.getProjectile().setMetadata("ender_bow", new FixedMetadataValue(ThePit.getInstance(), true));
                Utils.pointMetadataAndRemove(projectile, 2000, "ender_bow");
                //event.setProjectile(projectile);
            }
        }
    }

    @EventHandler
    public void onBowHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata("ender_bow") && event.getEntity().getShooter() != null) {
            if (event.getEntity().getShooter() instanceof Player player) {
                if (PlayerUtil.isVenom(player) || PlayerUtil.isEquippingSomber(player)) return;
                player.teleport(event.getEntity());
            }
        }
    }

    @Override
    public String getText(int level, Player player) {
        return (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).getRemaining()).replace(" ", ""));
    }
}
