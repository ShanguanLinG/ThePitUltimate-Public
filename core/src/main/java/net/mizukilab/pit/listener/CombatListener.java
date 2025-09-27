package net.mizukilab.pit.listener;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.*;
import cn.charlotte.pit.event.*;
import cn.charlotte.pit.events.EventFactory;
import cn.charlotte.pit.events.genesis.GenesisTeam;
import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkFactory;
import com.google.common.util.concurrent.AtomicDouble;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.mizukilab.pit.UtilKt;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.data.operator.PackedOperator;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.EnchantmentFactor;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.item.type.LuckyChestplate;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.map.kingsquests.item.Cherry;
import net.mizukilab.pit.movement.PlayerMoveHandler;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IPlayerAssist;
import net.mizukilab.pit.parm.listener.IPlayerBeKilledByEntity;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.parm.listener.IPlayerRespawn;
import net.mizukilab.pit.runnable.ProfileLoadRunnable;
import net.mizukilab.pit.util.FuncsKt;
import net.mizukilab.pit.util.MythicUtil;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.*;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.random.RandomUtil;
import net.mizukilab.pit.util.rank.RankUtil;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static net.mizukilab.pit.util.PublicUtil.processActionBarWithSettingProvided;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 11:16
 */

@AutoRegister
public class CombatListener implements Listener {

    ;
    public static CombatListener INSTANCE;
    public static double eventBoost = 2.0; //1.0 to close
    private final DecimalFormat numFormat = new DecimalFormat("0.00");
    private final DecimalFormat intFormat = new DecimalFormat("0");
    String boostString = " &6(限时加成x" + eventBoost + "倍奖励)";

    public CombatListener() {
        INSTANCE = this;
    }

    @NotNull
    private static String getBountyString(PlayerProfile killerProfile) {
        String bountyColor = "&6";
        if (ThePit.getInstance().getPitConfig().isGenesisEnable()) {
            GenesisTeam team = killerProfile.getGenesisData().getTeam();
            if (team == GenesisTeam.ANGEL) {
                bountyColor = "&b";
            }
            if (team == GenesisTeam.DEMON) {
                bountyColor = "&c";
            }
        }
        return bountyColor;
    }

    public static boolean isNight() {
        if (!ThePit.getInstance().getGlobalConfig().isCurfewEnable()) {
            return false;
        }

        final Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(System.currentTimeMillis());
        final int hour = instance.get(Calendar.HOUR_OF_DAY);

        return hour >= ThePit.getInstance().getGlobalConfig().getCurfewStart() && hour <= ThePit.getInstance().getGlobalConfig().getCurfewEnd();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStrike(PitStreakKillChangeEvent event) {
        final PlayerProfile profile = event.getPlayerProfile();
        if (FuncsKt.isPrivate(profile)) {
            return;
        }
        if (profile.getChosePerk().get(5) == null) {
            return;
        }

        //debug code

        double floor = MathHelper.floor(event.getFrom());
        double floor1 = MathHelper.floor(event.getTo());
        if (floor % 5 != 0
                && floor1 % 5 == 0 && event.getTo() > 0
                && floor != floor1) {
            CC.boardCast(MessageType.STREAK, "&c&l连杀! " + profile.getFormattedName()
                    + " &7已经 &c" + intFormat.format(floor1) + "&7 连杀了!");
        }
    }

    @EventHandler
    public void onTp(PlayerTeleportEvent event) {
        if (PlayerMoveHandler.getCantMoveList().contains(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            PlayerMoveHandler.checkMove(event.getTo(), event.getFrom(), event.getPlayer());
        } else {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                PlayerMoveHandler.checkMove(event.getTo(), event.getFrom(), event.getPlayer());
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onCombat(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player damager) {
                Player player = (Player) event.getEntity();

                PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                PlayerProfile damagerProfile = PlayerProfile.getPlayerProfileByUuid(damager.getUniqueId());

                //BeastMode boost
                if (PlayerUtil.isPlayerChosePerk(player, "beast_mode_mega_streak") && playerProfile.getStreakKills() >= 50) {
                    double boostLevel = Math.floor((playerProfile.getStreakKills() - 50) / 5);
                    event.setDamage(event.getDamage() + 0.1 * boostLevel);
                }

                //DiamondSword Boost
                ItemStack itemInHand = damager.getItemInHand();
                if (itemInHand != null && itemInHand.getType() == Material.DIAMOND_SWORD && "shopItem".equals(ItemUtil.getInternalName(itemInHand))) {
                    PlayerProfile targetProfile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                    if (targetProfile.getBounty() != 0) {
                        event.setDamage(event.getDamage() * 1.2);
                    }
                }

                //CombatSpade Boost
                if (itemInHand != null
                        && itemInHand.getType() == Material.DIAMOND_SPADE && "shopItem".equalsIgnoreCase(ItemUtil.getInternalName(itemInHand))) {
                    for (ItemStack is : player.getInventory().getArmorContents()) {
                        if (is.getType().name().startsWith("DIAMOND")) {
                            event.setDamage(event.getDamage() + 0.5);
                        }
                    }
                }
                if (itemInHand != null && !PlayerUtil.isVenom(player) && !PlayerUtil.isEquippingSomber(player)) {
                    int enchantLevel = Utils.getEnchantLevel(player.getItemInHand(), "bruiser_enchant");
                    if (enchantLevel > 0 && player.isBlocking()) {
                        event.setDamage(event.getDamage() - (enchantLevel / 2F) - (enchantLevel >= 3 ? 0.5 : 0));
                    }
                }

                this.postDamage(event, player, damager, playerProfile, damagerProfile, event.getFinalDamage(), false);
            } else if (event.getDamager() instanceof Projectile
                    && ((Projectile) event.getDamager()).getShooter() instanceof Player damager) {
                Player player = (Player) event.getEntity();

                PlayerProfile playerProfile
                        = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                PlayerProfile damagerProfile
                        = PlayerProfile.getPlayerProfileByUuid(damager.getUniqueId());

                postDamage(event, player, damager, playerProfile, damagerProfile, event.getFinalDamage(), true);

            } else if (event.getDamager() instanceof TNTPrimed && event.getEntity() instanceof Player) {
                List<MetadataValue> metadata = event.getDamager().getMetadata("internal");
                if (!metadata.isEmpty()) {
                    if ("tnt".equals(metadata.get(0).asString())) {
                        event.setDamage(event.getDamage() * (1 / 5F));
                    } else if ("red_packet".equals(metadata.get(0).asString())) {
                        event.setDamage(0);
                    } else if ("tnt_enchant_item".equals(metadata.get(0).asString())) {
                        if (PlayerUtil.isEquippingSomber((Player) event.getEntity())) {
                            event.setCancelled(true);
                            return;
                        }
                        List<MetadataValue> tntDamage = event.getDamager().getMetadata("damage");
                        if (!tntDamage.isEmpty()) {
                            event.setDamage(1 + tntDamage.get(0).asInt());
                            if (PlayerUtil.getDistance(event.getDamager().getLocation(), event.getEntity().getLocation()) > 3) {
                                event.setCancelled(true);
                            }
                        }
                    } else if ("insta_boom_enchant_item".equals(metadata.get(0).asString())) {
                        if (PlayerUtil.isEquippingSomber((Player) event.getEntity())) {
                            event.setCancelled(true);
                            return;
                        }
                        List<MetadataValue> tntDamage = event.getDamager().getMetadata("damage");
                        if (!tntDamage.isEmpty()) {
                            event.setDamage(0.5 * tntDamage.get(0).asInt());
                            if (PlayerUtil.getDistance(event.getDamager().getLocation(), event.getEntity().getLocation()) > 4) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProfileLoadRunnable.getInstance().handleQuit(player);
        runGCOnMetadatas(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKilled(PlayerDeathEvent event) {
        event.getEntity().setNoDamageTicks(0);
        event.getEntity().spigot().respawn();
        event.setDeathMessage(null);
        handlePlayerDeath(event.getEntity(), event.getEntity().getKiller(), true);
    }

    /**
     * Should be garbage collected params
     *
     * @param player
     */
    public void runGCOnMetadatas(Player player) {
        player.removeMetadata("showing_damage_data", ThePit.getInstance());
        player.removeMetadata("mirror_latest_active", ThePit.getInstance());
        //"STAFF_SPECTATOR"
        //lucky_chestplate
        //backing
        //sinking_moonlight
        player.removeMetadata("lastThroughTheHeart", ThePit.getInstance());
        player.removeMetadata("STAFF_SPECTATOR", ThePit.getInstance());
        player.removeMetadata("sinking_moonlight", ThePit.getInstance());
        player.removeMetadata("assured_strike", ThePit.getInstance());
        player.removeMetadata("lucky_chestplate", ThePit.getInstance());
        player.removeMetadata("backing", ThePit.getInstance());
        player.removeMetadata("combo_venom", ThePit.getInstance());
        player.removeMetadata("vanished", ThePit.getInstance());
        player.removeMetadata("leech_hit", ThePit.getInstance());
        player.removeMetadata("true_damage_immune", ThePit.getInstance());
        player.removeMetadata("regularity", ThePit.getInstance());
        player.removeMetadata("mixed_combat_" + player.getUniqueId(), ThePit.getInstance());

    }

    private void postDamage(@NotNull EntityDamageByEntityEvent event, Player player, Player damager, PlayerProfile playerProfile, PlayerProfile damagerProfile, double damage, boolean isShoot) {
        playerProfile.setCombatTimer(new Cooldown((playerProfile.getBounty() == 0 ? 24 : 48), TimeUnit.SECONDS));
        damagerProfile.setCombatTimer(new Cooldown((damagerProfile.getBounty() == 0 ? 24 : 48), TimeUnit.SECONDS));

        Map<UUID, DamageData> map = playerProfile.getDamageMap();
        map.putIfAbsent(damager.getUniqueId(), new DamageData(damager.getUniqueId()));

        DamageData damageData = map.get(damager.getUniqueId());

        if (damageData.getTimer().hasExpired()) {
            damageData.setDamage(damage);
        } else {
            damageData.setDamage(damageData.getDamage() + damage);
        }
        damageData.setTimer(new Cooldown(30, TimeUnit.SECONDS));

        playerProfile.getDamageMap().put(damager.getUniqueId(), damageData);

        playerProfile.setHurtDamage((long) (playerProfile.getHurtDamage() + damage));
        if (isShoot) {
            Entity damager1;
            if (event != null) {
                damager1 = event.getDamager();
            } else {
                damager1 = damager;
            }
            if (damager1 instanceof Arrow) {

                playerProfile.setBowHurtDamage((long) (playerProfile.getBowHurtDamage() + damage));
                damagerProfile.setBowHit(damagerProfile.getBowHit() + 1);
                damagerProfile.setArrowTotalDamage((long) (damagerProfile.getArrowTotalDamage() + damage));
            } else if (damager1 instanceof FishHook) {
                damagerProfile.setRodHit(damagerProfile.getRodHit() + 1);
            }
        } else {
            playerProfile.setMeleeHurtDamage((long) (playerProfile.getMeleeHurtDamage() + damage));
            damagerProfile.setMeleeHit(damagerProfile.getMeleeHit() + 1);
            damagerProfile.setMeleeTotalDamage((long) (damagerProfile.getMeleeTotalDamage() + damage));
        }
        damagerProfile.setTotalDamage((long) (damagerProfile.getTotalDamage() + damage));
        processActionBarWithSettingProvided(player, damager, (int) damage, Math.min(player.getHealth(), event.getFinalDamage()), damagerProfile);

        if (playerProfile.isLoaded()) {
            if (player.hasMetadata("backing")) {
                player.sendMessage(CC.translate("&c回城被取消."));
                player.removeMetadata("backing", ThePit.getInstance());
            }
        }


        //handle kill recap - start
        String damagerName = damagerProfile.getFormattedName();
        String playerName = playerProfile.getFormattedName();
        if (damagerProfile.isLoaded()) {
            KillRecap.DamageData damagerData = new KillRecap.DamageData();
            damagerData.setDisplayName(playerName);
            damagerData.setAttack(true);
            damagerData.setMelee(!isShoot);
            damagerData.setAfterHealth(Math.max(player.getHealth() - event.getFinalDamage(), 0));
            damagerData.setUsedItem(damager.getItemInHand());
            damagerData.setTimer(new Cooldown(10, TimeUnit.SECONDS));

            damagerProfile.getKillRecap()
                    .getDamageLogs()
                    .add(damagerData);
        }
        if (playerProfile.isLoaded()) {
            KillRecap.DamageData playerData = new KillRecap.DamageData();
            playerData.setDisplayName(damagerName);
            playerData.setAttack(false);
            playerData.setMelee(!isShoot);
            playerData.setAfterHealth(Math.max(player.getHealth() - event.getFinalDamage(), 0));
            playerData.setUsedItem(damager.getItemInHand());
            playerData.setTimer(new Cooldown(10, TimeUnit.SECONDS));
            playerData.setDamage(event.getFinalDamage());
            playerProfile.getKillRecap().getDamageLogs()
                    .add(playerData);
        }
        //handle kill recap - end
        playerProfile.setLastDamageAt(System.currentTimeMillis());
    }

    public void handleKill(Player killer, PlayerProfile killerProfile, LivingEntity player, PlayerProfile playerProfile) {
        handleKill(killer, killerProfile, player, playerProfile, false);
    }

    public void handleKill(Player killer, PlayerProfile killerProfile, LivingEntity player, PlayerProfile playerProfile, boolean npc) {
        try {
            boolean isNight = isNight();

            final String coloredName = RankUtil.getPlayerColoredName(player.getUniqueId());

            if (killerProfile.getPlayerOption().getBarPriority() != PlayerOption.BarPriority.ENCHANT_ONLY) {
                ActionBarUtil.sendActionBar1(killer, "kill", "&a&l击杀! " + coloredName + " ", 7);
            }

            //process drop armor - start
            //fixme: here is an error about async
            if (player instanceof Player && !npc) {
                //只有玩家需要掉落装备
                this.handleItemDrop(killerProfile, killer, (Player) player);
            }
            //process drop armor - end

            if (!isNight) {
                killerProfile.setStreakKills(killerProfile.getStreakKills() + 1);
            }
            killerProfile.setKills(killerProfile.getKills() + 1);

            killerProfile.kingsQuestsData.checkUpdate();
            if (killerProfile.kingsQuestsData.getAccepted()) {
                if (!killerProfile.kingsQuestsData.getCompleted()) {
                    killerProfile.kingsQuestsData.setKilledPlayer(killerProfile.kingsQuestsData.getKilledPlayer() + 1);
                }
            }

            double totalXp = 10.0d + killerProfile.getPrestige() * 0.5;
            double totalCoins = 10.0d + killerProfile.getPrestige() * 0.5;

            //process perk - start
            AtomicDouble coinsAtomic = new AtomicDouble(totalCoins);
            AtomicDouble expAtomic = new AtomicDouble(totalXp);
            this.handleGameEffect(killerProfile, killer, player, coinsAtomic, expAtomic);

            if (!isNight) {
                //process giving bounty - start
                this.handleAddBounty(killerProfile, killer);
                //process giving bounty - end
            }

            totalCoins = coinsAtomic.get();
            totalXp = expAtomic.get();

            final KillRecap killRecap = this.initializationKillRecap(playerProfile, killerProfile, killer, totalCoins, totalXp);

            //calculation kill reward - start
            AtomicDouble tempCoins = new AtomicDouble(totalCoins);
            AtomicDouble tempExp = new AtomicDouble(totalXp);
            this.calculationKillReward(killerProfile, playerProfile, killRecap, killer, tempCoins, tempExp);
            //calculation kill reward - end

            handleKillBounty(killerProfile, playerProfile, tempCoins);

            totalCoins = tempCoins.get();
            totalXp = tempExp.get();

            if (isNight) {
                totalCoins = totalCoins * 0.01;
                totalXp = totalXp * 0.01;
            }

            if (killerProfile.getLevel() < NewConfiguration.INSTANCE.getMaxLevel()) {
                killerProfile.setExperience(killerProfile.getExperience() + totalXp);
            } else {
                totalXp = 0;
            }

            killerProfile.setCoins(killerProfile.getCoins() + totalCoins);
            killerProfile.grindCoins(totalCoins);
            killerProfile.applyExperienceToPlayer(killer);

            //process quest - start
            if (player instanceof Player) {
                this.handleQuest(killerProfile, (Player) player);
            }
            //process quest - end

            this.handleGivePlayerKillReward(killer);
            //golden head and vampire - end

            if (!isNight) {
                //enchant - start
                this.handleMythicItemDrop(killerProfile, killer, player);
                //enchant - end

                this.handleCherryDrop(killer);
            }

            //refresh killer highest streak kills
            if (killerProfile.getHighestStreaks() < killerProfile.getStreakKills()) {
                killerProfile.setHighestStreaks((int) killerProfile.getStreakKills());
            }

            //call kill event
            final PitKillEvent event = new PitKillEvent(killer, player, totalCoins, totalXp);
            event.callEvent();

            totalCoins = event.getCoins();
            totalXp = event.getExp();

            double finalTotalCoins = totalCoins;
            double finalTotalXp = totalXp;
            Bukkit.getScheduler().runTaskAsynchronously(ThePit.getInstance(), () -> {
                //BoardCast msg - start
                this.handleBoardCastMessage(killerProfile, playerProfile, killer, player, finalTotalCoins, finalTotalXp);
                //BoardCast msg - end
            });

            if (player instanceof Player && !npc) {
                killRecap.completeLog((Player) player);
            }
        } catch (Exception e) {
            CC.printError(killer, e);
        }
    }

    private boolean hasPremiumItem(Player player) {
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null) {
                continue;
            }
            Material type = itemStack.getType();
            if (type.name().contains("LEATHER") || type.name().contains("DIAMOND")) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player player) {
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                profile.setShootAttack(profile.getShootAttack() + 1);
                if (!profile.isInArena()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                }
            }
        } else if (event.getEntity() instanceof FishHook hook) {
            if (hook.getShooter() instanceof Player) {
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(((Player) hook.getShooter()).getUniqueId());
                profile.setRodUsed(profile.getRodUsed() + 1);
            }
        }
    }

    public void handlePlayerDeath(Player player, Player killer, boolean shouldRespawn) {
        PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        boolean npc = PlayerUtil.isNPC(player);
        if (killer != null) {
            //what
            Player reallyKiller = Bukkit.getPlayer(killer.getUniqueId());
            if (reallyKiller != null || npc) {
                PlayerProfile killerProfile = PlayerProfile.getPlayerProfileByUuid(killer.getUniqueId());
                this.handleKill(killer, killerProfile, player, playerProfile, npc);
            }
        } else {
            EntityDamageEvent damageEvent = player.getLastDamageCause();
            if (damageEvent instanceof EntityDamageByEntityEvent entityEvent) {
                if (entityEvent.getDamager() instanceof Player) {
                    killer = (Player) entityEvent.getDamager();
                    PlayerProfile killerProfile = PlayerProfile.getPlayerProfileByUuid(killer.getUniqueId());
                    this.handleKill(killer, killerProfile, player, playerProfile);
                } else if (entityEvent.getDamager() instanceof TNTPrimed tntPrimed) {
                    List<MetadataValue> shooter = tntPrimed.getMetadata("shooter");
                    if (!shooter.isEmpty()) {
                        UUID uuid = UUID.fromString(shooter.get(0).asString());
                        if (uuid.equals(player.getUniqueId())) {
                            return;
                        }
                        killer = Bukkit.getPlayer(uuid);
                        if (killer != null && killer.isOnline()) {
                            PlayerProfile killerProfile = PlayerProfile.getPlayerProfileByUuid(uuid);
                            this.handleKill(killer, killerProfile, player, playerProfile);
                        }
                    }
                }
            }
        }
        //saves performance
        if (npc) { //NPC Name
            if (NewConfiguration.INSTANCE.getAlwaysCheckNPC()) {
                player.setGameMode(GameMode.SPECTATOR);
                player.setNoDamageTicks(0);
                player.spigot().respawn();
            }
            return;
        }
        final Player finalKiller = killer;

        double respawnTime = playerProfile.getRespawnTime();

        PlayerUtil.resetPlayer(player, true, false);
        double mythicProtectChance = 0;

        PlayerInventory inventory = player.getInventory();
        int divineMiracleEnchantLevel = Utils.getEnchantLevel(inventory.getLeggings(), "divine_miracle_enchant");
        if (PlayerUtil.isVenom(player) || PlayerUtil.isEquippingSomber(player)) {
            divineMiracleEnchantLevel = 0;
        }
        if (divineMiracleEnchantLevel > 0) {
            mythicProtectChance += 0.15 * divineMiracleEnchantLevel;
        }
        if (PlayerUtil.isPlayerUnlockedPerk(player, "divine_intervention")) {
            mythicProtectChance += 0.05 * PlayerUtil.getPlayerUnlockedPerkLevel(player, "divine_intervention");
        }

        //Promotion effect
        if (PlayerUtil.isPlayerChosePerk(player, "assistant_to_the_streaker")
                && PlayerUtil.isPlayerUnlockedPerk(player, "promotion")) {
            if (playerProfile.getStreakKills() >= 50) {
                if (PlayerUtil.isPlayerChosePerk(player, "over_drive")
                        || PlayerUtil.isPlayerChosePerk(player, "high_lander")
                        || PlayerUtil.isPlayerChosePerk(player, "beast_mode_mega_streak")
                        || PlayerUtil.isPlayerChosePerk(player, "hermit")) {
                    mythicProtectChance = 1;
                }
            }
            if (playerProfile.getStreakKills() > 100) {
                if ((PlayerUtil.isPlayerChosePerk(player, "uber_streak") && playerProfile.getStreakKills() >= 400)
                        || PlayerUtil.isPlayerChosePerk(player, "to_the_moon")) {
                    mythicProtectChance = 1;
                }
                if (PlayerUtil.isPlayerChosePerk(player, "uber_streak_plus") && playerProfile.getStreakKills() >= 1000) {
                    mythicProtectChance = 1;
                }
            }
        }
        //Funky Feather Item
        ItemLiveDropEvent itemLiveDropEvent = new ItemLiveDropEvent(mythicProtectChance);
        itemLiveDropEvent.callEvent();
        mythicProtectChance = itemLiveDropEvent.getChance();
        if (mythicProtectChance < 1 && !itemLiveDropEvent.isCancelled()) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if ("funky_feather".equals(ItemUtil.getInternalName(item))) {
                    if (item.getAmount() <= 1) {
                        inventory.setItem(i, new ItemBuilder(Material.AIR).build());
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        inventory.setItem(i, item);
                    }
                    mythicProtectChance = 1;
                    break;
                }
            }
        }
        boolean noProtect = !itemLiveDropEvent.isCancelled() && RandomUtil.hasSuccessfullyByChance(1 - mythicProtectChance);
        if (!itemLiveDropEvent.isCancelled()) {
            for (int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;

                final IMythicItem mythicSwordItem = ((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItemSync(item);
                if (mythicSwordItem == null) {
                    continue;
                }

                if (!noProtect) {
                    if ((mythicSwordItem instanceof LuckyChestplate) /*|| mythicSwordItem.getEnchantments().containsKey(new RealManEnchant())*/) {
                        noProtect = true;
                    }
                }
                if (noProtect) {
                    inventory.setItem(i, Utils.subtractLive(mythicSwordItem));
                }
            }
        }

        if (noProtect) {
            inventory.setHelmet(Utils.subtractLive(inventory.getHelmet()));
            inventory.setChestplate(Utils.subtractLive(inventory.getChestplate()));
        }
        IMythicItem leggings = MythicUtil.getMythicItem(inventory.getLeggings());
        if (leggings != null && (noProtect || leggings instanceof LuckyChestplate
                /*|| leggings.getEnchantments().containsKey(new RealManEnchant())*/)) {
            inventory.setLeggings(Utils.subtractLive(leggings));
        }
        if (noProtect) {
            inventory.setBoots(Utils.subtractLive(inventory.getBoots()));
        }

        if (!noProtect) {
            player.sendMessage(CC.translate("&d&l物品保护! &7由于一个天赋/附魔/物品/事件提供的概率保护,本次死亡没有损失背包内神话物品生命."));
        }
        for (ItemStack itemStack : inventory) {
            if (ItemUtil.isDeathDrop(itemStack)) {
                inventory.remove(itemStack);
            }
        }

        if (ItemUtil.isDeathDrop(inventory.getHelmet())) {
            inventory.setHelmet(new ItemStack(Material.AIR));
        }
        if (ItemUtil.isDeathDrop(inventory.getChestplate())) {
            inventory.setChestplate(new ItemStack(Material.AIR));
        }
        if (ItemUtil.isDeathDrop(inventory.getLeggings())) {
            inventory.setLeggings(new ItemStack(Material.AIR));
        }
        if (ItemUtil.isDeathDrop(inventory.getBoots())) {
            inventory.setBoots(new ItemStack(Material.AIR));
        }

        //process assist - start

        double totalDamage = 0;
        List<DamageData> activeDamage = new ArrayList<>();
        for (DamageData it : playerProfile.getDamageMap().values()) {
            if (it.getTimer().hasExpired()) {
                totalDamage += it.getDamage();
                activeDamage.add(it);
            }
        }
        if (totalDamage > 0) {
            this.handleAssist(player, finalKiller, activeDamage, (long) totalDamage);
        }


        //process assist - end

        //save status - start
        playerProfile.getDamageMap().clear();
        playerProfile.deActiveMegaSteak();
        playerProfile.setInArena(false);
        playerProfile.setDeaths(playerProfile.getDeaths() + 1);
        playerProfile.setCombatTimer(new Cooldown(0));
        playerProfile.setBountyStreak(0);
        playerProfile.setStrengthNum(0);
        playerProfile.setStrengthTimer(new Cooldown(0));
        //save status - end

        InventoryUtil.supplyItems(player);
        PackedOperator operator = (PackedOperator) playerProfile.toOperator();
        if (operator != null) {
            operator.pending(i -> {
                playerProfile.setInventory(PlayerInv.fromPlayerInventory(inventory));
            });
        }
        if (shouldRespawn) {
            ((CraftPlayer) player).getHandle().invulnerableTicks = 40;
            // player.setHealth(player.getMaxHealth());
            Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> player.spigot().respawn(), 10);

            if (playerProfile.getRespawnTime() <= 1.5) {
                playerProfile.setRespawnTime(0.1d);
            }

            if (respawnTime > 0.1) {
                new BukkitRunnable() {
                    private int remainingTime = (int) respawnTime;

                    @Override
                    public void run() {
                        if (remainingTime <= 0) {
                            this.cancel();
                            return;
                        }
                        TitleUtil.sendTitle(player, "&c你死了！", "&7将在 &6" + remainingTime + "秒 &7后复活", 5, 5, 20);
                        remainingTime--;
                    }
                }.runTaskTimer(ThePit.getInstance(), 0, 20);

                Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
                    this.doRespawn(player);
                    TitleUtil.sendTitle(player, "&a已复活！", " ", 5, 5, 20);
                }, (long) (respawnTime * 20L));
            } else {
                Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> doRespawn(player), 1L);
            }
        }
    }

    private void doRespawn(Player player) {
        if (!player.isOnline()) {
            return;
        }
        Location location = ThePit.getInstance().getPitConfig()
                .getSpawnLocations()
                .get(ThreadLocalRandom.current().nextInt(ThePit.getInstance().getPitConfig().getSpawnLocations().size()));

        if (player.getInventory().getLeggings() != null) {
            if (Utils.getEnchantLevel(player.getInventory().getLeggings(), "trash_panda_enchant") >= 1) {
                location = ThePit.getInstance().getPitConfig().getSewersLocation();
                player.sendMessage(CC.translate("&2&l垃圾拾荒者! &7你于下水道重生"));
            }
        }
        player.teleport(location);

        PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).setInArena(false);

        PlayerUtil.resetPlayer(player, true, false);
        player.setGameMode(GameMode.SURVIVAL);

        PlayerProfile.getPlayerProfileByUuid(player.getUniqueId())
                .applyExperienceToPlayer(player);
        TitleUtil.sendTitle(player, " ", " ", 5, 5, 20);

        for (IPlayerRespawn ins : ThePit.getInstance().getPerkFactory()
                .getPlayerRespawns()) {
            AbstractPerk perk = (AbstractPerk) ins;
            int perkPlayerLevel = perk.getPlayerLevel(player);
            if (perkPlayerLevel != -1) {
                ins.handleRespawn(perkPlayerLevel, player);
            }
        }

        EnchantmentFactor enchantmentFactor = ThePit.getInstance().getEnchantmentFactor();
        for (IPlayerRespawn ins : enchantmentFactor.getPlayerRespawns()) {
            AbstractEnchantment ench = (AbstractEnchantment) ins;

            int level = ench.getItemEnchantLevel(player.getInventory().getLeggings());
            if (level > 0) {
                ins.handleRespawn(level, player);
            }
        }
    }

    private void handleAssist(Player player, Player killer, List<DamageData> damageData, long totalDamage) {
        PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        boolean night = isNight();

        for (DamageData data : damageData) {
            if (killer != null && killer.getUniqueId().equals(data.getUuid())) {
                continue;
            }
            if (totalDamage <= 1) {
                return;
            }
            KillRecap killRecap = playerProfile.getKillRecap();
            KillRecap.AssistData assistData = new KillRecap.AssistData();
            Player assistPlayer = Bukkit.getPlayer(data.getUuid());
            if (assistPlayer != null && assistPlayer.isOnline()) {

                PlayerProfile assistProfile = PlayerProfile.getPlayerProfileByUuid(data.getUuid());

                assistData.setDisplayName(assistProfile.getFormattedName());

                double totalCoins = 10.0d + assistProfile.getPrestige() * 0.5;
                double totalXp = 10.0d + assistProfile.getPrestige() * 0.5;

                final PitAssistEvent event = new PitAssistEvent(assistPlayer, player, totalCoins, totalXp);
                event.callEvent();

                totalCoins = event.getCoins();
                totalXp = event.getExp();

                assistData.setBaseExp(10.0d + assistProfile.getPrestige() * 0.5);
                assistData.setBaseCoin(10.0d + assistProfile.getPrestige() * 0.5);

                AtomicDouble atomicDouble = new AtomicDouble();
                if (playerProfile.getStreakKills() != 0 && playerProfile.getStreakKills() % 10 == 0) {
                    totalXp += (playerProfile.getStreakKills() / 10d) * 5;
                    totalCoins += (playerProfile.getStreakKills() / 10d) * 5;
                    atomicDouble.getAndAdd((playerProfile.getStreakKills() / 10d) * 5);
                }

                if (assistProfile.getStreakKills() != 0 && assistProfile.getStreakKills() % 10 == 0) {
                    totalXp += Math.min(20, (assistProfile.getStreakKills() / 10d) * 5);
                    totalCoins += Math.min(20, (assistProfile.getStreakKills() / 10d) * 5);

                    atomicDouble.getAndAdd((Math.min(20, (assistProfile.getStreakKills() / 10d) * 5)));
                }
                assistData.setStreakCoin(atomicDouble.get());
                assistData.setStreakExp(atomicDouble.get());
                atomicDouble.set(0);

                if (playerProfile.getPrestige() - assistProfile.getPrestige() > 0) {
                    totalXp += (playerProfile.getPrestige() - assistProfile.getPrestige()) * 7;
                    totalCoins += (playerProfile.getPrestige() - assistProfile.getPrestige()) * 7;

                    atomicDouble.getAndAdd((playerProfile.getPrestige() - assistProfile.getPrestige()) * 7);
                }

                if (playerProfile.getLevel() - assistProfile.getLevel() >= 30) {
                    totalXp += (playerProfile.getLevel() - assistProfile.getLevel()) / 30d * 3;
                    totalCoins += (playerProfile.getLevel() - assistProfile.getLevel()) / 30d * 3;

                    atomicDouble.getAndAdd((playerProfile.getLevel() - assistProfile.getLevel()) / 30d * 3);
                }


                if (hasPremiumItem(player)) {
                    if (!hasPremiumItem(assistPlayer)) {
                        totalCoins += 10;
                        totalXp += 10;
                        atomicDouble.getAndAdd(10);
                    }
                }

                assistData.setLevelDisparityExp(atomicDouble.get());
                assistData.setLevelDisparityCoin(atomicDouble.get());

                //process perk - start
                AtomicDouble coinsAtomic = new AtomicDouble(totalCoins);
                AtomicDouble expAtomic = new AtomicDouble(totalXp);
                for (IPlayerAssist ins : ThePit.getInstance().getPerkFactory()
                        .getPlayerAssists()) {
                    AbstractPerk perk = (AbstractPerk) ins;
                    int perkPlayerLevel = perk.getPlayerLevel(assistPlayer);
                    if (perkPlayerLevel != -1) {
                        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
                        ins.handlePlayerAssist(perkPlayerLevel, assistPlayer, player, player.getLastDamage(), lastDamageCause.getFinalDamage(), coinsAtomic, expAtomic);
                    }
                }
                double percentage = data.getDamage() / totalDamage; //(0.0 ~ 1.0)
                if (percentage > 1) {
                    percentage = 1;
                }
                if (percentage < 0) {
                    percentage = 0;
                }
                assistData.setPercentage(percentage);
                assistData.setTotalCoin(totalCoins);
                assistData.setTotalExp(totalXp);
                killRecap.getAssistData().add(assistData);
                totalCoins = coinsAtomic.get() * (percentage);
                totalXp = expAtomic.get() * (percentage);
                //process perk - end

                if (night) {
                    totalXp *= 0.01;
                    totalCoins *= 0.01;
                }

                //Assistant to the streaker effect
                if (PlayerUtil.isPlayerBoughtPerk(assistPlayer, "assistant_to_the_streaker") && !isNight()) {
                    assistProfile.setStreakKills(assistProfile.getStreakKills() + Math.floor(100 * percentage) * 0.01);
                }
                assistProfile.setAssists(assistProfile.getAssists() + 1);
                assistProfile.setExperience(assistProfile.getExperience() + totalXp);
                assistProfile.setCoins(assistProfile.getCoins() + totalCoins);
                assistProfile.grindCoins(totalCoins);
                assistProfile.applyExperienceToPlayer(assistPlayer);


                totalCoins = eventBoost * totalCoins;
                totalXp = eventBoost * totalXp;
                assistPlayer.playSound(assistPlayer.getLocation(), Sound.ORB_PICKUP, 1, 1.7F);
                CC.send(MessageType.COMBAT, assistPlayer, CC.translate("&a&l助攻! &7" + numFormat.format(percentage * 100) + "% 的伤害在 " + playerProfile.getFormattedName() + " &6+" + numFormat.format(totalCoins) + "硬币 " + (assistProfile.getLevel() < 120 ? "&b+" + numFormat.format(totalXp) + "经验值" : "") + (eventBoost > 1 ? boostString : "")));
            }
        }
    }

    private void handleKillBounty(PlayerProfile killerProfile, PlayerProfile playerProfile, AtomicDouble coin) {
        //handle bounty - start
        if (playerProfile.getBounty() != 0 && ThePit.getInstance().getEventFactory().getActiveEpicEvent() == null) {
            String bountyColor = "&6";
            if (ThePit.getInstance().getPitConfig().isGenesisEnable()) {
                if (playerProfile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                    bountyColor = "&b";
                }
                if (playerProfile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                    bountyColor = "&c";
                }
            }
            CC.boardCast(MessageType.BOUNTY, CC.translate("&6&l赏金! " + playerProfile.getFormattedName() + " &7被 " + killerProfile.getFormattedName() + " &7击杀. " + bountyColor + "&l(" + playerProfile.getBounty() + "g)"));
            if (ThePit.getInstance().getPitConfig().isGenesisEnable() && killerProfile.getGenesisData().getTier() >= 5) {
                coin.set(1.5 * playerProfile.getBounty());
                killerProfile.grindCoins(1.5 * playerProfile.getBounty());
                killerProfile.setCoins(killerProfile.getCoins() + 1.5 * playerProfile.getBounty());
            } else {
                coin.set(playerProfile.getBounty());
                killerProfile.grindCoins(playerProfile.getBounty());
                killerProfile.setCoins(killerProfile.getCoins() + playerProfile.getBounty());
            }
            if (killerProfile.getBuffData().getBuff("bounty_solvent").getTier() > 0) {
                if (UtilKt.hasRealMan(Bukkit.getPlayer(killerProfile.getUuid()))) return;
                coin.set(coin.get() * 1.5);
            }
//            if (playerProfile.getBounty() >= 5000) {
//                new MaxBountyHunterMedal().addProgress(killerProfile, 1);
//            }
            playerProfile.setBounty(0);
        }
        //handle bounty - end
    }

    private void handleQuest(PlayerProfile killerProfile, Player beKilledPlayer) {
        //process quest - start
        QuestData currentQuest = killerProfile.getCurrentQuest();
        if (currentQuest != null && currentQuest.getEndTime() > System.currentTimeMillis()) {
            if (currentQuest.getCurrent() < currentQuest.getTotal()) {
                if (currentQuest.getKilled().add(beKilledPlayer.getUniqueId().toString())) {
                    currentQuest.setCurrent(currentQuest.getCurrent() + 1);
                    if (currentQuest.getCurrent() >= currentQuest.getTotal()) {
                        final Player player = Bukkit.getPlayer(killerProfile.getPlayerUuid());
                        if (player != null) {
                            new PitQuestCompleteEvent(player, currentQuest);
                        }
                    }
                }
            } else {
                currentQuest.setCurrent(currentQuest.getTotal());
            }
        }
        //process quest - end
    }

    private void handleCherryDrop(Player player) {
        boolean success = RandomUtil.hasSuccessfullyByChance(0.0002);
        if (success) {
            player.getInventory().addItem(Cherry.INSTANCE.toItemStack());
            ThePit.getInstance().getSoundFactory().playSound("cherry_sound", player);
            player.sendMessage(CC.translate("&d&l樱桃! &7你在战斗中获得樱桃!"));
        }
    }

    private void handleMythicItemDrop(PlayerProfile killerProfile, Player killer, LivingEntity beKilledPlayer) {
        int enchantPerkLevel;
        PerkData data = killerProfile.getUnlockedPerkMap().get("Mythicism");
        if (data != null && !UtilKt.hasRealMan(killer)) {
            enchantPerkLevel = data.getLevel();
        } else {
            enchantPerkLevel = -1;
        }
        if (enchantPerkLevel > -1) {
            Bukkit.getScheduler().runTaskAsynchronously(ThePit.getInstance(), () -> {
                double chance = NewConfiguration.INSTANCE.getMythicDropChance(killer) * (1 + (enchantPerkLevel - 1) * 0.02);
                int level = Utils.getEnchantLevel((IMythicItem) killerProfile.leggings, "pants_radar");
                if (level > 0) {
                    chance = (1 + level * 0.3) * chance;
                }
                level = Utils.getEnchantLevel((IMythicItem) killerProfile.heldItem, "pants_radar");
                if (level > 0) {
                    chance = (1 + level * 0.3) * chance;
                }
                boolean b = RandomUtil.hasSuccessfullyByChance(chance);
                if (b) {
                    AbstractPitItem item;
                    if (enchantPerkLevel >= 4) {
                        item = (AbstractPitItem) RandomUtil.helpMeToChooseOne(
                                new MythicBowItem(), new MythicSwordItem(), new MythicLeggingsItem());
                    } else {
                        item = (AbstractPitItem) RandomUtil.helpMeToChooseOne(
                                new MythicBowItem(), new MythicSwordItem());
                    }

                    ItemStack itemStack = item.toItemStack();

                    if (!killerProfile.isNotMythDrop()) {
                        if (InventoryUtil.isInvFull(killer.getInventory())) {
                            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                                beKilledPlayer.getWorld().dropItemNaturally(beKilledPlayer.getLocation(), itemStack);
                            });
                        } else {
                            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                                InventoryUtil.addInvReverse(killer.getInventory(), itemStack);
                            });
                        }
                        CC.send(MessageType.MISC, killer, "&d&l神话武器! &7你在战斗中拾取了掉落的神话物品!");
                    } else {

                        PlayerUtil.addAbsorptionHearts(killer, 4);
                        CC.send(MessageType.MISC, killer, "&6&l炼金术士! &7成功将神话物品转化为 &62❤ 生命吸收");
                    }
                    //fixme: change to sound system
                    new BukkitRunnable() {
                        int task = 0;

                        @Override
                        public void run() {
                            killer.playSound(beKilledPlayer.getLocation(), Sound.NOTE_PLING, 1, 0.1F + (0.5F * task));
                            task++;

                            if (task >= 6) {
                                cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(ThePit.getInstance(), 10, 5);
                }
            });
        }
    }

    private void handleItemDrop(PlayerProfile killerProfile, Player killer, Player beKilledPlayer) {
        try {
            if (PlayerUtil.isNPC(beKilledPlayer)) { //check npc
                return;
            }
            //lucky diamond
            boolean enabledLuckyDiamond = PlayerUtil.isPlayerChosePerk(killer, "LuckyDiamond");

            //drop armor
            for (ItemStack itemStack : beKilledPlayer.getInventory().getArmorContents()) {
                if (itemStack != null && itemStack.getType() != Material.AIR && Utils.toNMStackQuick(itemStack).getItem() instanceof ItemArmor) {
                    if (itemStack.getType().name().endsWith("HELMET")) {
                        continue;
                    }
                    final Location killerLoc = killer.getLocation().clone();
                    //iron armor
                    if (itemStack.getType().name().startsWith("IRON")) {
                        if (enabledLuckyDiamond && RandomUtil.hasSuccessfullyByChance(0.3)) {
                            ItemBuilder builder = new ItemBuilder(Material.valueOf(itemStack.getType().name().replace("IRON", "DIAMOND")))
                                    .deathDrop(true)
                                    .canDrop(false)
                                    .canSaveToEnderChest(false)
                                    .internalName("lucky_diamond");
                            Bukkit.getScheduler().runTask(
                                    ThePit.getInstance(),
                                    () -> Bukkit.getPluginManager().callEvent(
                                            new PlayerPickupItemEvent(
                                                    killer,
                                                    beKilledPlayer.getWorld().dropItemNaturally(
                                                            killerLoc, builder.build()), 1)));
                            continue;
                        }
                        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> beKilledPlayer.getWorld().dropItemNaturally(killer.getLocation(), itemStack));
                        continue;
                    }
                    //diamond armor
                    if (itemStack.getType().name().startsWith("DIAMOND")) {
                        //drop shop diamond armor only
                        if (itemStack.getType().name().endsWith("HELMET")) {
                            continue;
                        }
                        if ("shopItem".equals(ItemUtil.getInternalName(itemStack))) {
                            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> beKilledPlayer.getWorld().dropItemNaturally(killerLoc, itemStack));
                        }
                    }

                }
            }
        } catch (Exception e) {
            CC.printError(killer, e);
            CC.printError(beKilledPlayer, e);
        }
    }

    private void handleAddBounty(PlayerProfile killerProfile, Player killer) {
        if (ThePit.getInstance().getEventFactory().getActiveEpicEvent() == null && killerProfile.getBounty() < ((PlayerUtil.isPlayerChosePerk(killer, "high_lander") && killerProfile.getStreakKills() >= 50) ? 10000 : 5000)) {
            if (!killerProfile.getBountyCooldown().hasExpired()) {
                int bountyStreak = killerProfile.getBountyStreak();
                if (bountyStreak - 8 > 0) {
                    int i = bountyStreak - 8;
                    boolean b = RandomUtil.hasSuccessfullyByChance(i * 0.08);
                    if (b) {
                        int bounty = (int) RandomUtil.helpMeToChooseOne(100, 150, 200, 250);
                        killerProfile.setBounty(Math.min((PlayerUtil.isPlayerChosePerk(killer, "high_lander") && killerProfile.getStreakKills() >= 50) ? 10000 : 5000, killerProfile.getBounty() + bounty));
                        killerProfile.setBountyStreak(0);
                        String bountyColor = getBountyString(killerProfile);
                        CC.boardCast(MessageType.BOUNTY, "&6&l赏金! " + killerProfile.getFormattedName() + " &7当前已经被以 " + bountyColor + killerProfile.getBounty() + bountyColor + "g &7的金额悬赏了!");
                    }

                }
                killerProfile.setBountyStreak(killerProfile.getBountyStreak() + 1);
            } else {
                killerProfile.setBountyStreak(1);
            }

            killerProfile.setBountyCooldown(new Cooldown(1, TimeUnit.MINUTES));
        }
    }

    private void handleBoardCastMessage(PlayerProfile killerProfile, PlayerProfile playerProfile, Player killer, LivingEntity beKilledPlayer, double totalCoins, double totalXp) {

        if (beKilledPlayer instanceof Player) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> ((Player) beKilledPlayer).playSound(beKilledPlayer.getLocation(), Sound.ZOMBIE_INFECT, 1, 1.5F));
        }

        String genesisStatus = "";
        if (ThePit.getInstance().getPitConfig().isGenesisEnable() && killerProfile.getGenesisData().getTeam() != GenesisTeam.NONE) {
            if (killerProfile.getGenesisData().getTeam() == playerProfile.getGenesisData().getTeam()) {
                killerProfile.getGenesisData().setPoints(killerProfile.getGenesisData().getPoints() + 1);
                if (killerProfile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                    genesisStatus = " &b+1活动点数";
                }
                if (killerProfile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                    genesisStatus = " &c+1活动点数";
                }
            } else {
                killerProfile.getGenesisData().setPoints(killerProfile.getGenesisData().getPoints() + 2);
                if (killerProfile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                    genesisStatus = " &b+2活动点数";
                }
                if (killerProfile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                    genesisStatus = " &c+2活动点数";
                }
            }
        }

        final int streakNumberShort = killerProfile.addAndGetStreakNumberShort();
        String prefix;
        switch (streakNumberShort) {
            case 2: {
                ThePit.getInstance().getSoundFactory().playSound("double_streak", killer);
                prefix = "双杀";
                break;
            }
            case 3: {
                ThePit.getInstance().getSoundFactory().playSound("triple_streak", killer);
                prefix = "三杀";
                break;
            }
            case 4: {
                ThePit.getInstance().getSoundFactory().playSound("quadra_streak", killer);
                prefix = "四杀";
                break;
            }
            case 5: {
                ThePit.getInstance().getSoundFactory().playSound("streak", killer);
                prefix = "五杀";
                break;
            }
            default: {
                if (streakNumberShort > 5) {
                    prefix = "多杀";
                    ThePit.getInstance().getSoundFactory().playSound("streak", killer);
                } else {
                    killer.playSound(killer.getLocation(), Sound.ORB_PICKUP, 1, 1.9F);
                    prefix = "击杀";
                }
            }
        }

        if (totalXp > 0) {
            CC.send(MessageType.COMBAT, killer, CC.translate("&a&l" + prefix + "! " + RankUtil.getPlayerColoredName(beKilledPlayer.getUniqueId()).replace("null", "BOT") + " &6+" + numFormat.format(totalCoins) + "硬币 &b+" + numFormat.format(totalXp) + "经验值" + genesisStatus + (eventBoost > 1 ? boostString : "")));
        } else {
            CC.send(MessageType.COMBAT, killer, CC.translate("&a&l" + prefix + "! " + RankUtil.getPlayerColoredName(beKilledPlayer.getUniqueId()).replace("null", "BOT") + " &6+" + numFormat.format(totalCoins) + "硬币" + genesisStatus + (eventBoost > 1 ? boostString : "")));
        }
        if (playerProfile.isLoaded()) {
            if (beKilledPlayer instanceof Player deadplaper) {
                Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                    deadplaper.setGameMode(GameMode.SPECTATOR);
                    doRespawn(deadplaper);
                });
                String deathString = CC.translate("&c&l死亡! &7被 " + killerProfile.getFormattedName() + " &7击杀.");
                ChatComponentBuilder deathMsg = new ChatComponentBuilder(deathString)
                        .append(new ChatComponentBuilder(CC.translate(" &e&l死亡回放"))
                                .setCurrentHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.translate("&7点击查看你的死亡回放")).create()))
                                .setCurrentClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/killRecap"))
                                .create());
                CC.send(MessageType.COMBAT, (Player) beKilledPlayer, deathMsg.create());
            }
        }
    }

    private void handleGivePlayerKillReward(Player killer) {
        if ("rage_pit".equals(ThePit.getInstance().getEventFactory().getActiveEpicEventName())) {
            int limit = PlayerUtil.getPlayerHealItemLimit(killer);
            if (limit < 0) {
                limit = 2;
            }
            if (PlayerUtil.getPlayerHealItemAmount(killer) < limit) {
                killer.getInventory().addItem(new ItemBuilder(Material.BAKED_POTATO).name("&c&l愤怒的土豆").internalName("angry_potato").lore("", "&7这个土豆看起来", "&7好像格外暴躁...", "").deathDrop(true)
                        .removeOnJoin(true)
                        .canSaveToEnderChest(false)
                        .canDrop(false)
                        .isHealingItem(true)
                        .canTrade(false).build());
            }
        } else if (PlayerUtil.getAmountOfActiveHealingPerk(killer) == 0) {
            if (PlayerUtil.getPlayerHealItemAmount(killer) < PlayerUtil.getPlayerHealItemLimit(killer)) {
                killer.getInventory().addItem(new ItemBuilder(Material.GOLDEN_APPLE).canDrop(false).canSaveToEnderChest(false).deathDrop(true)
                        .removeOnJoin(true)
                        .canSaveToEnderChest(false)
                        .canDrop(false)
                        .isHealingItem(true)
                        .canTrade(false).build());
            }
        }
    }

    private void handleGameEffect(PlayerProfile killerProfile, Player killer, LivingEntity player, AtomicDouble coinsAtomic, AtomicDouble expAtomic) {
        PerkFactory perkFactory = ThePit.getInstance().getPerkFactory();
//        for (IPlayerKilledEntity ins : ThePit.getInstance().getPerkFactory()
//                .getPlayerKilledEntities()) {
//            AbstractPerk perk = (AbstractPerk) ins;
//            int perkPlayerLevel = perk.getPlayerLevel(killer);
//            if (perkPlayerLevel != -1) {
//                ins.handlePlayerKilled(perkPlayerLevel, killer, player, coinsAtomic, expAtomic);
//            }
//        }
        killerProfile.getUnlockedPerkMap().values().forEach(i -> {
            AbstractPerk abstractPerk = i.getHandle(perkFactory.getPerkMap());
            if (abstractPerk == null || !abstractPerk.isPassive()) {
                return;
            }
            if (abstractPerk instanceof IPlayerKilledEntity ins) {
                ins.handlePlayerKilled(i.getLevel(), killer, player, coinsAtomic, expAtomic);
            }
        });
        killerProfile.getChosePerk().values().forEach(i -> {
            AbstractPerk abstractPerk = i.getHandle(perkFactory.getPerkMap());
            if (abstractPerk == null || abstractPerk.isPassive()) {
                return;
            }
            if (abstractPerk instanceof IPlayerKilledEntity ins) {

                ins.handlePlayerKilled(i.getLevel(), killer, player, coinsAtomic, expAtomic);
            }
        });

        for (IPlayerKilledEntity ins : ThePit.getInstance().getEnchantmentFactor().getPlayerKilledEntities()) {
            AbstractEnchantment enchant = (AbstractEnchantment) ins;


            int level = enchant.getItemEnchantLevel(killerProfile.heldItem);
            GameEffectListener.processKilled(ins, level, killer, player, coinsAtomic, expAtomic);
            IMythicItem leggingItem = (IMythicItem) killerProfile.leggings;
            if (leggingItem != null) {
                level = enchant.getItemEnchantLevel(leggingItem);
                GameEffectListener.processKilled(ins, level, killer, player, coinsAtomic, expAtomic);
            }
        }

        if (player instanceof Player beKilledPlayer) {
            for (IPlayerBeKilledByEntity ins : ThePit.getInstance().getPerkFactory().getPlayerBeKilledByEntities()) {
                AbstractPerk perk = (AbstractPerk) ins;
                int perkPlayerLevel = perk.getPlayerLevel(beKilledPlayer);
                if (perkPlayerLevel != -1) {
                    GameEffectListener.processBeKilledByEntity(ins, perkPlayerLevel, beKilledPlayer, killer, coinsAtomic, expAtomic);
                }
            }
            for (IPlayerBeKilledByEntity ins : ThePit.getInstance().getEnchantmentFactor().getPlayerBeKilledByEntities()) {
                AbstractEnchantment enchant = (AbstractEnchantment) ins;

                int level = enchant.getItemEnchantLevel(beKilledPlayer.getItemInHand());
                if (beKilledPlayer.getItemInHand() != null
                        && beKilledPlayer.getItemInHand().getType() != Material.AIR
                        && beKilledPlayer.getItemInHand().getType() != Material.LEATHER_LEGGINGS) {
                    GameEffectListener.processBeKilledByEntity(ins, level, beKilledPlayer, killer, coinsAtomic, expAtomic);
                }
                if (beKilledPlayer.getInventory().getLeggings() != null
                        && beKilledPlayer.getInventory().getLeggings().getType() != Material.AIR) {
                    level = enchant.getItemEnchantLevel(beKilledPlayer.getInventory().getLeggings());
                    GameEffectListener.processBeKilledByEntity(ins, level, beKilledPlayer, killer, coinsAtomic, expAtomic);
                }
            }
        }

        //Genesis Boost Start
        if (killerProfile.getGenesisData().getTeam() == GenesisTeam.ANGEL && killerProfile.getGenesisData().getBoostTier() > 0) {
            expAtomic.getAndAdd(0.01 * killerProfile.getGenesisData().getBoostTier() * expAtomic.get());
        }
        if (killerProfile.getGenesisData().getTeam() == GenesisTeam.DEMON && killerProfile.getGenesisData().getBoostTier() > 0) {
            coinsAtomic.getAndAdd(0.01 * killerProfile.getGenesisData().getBoostTier() * expAtomic.get());
        }
        //Genesis Boost End
    }

    private void calculationKillReward(PlayerProfile killerProfile, PlayerProfile playerProfile, KillRecap killRecap, Player killer, AtomicDouble totalCoinsAtomic, AtomicDouble totalXpAtomic) {
        double totalXp = totalXpAtomic.get();
        double totalCoins = totalCoinsAtomic.get();

        PerkData data = killerProfile.getUnlockedPerkMap().get("XPPrestigeBoost");
        if (data != null) {
            totalXp += data.getLevel();
        }

        if (killerProfile.getStreakKills() <= 3) {
            totalXp += 4;
            totalCoins += 4;

            killRecap.setNotStreakExp(4);
            killRecap.setNotStreakCoin(4);
        }

        double streakAddon = 0;
        if (playerProfile.getStreakKills() != 0 && playerProfile.getStreakKills() % 10 == 0) {
            totalXp += (playerProfile.getStreakKills() / 10d) * 5;
            totalCoins += (playerProfile.getStreakKills() / 10d) * 5;

            streakAddon += (playerProfile.getStreakKills() / 10d) * 5;
        }

        if (killerProfile.getStreakKills() != 0 && killerProfile.getStreakKills() % 10 == 0) {
            totalXp += Math.min(20, (killerProfile.getStreakKills() / 10d) * 5);
            totalCoins += Math.min(20, (killerProfile.getStreakKills() / 10d) * 5);

            streakAddon += Math.min(20, (killerProfile.getStreakKills() / 10d) * 5);
        }

        killRecap.setStreakCoin(streakAddon);
        killRecap.setStreakExp(streakAddon);

        double levelAddon = 0;
        if (playerProfile.getPrestige() - killerProfile.getPrestige() > 0) {
            totalXp += (playerProfile.getPrestige() - killerProfile.getPrestige()) * 7;
            totalCoins += (playerProfile.getPrestige() - killerProfile.getPrestige()) * 7;

            levelAddon += (playerProfile.getPrestige() - killerProfile.getPrestige()) * 7;
        }

        if (playerProfile.getLevel() - killerProfile.getLevel() >= 30) {
            totalXp += (playerProfile.getLevel() - killerProfile.getLevel()) / 30d * 3;
            totalCoins += (playerProfile.getLevel() - killerProfile.getLevel()) / 30d * 3;

            levelAddon += (playerProfile.getLevel() - killerProfile.getLevel()) / 30d * 3;
        }

        if (playerProfile.getLevel() <= 10) {
            totalXp -= levelAddon;
            totalCoins -= levelAddon;
            levelAddon = 0;
        } else if (playerProfile.getLevel() <= 30) {
            totalXp -= 0.75 * levelAddon;
            totalCoins -= 0.75 * levelAddon;
            levelAddon = 0.25 * levelAddon;
        } else if (playerProfile.getLevel() <= 60 && playerProfile.getLevel() < killerProfile.getLevel()) {
            totalXp -= 0.5 * levelAddon;
            totalCoins -= 0.5 * levelAddon;
            levelAddon = 0.5 * levelAddon;
        }

        //val b = hasPremiumItem(killer);
        //if (b) {
        //    if (!b) {
        //        totalCoins += 10;
        //        totalXp += 10;

        //        levelAddon += 10;
        //    }
        //}

        killRecap.setLevelDisparityExp(levelAddon);
        killRecap.setLevelDisparityCoin(levelAddon);

        totalCoins = eventBoost * totalCoins;
        totalXp = eventBoost * totalXp;

        totalXpAtomic.set(totalXp);
        totalCoinsAtomic.set(totalCoins);
        //calculation kill reward - end
    }

    private KillRecap initializationKillRecap(PlayerProfile playerProfile, PlayerProfile killerProfile, Player killer, double totalCoins, double totalXp) {
        KillRecap killRecap = playerProfile.getKillRecap();

        killRecap.setKiller(killer.getUniqueId());

        killRecap.getAssistData().clear();
        killRecap.getPerk().clear();

        for (int i = 1; i < 5; i++) {
            PerkData perkData = killerProfile.getChosePerk().get(i);
            if (perkData != null) {
                killRecap.getPerk().add(perkData.getPerkInternalName());
            }
        }

        killRecap.setBaseCoin(totalCoins);
        killRecap.setBaseExp(totalXp);

        return killRecap;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onStreakChangeEvent(PitStreakKillChangeEvent event) {
        EventFactory factory = ThePit.getInstance().getEventFactory();
        if (factory.getActiveEpicEvent() != null) {
            event.setCancelled(true);
        }
    }
}
