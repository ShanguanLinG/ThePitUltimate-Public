package net.mizukilab.pit.runnable;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import cn.hutool.core.collection.ConcurrentHashSet;
import io.irina.backports.utils.SWMRHashTable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.SneakyThrows;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 17:33
 */
@Skip
public class BountyRunnable extends BukkitRunnable {

    private final Random random = new Random();
    private static final SWMRHashTable<UUID, AnimationData> animationDataMap = new SWMRHashTable<>();

    public static Map<UUID, AnimationData> getAnimationDataMap() {
        return animationDataMap;
    }

    public void gc(Set<HologramDisplay> sets) {
        sets.removeIf(holo -> {
            if (!holo.hologram.isSpawned()) {
                return false;
            }
            holo.hologram.deSpawn();
            return true;
        });
    }

    @Override
    public void run() {
        Set<UUID> shouldRemove = new ObjectOpenHashSet<>();
        animationDataMap.forEach((i, a) -> {
            Player player = Bukkit.getPlayer(i);
            if (player == null || !player.isOnline()) {
                gc(a.holograms);
                if (a.holograms.isEmpty()) {
                    shouldRemove.add(i);
                }
            }
        });
        shouldRemove.forEach(animationDataMap::remove);
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uniqueId = player.getUniqueId();
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(uniqueId);
            if (profile.getBounty() >= 500 || profile.getBounty() < 0) {
                animationDataMap.putIfAbsent(uniqueId, new AnimationData());
                String color = profile.bountyColor();
                playAnimation(player, profile.getBounty(), color);
            } else {
                AnimationData animationData = animationDataMap.get(uniqueId);
                if (animationData != null) {
                    gc(animationData.holograms);
                }
            }
        }
    }


    @SneakyThrows
    private void playAnimation(Player player, int bounty, String color) {
        AnimationData animationData = animationDataMap.get(player.getUniqueId());
        Set<HologramDisplay> holograms = animationData.holograms;

        if (holograms.size() < 3) {
            Location playerLocation = player.getLocation();
            double x = generatorLocDouble();
            double z = generatorLocDouble();
            Hologram newHologram = HologramAPI
                    .createHologram(playerLocation.clone().add(x, 0.1, z)
                            , CC.translate(color + "&l" + bounty + "g"));

            List<Player> reviewers = new ObjectArrayList<>(Bukkit.getOnlinePlayers());
            if (!player.hasPermission("pit.admin")) {
                reviewers.remove(player);
            }
            reviewers.removeIf(
                    target -> PlayerProfile.getPlayerProfileByUuid(target.getUniqueId())
                            .getPlayerOption().isBountyHiddenWhenNear()
                            && PlayerUtil.getDistance(target, player) < 8);

            newHologram.spawn(reviewers);
            holograms.add(new HologramDisplay(newHologram, x, z));

            animationData.spawnCooldown = new Cooldown(650);
        }
        holograms.removeIf(hologram -> {
            if (System.currentTimeMillis() > hologram.endTime) {
                hologram.hologram.deSpawn();
                return true;
            } else {
                Location location = player.getLocation().clone();
                location.setX(location.getX() + hologram.boostX);
                Hologram hologram1 = hologram.getHologram();
                Location location1 = hologram1.getLocation();
                location.setY(location1.getY() + (0.05 * Math.max(1,NewConfiguration.INSTANCE.getBountyTickInterval())));
                location.setZ(location.getZ() + hologram.boostZ);
                hologram1.setLocation(location);
                return false;
            }
        });
    }

    private double generatorLocDouble() {
        if (random.nextBoolean()) {
            return random.nextDouble();
        } else {
            return -random.nextDouble();
        }
    }

    public static class AnimationData {

        private final Set<HologramDisplay> holograms;
        private Cooldown spawnCooldown;

        public AnimationData() {
            this.holograms = new ConcurrentHashSet<>();
            this.spawnCooldown = new Cooldown(0);
        }

        public Set<HologramDisplay> getHolograms() {
            return holograms;
        }

        public Cooldown getSpawnCooldown() {
            return spawnCooldown;
        }
    }

    @Getter
    public static class HologramDisplay {

        private final Hologram hologram;
        private final double boostX;
        private final double boostZ;
        private final long endTime;

        public HologramDisplay(Hologram hologram, double boostX, double boostZ) {
            this.hologram = hologram;
            this.boostX = boostX;
            this.boostZ = boostZ;
            this.endTime = System.currentTimeMillis() + 2000;
        }
    }
}
