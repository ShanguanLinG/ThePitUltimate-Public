package net.mizukilab.pit.movement;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.operator.IOperator;
import cn.hutool.core.collection.ConcurrentHashSet;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.mizukilab.pit.config.PitWorldConfig;
import net.mizukilab.pit.runnable.ProfileLoadRunnable;
import net.mizukilab.pit.util.BlockUtil;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.aabb.AABB;
import net.mizukilab.pit.util.chat.ActionBarUtil;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;
import spg.lgdev.handler.MovementHandler;
import xyz.upperlevel.spigot.book.BookUtil;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/16 23:49
 */
public class PlayerMoveHandler implements MovementHandler, Listener {

    private static final Set<Player> cantMoveList = new ConcurrentHashSet<>();
    private static final Map<Player, FlightData> flyingPlayers = new WeakHashMap<>();
    private static final Map<Player, Long> slimeCooldowns = new WeakHashMap<>();
    private static final long SLIME_COOLDOWN_MS = 3000; // 3秒冷却时间

    private static class FlightData {
        private final ArmorStand armorStand;
        private final Vector direction;
        private final BukkitTask task;
        private final long startTime;

        public FlightData(ArmorStand armorStand, Vector direction, BukkitTask task) {
            this.armorStand = armorStand;
            this.direction = direction;
            this.task = task;
            this.startTime = System.currentTimeMillis();
        }

        public ArmorStand getArmorStand() {
            return armorStand;
        }

        public Vector getDirection() {
            return direction;
        }

        public BukkitTask getTask() {
            return task;
        }

        public long getStartTime() {
            return startTime;
        }
    }

    public PlayerMoveHandler() {
        Bukkit.getServer().getPluginManager().registerEvents(this, ThePit.getInstance());
    }

    public static Set<Player> getCantMoveList() {
        return PlayerMoveHandler.cantMoveList;
    }

    public static void checkMove(Location to, Location from, Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        //when X/Z Loc change
        if (to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) {
            if (!profile.isLoaded() && ProfileLoadRunnable.getInstance() != null && ProfileLoadRunnable.getInstance().getCooldownMap() != null && ProfileLoadRunnable.getInstance().getCooldownMap().containsKey(player.getUniqueId())) {
                ActionBarUtil.sendActionBar1(player, "system", "&c正在加载您的游戏数据,如长时间等待请尝试重新进入...", 2);

                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 9999999, 1, false), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, -100, false), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, -100, false), true);
                return;
            }
        }
        if(profile != PlayerProfile.NONE_PROFILE) {
            profile.setLastActionTimestamp(System.currentTimeMillis());
            if (to.getBlockX() != from.getBlockX() ||
                    to.getBlockY() != from.getBlockY() ||
                    to.getBlockZ() != from.getBlockZ()) {

                if (profile.isScreenShare()) {
                    BookUtil.openPlayer(player,
                            BookUtil.writtenBook()
                                    .title(CC.translate("&c$screenShareRequest"))
                                    .author("KleeLoveLife")
                                    .pages(
                                            new BookUtil.PageBuilder()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&4&l您因疑似作弊而被冻结!"))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0请在 3 分钟 内添加以下QQ:"))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0QQ: " + profile.getScreenShareQQ()))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0如关闭客户端/超时未添加"))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0等拒绝查端的行为,"))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0账号会被封禁 30 天!"))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0如遇到问题,可在公屏向管理员求助."))
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );
                }
                if (profile.getWipedData() != null && !profile.getWipedData().isKnow()) {
                    BookUtil.openPlayer(player,
                            BookUtil.writtenBook()
                                    .title(CC.translate("&c$wipeNotification #" + player.getName()))
                                    .author("KleeLoveLife")
                                    .pages(
                                            new BookUtil.PageBuilder()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&0您因" + profile.getWipedData().getReason()))
                                                                    .build()
                                                    )
                                                    .newLine()
                                                    .add(
                                                            CC.translate("&0我们已清除您的存档")
                                                    )
                                                    .newLine()
                                                    .add(
                                                            CC.translate("&0希望您在未来的游戏中")
                                                    )
                                                    .newLine()
                                                    .add(
                                                            CC.translate("&0遵守我们的规则，谢谢")
                                                    ).newLine()
                                                    .add(
                                                            CC.translate("&0如有疑问，请在论坛中申诉")
                                                    )
                                                    .newLine()
                                                    .newLine()
                                                    .add(
                                                            BookUtil.TextBuilder
                                                                    .of(CC.translate("&a我已知晓"))
                                                                    .onHover(BookUtil.HoverAction.showText(CC.translate("&f点击不再提示")))
                                                                    .onClick(BookUtil.ClickAction.runCommand("/iKnowIGotWiped"))
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );
                }

                boolean isInArena = isInArena(to);

                profile.setInArena(isInArena);

                if (!profile.isEditingMode()) {
                    if (BlockUtil.isBlockNearby(player.getLocation(), 3) && player.getGameMode() == GameMode.ADVENTURE) {
                        player.setGameMode(GameMode.SURVIVAL);
                        return;
                    }
                    if (profile.isInArena()) {
                        if (player.getGameMode() == GameMode.ADVENTURE) {
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                    } else {
                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.ADVENTURE);
                        }
                    }
                }
                if (PlayerUtil.isStaffSpectating(player)) {
                    player.setAllowFlight(true);
                }
            }
            if (player.isOnGround()) {
                boolean backing = player.hasMetadata("backing");
                if (backing) {
                    if (to.getBlockX() != from.getBlockX() ||
                            to.getBlockY() != from.getBlockY() ||
                            to.getBlockZ() != from.getBlockZ()) {
                        player.removeMetadata("backing", ThePit.getInstance());
                        player.sendMessage(CC.translate("&c回城被取消."));
                    }
                }
                Material currentBlock = to.clone().add(0, -1, 0).getBlock().getType();
                if (currentBlock == Material.SLIME_BLOCK) {
                    float initialYaw = player.getLocation().getYaw();
                    float initialPitch = player.getLocation().getPitch();
                    launchPlayer(player, to, initialYaw, initialPitch);
                }
            }
        }
    }

    public static void launchPlayer(Player player, Location to, float initialYaw, float initialPitch) {
        if (to.clone().add(0, -1, 0).getBlock().getType() == Material.SLIME_BLOCK) {
            if (flyingPlayers.containsKey(player)) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            Long lastUse = slimeCooldowns.get(player);
            if (lastUse != null && (currentTime - lastUse) < SLIME_COOLDOWN_MS) {
                long remainingTime = SLIME_COOLDOWN_MS - (currentTime - lastUse);
                double remainingSeconds = remainingTime / 1000.0;
                ActionBarUtil.sendActionBar1(player, "slime_cooldown",
                        String.format("&c弹射冷却中... %.1f秒", remainingSeconds), 1);
                return;
            }

            slimeCooldowns.put(player, currentTime);
            Location spawnLoc = player.getLocation().clone();
            ArmorStand armorStand = player.getWorld().spawn(spawnLoc, ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(true);
            armorStand.setCanPickupItems(false);
            armorStand.setCustomNameVisible(false);
            armorStand.setRemoveWhenFarAway(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(false);
            armorStand.setSmall(true);
            armorStand.setPassenger(player);
            final Vector direction = player.getLocation().getDirection().normalize();
            direction.setY(Math.max(direction.getY(), 0.3));
            direction.multiply(2.2);
            if (direction.length() == 0 || Double.isNaN(direction.getX()) || Double.isNaN(direction.getY()) || Double.isNaN(direction.getZ())) {
                direction.setX(1.0);
                direction.setY(0.5);
                direction.setZ(0.0);
                direction.normalize().multiply(2.2);
            }
            armorStand.setVelocity(direction);
            player.playSound(player.getLocation(), Sound.EXPLODE, 1.5f, 0.8f);
            player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1.0f, 1.2f);
            PacketPlayOutWorldParticles explosionPacket = new PacketPlayOutWorldParticles(
                    EnumParticle.EXPLOSION_LARGE, true,
                    (float) to.getX(), (float) to.getY(), (float) to.getZ(),
                    0.3F, 0.3F, 0.3F, 0, 8
            );
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(explosionPacket);

            BukkitTask flightTask = new BukkitRunnable() {
                private int ticks = 0;
                private final int upwardFlightTicks = 35;
                private boolean isDescending = false;

                @Override
                public void run() {
                    ticks++;
   /*                 if (!armorStand.isValid() || armorStand.getPassenger() != player) {
                        endFlight(player);
                        return;
                    }*/
                    if (armorStand.getPassenger() != player) {
                        try {
                            armorStand.setPassenger(player);
                        } catch (Exception e) {
                            endFlight(player);
                            return;
                        }
                    }

                    Vector currentVelocity;

                    if (ticks <= upwardFlightTicks) {
                        double speedMultiplier = Math.max(0.4, 1.0 - (ticks * 0.02));
                        currentVelocity = direction.clone().multiply(speedMultiplier);
                        isDescending = false;
                    } else {
                        if (!isDescending) {
                            isDescending = true;
                        }

                        Vector currentVel = armorStand.getVelocity();
                        double horizontalMultiplier = Math.max(0.1, 0.8 - ((ticks - upwardFlightTicks) * 0.02));

                        currentVelocity = new Vector(
                                direction.getX() * horizontalMultiplier,
                                currentVel.getY(),
                                direction.getZ() * horizontalMultiplier
                        );

                    }
                    try {
                        if (player.isOnline()) {
                            armorStand.setVelocity(currentVelocity);
                        }else {
                            endFlight(player);
                        }
                    } catch (Exception e) {
                        endFlight(player);
                        return;
                    }
                    if (isDescending) {
                        try {
                            Location currentLoc = armorStand.getLocation();
                            Location groundCheck = currentLoc.clone().add(0, -1.5, 0);

                            if (groundCheck.getBlock().getType().isSolid()) {
                                double distanceToGround = currentLoc.getY() - groundCheck.getBlockY();
                                if (distanceToGround <= 2.0) {
                                    endFlight(player);
                                    return;
                                }
                            }
                        } catch (Exception e) {
                        }
                    }

                    try {
                        if (ticks % 3 == 0) {
                            Location loc = armorStand.getLocation();
                            EnumParticle particleType = isDescending ? EnumParticle.DRIP_WATER : EnumParticle.CLOUD;

                            PacketPlayOutWorldParticles trailPacket = new PacketPlayOutWorldParticles(
                                    particleType, true,
                                    (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
                                    0.2F, 0.2F, 0.2F, 0, isDescending ? 1 : 2
                            );
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(trailPacket);
                        }
                    } catch (Exception e) {
                    }
                }
            }.runTaskTimer(ThePit.getInstance(), 1L, 1L);
            flyingPlayers.put(player, new FlightData(armorStand, direction, flightTask));
        }
    }

    private static void endFlight(Player player) {
        FlightData flightData = flyingPlayers.remove(player);
        if (flightData != null) {
            try {
                if (flightData.getTask() != null) {
                    flightData.getTask().cancel();
                }
            } catch (Exception e) {
            }

            ArmorStand armorStand = flightData.getArmorStand();
            if (armorStand != null && armorStand.isValid()) {
                try {
                    Location landingLoc = armorStand.getLocation().clone();
                    armorStand.eject();
                    Location safeLandingLoc = findSafeLandingLocation(landingLoc);

                    if (player.isOnline()) {
                        player.setPassenger(null);
                        try {
                            player.playSound(player.getLocation(), Sound.FALL_BIG, 0.8f, 1.0f);
                        } catch (Exception e) {
                        }

                        try {
                            PacketPlayOutWorldParticles landingPacket = new PacketPlayOutWorldParticles(
                                    EnumParticle.CLOUD, true,
                                    (float) safeLandingLoc.getX(), (float) safeLandingLoc.getY(), (float) safeLandingLoc.getZ(),
                                    0.5F, 0.1F, 0.5F, 0, 8
                            );
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(landingPacket);
                        } catch (Exception e) {
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                if (armorStand.isValid()) {
                                    armorStand.remove();
                                }
                            } catch (Exception e) {
                            }
                        }
                    }.runTaskLater(ThePit.getInstance(), 5L);

                } catch (Exception e) {

                    player.sendMessage(CC.translate("&c处理出错: " + e.getMessage()));
                    try {
                        armorStand.remove();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (flyingPlayers.containsKey(player)) {
            endFlight(player);
        }
        slimeCooldowns.remove(player);
        cantMoveList.remove(player);
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player && event.getDismounted() instanceof ArmorStand) {
            Player player = (Player) event.getEntity();
            ArmorStand armorStand = (ArmorStand) event.getDismounted();
            if (flyingPlayers.containsKey(player)) {
                FlightData flightData = flyingPlayers.remove(player);
                flightData.getTask().cancel();
                armorStand.remove();
            }
        }
    }
    @EventHandler
    public void onArmorStandInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            if (flyingPlayers.containsKey(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (cantMoveList.contains(event.getPlayer())) {
            final Location to = event.getTo();
            final Location from = event.getFrom();

            if (to.getBlockX() != from.getBlockX() ||
                    to.getBlockY() != from.getBlockY() ||
                    to.getBlockZ() != from.getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void handleUpdateLocation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        checkMove(location, location1, player.getPlayer());
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {
        IOperator iOperator = ThePit.getInstance().getProfileOperator().getIOperator(player.getUniqueId());
        if(iOperator != null) {
            PlayerProfile profile = iOperator.profile();
            if (profile != null) {
                profile.setLastActionTimestamp(System.currentTimeMillis());
            }
        }
    }

    private static boolean isInArena(Location to) {
        PitWorldConfig config = ThePit.getInstance().getPitConfig();
        Location pitLocA = config.getPitLocA();
        Location pitLocB = config.getPitLocB();
        final AABB aabb = new AABB(pitLocA.getX(), pitLocA.getY(), pitLocA.getZ(), pitLocB.getX(), pitLocB.getY(), pitLocB.getZ());

        final AABB playerAABB = new AABB(to.getX(), to.getY(), to.getZ(), to.getX() + 0.8, to.getY() + 2, to.getZ() + 0.8);


        final boolean intersects = aabb.intersectsWith(playerAABB);

        return !intersects;
    }

    private static Location findSafeLandingLocation(Location landingLoc) {
        Location safeLoc = landingLoc.clone();
        for (int i = 0; i < 10; i++) {
            Location checkLoc = safeLoc.clone().add(0, -i, 0);

            try {
                if (checkLoc.getBlock().getType().isSolid()) {
                    Location groundLoc = checkLoc.clone().add(0, 1, 0);
                    if (!groundLoc.getBlock().getType().isSolid() &&
                            !groundLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                        return groundLoc;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        return landingLoc.clone().add(0, 1, 0);
    }
}
