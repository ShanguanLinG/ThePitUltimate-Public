package cn.charlotte.pit.data;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.api.PitInternalHook;
import cn.charlotte.pit.buff.BuffData;
import cn.charlotte.pit.data.operator.IOperator;
import cn.charlotte.pit.data.sub.*;
import cn.charlotte.pit.event.PitGainCoinsEvent;
import cn.charlotte.pit.event.PitGainRenownEvent;
import cn.charlotte.pit.event.PitStreakKillChangeEvent;
import cn.charlotte.pit.events.genesis.GenesisTeam;
import cn.charlotte.pit.park.IParker;
import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.MegaStreak;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.annotations.Beta;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import io.irina.backports.utils.SWMRHashTable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.mizukilab.pit.UtilKt;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.medal.impl.challenge.HundredLevelMedal;
import net.mizukilab.pit.quest.AbstractQuest;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.chat.MessageType;
import net.mizukilab.pit.util.chat.TitleUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.level.LevelUtil;
import net.mizukilab.pit.util.random.RandomUtil;
import net.mizukilab.pit.util.rank.RankUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Warning;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mongojack.JacksonMongoCollection;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/29 23:04
 */
@Skip
@JsonIgnoreProperties(ignoreUnknown = true, value = {
        "inArena",
        "streakKills",
        "strikeAssist",
        "combatTimer",
        "editingMode",
        "damageReduced",
        "damageMap",
        "strengthNum",
        "strengthTimer",
        "bountyCooldown",
        "bountyStreak",
        "lastKilledPlayer",
        "killRecap",
        "screenShare",
        "screenShareQQ",
        "mailData",
        "tempInvUsing",
        "nickPrestige",
        "nickLevel",
        "invBackups",
        "noDamageAnimations",
        "liteStreakKill",
        "lastActionTimestamp",
        "buffData",
        "streakCooldown",
        "streakCount",
        "bot",
        "lastDamageAt",
        "heldItem",
        "leggings",
        "code",
        "leggingItemStack",
        "heldItemStack"
})
public class PlayerProfile {

    public static final UUID CONSTANT_UUID_BOT_UNLOADED_PLAYER = UUID.randomUUID();
    public final static PlayerProfile NONE_PROFILE = new NullProfile();

    //两张表
    public IOperator toOperator() {
        return ThePit.getInstance().getProfileOperator().getIOperator(getPlayerUuid());
    }
    // public static Map<UUID, BukkitRunnable> LOADING_MAP = new SWMRHashTable<>(); // do it static

    // public static final Map<UUID, BukkitRunnable> SAVING_MAP = new SWMRHashTable<>(); // do it static

    //private final static Map<UUID, PlayerProfile> cacheProfile = new SWMRHashTable<>();

    public int prestige;
    public List<String> claimedMail;
    public PlayerMailData mailData;
    @JsonIgnore
    private boolean loaded;
    private String playerName;
    private String uuid;
    private String lowerName;
    private long registerTime;
    private long lastLoginTime;
    private long lastLogoutTime;
    private long totalPlayedTime;
    private long yearPlayedTime;
    private long monthPlayedTime;
    private long weekPlayedTime;
    private long todayPlayedTime;
    private int kills;
    private int assists;
    private int deaths;
    private int highestStreaks;
    private long totalDamage;
    private long meleeTotalDamage;
    private long arrowTotalDamage;
    private long hurtDamage;
    private long meleeHurtDamage;
    private long bowHurtDamage;
    private int meleeAttack;
    private int shootAttack;
    private int meleeHit;
    private int bowHit;
    private int rodUsed;
    private int rodHit;
    private int goldPicked;
    private int fishingNumber;
    private int goldenHeadEaten;
    private double experience;
    private double coins;
    private int renown;
    private int bounty;
    private int actionBounty;
    private double respawnTime;
    private volatile PlayerInv inventory; //原子写入
    private volatile PlayerEnderChest enderChest;
    private int enderChestRow;
    private volatile PlayerWarehouse warehouse;

    //每次都遍历查询，效率低下
    //所以专用Map降低大O复杂度
    @Deprecated
    private List<PerkData> unlockedPerk;
    private Map<String, PerkData> unlockedPerkMap = new SWMRHashTable<>();
    @Deprecated
    private List<PerkData> boughtPerk;
    private Map<String, PerkData> boughtPerkMap = new SWMRHashTable<>();

    private Set<String> usedCdk;
    private Map<Integer, PerkData> chosePerk;
    private double totalExp;
    private List<String> autoBuyButtons;
    private TradeLimit tradeLimit;
    private MedalData medalData;
    private QuestLimit questLimit;
    private OfferData offerData;
    //累计获得硬币
    private double grindedCoins;
    private PlayerOption playerOption;
    private PlayerBanData playerBanData;
    private boolean supporter;
    private boolean supporterGivenByAdmin;
    //补偿信息
    private int remedyLevel;
    private double remedyExp;
    private String remedyDate;
    private int totalFishTimes;
    private int totalFishTreasureTimes;
    private int totalFishTrashTimes;
    //当前的任务
    private QuestData currentQuest;
    //上一次的任务
    private QuestData lastQuest;
    //是否开启了夜晚任务
    private boolean nightQuestEnable;
    private QuestCenter questCenter;
    private GenesisData genesisData;
    private List<String> currentQuestList;
    private double maxHealth;
    private int foodLevel;
    private float moveSpeed;
    private String enchantingItem;
    private String enchantingScience;
    //玩家是否在退出其他pit服务器，用于数据保存用，防止产生脏数据

    private String enchantingBook;
    private boolean login;
    private WipedData wipedData;
    //shouldn't save fields
    private BuffData buffData;
    private boolean inArena;
    private double streakKills;
    private Cooldown combatTimer;
    private boolean editingMode;
    private double damageReduced;
    private Map<UUID, DamageData> damageMap;
    private int strengthNum;
    private Cooldown strengthTimer;
    private UUID lastKilledPlayer;
    private Cooldown bountyCooldown;
    private int bountyStreak;
    private KillRecap killRecap;
    private boolean screenShare;
    private String screenShareQQ;

    //nick
    private boolean nicked;
    private String nickName;
    private int nickPrestige;
    private int nickLevel;

    //drop
    private boolean isNotMythDrop;

    private boolean tempInvUsing;
    private boolean noDamageAnimations;
    private double liteStreakKill;
    private long lastActionTimestamp;

    private double goldStackAddon = 0.0;
    private double goldStackMax = 0.5;

    private double xpStackAddon = 0.0;
    private double xpStackMax = 1.0;

    // private List<PlayerInvBackup> invBackups;

    private int todayCompletedUber;
    private long todayCompletedUberLastRefreshed;

    private int profileFormatVersion = 0;

    private Cooldown streakCooldown;
    private int streakCount;

    private boolean bot;

    private long lastDamageAt = -1L;
    //code = -1 = allow;
    //code = -2 = disallow
    public transient volatile byte code = -1;

    private Map<String, Double> extraMaxHealth = new SWMRHashTable<>();

    public KingsQuestsData kingsQuestsData = new KingsQuestsData();

    private long lastRenameTime = 0;

    public ItemStack heldItemStack;

    public ItemStack leggingItemStack;
    public AbstractPitItem heldItem; //make it public because it didn't have any synch operation
    public AbstractPitItem leggings; //make it public because it didn't have any synch operation

    public PlayerProfile(UUID uuid, String playerName) {
        //调用默认构造函数，初始化赋值
        this();
        this.uuid = uuid.toString();
        this.playerName = playerName;
        this.lowerName = playerName.toLowerCase();
        this.mailData = new PlayerMailData(uuid, playerName);
    }

    public synchronized PlayerProfile disallow() {
        if (this.code == -1) {
            this.code = -2;
            return this;
        }
        return NONE_PROFILE;
    }

    public synchronized PlayerProfile disallowUnsafe() {
        disallow();
        return this;
    }

    public synchronized PlayerProfile allow() {
        if (this.code == -2) {
            this.code = -1;
            return this;
        }
        return NONE_PROFILE;
    }

    public PlayerProfile() {
        this.inventory = new PlayerInv();
        this.enderChest = new PlayerEnderChest();
        this.warehouse = new PlayerWarehouse();
        this.killRecap = new KillRecap();
        this.buffData = new BuffData();
        this.combatTimer = new Cooldown(0);
        this.damageMap = new SWMRHashTable<>();
        this.unlockedPerk = new ObjectArrayList<>();
        this.boughtPerk = new ObjectArrayList<>();
        this.strengthTimer = new Cooldown(0);
        this.usedCdk = new ObjectOpenHashSet<>();
        this.enderChestRow = 3;
        this.respawnTime = 0.1;

        if (ThePit.isDEBUG_SERVER()) {
            this.coins = 10000000;
            this.renown = 100000;
            this.prestige = 20;
            this.experience = LevelUtil.getLevelTotalExperience(20, 120);
        } else {
            this.coins = 5000;
        }

        this.screenShare = false;
        this.screenShareQQ = "none";

        this.chosePerk = new SWMRHashTable<>();

        this.autoBuyButtons = new ObjectArrayList<>();

        this.medalData = new MedalData();

        this.tradeLimit = new TradeLimit();
        this.questLimit = new QuestLimit();
        this.offerData = new OfferData();

        this.playerOption = new PlayerOption();
        this.playerBanData = new PlayerBanData();
        this.bountyCooldown = new Cooldown(0);
        this.currentQuestList = new ObjectArrayList<>();
        this.genesisData = new GenesisData();
        // this.invBackups = new ObjectArrayList<>();
        this.claimedMail = new ObjectArrayList<>();

        this.nightQuestEnable = false;

        this.supporter = false;
        this.supporterGivenByAdmin = false;

        this.totalFishTimes = 0;
        this.totalFishTreasureTimes = 0;
        this.totalFishTrashTimes = 0;

        this.foodLevel = 20;
        this.maxHealth = 20.0d;
        this.moveSpeed = 0.2F;

        //Level 0 : NoRemedy
        //Level 1 : Remedy + 1 Perk
        //Level 2 : Remedy + 2 Perks
        this.remedyLevel = 0;
        this.remedyExp = 0;
        this.remedyDate = "none";

        this.mailData = new PlayerMailData();

        this.loaded = false;
    }

    public boolean isChoosePerk(String intName) {
        for (PerkData value : chosePerk.values()) {
            if (value.getPerkInternalName().equals(intName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 本方法仅作为兼容桥, 向下兼容存在代码, 已最大保证线程安全性以及最大解决bug等问题
     * 重定向于 PackedOperator
     *
     * @param uuid
     * @return the raw profile, if it exists and loaded
     */
    @Deprecated
    @JsonIgnore
    public static PlayerProfile getPlayerProfileByUuid(UUID uuid) {
        PlayerProfile rawCache = getRawCache(uuid);
        if (rawCache == null) {
            return NONE_PROFILE;
        }
        return rawCache;
    }

    public AbstractPerk getActiveMegaStreakObj() {
        if (!isLoaded()) {
            return null;
        }

        PerkData perkData = getChosePerk().get(5);
        if (perkData == null) {
            return null;
        }
        AbstractPerk handle = perkData.getHandle(ThePit.getInstance().getPerkFactory().getPerkMap());
        if (handle instanceof MegaStreak mega) {
            if (getStreakKills() >= mega.getStreakNeed()) {
                return handle;
            }
        }
        return null;
    }

    public void deActiveMegaSteak() {
        setStreakKills(0);
    }

    public static PlayerProfile getRawCache(UUID uuid) {
        IOperator operator = ThePit.getInstance().getProfileOperator().getIOperator(uuid);
        if (operator == null || !operator.isLoaded()) {
            return null;
        }

        return operator.profile();
    }

//    /**
//     * 该方法用于查找玩家，如果玩家可能离线时请使用本方法
//     * 注意！请异步调用本方法，如果在主线程上调用会抛异常
//     *
//     * @param uuid 寻找的玩家UUID
//     * @return 目标玩家玩家档案，如果该玩家未注册，则返回null
//     */
//    @JsonIgnore
//    public static PlayerProfile getOrLoadPlayerProfileByUuid(UUID uuid) {
//        PlayerProfile profile = cacheProfile.get(uuid);
//        if (profile != null) {
//            return profile;
//        }
//        return loadPlayerProfileByUuid(uuid);
//    }

    /**
     * 该方法用于查找玩家，如果玩家可能离线时请使用本方法
     * 注意！请异步调用本方法，如果在主线程上调用会抛异常
     * 简称阻塞
     *
     * @param uuid 目标玩家 UUID
     * @return 目标玩家玩家档案，如果该玩家未注册，则返回null
     */
    public static PlayerProfile loadPlayerProfileByUuid(UUID uuid) {
        if (Bukkit.getServer().isPrimaryThread()) {
            new RuntimeException("Shouldn't load profile on primary thread!").printStackTrace();
        }

        PlayerProfile playerProfile = ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .findOne(Filters.eq("uuid", uuid.toString()));

        if (playerProfile != null) {
            //load mail
            loadMail(playerProfile, uuid);
            //playerProfile.loadInvBackups();
        }

        return playerProfile;
    }

    /**
     * 进行垃圾回收。。。, 可能会误回收, 我存在哥牛逼
     *
     * @param invBackups
     * @param playerProfile
     * @param add
     */
    public static void gcBackups(Iterable<PlayerInvBackup> invBackups, PlayerProfile playerProfile, boolean add) {
        long lastTime = 0;
        JacksonMongoCollection<PlayerInvBackup> invCollection = ThePit.getInstance().getMongoDB().getInvCollection();
        for (PlayerInvBackup backup : invBackups) {
            long between = Math.abs(ChronoUnit.DAYS.between(Instant.now(), Instant.ofEpochMilli(backup.getTimeStamp())));
            if (Math.abs(backup.getTimeStamp() - lastTime) < 10 * 60 * 1000
                    || between > 20) {
                lastTime = backup.getTimeStamp();
                invCollection.deleteOne(Filters.eq("backupUuid", backup.getBackupUuid()));
                continue;
            }
            lastTime = backup.getTimeStamp();
        }
    }

    /**
     * 该方法用于查找玩家，如果玩家可能离线时请使用本方法
     * 注意！请异步调用本方法，如果在主线程上调用会抛异常
     * 简称阻塞
     *
     * @param name 目标玩家名字
     * @return 目标玩家玩家档案，如果该玩家未注册，则返回null
     */
    @JsonIgnore
    public static PlayerProfile loadPlayerProfileByName(String name) {
        return ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .findOne(Filters.eq("lowerName", name.toLowerCase())); //PlayerProfile lookup
    }

    public static void saveAll() {
        ThePit.getInstance().getProfileOperator().doSaveProfiles();
    }

    public static void loadMail(PlayerProfile playerProfile, UUID uuid) {
        PlayerMailData mailData = ThePit.getInstance()
                .getMongoDB()
                .getMailCollection()
                .findOne(Filters.eq("uuid", uuid.toString()));

        if (mailData == null) {
            mailData = new PlayerMailData();
            mailData.setName(playerProfile.playerName);
            mailData.setNameLower(playerProfile.lowerName);
            mailData.setUuid(playerProfile.uuid);
        }

        mailData.cleanUp();

        playerProfile.setMailData(mailData);
    }

    public PlayerProfile save(Player player) {
        this.totalExp = experience;
        for (int i = 0; i < prestige; i++) {
            this.totalExp = totalExp + LevelUtil.getLevelTotalExperience(i, 120);
        }

        if (!this.loaded) {
            return this;
        }


        saveData(player);
        return this;
    }

    public void saveData(Player player) {
        final long now = System.currentTimeMillis();
        if (player != null) {
            this.setInventory(InventoryUtil.playerInventoryFromPlayer(player));
        }
        //if (invBackups.isEmpty() || invBackups.stream().noneMatch(backup -> now - backup.getTimeStamp() < 10 * 60 * 1000)) {
        final PlayerInvBackup backup = new PlayerInvBackup();
        backup.setUuid(this.uuid);
        backup.setTimeStamp(now);
        backup.setBackupUuid(UUID.randomUUID().toString());
        backup.setInv(this.inventory);
        backup.setChest(this.enderChest);
        backup.setTimeStamp(System.currentTimeMillis());

        backup.save();
//        gcBackups(gcBackupIterators(), this, true);
//           gcBackups(invBackups,this,false);
// }

        ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .replaceOne(Filters.eq("uuid", this.uuid), this, new ReplaceOptions().upsert(true));
    }

    public static void bootstrapProfile(PlayerProfile profile) {
        //refresh quests - start
        profile.refreshQuest();
        profile.refreshGenesisData();
        //refresh quests - end


        Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
            try {
                final Player player = Bukkit.getPlayer(profile.getPlayerUuid());
                if (player != null) {
                    if (profile.isNicked()) {
                        player.setDisplayName(profile.nickName);
                        if (profile.prestige <= 0) {
                            profile.nickPrestige = 0;
                        } else {
                            profile.nickPrestige = RandomUtil.random.nextInt(profile.getPrestige()) + 1;
                        }

                        profile.nickLevel = RandomUtil.random.nextInt(profile.getLevel() + 1);
                    }
                }
            } catch (Exception e) {
                Bukkit.getOnlinePlayers()
                        .forEach(player -> CC.printError(player, e));
            }
        });
        profile.loaded = true;
    }

    public PlayerProfile load() {

        PlayerProfile profile = PlayerProfile.loadPlayerProfileByUuid(this.getPlayerUuid());
        if (profile == null) {
            this.registerTime = System.currentTimeMillis();
            //refresh quests - start
            this.refreshQuest();
            //refresh quests - end


            this.loaded = true;

            return this;
        }

        bootstrapProfile(profile);

        return profile;
    }

    private void refreshQuest() {
        if (this.getCurrentQuestList().isEmpty()) {
            List<AbstractQuest> quests = ThePit.getInstance()
                    .getQuestFactory()
                    .getQuests();

            List<AbstractQuest> list = new ArrayList<>(quests);
            Collections.shuffle(list);

            for (int i = 0; i < 3; i++) {

                AbstractQuest quest = list.get(i);
                int level = RandomUtil.random.nextInt(quest.getMaxLevel()) + 1;
                this.getCurrentQuestList().add(quest.getQuestInternalName() + ":" + level);

            }
        }
    }

    public boolean isBanned() {
        return this.playerBanData.getEnd() > System.currentTimeMillis();
    }

    public void checkUpdate(double newExperience) {
        final Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null) {
            applyExperienceToPlayer(player);
            final int newLevel = LevelUtil.getLevelByExp(prestige, newExperience);
            final int oldLevel = this.getLevel();

            if (newLevel > oldLevel) {

                if (newLevel >= 100) {
                    new HundredLevelMedal().setProgress(this, 1);
                }

                final String newLevelTag = LevelUtil.getLevelTag(this.prestige, newExperience);
                final String oldLevelTag = LevelUtil.getLevelTag(this.prestige, this.experience);

                CC.send(MessageType.MISC, player, "&b&l天坑升级! " + oldLevelTag + " &7➠ " + newLevelTag);
                TitleUtil.sendTitle(player, CC.translate("&6&l升 级!"), CC.translate(oldLevelTag + " &7➠ " + newLevelTag), 10, 20, 10);
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
            }
        }
    }

    @JsonIgnore
    public void applyExperienceToPlayer(Player player) {
        int level = getLevel();
        player.setLevel(level);
        if (level >= ThePit.getInstance().getGlobalConfig().maxLevel) {
            this.experience = LevelUtil.getLevelTotalExperience(this.prestige, ThePit.getInstance().getGlobalConfig().maxLevel);
            player.setExp(1);
            return;
        }
        player.setExp(LevelUtil.getLevelProgress(this.prestige, this.experience));
    }

    @JsonIgnore
    public int getLevel() {
        return LevelUtil.getLevelByExp(this.prestige, this.experience);
    }

    @JsonIgnore
    public String getPrestigeColor() {
        return LevelUtil.getPrestigeColor(prestige);
    }

    @JsonIgnore
    public String geLevelColor() {
        return LevelUtil.getLevelColor(this.getLevel());
    }

    @JsonIgnore
    UUID cachedUUID;

    @JsonIgnore
    public UUID getPlayerUuid() {
        if (cachedUUID == null) {
            cachedUUID = UUID.fromString(this.uuid);
        }
        return cachedUUID;
    }

    @JsonIgnore
    public void grindCoins(double coins) {
        final Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null) {
            new PitGainCoinsEvent(player, coins).callEvent();
        }
        this.setGrindedCoins(this.getGrindedCoins() + coins);
    }

    @JsonIgnore
    public void refreshGenesisData() {
        if (ThePit.getInstance().getGlobalConfig().getGenesisSeason() != this.getGenesisData().getSeason()) {
            int boostTier = this.getGenesisData().getBoostTier();
            GenesisTeam team = this.getGenesisData().getTeam();
            this.setGenesisData(new GenesisData());
            this.getGenesisData().setBoostTier(boostTier);
            this.getGenesisData().setTeam(team);
            this.getGenesisData().setSeason(ThePit.getInstance().getGlobalConfig().getGenesisSeason());
        }
    }

    public boolean wipe(String reason) {
        PlayerProfile profile = new PlayerProfile(this.getPlayerUuid(), this.playerName);

        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player == null || !player.isOnline()) {
            if (this.isLogin()) {
                return false;
            }
            WipedData wipedData = new WipedData();
            wipedData.setWipedProfile(this);
            wipedData.setReason(reason);
            wipedData.setWipedTimestamp(System.currentTimeMillis());

            profile.setWipedData(wipedData);

            ThePit.getInstance()
                    .getMongoDB()
                    .getProfileCollection()
                    .replaceOne(Filters.eq("uuid", this.uuid), profile, new ReplaceOptions().upsert(true));
        } else {
            if (!this.loaded) {
                return false;
            }
            WipedData wipedData = new WipedData();
            wipedData.setWipedProfile(this);
            wipedData.setWipedTimestamp(System.currentTimeMillis());
            wipedData.setReason(reason);
            profile.setRegisterTime(System.currentTimeMillis());

            profile.setWipedData(wipedData);
            profile.setLoaded(true);

            // cacheProfile.put(this.getPlayerUuid(), profile);
            //TODO edit
            ThePit.getInstance().getProfileOperator().getIOperator(uuid).wipe(profile);
        }
        return true;
    }

    public boolean unWipe() {
        WipedData wipedData = this.wipedData;
        if (wipedData == null) {
            return false;
        }

        Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
                player.kickPlayer("working..");
            });
        }

        ThePit.getInstance()
                .getMongoDB()
                .getProfileCollection()
                .replaceOne(Filters.eq("uuid", this.uuid), wipedData.getWipedProfile(), new ReplaceOptions().upsert(true));

        return true;
    }

    public String getFormattedName() {
        return getFormattedLevelTag() + " " + RankUtil.getPlayerColoredName(this.getPlayerUuid());
    }

    public String getFormattedNameWithRoman() {
        return getFormattedLevelTagWithRoman() + " " + RankUtil.getPlayerColoredName(this.getPlayerUuid());
    }

    public String getFormattedLevelTag() {
        if (nicked) {
            return LevelUtil.getLevelTag(this.nickPrestige, this.nickLevel);
        }
        return LevelUtil.getLevelTag(this.getPrestige(), this.getLevel());
    }

    public String getFormattedLevelTagTabSpec() {
        if (nicked) {
            return LevelUtil.getLevelTagTabListSpec(this.nickPrestige, this.nickLevel);
        }
        return LevelUtil.getLevelTagTabListSpec(this.getPrestige(), this.getLevel());
    }

    public String getFormattedLevelTagWithRoman() {
        if (nicked) {
            return LevelUtil.getLevelTagWithRoman(this.nickPrestige, this.nickLevel);
        }
        return LevelUtil.getLevelTagWithRoman(this.getPrestige(), this.getLevel());
    }

    public void addLiteStreakKill(double value) {
        this.liteStreakKill += value;
        if (liteStreakKill > 1) {
            final int streakKill = (int) liteStreakKill;
            liteStreakKill = liteStreakKill - streakKill;

            this.streakKills += liteStreakKill;
        }
    }

    public int addAndGetStreakNumberShort() {
        if (streakCooldown == null || streakCooldown.hasExpired()) {
            streakCount = 0;
            streakCount++;
            streakCooldown = new Cooldown(10, TimeUnit.SECONDS);
            return streakCount;
        }

        streakCooldown = new Cooldown(10, TimeUnit.SECONDS);
        streakCount++;

        return streakCount;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @JsonIgnore
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLowerName() {
        return this.lowerName;
    }

    public void setLowerName(String lowerName) {
        this.lowerName = lowerName;
    }

    public long getRegisterTime() {
        return this.registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public long getLastLogoutTime() {
        return this.lastLogoutTime;
    }

    public void setLastLogoutTime(long lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public long getTotalPlayedTime() {
        return this.totalPlayedTime;
    }

    public void setTotalPlayedTime(long totalPlayedTime) {
        this.totalPlayedTime = totalPlayedTime;
    }

    public long getYearPlayedTime() {
        return this.yearPlayedTime;
    }

    public void setYearPlayedTime(long yearPlayedTime) {
        this.yearPlayedTime = yearPlayedTime;
    }

    public long getMonthPlayedTime() {
        return this.monthPlayedTime;
    }

    public void setMonthPlayedTime(long monthPlayedTime) {
        this.monthPlayedTime = monthPlayedTime;
    }

    public long getWeekPlayedTime() {
        return this.weekPlayedTime;
    }

    public void setWeekPlayedTime(long weekPlayedTime) {
        this.weekPlayedTime = weekPlayedTime;
    }

    public long getTodayPlayedTime() {
        return this.todayPlayedTime;
    }

    public void setTodayPlayedTime(long todayPlayedTime) {
        this.todayPlayedTime = todayPlayedTime;
    }

    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getAssists() {
        return this.assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getHighestStreaks() {
        return this.highestStreaks;
    }

    public void setHighestStreaks(int highestStreaks) {
        this.highestStreaks = highestStreaks;
    }

    public long getTotalDamage() {
        return this.totalDamage;
    }

    public void setTotalDamage(long totalDamage) {
        this.totalDamage = totalDamage;
    }

    public long getMeleeTotalDamage() {
        return this.meleeTotalDamage;
    }

    public void setMeleeTotalDamage(long meleeTotalDamage) {
        this.meleeTotalDamage = meleeTotalDamage;
    }

    public long getArrowTotalDamage() {
        return this.arrowTotalDamage;
    }

    public void setArrowTotalDamage(long arrowTotalDamage) {
        this.arrowTotalDamage = arrowTotalDamage;
    }

    public long getHurtDamage() {
        return this.hurtDamage;
    }

    public void setHurtDamage(long hurtDamage) {
        this.hurtDamage = hurtDamage;
    }

    public long getMeleeHurtDamage() {
        return this.meleeHurtDamage;
    }

    public void setMeleeHurtDamage(long meleeHurtDamage) {
        this.meleeHurtDamage = meleeHurtDamage;
    }

    public long getBowHurtDamage() {
        return this.bowHurtDamage;
    }

    public void setBowHurtDamage(long bowHurtDamage) {
        this.bowHurtDamage = bowHurtDamage;
    }

    public int getMeleeAttack() {
        return this.meleeAttack;
    }

    public void setMeleeAttack(int meleeAttack) {
        this.meleeAttack = meleeAttack;
    }

    public int getShootAttack() {
        return this.shootAttack;
    }

    public void setShootAttack(int shootAttack) {
        this.shootAttack = shootAttack;
    }

    public int getMeleeHit() {
        return this.meleeHit;
    }

    public void setMeleeHit(int meleeHit) {
        this.meleeHit = meleeHit;
    }

    public int getBowHit() {
        return this.bowHit;
    }

    public void setBowHit(int bowHit) {
        this.bowHit = bowHit;
    }

    public int getRodUsed() {
        return this.rodUsed;
    }

    public void setRodUsed(int rodUsed) {
        this.rodUsed = rodUsed;
    }

    public int getRodHit() {
        return this.rodHit;
    }

    public void setRodHit(int rodHit) {
        this.rodHit = rodHit;
    }

    public int getGoldPicked() {
        return this.goldPicked;
    }

    public void setGoldPicked(int goldPicked) {
        this.goldPicked = goldPicked;
    }

    public int getFishingNumber() {
        return this.fishingNumber;
    }

    public void setFishingNumber(int fishingNumber) {
        this.fishingNumber = fishingNumber;
    }

    public int getGoldenHeadEaten() {
        return this.goldenHeadEaten;
    }

    public void setGoldenHeadEaten(int goldenHeadEaten) {
        this.goldenHeadEaten = goldenHeadEaten;
    }

    public double getExperience() {
        return this.experience;
    }

    public void setExperience(double experience) {
        if (loaded) {
            checkUpdate(experience);
        }
        this.experience = experience;
    }

    public int getPrestige() {
        return this.prestige;
    }

    public void setPrestige(int prestige) {
        if (ThePit.isDEBUG_SERVER()) {
            prestige = 20;
        } else {
            this.prestige = prestige;
        }
    }

    public double getCoins() {
        return this.coins;
    }

    public void setCoins(double coins) {
        this.coins = coins;
    }

    public int getRenown() {
        return this.renown;
    }

    public void setRenown(int renown) {
        final Player player = Bukkit.getPlayer(this.getPlayerUuid());
        if (player != null && renown > this.renown) {
            new PitGainRenownEvent(player, renown - this.renown).callEvent();
        }
        this.renown = renown;
    }

    public int getBounty() {
        return this.bounty;
    }

    public String bountyColor() {
        //  Player player = Bukkit.getPlayer(getPlayerUuid());
        // if (player != null) {
//            boolean itemHasEnchant = Limit24520Ench.instance.isItemHasEnchant(player.getInventory().getLeggings());
//            if (itemHasEnchant) {
//                return "&d";
//            }
        //}


        GenesisData genesisData1 = getGenesisData();
        GenesisTeam team = genesisData1.getTeam();
        if (ThePit.getInstance().getGlobalConfig().isGenesisEnable()) {
            return switch (team) {
                case ANGEL -> "&b";
                case DEMON -> "&c";
                default -> "&6";
            };
        }
        return "&6";
    }

    public void setBounty(int bounty) {
    /*    if (bounty >= 5000) {
           final Player player = Bukkit.getPlayer(this.getPlayerUuid());
          if (player != null) {
                new MaxBountyMedal().setProgress(PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()), 1);
           }
        }*/
        this.bounty = bounty;
    }

    public int getActionBounty() {
        return this.actionBounty;
    }

    public void setActionBounty(int actionBounty) {
        this.actionBounty = actionBounty;
    }

    public double getRespawnTime() {
        return this.respawnTime;
    }

    public void setRespawnTime(double respawnTime) {
        this.respawnTime = respawnTime;
    }

    public PlayerInv getInventory() {
        return this.inventory;
    }


    public void setInventory(PlayerInv inv) {
        if (this.tempInvUsing) {
            return;
        }
        this.inventory = inv;
    }

    @Beta
    public PlayerProfile setInventoryUnsafe(PlayerInv inv) {
        this.inventory = inv;
        return this;
    }

    public PlayerEnderChest getEnderChest() {
        return this.enderChest;
    }

    public void setEnderChest(PlayerEnderChest enderChest) {
        this.enderChest = enderChest;
    }

    public int getEnderChestRow() {
        return this.enderChestRow;
    }

    public void setEnderChestRow(int enderChestRow) {
        this.enderChestRow = enderChestRow;
    }

    public PlayerWarehouse getWarehouse() {
        if (warehouse == null) {
            warehouse = new PlayerWarehouse();
        }
        return warehouse;
    }

    public void setWarehouse(PlayerWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Deprecated
    public List<PerkData> getUnlockedPerk() {
        return this.unlockedPerk;
    }

    public void setUnlockedPerk(List<PerkData> unlockedPerk) {
        this.unlockedPerk = unlockedPerk;
    }

    @Deprecated
    public List<PerkData> getBoughtPerk() {
        return this.boughtPerk;
    }

    public void setBoughtPerk(List<PerkData> boughtPerk) {
        this.boughtPerk = boughtPerk;
    }

    public Set<String> getUsedCdk() {
        return this.usedCdk;
    }

    public void setUsedCdk(Set<String> usedCdk) {
        this.usedCdk = usedCdk;
    }

    public Map<Integer, PerkData> getChosePerk() {
        return this.chosePerk;
    }

    public void setChosePerk(Map<Integer, PerkData> chosePerk) {
        this.chosePerk = chosePerk;
    }

    public double getTotalExp() {
        return this.totalExp;
    }

    public void setTotalExp(double totalExp) {
        this.totalExp = totalExp;
    }

    public List<String> getAutoBuyButtons() {
        return this.autoBuyButtons;
    }

    public void setAutoBuyButtons(List<String> autoBuyButtons) {
        this.autoBuyButtons = autoBuyButtons;
    }

    public TradeLimit getTradeLimit() {
        return this.tradeLimit;
    }

    public void setTradeLimit(TradeLimit tradeLimit) {
        this.tradeLimit = tradeLimit;
    }

    public MedalData getMedalData() {
        return this.medalData;
    }

    public void setMedalData(MedalData medalData) {
        this.medalData = medalData;
    }

    public QuestLimit getQuestLimit() {
        return this.questLimit;
    }

    public void setQuestLimit(QuestLimit questLimit) {
        this.questLimit = questLimit;
    }

    public OfferData getOfferData() {
        return this.offerData;
    }

    public void setOfferData(OfferData offerData) {
        this.offerData = offerData;
    }

    public double getGrindedCoins() {
        return this.grindedCoins;
    }

    public void setGrindedCoins(double grindedCoins) {
        this.grindedCoins = grindedCoins;
    }

    public PlayerOption getPlayerOption() {
        return this.playerOption;
    }

    public void setPlayerOption(PlayerOption playerOption) {
        this.playerOption = playerOption;
    }

    public PlayerBanData getPlayerBanData() {
        return this.playerBanData;
    }

    public void setPlayerBanData(PlayerBanData playerBanData) {
        this.playerBanData = playerBanData;
    }

    public boolean isSupporter() {
        PitInternalHook api = ThePit.getApi();
        if (api == null) return false;

        if (supporter && !api.getRemoveSupportWhenNoPermission()) return true;

        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return false;

        return player.hasPermission(api.getPitSupportPermission());
    }

    public void setSupporter(boolean supporter) {
        this.supporter = supporter;
    }

    public boolean isSupporterGivenByAdmin() {
        return this.supporterGivenByAdmin;
    }

    public void setSupporterGivenByAdmin(boolean supporterGivenByAdmin) {
        this.supporterGivenByAdmin = supporterGivenByAdmin;
    }

    public int getRemedyLevel() {
        return this.remedyLevel;
    }

    public void setRemedyLevel(int remedyLevel) {
        this.remedyLevel = remedyLevel;
    }

    public double getRemedyExp() {
        return this.remedyExp;
    }

    public void setRemedyExp(double remedyExp) {
        this.remedyExp = remedyExp;
    }

    public String getRemedyDate() {
        return this.remedyDate;
    }

    public void setRemedyDate(String remedyDate) {
        this.remedyDate = remedyDate;
    }

    public int getTotalFishTimes() {
        return this.totalFishTimes;
    }

    public void setTotalFishTimes(int totalFishTimes) {
        this.totalFishTimes = totalFishTimes;
    }

    public int getTotalFishTreasureTimes() {
        return this.totalFishTreasureTimes;
    }

    public void setTotalFishTreasureTimes(int totalFishTreasureTimes) {
        this.totalFishTreasureTimes = totalFishTreasureTimes;
    }

    public int getTotalFishTrashTimes() {
        return this.totalFishTrashTimes;
    }

    public void setTotalFishTrashTimes(int totalFishTrashTimes) {
        this.totalFishTrashTimes = totalFishTrashTimes;
    }

    public QuestData getCurrentQuest() {
        return this.currentQuest;
    }

    public void setCurrentQuest(QuestData currentQuest) {
        this.currentQuest = currentQuest;
    }

    public QuestData getLastQuest() {
        return this.lastQuest;
    }

    public void setLastQuest(QuestData lastQuest) {
        this.lastQuest = lastQuest;
    }

    public boolean isNightQuestEnable() {
        return this.nightQuestEnable;
    }

    public void setNightQuestEnable(boolean nightQuestEnable) {
        this.nightQuestEnable = nightQuestEnable;
    }

    public QuestCenter getQuestCenter() {
        return this.questCenter;
    }

    public void setQuestCenter(QuestCenter questCenter) {
        this.questCenter = questCenter;
    }

    public GenesisData getGenesisData() {
        return this.genesisData;
    }

    public void setGenesisData(GenesisData genesisData) {
        this.genesisData = genesisData;
    }

    public List<String> getCurrentQuestList() {
        return this.currentQuestList;
    }

    public void setCurrentQuestList(List<String> currentQuestList) {
        this.currentQuestList = currentQuestList;
    }

    public double getMaxHealth() {
        return 20.0 + getExtraMaxHealthValue();
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public float getMoveSpeed() {
        return this.moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public String getEnchantingItem() {
        return this.enchantingItem;
    }

    public void setEnchantingItem(String enchantingItem) {
        this.enchantingItem = enchantingItem;
    }

    public String getEnchantingScience() {
        return this.enchantingScience;
    }

    public void setEnchantingScience(String enchantingScience) {
        this.enchantingScience = enchantingScience;
    }

    public boolean isLogin() {
        return this.login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public WipedData getWipedData() {
        return this.wipedData;
    }

    public void setWipedData(WipedData wipedData) {
        this.wipedData = wipedData;
    }

    public List<String> getClaimedMail() {
        return this.claimedMail;
    }

    public void setClaimedMail(List<String> claimedMail) {
        this.claimedMail = claimedMail;
    }

    public BuffData getBuffData() {
        return this.buffData;
    }

    public void setBuffData(BuffData buffData) {
        this.buffData = buffData;
    }

    public boolean isInArena() {
        return this.inArena;
    }

    public void setInArena(boolean inArena) {
        if (inArena && !this.inArena) {
            final Player player = Bukkit.getPlayer(getPlayerUuid());
            if (player != null) {
                UtilKt.releaseItem(player);
            }
        }

        this.inArena = inArena;
    }

    public double getStreakKills() {
        return this.streakKills;
    }

    public void setStreakKills(double kills) {
        final PitStreakKillChangeEvent event = new PitStreakKillChangeEvent(this, this.streakKills, kills);
        if (kills > 0) {
            event.callEvent();
        }
        if (event.isCancelled()) {
            return;
        }

        this.streakKills = kills;
    }

    public static synchronized final void saveAllSync(boolean silent) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                if (profile.isLoaded()) {
                    profile.setInventory(InventoryUtil.playerInventoryFromPlayer(player));
                    profile.save(player);
                    if (!silent) {
                        CC.boardCast0("&6&l公告! &7正在保存 " + player.getDisplayName() + " 玩家的数据...");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Cooldown getCombatTimer() {
        return this.combatTimer;
    }

    public void setCombatTimer(Cooldown combatTimer) {
        this.combatTimer = combatTimer;
    }

    public boolean isEditingMode() {
        return this.editingMode;
    }

    public void setEditingMode(boolean editingMode) {
        this.editingMode = editingMode;
    }

    public double getDamageReduced() {
        return this.damageReduced;
    }

    public void setDamageReduced(double damageReduced) {
        this.damageReduced = damageReduced;
    }

    public Map<UUID, DamageData> getDamageMap() {
        return this.damageMap;
    }

    public void setDamageMap(Map<UUID, DamageData> damageMap) {
        this.damageMap = damageMap;
    }

    public int getStrengthNum() {
        return this.strengthNum;
    }

    public void setStrengthNum(int strengthNum) {
        this.strengthNum = strengthNum;
    }

    public Cooldown getStrengthTimer() {
        return this.strengthTimer;
    }

    public void setStrengthTimer(Cooldown strengthTimer) {
        this.strengthTimer = strengthTimer;
    }

    public UUID getLastKilledPlayer() {
        return this.lastKilledPlayer;
    }

    public void setLastKilledPlayer(UUID lastKilledPlayer) {
        this.lastKilledPlayer = lastKilledPlayer;
    }

    public Cooldown getBountyCooldown() {
        return this.bountyCooldown;
    }

    public void setBountyCooldown(Cooldown bountyCooldown) {
        this.bountyCooldown = bountyCooldown;
    }

    public int getBountyStreak() {
        return this.bountyStreak;
    }

    public void setBountyStreak(int bountyStreak) {
        this.bountyStreak = bountyStreak;
    }

    public KillRecap getKillRecap() {
        return this.killRecap;
    }

    public void setKillRecap(KillRecap killRecap) {
        this.killRecap = killRecap;
    }

    public boolean isScreenShare() {
        return this.screenShare;
    }

    public void setScreenShare(boolean screenShare) {
        this.screenShare = screenShare;
    }

    public String getScreenShareQQ() {
        return this.screenShareQQ;
    }

    public void setScreenShareQQ(String screenShareQQ) {
        this.screenShareQQ = screenShareQQ;
    }

    public PlayerMailData getMailData() {
        return this.mailData;
    }

    public void setMailData(PlayerMailData mailData) {
        this.mailData = mailData;
    }

    public boolean isNicked() {
        return this.nicked;
    }

    public void setNicked(boolean nicked) {
        this.nicked = nicked;
    }


    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void updateNick() {
        Player player = Bukkit.getPlayer(getPlayerUuid());
        if (player != null) {
            IParker parker = ThePit.getInstance().getParker();
            parker.hideAlways(player);
            parker.showAlways(player);
        }
    }

    public int getNickPrestige() {
        return this.nickPrestige;
    }

    public void setNickPrestige(int nickPrestige) {
        this.nickPrestige = nickPrestige;
    }

    public int getNickLevel() {
        return this.nickLevel;
    }

    public void setNickLevel(int nickLevel) {
        this.nickLevel = nickLevel;
    }

    public boolean isNotMythDrop() {
        return isNotMythDrop;
    }

    public void setNotMythDrop(boolean notMythDrop) {
        isNotMythDrop = notMythDrop;
    }

    public boolean isTempInvUsing() {
        return this.tempInvUsing;
    }

    public void setTempInvUsing(boolean tempInvUsing) {
        this.tempInvUsing = tempInvUsing;
    }

    public boolean isNoDamageAnimations() {
        return this.noDamageAnimations;
    }

    public void setNoDamageAnimations(boolean noDamageAnimations) {
        this.noDamageAnimations = noDamageAnimations;
    }

    public double getLiteStreakKill() {
        return this.liteStreakKill;
    }

    public void setLiteStreakKill(double liteStreakKill) {
        this.liteStreakKill = liteStreakKill;
    }

    public long getLastActionTimestamp() {
        return this.lastActionTimestamp;
    }

    public void setLastActionTimestamp(long lastActionTimestamp) {
        this.lastActionTimestamp = lastActionTimestamp;
    }

    @Beta
    @Warning
    public Iterable<PlayerInvBackup> getInvBackups() {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getLogger().warning("Executing fetch inventory backups on main thread for " + this.playerName);
        }
        return ThePit.getInstance().getMongoDB().
                getInvCollection().find(Filters.eq("uuid", uuid));
    }

    @Beta
    @Warning
    public Iterable<PlayerInvBackup> gcBackupIterators() {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getLogger().warning("Executing fetch inventory backups on main thread for " + this.playerName);
        }
        return ThePit.getInstance().getMongoDB().
                getInvCollection().find(Filters.eq("uuid", uuid)).projection(Projections.include("timeStamp", "backupUuid", "uuid"));
    }

    public double getGoldStackAddon() {
        return goldStackAddon;
    }

    public void setGoldStackAddon(double goldStackAddon) {
        this.goldStackAddon = goldStackAddon;
    }

    public double getGoldStackMax() {
        return goldStackMax;
    }

    public void setGoldStackMax(double goldStackMax) {
        this.goldStackMax = goldStackMax;
    }

    public double getXpStackAddon() {
        return xpStackAddon;
    }

    public void setXpStackAddon(double xpStackAddon) {
        this.xpStackAddon = xpStackAddon;
    }

    public double getXpStackMax() {
        return xpStackMax;
    }

    public void setXpStackMax(double xpStackMax) {
        this.xpStackMax = xpStackMax;
    }

    public int getTodayCompletedUber() {
        return todayCompletedUber;
    }

    public void setTodayCompletedUber(int todayCompletedUber) {
        this.todayCompletedUber = todayCompletedUber;
    }

    public long getTodayCompletedUberLastRefreshed() {
        return todayCompletedUberLastRefreshed;
    }

    public void setTodayCompletedUberLastRefreshed(long todayCompletedUberLastRefreshed) {
        this.todayCompletedUberLastRefreshed = todayCompletedUberLastRefreshed;
    }

    public Cooldown getStreakCooldown() {
        return streakCooldown;
    }

    public void setStreakCooldown(Cooldown streakCooldown) {
        this.streakCooldown = streakCooldown;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public long getLastDamageAt() {
        return lastDamageAt;
    }

    public void setLastDamageAt(long lastDamageAt) {
        this.lastDamageAt = lastDamageAt;
    }

    public Map<String, Double> getExtraMaxHealth() {
        return extraMaxHealth;
    }

    public void setExtraMaxHealth(HashMap<String, Double> extraMaxHealth) {
        this.extraMaxHealth = extraMaxHealth;
    }

    public double getExtraMaxHealthValue() {
        Collection<Double> values = extraMaxHealth.values();
        double valued = 0D;
        for (Double value : values) {
            valued += value;
        }
        return valued;
    }

    public String getEnchantingBook() {
        return enchantingBook;
    }

    public void setEnchantingBook(String enchantingBook) {
        this.enchantingBook = enchantingBook;
    }

    public Map<String, PerkData> getUnlockedPerkMap() {
        return unlockedPerkMap;
    }

    public Map<String, PerkData> getBoughtPerkMap() {
        return boughtPerkMap;
    }

    public void setUnlockedPerkMap(Map<String, PerkData> unlockedPerkMap) {
        this.unlockedPerkMap = unlockedPerkMap;
    }

    public void setBoughtPerkMap(Map<String, PerkData> boughtPerkMap) {
        this.boughtPerkMap = boughtPerkMap;
    }

    public int getProfileFormatVersion() {
        return profileFormatVersion;
    }

    public void setProfileFormatVersion(int profileFormatVersion) {
        this.profileFormatVersion = profileFormatVersion;
    }

    public long getLastRenameTime() {
        return lastRenameTime;
    }

    public void setLastRenameTime(long lastRenameTime) {
        this.lastRenameTime = lastRenameTime;
    }

    public static class NullProfile extends PlayerProfile {
        public NullProfile() {
            super(PlayerProfile.CONSTANT_UUID_BOT_UNLOADED_PLAYER, "NotLoadPlayer");
        }

        public boolean isLoaded() {
            return false;
        }



        @Override
        public PlayerProfile save(Player player) {
            return this;
        }

        @Override
        public PlayerProfile disallow() {
            return this;
        }

        @Override
        public PlayerProfile allow() {
            return this;
        }

        @Override
        public boolean isBot() {
            return true;
        }

        @Override
        public void saveData(Player player) {
            throw new UnsupportedOperationException("Never save the not loaded player");
        }

        @Override
        public IOperator toOperator() {
            return super.toOperator();
        }

        @Override
        public void setLastLoginTime(long lastLoginTime) {
        }

        @Override
        public void setRegisterTime(long registerTime) {
        }

        @Override
        public void setTotalPlayedTime(long totalPlayedTime) {
        }

        @Override
        public void setLastLogoutTime(long lastLogoutTime) {
        }

        @Override
        public void setYearPlayedTime(long yearPlayedTime) {
        }

        @Override
        public void setMonthPlayedTime(long monthPlayedTime) {
        }

        @Override
        public void setWeekPlayedTime(long weekPlayedTime) {
        }

        @Override
        public void setTodayPlayedTime(long todayPlayedTime) {
        }

        @Override
        public void setKills(int kills) {
        }

        @Override
        public void setAssists(int assists) {
        }

        @Override
        public void setDeaths(int deaths) {
        }

        @Override
        public void setHighestStreaks(int highestStreaks) {
        }

        @Override
        public void setTotalDamage(long totalDamage) {
        }

        @Override
        public void setMeleeTotalDamage(long meleeTotalDamage) {
        }

        @Override
        public void setArrowTotalDamage(long arrowTotalDamage) {
        }

        @Override
        public void setHurtDamage(long hurtDamage) {
        }

        @Override
        public void setMeleeHurtDamage(long meleeHurtDamage) {
        }

        @Override
        public void setBowHurtDamage(long bowHurtDamage) {
        }

        @Override
        public void setMeleeAttack(int meleeAttack) {
        }

        @Override
        public void setShootAttack(int shootAttack) {
        }

        @Override
        public void setMeleeHit(int meleeHit) {
        }

        @Override
        public void setBowHit(int bowHit) {
        }

        @Override
        public void setRodUsed(int rodUsed) {
        }

        @Override
        public void setRodHit(int rodHit) {
        }

        @Override
        public void setGoldPicked(int goldPicked) {
        }

        @Override
        public void setFishingNumber(int fishingNumber) {
        }

        @Override
        public void setGoldenHeadEaten(int goldenHeadEaten) {
        }

        @Override
        public void setExperience(double experience) {
        }

        @Override
        public void setPrestige(int prestige) {
        }

        @Override
        public void setCoins(double coins) {
        }

        @Override
        public void setRenown(int renown) {
        }

        @Override
        public void setBounty(int bounty) {
        }

        @Override
        public void setActionBounty(int actionBounty) {
        }

        @Override
        public void setRespawnTime(double respawnTime) {
        }

        @Override
        public void setInventory(PlayerInv inv) {
        }

        @Override
        public void setEnderChest(PlayerEnderChest enderChest) {
        }

        @Override
        public PlayerProfile setInventoryUnsafe(PlayerInv inv) {
            return this;
        }

        @Override
        public void setEnderChestRow(int enderChestRow) {
        }

        @Override
        public void setUnlockedPerk(List<PerkData> unlockedPerk) {
        }

        @Override
        public void setBoughtPerk(List<PerkData> boughtPerk) {
        }

        @Override
        public void setUsedCdk(Set<String> usedCdk) {
        }

        @Override
        public void setChosePerk(Map<Integer, PerkData> chosePerk) {
        }

        @Override
        public void setTotalExp(double totalExp) {
        }

        @Override
        public void setAutoBuyButtons(List<String> autoBuyButtons) {
        }

        @Override
        public void setTradeLimit(TradeLimit tradeLimit) {
        }

        @Override
        public void setMedalData(MedalData medalData) {
        }

        @Override
        public void setQuestLimit(QuestLimit questLimit) {
        }

        @Override
        public void setOfferData(OfferData offerData) {
        }

        @Override
        public void setGrindedCoins(double grindedCoins) {
        }

        @Override
        public void setPlayerOption(PlayerOption playerOption) {
        }

        @Override
        public void setPlayerBanData(PlayerBanData playerBanData) {
        }

        @Override
        public void setSupporter(boolean supporter) {

        }

        @Override
        public void setSupporterGivenByAdmin(boolean supporterGivenByAdmin) {
        }

        @Override
        public void setRemedyLevel(int remedyLevel) {
        }

        @Override
        public void setRemedyExp(double remedyExp) {
        }

        @Override
        public void setRemedyDate(String remedyDate) {
        }

        @Override
        public void setTotalFishTimes(int totalFishTimes) {
        }

        @Override
        public void setTotalFishTreasureTimes(int totalFishTreasureTimes) {
        }

        @Override
        public void setTotalFishTrashTimes(int totalFishTrashTimes) {
        }

        @Override
        public void setCurrentQuest(QuestData currentQuest) {

        }

        @Override
        public void setLastQuest(QuestData lastQuest) {
        }

        @Override
        public void setNightQuestEnable(boolean nightQuestEnable) {
        }

        @Override
        public void setQuestCenter(QuestCenter questCenter) {
        }

        @Override
        public void setGenesisData(GenesisData genesisData) {
        }

        @Override
        public void setCurrentQuestList(List<String> currentQuestList) {
        }

        @Override
        public void setMaxHealth(double maxHealth) {
        }

        @Override
        public void setFoodLevel(int foodLevel) {
        }

        @Override
        public void setMoveSpeed(float moveSpeed) {
        }

        @Override
        public void setEnchantingItem(String enchantingItem) {
        }

        @Override
        public void setEnchantingScience(String enchantingScience) {
        }

        @Override
        public void setLogin(boolean login) {
        }

        @Override
        public void setWipedData(WipedData wipedData) {
        }

        @Override
        public void setBuffData(BuffData buffData) {
        }

        @Override
        public void setClaimedMail(List<String> claimedMail) {
        }

        @Override
        public void setInArena(boolean inArena) {
        }

        @Override
        public void setStreakKills(double kills) {
        }

        @Override
        public void setCombatTimer(Cooldown combatTimer) {
        }

        @Override
        public void setEditingMode(boolean editingMode) {
        }

        @Override
        public void setDamageReduced(double damageReduced) {
        }

        @Override
        public void setDamageMap(Map<UUID, DamageData> damageMap) {
        }

        @Override
        public void setStrengthNum(int strengthNum) {
        }

        @Override
        public void setStrengthTimer(Cooldown strengthTimer) {
        }

        @Override
        public void setLastKilledPlayer(UUID lastKilledPlayer) {
        }

        @Override
        public void setBountyCooldown(Cooldown bountyCooldown) {
        }

        @Override
        public void setBountyStreak(int bountyStreak) {
        }

        @Override
        public void setKillRecap(KillRecap killRecap) {
        }

        @Override
        public void setScreenShare(boolean screenShare) {
        }

        @Override
        public void setScreenShareQQ(String screenShareQQ) {
        }

        @Override
        public void setMailData(PlayerMailData mailData) {
        }

        @Override
        public void setNicked(boolean nicked) {
        }

        @Override
        public void setNickPrestige(int nickPrestige) {
        }

        @Override
        public void setNickLevel(int nickLevel) {
        }

        @Override
        public void setTempInvUsing(boolean tempInvUsing) {
        }

        @Override
        public void setNoDamageAnimations(boolean noDamageAnimations) {

        }

        @Override
        public void setLiteStreakKill(double liteStreakKill) {
        }

        @Override
        public void setLastActionTimestamp(long lastActionTimestamp) {
        }

        @Override
        public void setGoldStackAddon(double goldStackAddon) {
        }

        @Override
        public void setGoldStackMax(double goldStackMax) {
        }

        @Override
        public void setXpStackAddon(double xpStackAddon) {
        }

        @Override
        public void setXpStackMax(double xpStackMax) {
        }

        @Override
        public Iterable<PlayerInvBackup> gcBackupIterators() {
            return new ObjectArrayList<>();
        }

        @Override
        public Iterable<PlayerInvBackup> getInvBackups() {
            return new ObjectArrayList<>();
        }

        @Override
        public void setTodayCompletedUber(int todayCompletedUber) {
        }

        @Override
        public void setTodayCompletedUberLastRefreshed(long todayCompletedUberLastRefreshed) {
        }

        @Override
        public void setStreakCooldown(Cooldown streakCooldown) {
        }

        @Override
        public void setStreakCount(int streakCount) {
        }

        @Override
        public void setBot(boolean bot) {
        }

        @Override
        public void setLastDamageAt(long lastDamageAt) {
        }

        @Override
        public void setExtraMaxHealth(HashMap<String, Double> extraMaxHealth) {
        }

        @Override
        public void setEnchantingBook(String enchantingBook) {
        }

        @Override
        public void setUnlockedPerkMap(Map<String, PerkData> unlockedPerkMap) {
        }

        @Override
        public void setProfileFormatVersion(int profileFormatVersion) {
        }

        @Override
        public void setLastRenameTime(long lastRenameTime) {
        }

        @Override
        public void setBoughtPerkMap(Map<String, PerkData> boughtPerkMap) {

        }

        @Override
        public boolean isInArena() {
            return false;
        }

        @Override
        public int getBounty() {
            return 0;
        }
    }
}
