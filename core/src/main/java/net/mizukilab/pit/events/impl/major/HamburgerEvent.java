package net.mizukilab.pit.events.impl.major;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.event.PitKillEvent;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import cn.charlotte.pit.events.trigger.type.IEpicEvent;
import cn.charlotte.pit.events.AbstractEvent;
import cn.charlotte.pit.events.trigger.type.addon.IScoreBoardInsert;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.schematic.SchematicFormat;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.medal.impl.challenge.PizzaEventMedal;
import net.mizukilab.pit.runnable.ClearRunnable;
import net.mizukilab.pit.util.NameUtils;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.chat.MessageType;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.random.RandomUtil;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/13 11:48
 */
@Getter
public class HamburgerEvent extends AbstractEvent implements IEpicEvent, Listener, IScoreBoardInsert {

    private final static String skinValue;

    static {
        skinValue = "eyJ0aW1lc3RhbXAiOjE0NzkxODY3Mjg0MTMsInByb2ZpbGVJZCI6ImYzYjU2YWJiNmM1YTQ2YTM4YTZlMTdiNmFjN2IxMGMzIiwicHJvZmlsZU5hbWUiOiJmb29kIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iMGUzOGMxNzZkYmY3ZGY5YjA2MzJjMjU2ZWViNmM1YWFjYTk5ZTFjOGMxYTUzMDY1NmVhZmYwNDE3YWVkMjIifX19";
    }

    private final Map<UUID, PizzaData> pizzaDataMap;
    private final ItemStack pizza;
    private final Map<UUID, VillagerData> villagerDataMap;
    private final List<Location> spawnedLocations;
    private Location location;
    private Villager villager;
    private Hologram hologram;
    private int done;
    private boolean end;
    private BukkitRunnable runnable;
    private Cooldown timer = new Cooldown(5, TimeUnit.MINUTES);
    private EditSession session;

    @SneakyThrows
    public HamburgerEvent() {
        this.pizzaDataMap = new HashMap<>();
        this.villagerDataMap = new HashMap<>();
        this.spawnedLocations = new ArrayList<>();
        this.pizza = new ItemBuilder(Material.SKULL_ITEM)
                .durability(3)
                .internalName("ham")
                .removeOnJoin(true)
                .name("&c汉堡!")
                .lore("&7请将这些汉堡配送到附近的村民手里...", "&7&m配送要求:", "  &7&m不可飞行,不可攀爬,不可冲刺,不可沾染元素气息", "")
                .setSkullProperty(skinValue)
                .build();
    }

    public static void redo(Player player, Location location) {
        player.sendBlockChange(location.clone().add(0, -1, 0), location.clone().add(0, -1, 0).getBlock().getType(), location.clone().add(0, -1, 0).getBlock().getData());
        player.sendBlockChange(location.clone().add(0, -2, 0), location.clone().add(0, -2, 0).getBlock().getType(), location.clone().add(0, -2, 0).getBlock().getData());
    }

    @Override
    public String getEventInternalName() {
        return "ham";
    }

    @Override
    public String getEventName() {
        return "天坑外卖";
    }

    @Override
    public int requireOnline() {
        return NewConfiguration.INSTANCE.getEventOnlineRequired().get(getEventInternalName());
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.SKULL) {
                final Skull skull = (Skull) event.getClickedBlock().getState();
                if (skull.getSkullType() == SkullType.PLAYER) {
                    Location location1 = event.getClickedBlock().getLocation();
                    if (location.getWorld() != location1.getWorld()) {
                        return;
                    }
                    if (location.distance(location1) <= 3) {
                        final Player player = event.getPlayer();
                        InventoryUtil.removeItemWithInternalName(player, "ham");

                        for (int i = 0; i < 6; i++) {
                            player.getInventory().addItem(new ItemBuilder(pizza)
                                    .dontStack()
                                    .build());
                        }

                        final PizzaData playerData = pizzaDataMap.get(player.getUniqueId());
                        playerData.hamburger = 6;

                        player.sendMessage(CC.translate("&6&l汉堡! &7你已领取汉堡,请将其送至周围的村民手上以获取现金."));
                        player.sendMessage(CC.translate("&6&l汉堡! &7将获取的现金交给此处的村民以完成订单."));
                    }
                }
            }
        }
    }

    @Override
    public void onActive() {

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer.hasExpired()) {
                    cancel();
                    if (HamburgerEvent.this.equals(ThePit.getInstance().getEventFactory().getActiveEpicEvent())) {
                        ThePit.getInstance()
                                .getEventFactory()
                                .inactiveEvent(HamburgerEvent.this);
                    }
                }

                Bukkit.getOnlinePlayers().forEach(player -> {
                    PlayerProfile.getPlayerProfileByUuid(player.getUniqueId())
                            .setMoveSpeed(0.2F * 1.6F);
                });
            }
        };
        this.runnable.runTaskTimer(ThePit.getInstance(), 20, 20);

        Bukkit.getPluginManager().registerEvents(this, ThePit.getInstance());

        Bukkit.getOnlinePlayers().forEach(player -> {
            this.pizzaDataMap.put(player.getUniqueId(), new PizzaData(player.getUniqueId(), player.getDisplayName()));
        });


        BukkitWorld world = new BukkitWorld(Bukkit.getWorlds().get(0));
        this.location = ThePit.getInstance().getPitConfig().getHamburgerOfferNpcLocA(); //villager location

        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            final Collection<Player> players = PlayerUtil.getNearbyPlayers(location, 5);
            final List<Location> locations = ThePit.getInstance().getPitConfig().getSpawnLocations();

            for (Player player : players) {
                player.teleport(locations.get(RandomUtil.random.nextInt(locations.size())));
                player.sendMessage(CC.translate("&c为了保证您的安全，我们已将您传送回出生点"));
            }

            ClearRunnable.getClearRunnable().getPlacedBlock().removeIf((i, blockData) -> {
                if (blockData.getLocation().distance(location) <= 5) {
                    blockData.getLocation().getBlock().setType(Material.AIR);
                    return true;
                }
                return false;
            });
        });

        FaweAPI.getTaskManager().async(() -> {
            try {
                final InputStream inputStream = ThePit.getInstance().getClass().getClassLoader().getResourceAsStream("hamburger.schematic");

                BlockVector vector = new BlockVector(location.getX(), location.getY(), location.getZ());

                session = FaweAPI.getEditSessionBuilder(world).build();

                final MCEditSchematicFormat mcedit = (MCEditSchematicFormat) SchematicFormat.MCEDIT;

                final CuboidClipboard clipboard = mcedit.load(inputStream);


                clipboard.paste(session, vector, false);

                session.flushQueue();

                Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                    try {
                        this.villager = (Villager) Bukkit.getWorlds().get(0).spawnEntity(location, EntityType.VILLAGER);
                        final EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
                        NBTTagCompound tag = entityVillager.getNBTTag();
                        if (tag == null) {
                            tag = new NBTTagCompound();
                        }
                        entityVillager.c(tag);
                        tag.setInt("NoAI", 1);
                        entityVillager.f(tag);
                        villager.setAdult();

                        hologram = HologramAPI.createHologram(location.clone().add(0, 2.4, 0), CC.translate("&d请给我钱"));
                        hologram.spawn();
                        hologram.setAttachedTo(villager);
                    } catch (Exception e) {
                        Bukkit.getOnlinePlayers()
                                .forEach(player -> {
                                    CC.printError(player, e);
                                });
                    }
                });
            } catch (Exception e) {
                Bukkit.getOnlinePlayers()
                        .forEach(player -> {
                            CC.printError(player, e);
                        });
            }
        });


        for (int i = 0; i < 20; i++) {
            this.spawnVillager();
        }
    }

    @EventHandler
    public void onKill(PitKillEvent event) {
        final PizzaData data = this.pizzaDataMap.get(event.getKiller().getUniqueId());
        if (data == null) {
            return;
        }
        final PizzaData targetData = this.pizzaDataMap.get(event.getTarget().getUniqueId());
        if (targetData == null) {
            return;
        }

        data.money += targetData.money;
        targetData.money = 0;

        data.hamburger += targetData.hamburger;
        for (int i = 0; i < targetData.hamburger; i++) {
            event.getKiller().getInventory().addItem(new ItemBuilder(pizza)
                    .build());
        }

        if (targetData.hamburger > 0 || targetData.money > 0) {
            CC.send(MessageType.EVENT, event.getKiller(), "&6&l天坑外卖! &7你通过击杀一名玩家夺取了 &6" + targetData.money + "$ &7.");
        }

        targetData.hamburger = 0;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.SKULL) {
            event.setCancelled(true);
        }
        if (event.getBlockPlaced().getLocation().distance(location) < 8) {
            ClearRunnable.getClearRunnable().placeBlock(event.getBlock().getLocation(), new Cooldown(8, TimeUnit.SECONDS));
        }
    }

    @Override
    public void onInactive() {
        end = true;
        HandlerList.unregisterAll(this);
        this.runnable.cancel();

        hologram.deSpawn();
        villager.remove();

        FaweAPI.getTaskManager().async(() -> {
            session.undo(session);
            session.flushQueue();
        });

        for (Map.Entry<UUID, VillagerData> entry : this.villagerDataMap.entrySet()) {
            entry.getValue().remove(this);
        }

        final List<PizzaData> list = pizzaDataMap.values()
                .stream()
                .sorted(Comparator.comparingInt(value -> {
                    final PizzaData data = (PizzaData) value;
                    return data.paidMoney;
                }).reversed())
                .toList();

        Map<UUID, Integer> rankMap = new HashMap<>();
        int rankNumber = 0;
        for (PizzaData data : list) {
            rankMap.put(data.uuid, rankNumber);
            rankNumber++;
        }

        CC.boardCast(CC.CHAT_BAR);
        CC.boardCast("&6&l天坑事件结束: " + this.getEventName() + "&6&l!");

        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryUtil.removeItemWithInternalName(player, "ham");

            final int rank = rankMap.get(player.getUniqueId()) + 1;
            final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            final PizzaData data = pizzaDataMap.get(player.getUniqueId());

            profile.setMoveSpeed(0.2F);

            double rewardCoins = 0;
            int rewardRenown = 0;
            if (rank <= 3) {
                rewardCoins += 2000;
                rewardRenown += 2;
            } else if (rank <= 20) {
                rewardCoins += 500;
                rewardRenown += 1;
            } else {
                rewardCoins += 100;
            }
            if (done >= 600) {
                rewardCoins = 2 * rewardCoins;
            }
            if (ThePit.getInstance().getPitConfig().isGenesisEnable() && profile.getGenesisData().getTier() >= 5 && rewardRenown > 0) {
                rewardRenown++;
            }
            int enchantBoostLevel = Utils.getEnchantLevel(player.getInventory().getLeggings(), "Paparazzi");
            if (PlayerUtil.isVenom(player) || PlayerUtil.isEquippingSomber(player)) {
                enchantBoostLevel = 0;
            }
            if (enchantBoostLevel > 0) {
                rewardCoins += 0.5 * enchantBoostLevel * rewardCoins;
                rewardRenown += Math.floor(0.5D * enchantBoostLevel * rewardRenown);
                MythicLeggingsItem mythicLeggings = new MythicLeggingsItem();
                mythicLeggings.loadFromItemStack(player.getInventory().getLeggings());
                if (mythicLeggings.isEnchanted()) {
                    if (mythicLeggings.getMaxLive() > 0 && mythicLeggings.getLive() <= 2) {
                        player.getInventory().setLeggings(new ItemStack(Material.AIR));
                    } else {
                        mythicLeggings.setLive(mythicLeggings.getLive() - 2);
                        player.getInventory().setLeggings(mythicLeggings.toItemStack());
                    }
                }
            }
            if (PlayerUtil.isPlayerUnlockedPerk(player, "self_confidence")) {
                if (rank <= 5) {
                    rewardCoins += 5000;
                } else if (rank <= 10) {
                    rewardCoins += 2500;
                } else if (rank <= 15) {
                    rewardCoins += 1000;
                }
            }
            profile.grindCoins(rewardCoins);
            profile.setCoins(profile.getCoins() + rewardCoins);
            profile.setRenown(profile.getRenown() + rewardRenown);
            profile.kingsQuestsData.checkUpdate();
            if (profile.kingsQuestsData.getAccepted()) {
                if (!profile.kingsQuestsData.getCompleted()) {
                    profile.kingsQuestsData.setCollectedRenown(profile.kingsQuestsData.getCollectedRenown() + rewardRenown);
                }
            }

            if (data.getHamOrdered() >= 35) {
                new PizzaEventMedal().addProgress(profile, 1);
            }

            player.sendMessage(CC.translate("&6你的奖励: &6+" + rewardCoins + "硬币 &e+" + rewardRenown + "声望"));
            player.sendMessage(CC.translate("&6&l你: &7完成了总计 &6" + data.paidMoney + "$ &7的订单 (排名#" + rank + ")"));
            if (done >= 500) {
                player.sendMessage(CC.translate("&6&l全局奖励: &a&l成功! &7所有人获得的金币翻倍!"));
            } else {
                player.sendMessage(CC.translate("&6&l全局奖励: &c&l失败! &7所有人累计完成了 &c" + done + "&7/500 &7份订单."));
            }
            if (list.size() >= 3) {
                player.sendMessage(CC.translate("&6顶级玩家: "));
                for (int i = 0; i < 3; i++) {
                    Player top = Bukkit.getPlayer(list.get(i).getUuid());
                    if (top != null && top.isOnline()) {
                        PlayerProfile topProfile = PlayerProfile.getPlayerProfileByUuid(top.getUniqueId());

                        int d = list.get(i).paidMoney;
                        player.sendMessage(CC.translate(" &e&l#" + (i + 1) + " " + topProfile.getFormattedName() + " &e订单业绩 &6" + d + "&6$"));
                    }
                }
            }
        }
        CC.boardCast(CC.CHAT_BAR);


    }

    private void spawnVillager() {
        final List<Location> configLoc = ThePit.getInstance().getPitConfig().getHamburgerNpcLocA();
        final List<Location> locations = new ArrayList<>(configLoc);
        locations.removeAll(spawnedLocations);
        final Location location = locations.get(RandomUtil.random.nextInt(locations.size()));
        spawnedLocations.add(location);
        if (location.getBlock().getType() != Material.AIR) {
            location.getBlock().setType(Material.AIR);
        }
        final Block upBlock = location.clone().add(0, 1, 0).getBlock();
        if (upBlock.getType() != Material.AIR) {
            upBlock.setType(Material.AIR);
        }

//        for (Player player : Bukkit.getOnlinePlayers()) {
//            createBeacon(player, location);
//        }

        try {
            final World world = Bukkit.getWorlds().get(0);
            final Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            final EntityVillager entityVillager = ((CraftVillager) villager).getHandle();
            NBTTagCompound tag = entityVillager.getNBTTag();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            entityVillager.c(tag);
            tag.setInt("NoAI", 1);
            tag.setInt("Silent", 1);
            entityVillager.f(tag);
            villager.setAdult();


            this.villagerDataMap.put(villager.getUniqueId(), new VillagerData(villager.getUniqueId(), villager));
        } catch (Exception e) {
            Bukkit.getOnlinePlayers()
                    .forEach(player -> {
                        CC.printError(player, e);
                    });
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PitProfileLoadedEvent event) {
        final Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
        if (this.pizzaDataMap.containsKey(player.getUniqueId())) {
            this.pizzaDataMap.get(player.getUniqueId()).hamburger = 0;
            return;
        }
        this.pizzaDataMap.put(player.getUniqueId(), new PizzaData(player.getUniqueId(), player.getDisplayName()));
    }

    @Override
    public List<String> insert(Player player) {
        final PizzaData data = pizzaDataMap.get(player.getUniqueId());
        if (data == null) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        lines.add("&f活动结束: &e" + TimeUtil.millisToTimer(timer.getRemaining()));
        lines.add("&f现金: &6" + data.money + "$");
        lines.add("&f已交付: &a" + data.paidMoney + "$");

        return lines;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            final Villager clicked = (Villager) event.getRightClicked();
            final VillagerData villager = villagerDataMap.get(clicked.getUniqueId());
            if (villager != null) {
                event.setCancelled(true);

                final Player player = event.getPlayer();

                if (villager.getState() == VillagerState.DONE) {
                    CC.send(MessageType.EVENT, player, "&c这个村民已经拿到了他的外卖!");
                    return;
                }

                if ("ham".equals(ItemUtil.getInternalName(player.getItemInHand()))) {
                    PlayerUtil.takeOneItemInHand(player);
                    villager.changeState(this, player);

                    final PizzaData playerData = pizzaDataMap.get(player.getUniqueId());
                    playerData.money += villager.coins;
                    playerData.hamburger--;
                    playerData.hamOrdered++;

                    for (Player target : Bukkit.getOnlinePlayers()) {
                        redo(target, villager.getLocation());
                    }

                    done++;
                }
            } else if (clicked.getUniqueId().equals(clicked.getUniqueId())) {
                event.setCancelled(true);

                final Player player = event.getPlayer();
                final PizzaData data = pizzaDataMap.get(player.getUniqueId());

                if (data.money == 0) {
                    return;
                }

                data.paidMoney += data.money;

                CC.send(MessageType.EVENT, player, "&a&l订单交付! &7你已将手持价值 &6" + data.money + "$ &7的订单成功交付!");

                data.money = 0;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager) {
            if (villagerDataMap.containsKey(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
            }
            if (villager.getUniqueId().equals(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    private enum VillagerState {
        WAITING,
        DONE
    }

    @Data
    @RequiredArgsConstructor
    public static class PizzaData {

        private final UUID uuid;
        private final String name;

        private int hamburger = 0;
        private int money = 0;
        private int paidMoney = 0;
        private int hamOrdered = 0;
    }

    @Data
    private static class VillagerData {

        private final UUID uuid;
        private final Villager villager;
        private final Hologram hologram;
        private VillagerState state;
        private int coins;
        private Location location;

        public VillagerData(UUID uuid, Villager villager) {
            this.uuid = uuid;
            this.villager = villager;
            this.location = villager.getLocation();

            final String name = NameUtils.getChinese(3);

            coins = (int) RandomUtil.helpMeToChooseOne(5, 10, 15, 20);

            this.hologram = HologramAPI.createHologram(villager.getLocation().clone().add(0, 2.4, 0), CC.translate("&c&l" + name));
            this.hologram.spawn();
            this.hologram.setAttachedTo(villager);
            this.hologram.addLineAbove(CC.translate("&7配送奖励: &6" + coins + "$"));

            this.state = VillagerState.WAITING;
        }

        public void changeState(HamburgerEvent event, Player player) {
            this.state = VillagerState.DONE;
            this.hologram.removeLineAbove();
            final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            this.hologram.setText(CC.translate("&a&l谢谢你! " + profile.getFormattedName()));
            Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
                remove(event);
                if (event.end) {
                    return;
                }
                event.spawnVillager();
            }, 20 * 3L);
        }

        public void remove(HamburgerEvent event) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                try {
                    if (this.villager != null && !this.villager.isDead()) {
                        this.villager.remove();

                        for (Player target : Bukkit.getOnlinePlayers()) {
                            redo(target, villager.getLocation());
                        }
                    }

                    if (this.hologram != null && this.hologram.isSpawned()) {
                        final Hologram above = this.hologram.getLineAbove();
                        if (above != null && above.isSpawned()) {
                            above.deSpawn();
                        }
                        this.hologram.deSpawn();
                    }
                    event.villagerDataMap.remove(this.uuid);
                    event.spawnedLocations.remove(this.location);
                } catch (Exception e) {
                    Bukkit.getOnlinePlayers()
                            .forEach(player -> {
                                CC.printError(player, e);
                            });
                }
            });
        }
    }

}
