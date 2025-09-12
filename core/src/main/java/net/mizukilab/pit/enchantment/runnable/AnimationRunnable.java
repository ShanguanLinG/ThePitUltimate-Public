package net.mizukilab.pit.enchantment.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.config.PitWorldConfig;
import net.mizukilab.pit.enchantment.menu.MythicWellMenu;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.util.ParticleUtil;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Menu;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/11 21:10
 */
@Getter
@Skip
public class AnimationRunnable extends BukkitRunnable {

    private final Map<UUID, AnimationData> animations = new ConcurrentHashMap<>();
    private final List<Location> animationLocations;

    public AnimationRunnable() {
        final PitWorldConfig pitWorldConfig = ThePit.getInstance().getPitConfig();
        final Location loc = pitWorldConfig.getEnchantLocation();
        final Location center;
        if (loc == null) {
            center = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        } else {
            center = loc.clone().add(0.0, -1.0, 0.0);
        }
        this.animationLocations = Arrays.asList(
                center.clone().add(-1, 0, 0),   // 19: 左中
                center.clone().add(-1, 0, -1),  // 10: 左上
                center.clone().add(0, 0, -1),   // 11: 中上
                center.clone().add(1, 0, -1),   // 12: 右上
                center.clone().add(1, 0, 0),    // 21: 右中
                center.clone().add(1, 0, 1),    // 30: 右下
                center.clone().add(0, 0, 1),    // 29: 中下
                center.clone().add(-1, 0, 1)    // 28: 左下
        );


        this.runTaskTimerAsynchronously(ThePit.getInstance(), 1, 1);

    }

    @Override
    public void run() {
        synchronized (animations) {
            final Object2ObjectOpenHashMap<UUID, AnimationData> removeMap = new Object2ObjectOpenHashMap<>(animations);
            if (!NewConfiguration.INSTANCE.getRapidEnchanting()) {
                removeMap.forEach((uuid, animationData) -> {
                    // 如果玩家不在线，直接移除
                    if (!animationData.getPlayer().isOnline()) {
                        animations.remove(uuid);
                        return;
                    }

                    // 如果正在附魔中，不要移除动画数据，即使GUI暂时不是MythicWellMenu
                    if (animationData.isStartEnchanting() && !animationData.isFinished()) {
                        return;
                    }

                    // 只有在非附魔状态下且不在MythicWellMenu中时才移除
                    if (!(Menu.currentlyOpenedMenus.get(animationData.getPlayer().getName()) instanceof MythicWellMenu)) {
                        animations.remove(uuid);
                    }
                });
            } else {
                removeMap.forEach((uuid, animationData) -> {
                    if (!animationData.getPlayer().isOnline() ||
                            !(Menu.currentlyOpenedMenus.get(animationData.getPlayer().getName()) instanceof MythicWellMenu)) {
                        animations.remove(uuid);
                    }
                });
            }

            for (AnimationData data : animations.values()) {
                data.animationGlobalTick++;
                Menu menu = Menu.currentlyOpenedMenus.get(data.getPlayer().getName());

                if (data.isFinished()) {
                    Player player = data.getPlayer();
                    PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                    String mythicColor = ItemUtil.getItemStringData(InventoryUtil.deserializeItemStack(profile.getEnchantingItem()), "mythic_color");
                    MythicColor foundColor = null;
                    for (MythicColor color : MythicColor.values()) {
                        if (color.getInternalName().equals(mythicColor)) {
                            foundColor = color;
                            break;
                        }
                    }
                    if (foundColor == null) {
                        continue;
                    }

                    for (Location location : animationLocations) {
                        player.sendBlockChange(location, Material.STAINED_GLASS, foundColor.getColorByte());
                    }
                    continue;
                }

                if (data.isStartEnchanting()) {
                    Player player = data.getPlayer();
                    PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                    String mythic_color = ItemUtil.getItemStringData(InventoryUtil.deserializeItemStack(profile.getEnchantingItem()), "mythic_color");
                    MythicColor foundColor = null;
                    for (MythicColor color : MythicColor.values()) {
                        if (color.getInternalName().equals(mythic_color)) {
                            foundColor = color;
                            break;
                        }
                    }
                    if (foundColor == null) {
                        continue;
                    }
                    handleEnchantingAnimation(data, player, foundColor, menu);
                } else {
                    if (data.animationTick % 4 != 0) {
                        data.animationTick++;
                        continue;
                    }

                    int realTick = (data.animationTick / 4) % 8;

                    if (menu instanceof MythicWellMenu) {
                        menu.openMenu(data.getPlayer());
                    }

                    if (data.animationTick > 0) {
                        int prevIndex = (realTick - 1 + 8) % 8;
                        data.player.sendBlockChange(animationLocations.get(prevIndex), Material.STAINED_GLASS, (byte) 0);
                    }

                    Location location = animationLocations.get(realTick);
                    data.player.sendBlockChange(location, Material.STAINED_GLASS, data.color);
                }
                data.animationTick++;
            }
        }
    }

    private void handleEnchantingAnimation(AnimationData data, Player player, MythicColor foundColor, Menu menu) {
        int tick = data.animationTick;
        if (tick <= 11) {
            byte[] colors = {14, 1, 4, 5, 11, 10, 6, 0};
            byte currentColor = colors[tick % colors.length];

            for (Location location : animationLocations) {
                player.sendBlockChange(location, Material.STAINED_GLASS, currentColor);
            }
            if (tick % 3 == 0) {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.5F + (tick / 3) * 0.1F);
                spawnParticles(player, "SPELL_MOB", foundColor);
            }
        } else if (tick <= 35) {
            int rotationIndex = ((tick - 12) / 3) % 8;
            for (Location location : animationLocations) {
                player.sendBlockChange(location, Material.STAINED_GLASS, (byte) 0);
            }
            player.sendBlockChange(animationLocations.get(rotationIndex), Material.STAINED_GLASS, foundColor.getColorByte());

            if ((tick - 12) % 3 == 0) {
                float pitch = 0.5F + ((tick - 12) / 3) * 0.15F;
                player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, pitch);
                spawnSpiralParticles(player, foundColor);
            }
        } else if (tick <= 59) {
            int burstIndex = ((tick - 36) / 3) % 8;

            for (int i = 0; i <= burstIndex; i++) {
                player.sendBlockChange(animationLocations.get(i), Material.STAINED_GLASS, foundColor.getColorByte());
            }

            if ((tick - 36) % 3 == 0) {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.0F + burstIndex * 0.1F);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5F, 2.0F);
                spawnMagicParticles(player, foundColor);
            }

            if (tick == 59) {
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1.2F);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 0.5F, 1.5F);
                spawnFinalMagicParticles(player, foundColor);
            }
        } else {
            if (!data.finished) {
                data.finished = true;

                for (Location location : animationLocations) {
                    player.sendBlockChange(location, Material.STAINED_GLASS, foundColor.getColorByte());
                }

                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1.0F);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2.0F);
                spawnFinalBurstParticles(player, foundColor);

                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                String currentItemStr = profile.getEnchantingItem();
                if (currentItemStr != null) {
                    ItemStack currentItem = InventoryUtil.deserializeItemStack(currentItemStr);
                    if (currentItem != null && currentItem.getType() != Material.AIR) {
                        profile.setEnchantingItem(InventoryUtil.serializeItemStack(currentItem));
                    }
                }

                Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                    if (player.isOnline()) {
                        Menu currentMenu = Menu.currentlyOpenedMenus.get(player.getName());
                        if (currentMenu instanceof MythicWellMenu) {
                            currentMenu.openMenu(player);
                        }
                    }
                });

            }
            return;
        }
        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            if (player.isOnline()) {
                Menu currentMenu = Menu.currentlyOpenedMenus.get(player.getName());
                if (currentMenu instanceof MythicWellMenu) {
                    currentMenu.openMenu(player);
                }
            }
        });
    }

    private void spawnParticles(Player player, String particleType, MythicColor color) {
        Location center = player.getLocation().add(0, 1, 0);
        int[] rgb = ParticleUtil.getColorFromMythicColor(color.getInternalName());
        ParticleUtil.createMagicCircleParticles(player, center, 1.5, rgb[0], rgb[1], rgb[2]);
        player.playSound(center, Sound.FIZZ, 0.5F, 1.5F);
    }

    private void spawnSpiralParticles(Player player, MythicColor color) {
        Location center = player.getLocation().add(0, 1, 0);
        int[] rgb = ParticleUtil.getColorFromMythicColor(color.getInternalName());

        ParticleUtil.createSpiralParticles(player, center, rgb[0], rgb[1], rgb[2]);
        player.playSound(player.getLocation(), Sound.PORTAL, 0.3F, 2.0F);
    }

    private void spawnMagicParticles(Player player, MythicColor color) {
        Location center = player.getLocation().add(0, 1, 0);
        int[] rgb = ParticleUtil.getColorFromMythicColor(color.getInternalName());

        ParticleUtil.createMagicParticles(player, center, rgb[0], rgb[1], rgb[2]);
        player.playSound(player.getLocation(), Sound.PORTAL, 0.3F, 1.5F);
    }

    private void spawnFinalMagicParticles(Player player, MythicColor color) {
        Location center = player.getLocation().add(0, 1, 0);
        int[] rgb = ParticleUtil.getColorFromMythicColor(color.getInternalName());

        ParticleUtil.createSpiralParticles(player, center, rgb[0], rgb[1], rgb[2]);
        player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE, 1.0F, 1.5F);
    }


    private void spawnFinalBurstParticles(Player player, MythicColor color) {
        Location center = player.getLocation().add(0, 1, 0);
        int[] rgb = ParticleUtil.getColorFromMythicColor(color.getInternalName());
        ParticleUtil.createFinalBurstParticles(player, center, rgb[0], rgb[1], rgb[2]);
        player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1.0F, 1.0F);
        player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE, 1.0F, 1.5F);
    }


    public void sendReset(Player player) {
        for (Location location : animationLocations) {
            Block block = location.getBlock();
            player.sendBlockChange(location, block.getType(), block.getData());
        }
    }

    public void sendStart(Player player) {
        for (Location location : animationLocations) {
            player.sendBlockChange(location, Material.STAINED_GLASS, (byte) 0);
        }
    }

    @Getter
    @Setter
    public static class AnimationData {

        private final Player player;
        private int animationGlobalTick = 0;
        private int animationTick = 0;
        private byte color = (byte) 6;
        private boolean startEnchanting = false;
        private boolean finished = false;

        public void reset() {
            animationTick = 0;
            startEnchanting = false;
            finished = false;
        }

        public AnimationData(Player player) {
            this.player = player;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnimationData data = (AnimationData) o;
            return Objects.equals(player.getUniqueId(), data.player.getUniqueId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(player.getUniqueId());
        }
    }
}
