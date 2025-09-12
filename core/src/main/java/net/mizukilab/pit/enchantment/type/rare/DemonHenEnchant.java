package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.parm.listener.IPlayerShootEntity;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@BowOnly
@AutoRegister
public class DemonHenEnchant extends AbstractEnchantment implements IActionDisplayEnchant, IPlayerShootEntity, Listener, IPlayerKilledEntity {

    private static final Map<UUID, Cooldown> cooldown = new ConcurrentHashMap<>();
    

    private final Map<UUID, DemonHenData> demonHens = new ConcurrentHashMap<>();
    

    private final Set<UUID> explodingPlayers = ConcurrentHashMap.newKeySet();

    private static class DemonHenData {
        final UUID ownerUUID;
        final LivingEntity henEntity;
        final long spawnTime;
        final double maxHealth;
        
        DemonHenData(UUID ownerUUID, LivingEntity henEntity) {
            this.ownerUUID = ownerUUID;
            this.henEntity = henEntity;
            this.spawnTime = System.currentTimeMillis();
            this.maxHealth = henEntity.getMaxHealth();
        }
        
        boolean isExpired() {

            return System.currentTimeMillis() - spawnTime > 15000;
        }
        
        boolean shouldExplode() {

            return henEntity.isOnGround() || (henEntity.getHealth() / maxHealth) < 0.5;
        }
        
        boolean isValid() {
            return henEntity != null && henEntity.isValid() && !henEntity.isDead();
        }
    }

    public DemonHenEnchant() {

        new BukkitRunnable() {
            @Override
            public void run() {
                manageDemonHens();
            }
        }.runTaskTimer(ThePit.getInstance(), 20L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredData();
            }
        }.runTaskTimer(ThePit.getInstance(), 600L, 600L);
    }

    private void manageDemonHens() {
        if (demonHens.isEmpty()) return;
        
        Iterator<Map.Entry<UUID, DemonHenData>> iterator = demonHens.entrySet().iterator();
        List<UUID> toExplode = new ArrayList<>();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, DemonHenData> entry = iterator.next();
            UUID henId = entry.getKey();
            DemonHenData data = entry.getValue();
            

            if (!data.isValid() || data.isExpired()) {
                if (data.isValid()) {
                    data.henEntity.remove();
                }
                iterator.remove();
                continue;
            }
            

            if (data.shouldExplode()) {
                toExplode.add(henId);
            }
        }
        

        if (!toExplode.isEmpty()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                for (UUID henId : toExplode) {
                    explodeDemonHen(henId);
                }
            });
        }
    }

    private void explodeDemonHen(UUID henId) {
        DemonHenData data = demonHens.remove(henId);
        if (data == null || !data.isValid()) return;
        
        LivingEntity hen = data.henEntity;
        Location location = hen.getLocation();
        Player owner = Bukkit.getPlayer(data.ownerUUID);

        float healthScaled = (float) (hen.getHealth() / hen.getMaxHealth());
        float explosionPower = 1.25F * Math.max(healthScaled, 0.3F);
        

        hen.remove();

        if (owner != null) {
            explodingPlayers.add(owner.getUniqueId());
        }
        
        try {

            ((CraftWorld) location.getWorld()).getHandle().createExplosion(
                owner != null ? ((CraftEntity) owner).getHandle() : null,
                location.getX(), location.getY(), location.getZ(),
                explosionPower, false, false
            );
            

            applyKnockback(location);
            
        } finally {

            if (owner != null) {
                Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
                    explodingPlayers.remove(owner.getUniqueId());
                }, 5L);
            }
        }
    }

    private void applyKnockback(Location explosionLocation) {
        Collection<Entity> nearbyEntities = explosionLocation.getWorld().getNearbyEntities(explosionLocation, 3, 3, 3);
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity.isValid() && !PlayerProfile.getPlayerProfileByUuid(entity.getUniqueId()).isInArena()) {
                Vector knockback = entity.getLocation().toVector()
                    .subtract(explosionLocation.toVector())
                    .normalize()
                    .multiply(1.5)
                    .setY(Math.abs(ThreadLocalRandom.current().nextDouble(0.5, 1.2)));
                
                entity.setVelocity(knockback);
            }
        }
    }

    private void cleanupExpiredData() {

        cooldown.entrySet().removeIf(entry -> entry.getValue().hasExpired());
        

        explodingPlayers.clear();
    }

    @Override
    public String getEnchantName() {
        return "恶魔鸡";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "evil_hen";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return new Cooldown(3, TimeUnit.SECONDS);
    }

    @Override
    public String getUsefulnessLore(int i) {
        return "箭击中玩家时会生成 &f" + i + "&7 只爆炸性的鸡。/s" +
                "小鸡在爆炸中对玩家造成大量击退和 &c1.0 ❤&7 扩散伤害 (2秒冷却)";
    }

    @PlayerOnly
    @BowOnly
    @Override
    public void handleShootEntity(int level, Player player, Entity entity, double v, AtomicDouble atomicDouble, AtomicDouble atomicDouble1, AtomicBoolean atomicBoolean) {
        cooldown.putIfAbsent(player.getUniqueId(), new Cooldown(0, TimeUnit.SECONDS));
        if (!cooldown.get(player.getUniqueId()).hasExpired()) {
            return;
        }
        

        String internalName = ItemUtil.getInternalName(player.getItemInHand());
        if (!"mythic_bow".equals(internalName)) {
            return;
        }
        
        cooldown.put(player.getUniqueId(), new Cooldown(2, TimeUnit.SECONDS));
        Location targetLocation = entity.getLocation();
        

        for (int i = 0; i < level; i++) {
            spawnDemonHen(player, targetLocation);
        }
    }

    private void spawnDemonHen(Player owner, Location targetLocation) {
        Location henLocation = getHenLocation(targetLocation);
        LivingEntity hen = (LivingEntity) henLocation.getWorld().spawnEntity(henLocation, EntityType.CHICKEN);

        hen.setCustomNameVisible(true);
        hen.setCustomName("§c恶魔鸡");
        hen.setMaxHealth(20.0);
        hen.setHealth(20.0);

        Vector velocity = targetLocation.toVector().subtract(henLocation.toVector()).normalize().multiply(0.3);
        velocity.setY(0.2);
        hen.setVelocity(velocity);

        demonHens.put(hen.getUniqueId(), new DemonHenData(owner.getUniqueId(), hen));
    }

    private Location getHenLocation(Location location) {
        Location clone = location.clone();
        float rad = (float) Math.toRadians(ThreadLocalRandom.current().nextInt(360));
        float range = ThreadLocalRandom.current().nextFloat() * 2 + 1;
        float x = MathHelper.sin(rad) * range;
        float z = MathHelper.cos(rad) * range;
        return clone.add(x, ThreadLocalRandom.current().nextInt(2) + 1, z);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID playerUUID = e.getPlayer().getUniqueId();
        cooldown.remove(playerUUID);
        explodingPlayers.remove(playerUUID);

        demonHens.entrySet().removeIf(entry -> {
            DemonHenData data = entry.getValue();
            if (playerUUID.equals(data.ownerUUID)) {
                if (data.isValid()) {
                    data.henEntity.remove();
                }
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {

        demonHens.remove(e.getEntity().getUniqueId());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {

        demonHens.entrySet().removeIf(entry -> {
            DemonHenData data = entry.getValue();
            if (data.isValid() && data.henEntity.getLocation().getChunk().equals(e.getChunk())) {
                data.henEntity.remove();
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

            if (e.getEntity() instanceof Player) {
                Player player = (Player) e.getEntity();
                if (explodingPlayers.contains(player.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        if (target.getLastDamageCause() != null && 
            target.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

            coins.set(coins.get() * 0.2);
            experience.set(experience.get() * 0.2);
        }
    }

    @Override
    public String getText(int level, Player player) {
        Cooldown playerCooldown = cooldown.get(player.getUniqueId());
        if (playerCooldown == null || playerCooldown.hasExpired()) {
            return "&a&l✔";
        } else {
            return "&c&l" + TimeUtil.millisToRoundedTime(playerCooldown.getRemaining()).replace(" ", "");
        }
    }
}
