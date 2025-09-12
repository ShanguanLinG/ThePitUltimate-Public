package net.mizukilab.pit.npc;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import lombok.Getter;
import lombok.SneakyThrows;
import net.mizukilab.pit.npc.events.CustomEntityNPCInteractEvent;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class CustomEntityNPCFactory implements Listener {

    @Getter
    private final List<AbstractCustomEntityNPC> customEntityNPCs = new ArrayList<>();

    @Getter
    private final Map<UUID, AbstractCustomEntityNPC> entityToNPCMap = new HashMap<>();

    private final Map<UUID, Map<UUID, List<Hologram>>> playerNPCHolograms = new HashMap<>();

    public boolean isCustomNPC(Entity entity) {
        return entityToNPCMap.containsKey(entity.getUniqueId());
    }

    public AbstractCustomEntityNPC getCustomNPC(Entity entity) {
        return entityToNPCMap.get(entity.getUniqueId());
    }


    public void reload() {
        cleanup();
        customEntityNPCs.forEach(this::createNPCEntity);
        Bukkit.getOnlinePlayers().forEach(this::showNPCsToPlayer);
    }


    public void showNPCsToPlayer(Player player) {
        cleanupPlayerHolograms(player);
        
        customEntityNPCs.forEach(npc -> {
            if (npc.getEntity() != null && !npc.getEntity().isDead()) {
                createPersonalHologramForPlayer(npc, player);
            }
        });
    }
    
    private void cleanupPlayerHolograms(Player player) {
        Map<UUID, List<Hologram>> playerHolograms = playerNPCHolograms.get(player.getUniqueId());
        if (playerHolograms != null) {
            playerHolograms.values().forEach(holograms -> {
                holograms.forEach(hologram -> {
                    if (hologram.isSpawned()) {
                        hologram.deSpawn();
                    }
                });
            });
            playerHolograms.clear();
        }
    }
    
    private void createPersonalHologramForPlayer(AbstractCustomEntityNPC npc, Player player) {
        if (npc.getEntity() == null) return;

        double hologramHeight = getHologramHeight(npc.getEntityType());
        Location baseLocation = npc.getEntity().getLocation().add(0, hologramHeight, 0);
        List<String> lines = npc.getNpcTextLine(player);

        if (!lines.isEmpty()) {
            List<Hologram> holograms = new ArrayList<>();
            
            for (int i = 0; i < lines.size(); i++) {
                Location lineLocation = baseLocation.clone().add(0, (lines.size() - 1 - i) * 0.29, 0);
                Hologram hologram = HologramAPI.createHologram(lineLocation, CC.translate(lines.get(i)));
                hologram.spawn(Collections.singletonList(player));
                holograms.add(hologram);
            }

            playerNPCHolograms.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(npc.getEntity().getUniqueId(), holograms);
        }
    }


    @SneakyThrows
    public void init(Collection<Class<? extends AbstractCustomEntityNPC>> classes) {
        cleanupWorldEntities();

        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
            for (Class<?> clazz : classes) {
                if (AbstractCustomEntityNPC.class.isAssignableFrom(clazz)) {
                    try {
                        AbstractCustomEntityNPC customNPC = (AbstractCustomEntityNPC) clazz.getConstructor().newInstance();
                        createNPCEntity(customNPC);
                        customEntityNPCs.add(customNPC);
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("创建自定义NPC失败: " + clazz.getSimpleName() + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

        }, 20L);
        Bukkit.getScheduler().runTaskTimer(ThePit.getInstance(), this::maintainNPCs, 20L, 20L);
        Bukkit.getScheduler().runTaskTimer(ThePit.getInstance(), this::updateHolograms, 100L, 100L);
    }

    private void createNPCEntity(AbstractCustomEntityNPC npc) {
        try {
            Location location = npc.getNpcSpawnLocation();
            if (location == null || location.getWorld() == null) {
                return;
            }

            Entity entity = location.getWorld().spawnEntity(location, npc.getEntityType());


            initializeNPCEntity(entity, npc);


            npc.setEntity(entity);
            entityToNPCMap.put(entity.getUniqueId(), npc);

        } catch (Exception e) {
            Bukkit.getLogger().severe("创建NPC失败: " + npc.getNpcInternalName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeNPCEntity(Entity entity, AbstractCustomEntityNPC npc) {

        npc.initializeEntity(entity);

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            if (entity.getType() == EntityType.BAT) {
                try {
                    Bat bat = (Bat) entity;
                    bat.setAwake(true);
                    
                    net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftLivingEntity) livingEntity).getHandle();
                    net.minecraft.server.v1_8_R3.NBTTagCompound nbt = new net.minecraft.server.v1_8_R3.NBTTagCompound();
                    nmsEntity.c(nbt);
                    nbt.setInt("NoAI", 1);
                    nbt.setInt("Silent", 1);
                    nbt.setByte("BatFlags", (byte) 1);
                    nmsEntity.f(nbt);
                    

                    final Location fixedLocation = entity.getLocation();
                    Bukkit.getScheduler().runTaskTimer(ThePit.getInstance(), () -> {
                        if (entity.isValid() && !entity.isDead()) {
                            if (entity.getLocation().distance(fixedLocation) > 0.1) {
                                entity.teleport(fixedLocation);
                            }

                            if (entity instanceof Bat) {
                                ((Bat) entity).setAwake(true);
                            }
                        }
                    }, 1L, 5L);
                    
                } catch (Exception e) {
                    Bukkit.getLogger().warning("失败: " + e.getMessage());
                }
            } else if (!npc.canMove()) {
                try {
                    net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftLivingEntity) livingEntity).getHandle();
                    net.minecraft.server.v1_8_R3.NBTTagCompound nbt = new net.minecraft.server.v1_8_R3.NBTTagCompound();
                    nmsEntity.c(nbt);
                    nbt.setInt("NoAI", 1);
                    nbt.setInt("Silent", 1);
                    nmsEntity.f(nbt);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("失败: " + e.getMessage());
                }
            }
            
            if (npc.getNpcHeldItem() != null && livingEntity instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.inventory.EntityEquipment equipment = livingEntity.getEquipment();
                if (equipment != null) {
                    equipment.setItemInHand(npc.getNpcHeldItem());
                }
            }

            if (entity instanceof Ageable) {
                Ageable ageable = (Ageable) entity;
                if (npc.isAdult()) {
                    ageable.setAdult();
                } else {
                    ageable.setBaby();
                }
            }
        }
    }

    private double getHologramHeight(EntityType entityType) {
        switch (entityType) {
            case IRON_GOLEM:
                return 2.5;
            case SILVERFISH, BAT:
                return 0.6;
            default:
                return 1.9;
        }
    }



    private void maintainNPCs() {
        customEntityNPCs.removeIf(npc -> {
            Entity entity = npc.getEntity();
            if (entity == null || entity.isDead()) {
                if (entity != null) {
                    entityToNPCMap.remove(entity.getUniqueId());
                    cleanupNPCHolograms(entity.getUniqueId());
                }
                return true;
            }
            return false;
        });
    }
    
    private void updateHolograms() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<UUID, List<Hologram>> playerHolograms = playerNPCHolograms.get(player.getUniqueId());
            if (playerHolograms != null) {
                for (UUID npcEntityId : playerHolograms.keySet()) {
                    AbstractCustomEntityNPC npc = entityToNPCMap.get(npcEntityId);
                    if (npc != null && npc.getEntity() != null && !npc.getEntity().isDead()) {
                        updateNPCHologramForPlayer(npc, player);
                    }
                }
            }
        }
    }
    
    private void updateNPCHologramForPlayer(AbstractCustomEntityNPC npc, Player player) {
        List<Hologram> existingHolograms = playerNPCHolograms.get(player.getUniqueId()).get(npc.getEntity().getUniqueId());
        if (existingHolograms == null) return;
        
        List<String> newLines = npc.getNpcTextLine(player);
        if (existingHolograms.size() != newLines.size()) {
            existingHolograms.forEach(hologram -> {
                if (hologram.isSpawned()) {
                    hologram.deSpawn();
                }
            });
            createPersonalHologramForPlayer(npc, player);
        } else {
            for (int i = 0; i < newLines.size(); i++) {
                existingHolograms.get(i).setText(CC.translate(newLines.get(i)));
            }
        }
    }
    
    private void cleanupNPCHolograms(UUID npcEntityId) {
        playerNPCHolograms.values().forEach(playerHolograms -> {
            List<Hologram> holograms = playerHolograms.remove(npcEntityId);
            if (holograms != null) {
                holograms.forEach(hologram -> {
                    if (hologram.isSpawned()) {
                        hologram.deSpawn();
                    }
                });
            }
        });
    }

    private void cleanupWorldEntities() {
        try {
            Bukkit.getWorlds().forEach(world -> {
                
                List<Entity> entitiesToRemove = new ArrayList<>();

                for (Entity entity : world.getEntities()) {
                    if (!(entity instanceof Player) && 
                        !(entity instanceof org.bukkit.entity.Item) &&
                        !(entity instanceof org.bukkit.entity.ExperienceOrb)) {
                        entitiesToRemove.add(entity);
                    }
                }

                entitiesToRemove.forEach(Entity::remove);

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        entityToNPCMap.values().forEach(npc -> {
            if (npc.getEntity() != null && !npc.getEntity().isDead()) {
                npc.getEntity().remove();
            }
        });

        playerNPCHolograms.values().forEach(playerHolograms -> {
            playerHolograms.values().forEach(holograms -> {
                holograms.forEach(hologram -> {
                    if (hologram.isSpawned()) {
                        hologram.deSpawn();
                    }
                });
            });
        });

        entityToNPCMap.clear();
        playerNPCHolograms.clear();
    }


    @EventHandler
    public void onPlayerJoin(PitProfileLoadedEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
        if (player == null || !player.isOnline()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
            showNPCsToPlayer(player);
        }, 20L);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        AbstractCustomEntityNPC npc = getCustomNPC(entity);

        if (npc != null) {
            event.setCancelled(true);

            CustomEntityNPCInteractEvent interactEvent = new CustomEntityNPCInteractEvent(
                    event.getPlayer(), npc, entity
            );
            Bukkit.getPluginManager().callEvent(interactEvent);

            if (!interactEvent.isCancelled()) {
                npc.handlePlayerInteract(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        AbstractCustomEntityNPC npc = getCustomNPC(entity);

        if (npc != null && !npc.canTakeDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        AbstractCustomEntityNPC npc = getCustomNPC(entity);

        if (npc != null) {

            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cleanupPlayerHolograms(event.getPlayer());
        playerNPCHolograms.remove(event.getPlayer().getUniqueId());
    }
} 