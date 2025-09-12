package net.mizukilab.pit.events.impl.major;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PlayerInv;
import cn.charlotte.pit.event.PitPlayerSpawnEvent;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import cn.charlotte.pit.events.AbstractEvent;
import cn.charlotte.pit.events.trigger.type.IEpicEvent;
import cn.charlotte.pit.events.trigger.type.addon.IPreparative;
import cn.charlotte.pit.events.trigger.type.addon.IScoreBoardInsert;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BlockHeadEvent extends AbstractEvent implements IEpicEvent, Listener, IPreparative, IScoreBoardInsert {
    private final Map<UUID, PlayerBlockHeadData> dataMap = new HashMap<>();
    private final Map<UUID, Integer> rankMap = new HashMap<>();
    public final Map<UUID, PowerUP> pup = new HashMap<>();
    private static final List<Material> canBeUsed = Arrays.asList(Material.STONE, Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.WOOD, Material.BEDROCK
            , Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE
            , Material.LOG, Material.LAPIS_ORE,
            Material.LAPIS_BLOCK, Material.SANDSTONE, Material.NOTE_BLOCK, Material.BED_BLOCK, Material.PISTON_STICKY_BASE
            , Material.WOOL, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.BRICK, Material.TNT, Material.BOOKSHELF, Material.MOSSY_COBBLESTONE, Material.OBSIDIAN,
            Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.FURNACE, Material.BURNING_FURNACE, Material.REDSTONE_ORE, Material.ICE, Material.GLOWSTONE, Material.SMOOTH_BRICK, Material.MELON_BLOCK
            , Material.PUMPKIN_STEM, Material.MELON_STEM,
            Material.NETHER_BRICK, Material.ENDER_STONE, Material.EMERALD_ORE, Material.OBSIDIAN);
    private final Map<String, BlockData> oriBlock = new HashMap<>();
    private final List<Material> selectedMaterials = new ArrayList<>();
    private final List<Entity> entities = new ArrayList<>();
    private final Cooldown timer = new Cooldown(5L, TimeUnit.MINUTES);
    private int allblocks = 0;
    private static final Random random = new Random();


    @Override
    public String getEventInternalName() {
        return "block_head";
    }

    @Override
    public String getEventName() {
        return "&9&l方块划地战";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().getOrDefault("block_head",10);
    }

    @Override
    public void onPreActive() {

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (!profile.isLoaded()) {
            return;
        }

        PlayerBlockHeadData data = dataMap.get(player.getUniqueId());

        if (data != null) {
            if (System.currentTimeMillis() - get(player, "trail") < 15000) {
                if (!player.getLocation().add(0.0, -1.0, 0.0).getBlock().getType().equals(Material.AIR) && !player.getLocation().add(0.0, -1.0, 0.0).getBlock().getType().equals(Material.STATIONARY_WATER) && !player.getLocation().add(0.0, -1.0, 0.0).getBlock().getType().equals(Material.SNOW)) {
                    Location belowLocation = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY() - 1, player.getLocation().getZ());
                    if (oriBlock.containsKey(loc(belowLocation))) {
                        Player blockBelonger = oriBlock.get(loc(belowLocation)).getBelonger();
                        if (dataMap.containsKey(blockBelonger.getUniqueId())) {
                            dataMap.get(blockBelonger.getUniqueId()).belong--;
                        }
                        oriBlock.get(loc(belowLocation)).setBelonger(player);
                        data.belong++;
                        belowLocation.getBlock().setType(data.block);
                    } else {
                        BlockData blockData = new BlockData(belowLocation.getBlock().getType(), belowLocation.getBlock().getData(), player);
                        oriBlock.put(loc(belowLocation), blockData);
                        data.belong++;
                        allblocks++;
                        belowLocation.getBlock().setType(data.block);
                    }
                }
            }
        }
        entities.forEach(nearbyEntities -> {
            Location location = nearbyEntities.getLocation();
            if(player.getWorld() != location.getWorld()){
                return;
            }
            if (nearbyEntities.getType() != EntityType.ARMOR_STAND || nearbyEntities.isDead() || (location.distanceSquared(player.getLocation()) > 1 && location.subtract(0, -1, 0).distanceSquared(player.getLocation()) > 1)) {
                return;
            }
            ArmorStand stand = (ArmorStand) nearbyEntities;
            if (stand.getCustomName().contains("快速染径") && (System.currentTimeMillis() - get(player, "trail") > 15000)) {
                refresh(player, "trail");
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1));
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 100.0f, 1.0f);
                player.sendMessage(CC.translate("&9&l快速染径！ &7你获得了15秒的 &f速度II&7,以及你经过的地方将被你染色！"));
            }
            if (stand.getCustomName().contains("超级恢复") && (System.currentTimeMillis() - get(player, "heal") > 15000)) {
                refresh(player, "heal");
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 99999, 1));
                player.setHealth(player.getMaxHealth());
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 100.0f, 1.0f);
                player.sendMessage(CC.translate("&9&l超级恢复！ &7你获得了4颗心的额外生命!并且你的血量已恢复最大值"));
            }
            if (stand.getCustomName().contains("钻石套装") && (System.currentTimeMillis() - get(player, "diamond") > 15000)) {
                refresh(player, "diamond");
                PlayerInventory inventory = player.getInventory();
                ItemStack chestplate = inventory.getChestplate();
                ItemStack leggings = inventory.getLeggings();
                ItemStack boot = inventory.getBoots();
                inventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                inventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                inventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                if (!InventoryUtil.isInvFull(player, 2)) {
                    if (chestplate != null) {
                        inventory.addItem(chestplate);
                    }
                    if (leggings != null) {
                        inventory.addItem(leggings);
                    }
                    if (boot != null) {
                        inventory.addItem(boot);
                    }

                } else {
                    if (chestplate != null && ItemUtil.isMythicItem(chestplate)) {
                        inventory.setChestplate(chestplate);
                    }
                    if (leggings != null && ItemUtil.isMythicItem(leggings)) {
                        inventory.setLeggings(leggings);
                    }
                    if (boot != null && ItemUtil.isMythicItem(boot)) {
                        inventory.setBoots(boot);
                    }
                }
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 100.0f, 1.0f);
                player.sendMessage(CC.translate("&9&l钻石套装！ &7你已装备钻石套装！"));
            }
            if (stand.getCustomName().contains("战斗加成") && (System.currentTimeMillis() - get(player, "combat") > 15000)) {
                refresh(player, "combat");
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 100.0f, 1.0f);
                player.sendMessage(CC.translate("&9&l战斗加成！ &7击杀玩家后,方块爆炸的半径增加！"));
            }
            stand.remove();
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        PlayerBlockHeadData killerData = null;
        final PlayerBlockHeadData victimData = dataMap.get(victim.getUniqueId());
        if (victimData == null) {
            return;
        }
        if (killer != null) {
            killerData = dataMap.get(killer.getUniqueId());
        }
        Location killerLocation = killer.getLocation();
        int r = 4;
        if (System.currentTimeMillis() - get(killer, "combat") < 15000) {
            r = 6;
        }
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                Location blockLocation = new Location(killerLocation.getWorld(), killerLocation.getX() + x, killerLocation.getY() - 1, killerLocation.getZ() + z);
                if (!blockLocation.getBlock().getType().equals(Material.AIR) && !blockLocation.getBlock().getType().equals(Material.STATIONARY_WATER) && !blockLocation.getBlock().getType().equals(Material.SNOW)) {
                    if (oriBlock.containsKey(loc(blockLocation))) {
                        Player blockBelonger = oriBlock.get(loc(blockLocation)).getBelonger();
                        if (dataMap.containsKey(blockBelonger.getUniqueId())) {
                            dataMap.get(blockBelonger.getUniqueId()).belong--;
                        }
                        oriBlock.get(loc(blockLocation)).setBelonger(killer);
                        killerData.belong++;
                        blockLocation.getBlock().setType(killerData.block);
                    } else {
                        BlockData blockData = new BlockData(blockLocation.getBlock().getType(), blockLocation.getBlock().getData(), killer);
                        oriBlock.put(loc(blockLocation), blockData);
                        killerData.belong++;
                        allblocks++;
                        blockLocation.getBlock().setType(killerData.block);
                    }
                } else if (!blockLocation.add(0.0, -1.0, 0.0).getBlock().getType().equals(Material.AIR) && !blockLocation.add(0.0, -1.0, 0.0).getBlock().getType().equals(Material.STATIONARY_WATER) && !blockLocation.add(0.0, -1.0, 0.0).getBlock().getType().equals(Material.SNOW)) {
                    if (oriBlock.containsKey(loc(blockLocation.add(0.0, -1.0, 0.0)))) {
                        Player blockBelonger = oriBlock.get(loc(blockLocation.add(0.0, -1.0, 0.0))).getBelonger();
                        if (dataMap.containsKey(blockBelonger.getUniqueId())) {
                            dataMap.get(blockBelonger.getUniqueId()).belong--;
                        }
                        oriBlock.get(loc(blockLocation.add(0.0, -1.0, 0.0))).setBelonger(killer);
                        killerData.belong++;
                        blockLocation.add(0.0, -1.0, 0.0).getBlock().setType(killerData.block);
                    } else {
                        BlockData blockData = new BlockData(blockLocation.add(0.0, -1.0, 0.0).getBlock().getType(), blockLocation.add(0.0, -1.0, 0.0).getBlock().getData(), killer);
                        oriBlock.put(loc(blockLocation.add(0.0, -1.0, 0.0)), blockData);
                        killerData.belong++;
                        allblocks++;
                        blockLocation.add(0.0, -1.0, 0.0).getBlock().setType(killerData.block);
                    }
                } else if (!blockLocation.add(0.0, -2.0, 0.0).getBlock().getType().equals(Material.AIR) && !blockLocation.add(0.0, -2.0, 0.0).getBlock().getType().equals(Material.STATIONARY_WATER) && !blockLocation.add(0.0, -2.0, 0.0).getBlock().getType().equals(Material.SNOW)) {
                    if (oriBlock.containsKey(loc(blockLocation.add(0.0, -2.0, 0.0)))) {
                        Player blockBelonger = oriBlock.get(loc(blockLocation.add(0.0, -2.0, 0.0))).getBelonger();
                        if (dataMap.containsKey(blockBelonger.getUniqueId())) {
                            dataMap.get(blockBelonger.getUniqueId()).belong--;
                        }
                        oriBlock.get(loc(blockLocation.add(0.0, -2.0, 0.0))).setBelonger(killer);
                        killerData.belong++;
                        blockLocation.add(0.0, -2.0, 0.0).getBlock().setType(killerData.block);
                    } else {
                        BlockData blockData = new BlockData(blockLocation.add(0.0, -2.0, 0.0).getBlock().getType(), blockLocation.add(0.0, -2.0, 0.0).getBlock().getData(), killer);
                        oriBlock.put(loc(blockLocation.add(0.0, -2.0, 0.0)), blockData);
                        killerData.belong++;
                        allblocks++;
                        blockLocation.add(0.0, -2.0, 0.0).getBlock().setType(killerData.block);
                    }
                }
            }
        }

        World world = killer.getLocation().getWorld();
        world.createExplosion(killer.getLocation().getX(), killer.getLocation().getY(), killer.getLocation().getZ(), (float) 0, false, false);
        Location location = killer.getLocation().add(0.0, 2.0, 0.0);
        Vector vector = killer.getLocation().getDirection().multiply(6.5);
        if (vector.getY() > 2.0) {
            vector.setY(2.0);
        }
        FallingBlock fb = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb2 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb3 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb4 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb5 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb6 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb7 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        FallingBlock fb8 = location.getWorld().spawnFallingBlock(location, killerData.block, (byte) 0);
        double vector1 = 0.4;
        double vector2 = 0.3;
        fb.setVelocity(new Vector(vector1, 0.0, 0.0));
        fb2.setVelocity(new Vector(0.0, 0.0, vector1));
        fb3.setVelocity(new Vector(-vector1, 0.0, 0.0));
        fb4.setVelocity(new Vector(0.0, 0.0, -vector1));
        fb5.setVelocity(new Vector(vector2, 0.0, vector2));
        fb6.setVelocity(new Vector(-vector2, 0.0, vector2));
        fb7.setVelocity(new Vector(vector2, 0.0, -vector2));
        fb8.setVelocity(new Vector(-vector2, 0.0, -vector2));
        FallingBlock[] fallingBlocks = new FallingBlock[]{fb, fb2, fb3, fb4, fb5, fb6, fb7, fb8};
        for (FallingBlock fallingBlock : fallingBlocks) {
            fallingBlock.setDropItem(false);
        }
        if (victim.getInventory().getHelmet() == null) {
            Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> victim.getInventory().setHelmet(new ItemStack(victimData.block)), 10L);
        }
        pup.remove(victim.getUniqueId());
    }

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            fallingBlock.remove();
            event.setCancelled(true);
        }
    }

    private void sendPacket(Player player) {
        PacketPlayOutEntityEquipment packet;
        packet = new PacketPlayOutEntityEquipment(player.getEntityId(), 4, CraftItemStack.asNMSCopy(new ItemStack(dataMap.get(player.getUniqueId()).block)));
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) {
                continue;
            }
            ((CraftPlayer) target).getHandle()
                    .playerConnection
                    .sendPacket(packet);
        }
        //player.getInventory().setHelmet(new ItemStack(dataMap.get(player.getUniqueId()).block));
    }

    @Override
    public void onActive() {
        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            Bukkit.getPluginManager().registerEvents(this, ThePit.getInstance());
            selectedMaterials.clear();
            this.allblocks = 0;
            List<String> types = Arrays.asList("quicktrail", "combatboost", "superheal", "diamondarmor");

            for (Location location : ThePit.getInstance().getPitConfig().getBlockHeadLocations()) {
                entities.add(spawnPowerUp(location, types.get(random.nextInt(types.size()))));
            }

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.sendMessage(CC.translate("&5&l大型事件！ &9&l方块划地战 &7开始！"));
                if (PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).isLoaded()) {
                    newdata(player);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (BlockHeadEvent.this.timer.hasExpired()) {
                        this.cancel();
                        if (BlockHeadEvent.this.equals(ThePit.getInstance().getEventFactory().getActiveEpicEvent())) {
                            ThePit.getInstance().getEventFactory().inactiveEvent(BlockHeadEvent.this);
                        }
                    } else {
                        ThePit.getInstance()
                                .getBossBar()
                                .getBossBar()
                                .setTitle(CC.translate("&5&l大型事件! &6&l" + getEventName() + " &7将在 &a" + TimeUtil.millisToTimer(timer.getRemaining()) + "&7 后结束!"));
                        ThePit.getInstance()
                                .getBossBar()
                                .getBossBar()
                                .setProgress(timer.getRemaining() / (1000 * 60 * 5f));
                        List<Entity> curEnt = new ArrayList<>(entities.size());
                        Iterator<Entity> iterator = entities.iterator();
                        while (iterator.hasNext()) {
                            Entity entity = iterator.next();
                            if (entity.isDead()) {
                                iterator.remove();
                                curEnt.add(spawnPowerUp(entity.getLocation(), types.get(random.nextInt(types.size()))));
                            }
                        }
                        entities.addAll(curEnt);
                    }
                }
            }.runTaskTimer(ThePit.getInstance(), 20L, 20L);

        });
    }

    @Override
    public void onInactive() {
        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            ThePit.getInstance().getBossBar().getBossBar().setTitle("");
            HashSet<Map.Entry<UUID, PlayerBlockHeadData>> entry = new HashSet<>(dataMap.entrySet());
            List collect = entry.stream().map(Map.Entry::getValue).sorted(Comparator.comparingDouble(PlayerBlockHeadData::getBelong).reversed()).collect(Collectors.toList());
            int rank = 1;
            for (Object data : collect) {
                Player player = Bukkit.getPlayer(((PlayerBlockHeadData) data).uuid);
                if (player == null || !player.isOnline()) {
                    continue;
                }
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                if (!profile.isLoaded()) {
                    continue;
                }
                player.sendMessage(CC.GOLD + CC.CHAT_BAR);
                player.sendMessage(CC.translate("&6&l天坑事件结束: &c" + this.getEventName()));
                double rewardCoins = 0.0;
                int rewardRenown = 0;
                if (rank <= 3) {
                    rewardCoins += 2000.0;
                    rewardRenown += 2;
                } else if (rank <= 20) {
                    rewardCoins += 500.0;
                    ++rewardRenown;
                } else {
                    rewardCoins += 100.0;
                }
                if (allblocks >= 10000) {
                    rewardCoins = 1250;
                }
                if (ThePit.getInstance().getPitConfig().isGenesisEnable() && profile.getGenesisData().getTier() >= 5 && rewardRenown > 0) {
                    ++rewardRenown;
                }
                if (PlayerUtil.isPlayerUnlockedPerk(player, "self_confidence")) {
                    if (rank <= 5) {
                        rewardCoins += 5000.0;
                    } else if (rank <= 10) {
                        rewardCoins += 2500.0;
                    } else if (rank <= 15) {
                        rewardCoins += 1000.0;
                    }
                }
                profile.grindCoins(rewardCoins);
                profile.setCoins(profile.getCoins() + rewardCoins);
                profile.setRenown(profile.getRenown() + rewardRenown);
                //KingQuestData.addQuestRenown(player, rewardRenown);
                player.sendMessage(CC.translate("&6你的奖励: &6+" + rewardCoins + "硬币 &e+" + rewardRenown + "声望"));
                if (allblocks >= 10000) {
                    player.sendMessage(CC.translate("&6&l全局奖励: &a&l成功！ &7所有人额外获得1250硬币！"));
                } else {
                    player.sendMessage(CC.translate("&6&l全局奖励: &c&l失败！ &7全服只占领了" + allblocks + "个方块"));
                }
                player.sendMessage(CC.translate("&6&l你: &7共占领了 " + dataMap.get(player.getUniqueId()).belong + " 个方块！ &7(排名#" + getRank(player) + ")"));
                if (collect.size() >= 3) {
                    player.sendMessage(CC.translate("&6顶级玩家: "));
                    for (int i = 0; i < 3; ++i) {
                        Player top = Bukkit.getPlayer(((PlayerBlockHeadData) collect.get(i)).uuid);
                        if (top == null || !top.isOnline()) {
                            continue;
                        }
                        PlayerProfile topProfile = PlayerProfile.getPlayerProfileByUuid(top.getUniqueId());
                        player.sendMessage(CC.translate(" &e&l#" + (i + 1) + " " + topProfile.getFormattedName() + " &9共占领了 &e" + dataMap.get(top.getUniqueId()).belong + " &9个方块"));
                    }
                }
                player.sendMessage(CC.GOLD + CC.CHAT_BAR);
                ++rank;
            }
            allblocks = 0;
            HandlerList.unregisterAll(this);
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                profile.setTempInvUsing(false);
                PlayerBlockHeadData data = dataMap.get(player.getUniqueId());
                if (data != null) {
                    dataMap.remove(player.getUniqueId());
                }
                PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getInventory().applyItemToPlayer(player);
            }
            entities.removeIf(i -> {
                i.remove();
                return true;
            });
            for (String loc : oriBlock.keySet()) {
                Location location = toLoc(loc);
                BlockData blockData = oriBlock.get(loc);
                if (oriBlock.get(loc) != null) {
                    location.getBlock().setType(blockData.getType());
                    location.getBlock().setData(blockData.getData());
                }
            }
            oriBlock.clear();
            pup.clear();
        });
    }

    @Override
    public List<String> insert(Player player) {
        ArrayList<String> line = new ArrayList<>();
        line.add("&f剩余时间: &a" + TimeUtil.millisToTimer(this.timer.getRemaining()));
        PlayerBlockHeadData data = dataMap.get(player.getUniqueId());
        if (data == null) {
            line.add("&f已占领方块: &a0 &7(0.0%)");
            return line;
        }
        line.add("&f已占领方块: &a" + data.belong + "" +
                " &7(" + Math.round((Math.min((double) data.belong / this.allblocks, 1.0) * 100 * 10.0))
                / 10.0 + "%)");
        return line;
    }

    @EventHandler
    public void onSpawn(PitPlayerSpawnEvent event) {
        PlayerBlockHeadData data = dataMap.get(event.getPlayer().getUniqueId());
        PlayerProfile.getPlayerProfileByUuid(event.getPlayer().getUniqueId()).getInventory().applyItemToPlayer(event.getPlayer());
        sendPacket(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerBlockHeadData data = dataMap.get(player.getUniqueId());
        if (data == null) {
            return;
        }
        dataMap.remove(player.getUniqueId());
    }

    @EventHandler
    @SneakyThrows
    public void onProfileLoad(PitProfileLoadedEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
        newdata(player);
    }

    public int getRank(Player player) {
        PlayerBlockHeadData data = dataMap.get(player.getUniqueId());
        HashSet<Map.Entry<UUID, PlayerBlockHeadData>> entry = new HashSet<>(dataMap.entrySet());
        HashMap<Integer, Integer> rank = new HashMap<>();
        int i = 1;
        for (PlayerBlockHeadData en : entry.stream().map(Map.Entry::getValue).sorted(Comparator.comparingDouble(PlayerBlockHeadData::getBelong).reversed()).collect(Collectors.toList())) {
            rank.put(en.belong, i);
            ++i;
        }
        return rank.get(data.belong);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    public static class PlayerBlockHeadData {
        private UUID uuid;
        private String name;
        private Material block;
        private int belong;

        public double getBelong() {
            return this.belong;
        }
    }

    public void newdata(Player player) {
        PlayerBlockHeadData data;
        if (!dataMap.containsKey(player.getUniqueId())) {
            data = new PlayerBlockHeadData();
            data.uuid = player.getUniqueId();
            data.name = player.getName();
            dataMap.put(player.getUniqueId(), data);
        }
        data = dataMap.get(player.getUniqueId());
        data.belong = 0;
        data.block = pickRandomBlockMaterial();
        dataMap.put(player.getUniqueId(), data);

        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        profile.setInventory(PlayerInv.fromPlayerInventory(player.getInventory()));
        profile.setTempInvUsing(true);
        sendPacket(player);
    }

    @Getter
    public static class BlockData {
        private final Material type;
        private final byte data;
        @Setter
        private Player belonger;

        public BlockData(Material type, byte data, Player belonger) {
            this.type = type;
            this.data = data;
            this.belonger = belonger;
        }

    }

    public static class PowerUP {
        public UUID uuid;
        public long trail;
        public long combat;
        public long heal;
        public long diamond;

        public void setUUID(UUID uuid) {
            this.uuid = uuid;
        }

        public long get(String need) {
            switch (need) {
                case "trail":
                    return this.trail;
                case "combat":
                    return this.combat;
                case "heal":
                    return this.heal;
                case "diamond":
                    return this.diamond;
            }
            return this.trail;
        }

        public void refresh(String need) {
            switch (need) {
                case "trail":
                    this.trail = System.currentTimeMillis();
                    break;
                case "combat":
                    this.combat = System.currentTimeMillis();
                    break;
                case "heal":
                    this.heal = System.currentTimeMillis();
                    break;
                case "diamond":
                    this.diamond = System.currentTimeMillis();
                    break;
            }
        }

        public PowerUP(UUID uuid) {
            this.uuid = uuid;
            this.trail = 0;
            this.combat = 0;
            this.heal = 0;
            this.diamond = 0;
        }
    }

    public long get(Player player, String need) {
        pup.putIfAbsent(player.getUniqueId(), new PowerUP(player.getUniqueId()));
        PowerUP data = pup.get(player.getUniqueId());
        return data.get(need);
    }

    public void refresh(Player player, String need) {
        pup.putIfAbsent(player.getUniqueId(), new PowerUP(player.getUniqueId()));
        PowerUP data = pup.get(player.getUniqueId());
        data.refresh(need);
        pup.remove(player.getUniqueId());
        pup.put(player.getUniqueId(), data);
    }

    public Material pickRandomBlockMaterial() {
        if (selectedMaterials.size() == canBeUsed.size()) {
            selectedMaterials.clear();
        }

        List<Material> availableMaterials = new ArrayList<>(canBeUsed);
        availableMaterials.removeAll(selectedMaterials);

        int randomIndex = random.nextInt(availableMaterials.size());
        Material selectedMaterial = availableMaterials.get(randomIndex);
        selectedMaterials.add(selectedMaterial);

        return selectedMaterial;
    }

    public static ArmorStand spawnPowerUp(Location location, final String type) {
        location.setPitch(0.0f);
        location.setYaw(0.0f);
        final ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setCanPickupItems(false);
        stand.setVisible(false);
        switch (type) {
            case "quicktrail":
                stand.setCustomName("§e§l快速染径");
                break;
            case "combatboost":
                stand.setCustomName("§c§l战斗加成");
                break;
            case "superheal":
                stand.setCustomName("§a§l超级恢复");
                break;
            case "diamondarmor":
                stand.setCustomName("§b§l钻石套装");
                break;
        }
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta sm = (SkullMeta) is.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        switch (type) {
            case "quicktrail":
                profile.getProperties().put("textures", new Property("texture", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMDFiYTI4MTNkYWJlNTVjMWM1OGYyNThiNDNlOTMxNWViYjFiZTlhZTI1NmRiZmFhYjY2YWVhZjdhODA0ZSJ9fX0="));
                break;
            case "combatboost":
                profile.getProperties().put("textures", new Property("texture", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIxODY4ZmFjZjQ5MTk1YWQzM2I4MTBlZDgwNmI0MTUwZmYwY2IxOTUzNDI4NTIxYTZhYzg3NjM3Y2M4NDViIn19fQ=="));
                break;
            case "superheal":
                profile.getProperties().put("textures", new Property("texture", "ewogICJ0aW1lc3RhbXAiIDogMTY3MDY3MzA1NjI1NSwKICAicHJvZmlsZUlkIiA6ICI5ZTI3MGUwNzc1ZDg0OWI5Yjk2OTlmMzU3YjVlZjc4NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJBcHBsaW5ncyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZTg0MDg4OWRjODM3NTc3ZjI1YTM2OWNkZmU4OTU2ZDI4ODgzYzk2NzIzYWM5OTQ5ZmMyZGExZjUwZjJhMWNhIgogICAgfQogIH0KfQ=="));
                break;
            case "diamondarmor":
                profile.getProperties().put("textures", new Property("texture", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM0M2Q2NGQ3YjYwNWQ1MWM1ZWE3Y2I0Y2M2N2NiOTQwNGM4YmU0ZGNiMjk3OWI0MmVlZDExOGFjMjg2NDJkMSJ9fX0="));
                break;
        }
        try {
            Field profileField = sm.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(sm, profile);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        is.setItemMeta(sm);
        if (!"diamondarmor".equals(type)) {
            stand.setHelmet(is);
        } else {
            stand.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        }
        return stand;
    }

    public String loc(Location loc) {
        return loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();
    }

    public Location toLoc(String coordinates) {
        String[] parts = coordinates.split(",");
        if (parts.length != 3) {
            return null;
        }
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        return new Location(Bukkit.getWorld("world"), x, y, z);
    }
}