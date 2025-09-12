package net.mizukilab.pit.events.impl;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.events.AbstractEvent;
import cn.charlotte.pit.events.trigger.type.INormalEvent;
import cn.charlotte.pit.events.trigger.type.addon.IScoreBoardInsert;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.Getter;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.menu.pack.PackageMenu;
import net.mizukilab.pit.util.DirectionUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.random.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/30 16:41
 */

public class CarePackageEvent extends AbstractEvent implements INormalEvent, Listener, IScoreBoardInsert {

    @Getter
    private Location chest;
    @Getter
    private ChestData chestData;


/*
    private final Cooldown endTimer = new Cooldown(5, TimeUnit.MINUTES);
*/

    @Override
    public String getEventInternalName() {
        return "care_package";
    }

    @Override
    public String getEventName() {
        return "空投";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    private Location generateLocation() {
        return RandomUtil.generateRandomLocation();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (ThePit.getInstance().getEventFactory().getActiveNormalEvent() == this) {
            final Hologram firstHologram = chestData.getFirstHologram();
            if (firstHologram != null) {
                firstHologram.spawn(Collections.singletonList(event.getPlayer()));
            }

            final Hologram secondHologram = chestData.getSecondHologram();
            if (secondHologram != null) {
                secondHologram.spawn(Collections.singletonList(event.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (ThePit.getInstance().getEventFactory().getActiveNormalEvent() == this) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST && event.getClickedBlock().getLocation().equals(chest)) {
                event.setCancelled(true);
                if (chestData == null) {
                    return;
                }
                if (chestData.getNum() <= 0) {
                    click(event.getPlayer(), chestData);
                    return;
                }
                if (chestData.isLeft()) {
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        click(event.getPlayer(), chestData);
                    }
                } else {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        click(event.getPlayer(), chestData);
                    }
                }
            }
        }
    }

    private void click(Player player, ChestData data) {
        Hologram secondHologram = data.getSecondHologram();
        if (data.getNum() <= 0) {
            new PackageMenu().openMenu(player);
            data.getFirstHologram().deSpawn();
            secondHologram.setText(CC.translate("&a&l右键开启"));
            return;
        }
        data.setNum(data.getNum() - 1);

        chest.getWorld().playSound(chest, Sound.ZOMBIE_WOODBREAK, 0.5F, 1.5F);

        data.setLeft(!data.isLeft());
        Hologram hologram = data.getFirstHologram();
        hologram.setText(CC.translate(Utils.randomColor().toString() + "&l" + data.getNum()));
        secondHologram.setText(CC.translate("&a&l" + (data.isLeft() ? "左键" : "右键") + "点击"));
    }

    @Override
    public void onActive() {
        final List<Location> locations = ThePit.getInstance().getPitConfig().getPackageLocations();
        if (locations.isEmpty()) {
            CC.boardCast0("&c警告! &6空投&7 坐标信息未配置, 请联系管理员");
            ThePit.getInstance().getEventFactory().inactiveEvent(this);
            return;
        }

        final Location location = locations.get(RandomUtil.random.nextInt(locations.size())).clone().getBlock().getLocation();

        Bukkit.getPluginManager()
                .registerEvents(this, ThePit.getInstance());
        location.getWorld().strikeLightningEffect(location);
        CC.boardCast0("&6&l空投! &7一个新的空投已在地图降落!打开可以获得神话物品,声望等稀有物资!");
        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            location.getBlock().setType(Material.CHEST);
        });
        chest = location;

        Map<Integer, ItemStack> items = PackageMenu.getItems();
        for (int i = 0; i < RandomUtil.random.nextInt(3) + 3; i++) {
            int nextInt = RandomUtil.random.nextInt(27);
            while (items.get(nextInt) != null && items.get(nextInt).getType() != Material.AIR) {
                nextInt = RandomUtil.random.nextInt(27);
            }
            items.put(nextInt, (ItemStack) RandomUtil.helpMeToChooseOne(
                    new MythicLeggingsItem().toItemStack(),
                    new MythicSwordItem().toItemStack(),
                    new MythicBowItem().toItemStack(),
                    new MythicSwordItem().toItemStack(),
                    new MythicBowItem().toItemStack()
            ));
        }

        for (int i = 0; i < RandomUtil.random.nextInt(3) + 3; i++) {
            int nextInt = RandomUtil.random.nextInt(27);
            while (items.get(nextInt) != null && items.get(nextInt).getType() != Material.AIR) {
                nextInt = RandomUtil.random.nextInt(27);
            }
            items.put(nextInt, new ItemBuilder(Material.GOLD_BLOCK).name("&e+2声望").internalName("renown_reward").shiny().build());
        }

        for (int i = 0; i < RandomUtil.random.nextInt(3) + 3; i++) {
            if (items.get(i) == null) {
                items.put(i, (ItemStack) RandomUtil.helpMeToChooseOne(new ItemBuilder(Material.EXP_BOTTLE)
                                .name("&b+1000经验值").internalName("xp_reward").shiny().build(),
                        new ItemBuilder(Material.GOLD_INGOT).name("&6+1000硬币").internalName("coin_reward").shiny().build()));
            }
        }

        chestData = new ChestData();
        chestData.setFirstHologram(HologramAPI.createHologram(location.getBlock().getLocation().clone().add(0.5, 2.4, 0.5), CC.translate("&a&l200")));
        chestData.setSecondHologram(HologramAPI.createHologram(location.getBlock().getLocation().clone().add(0.5, 2.0, 0.5), CC.translate("&a&l左键点击")));

        chestData.getFirstHologram().spawn();
        chestData.getSecondHologram().spawn();
    }


    @Override
    public void onInactive() {
        HandlerList.unregisterAll(this);

        chestData.getSecondHologram().deSpawn();
        chestData.getFirstHologram().deSpawn();
        PackageMenu.getItems().clear();
        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            chest.getBlock().setType(Material.AIR);
        });
        //we didnt have to set chestData = null;
    }

    @Override
    public List<String> insert(Player player) {
        if (chest == null) return null;

        List<String> lines = new ObjectArrayList<>();
        String targetDirection = DirectionUtil.getDetailedDirection(player, chest);
        int distance = (int) player.getLocation().distance(chest);

  /*      if (endTimer.getRemaining() > 2 * 60 * 1000L) {
            lines.add("&f剩余: &a" + TimeUtil.millisToTimer(endTimer.getRemaining()));
        } else if (endTimer.getRemaining() >= 60 * 1000L) {
            lines.add("&f剩余: &e" + TimeUtil.millisToTimer(endTimer.getRemaining()));
        } else {
            lines.add("&f剩余: &c" + TimeUtil.millisToTimer(endTimer.getRemaining()));
        }
*/
        if (!chestData.getRewarded().contains(player.getUniqueId())) {
            if (chestData.getNum() == 200) {
                lines.add("&f追踪: &c&l? &f" + distance + "m");

            } else if (chestData.getNum() > 0) {
                lines.add("&f追踪: &c&l" + targetDirection + " &f" + distance + "m");
            } else {
                lines.add("&f追踪: &a&l" + targetDirection + " &f" + distance + "m");
            }
        }

        return lines;
    }

    @Data
    public static class ChestData {

        private boolean left = true;
        private int num = 200;
        private Hologram firstHologram;
        private Hologram secondHologram;
        private List<UUID> rewarded = new ObjectArrayList<>();
    }
}
