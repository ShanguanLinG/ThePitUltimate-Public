package cn.charlotte.pit;

import cn.charlotte.pit.api.PitInternalHook;
import cn.charlotte.pit.api.PointsAPI;
import cn.charlotte.pit.buff.BuffFactory;
import cn.charlotte.pit.data.FixedRewardData;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.operator.IProfilerOperator;
import cn.charlotte.pit.event.OriginalTimeChangeEvent;
import cn.charlotte.pit.events.EventFactory;
import cn.charlotte.pit.events.EventsHandler;
import cn.charlotte.pit.park.IParker;
import cn.charlotte.pit.perk.AbstractPerk;
import cn.charlotte.pit.perk.PerkFactory;
import cn.charlotte.pit.util.hologram.packet.PacketHologramRunnable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.mizukilab.pit.actionbar.IActionBarManager;
import net.mizukilab.pit.config.ConfigManager;
import net.mizukilab.pit.config.PitGlobalConfig;
import net.mizukilab.pit.config.PitWorldConfig;
import net.mizukilab.pit.database.MongoDB;
import net.mizukilab.pit.enchantment.EnchantmentFactor;
import net.mizukilab.pit.hologram.HologramFactory;
import net.mizukilab.pit.item.IItemFactory;
import net.mizukilab.pit.item.ItemFactor;
import net.mizukilab.pit.license.CommonLoader;
import net.mizukilab.pit.listener.SafetyJoinListener;
import net.mizukilab.pit.map.MapSelector;
import net.mizukilab.pit.medal.MedalFactory;
import net.mizukilab.pit.minigame.MiniGameController;
import net.mizukilab.pit.movement.PlayerMoveHandler;
import net.mizukilab.pit.npc.NpcFactory;
import net.mizukilab.pit.pet.PetFactory;
import net.mizukilab.pit.quest.QuestFactory;
import net.mizukilab.pit.runnable.ClearRunnable;
import net.mizukilab.pit.runnable.DayNightCycleRunnable;
import net.mizukilab.pit.runnable.ProfileLoadRunnable;
import net.mizukilab.pit.runnable.RebootRunnable;
import net.mizukilab.pit.trade.TradeMonitorRunnable;
import net.mizukilab.pit.util.BannerUtil;
import net.mizukilab.pit.util.DateCodeUtils;
import net.mizukilab.pit.util.bossbar.BossBarHandler;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.dependencies.Dependency;
import net.mizukilab.pit.util.dependencies.DependencyManager;
import net.mizukilab.pit.util.dependencies.loaders.LoaderType;
import net.mizukilab.pit.util.dependencies.loaders.ReflectionClassLoader;
import net.mizukilab.pit.util.menu.MenuUpdateTask;
import net.mizukilab.pit.util.nametag.NametagHandler;
import net.mizukilab.pit.util.rank.RankUtil;
import net.mizukilab.pit.util.sign.SignGui;
import net.mizukilab.pit.util.sound.SoundFactory;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.slf4j.Logger;
import pku.yim.license.MagicLicense;
import pku.yim.license.PluginProxy;
import pku.yim.license.Resource;
import redis.clients.jedis.JedisPool;
import spg.lgdev.iSpigot;
import zone.rong.imaginebreaker.ImagineBreaker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


/**
 * @author EmptyIrony, Misoryan, KleeLoveLife, Rabbit0w0, Araykal
 */
public class ThePit extends JavaPlugin implements PluginMessageListener, PluginProxy {
    public static String BASE_VERSION;
    @Getter
    public static PitInternalHook api;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ThePit.class);
    private static boolean DEBUG_SERVER = false;
    private static String bungeeServerName;
    private static ThePit instance;
    public static boolean isMechanical;
    @Getter
    private MongoDB mongoDB;
    @Getter
    private JedisPool jedis;
    //ol
    @Setter
    @Getter
    private PitWorldConfig pitConfig;
    @Setter
    @Getter
    private EnchantmentFactor enchantmentFactor;
    @Getter
    private NpcFactory npcFactory;
    @Getter
    private net.mizukilab.pit.npc.CustomEntityNPCFactory customEntityNPCFactory;
    @Getter
    private NametagHandler nametagHandler;
    private IItemFactory factory;
    @Getter
    private MedalFactory medalFactory;
    @Getter
    private PerkFactory perkFactory;
    @Getter
    private BuffFactory buffFactory;
    @Getter
    private HologramFactory hologramFactory;
    @Getter
    private EventFactory eventFactory;
    @Getter
    private PlayerMoveHandler movementHandler;
    @Getter
    private QuestFactory questFactory;
    @Getter
    private SignGui signGui;
    @Getter
    private BossBarHandler bossBar;
    @Getter
    private MapSelector mapSelector;
    @Getter
    private ItemFactor itemFactor;
    @Getter
    private RebootRunnable rebootRunnable;
    @Getter
    private MiniGameController miniGameController;
    @Getter
    private SoundFactory soundFactory;
    @Getter
    private PetFactory petFactory;
    @Setter
    private IProfilerOperator profileOperator;
    private PlayerPointsAPI playerPoints;
    private LuckPerms luckPerms;

    @Setter
    @Getter
    private PointsAPI pointsAPI;
    @Setter
    @Getter
    private String serverId;
    @Getter
    private BukkitAudiences audiences;
    @Setter
    @Getter
    private IActionBarManager actionBarManager;

    //这里别用fastutil 依赖没加载会报错
    @Getter
    private final Set<AbstractPerk> disabledPerks = new HashSet<>();
    @Getter
    @Setter
    private IParker parker;

    public static boolean isDEBUG_SERVER() {
        return ThePit.DEBUG_SERVER;
    }

    public static ThePit getInstance() {
        return ThePit.instance;
    }

    public static String getBungeeServerName() {
        return bungeeServerName == null ? "THEPIT" : bungeeServerName.toUpperCase();
    }

    public IProfilerOperator getProfileOperator() {
        return profileOperator;
    }

    private static void setBungeeServerName(String name) {
        bungeeServerName = name;
    }


    @Override
    public void onEnable() {
        BASE_VERSION = this.getDescription().getVersion();
        audiences = BukkitAudiences.create(this);
        BannerUtil.printFileContent("banner.txt");
        serverId = DateCodeUtils.dateToCode(LocalDate.now());

        // ensure the warm up component
        Component.text(-1);

        saveDefaultConfig();

        hookMechanical();
        if (!hookPlayerPoints()) this.getLogger().warning("Dependency not found: PlayerPoints");
        if (!hookLuckPerms()) this.getLogger().warning("Dependency not found: LuckPerms");

        boolean whiteList = Bukkit.getServer().hasWhitelist();
        Bukkit.getServer().setWhitelist(true);

        iSpigot spigot = new iSpigot();
        Bukkit.getServer().getPluginManager().registerEvents(spigot, this);
        //preload
        try {
            preLoad(whiteList);
        } catch (Exception e) {
            e.printStackTrace();
            disablePlugin();
            return;
        }
        //Post load, delayed init
        Bukkit.getScheduler().runTask(this,() -> {
            CommonLoader.bootstrap(this);
            postLoad();
        });
    }

    private void postLoad() {
        loadEventPoller();
    }

    private void preLoad(boolean whiteList) throws Exception {
        if (!this.loadConfig()) {
            throw new IllegalStateException("Failed to load config");
        }
        this.loadMapSelector();

        this.loadDatabase();

        this.loadListener();
        this.loadItemFactor();
        this.loadMenu();
        this.loadNpc();
        this.loadGame();
        this.loadMedals();
        this.loadBuffs();
        this.loadHologram();
        this.loadSound();
        this.loadPerks();
        this.loadEnchantment();
        this.loadQuest();
        this.loadEvents();
        this.loadMoveHandler();

        this.loadQuest();
        this.initBossBar();

        this.initPet();
        Bukkit.getPluginManager().registerEvents(new PlayerMoveHandler(), this);
        this.signGui = new SignGui(this);

        this.rebootRunnable = new RebootRunnable();
        this.rebootRunnable.runTaskTimerAsynchronously(this, 20, 20);

        this.miniGameController = new MiniGameController();
        this.miniGameController.runTaskTimerAsynchronously(this, 1, 1);
        new DayNightCycleRunnable().runTaskTimer(this, 20, 20);


        //TODO 待修复
        bootstrapWorld();

        Messenger messenger = this.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(this, "BungeeCord");
        messenger.registerIncomingPluginChannel(this, "BungeeCord", this);
        FixedRewardData.Companion.refreshAll();
        Bukkit.getServer().setWhitelist(whiteList);
        new ProfileLoadRunnable(this);
    }

    private void loadMapSelector() {
        this.mapSelector = new MapSelector(this);
    }

    private void loadEventPoller() {
        EventsHandler.INSTANCE.loadFromDatabase();
    }

    private static void bootstrapWorld() {
        for (World world : Bukkit.getWorlds()) {
            world.getEntities().forEach(e -> {
                if (e instanceof ArmorStand) {
                    e.remove();
                    return;
                }
                if (e instanceof Item it) {
                    if (it.getItemStack().getType() == Material.GOLD_INGOT) {
                        it.remove(); //garbage remove pieces 修复内存碎片整合慢问题
                    }
                }
            });
            world.setGameRuleValue("keepInventory", "true");
            world.setGameRuleValue("mobGriefing", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
        }
    }

    private void loadItemFactor() {
        this.itemFactor = new ItemFactor();
    }


    public void sendLogs(String s) {
        Bukkit.getConsoleSender().sendMessage(s);
    }

    @Override
    public void onDisable() {
        PacketHologramRunnable.deSpawnAll();
        synchronized (Bukkit.getOnlinePlayers()) {
            CC.boardCast0("&6&l公告! &7正在执行关闭服务器...");
            PlayerProfile.saveAllSync(false);
            CC.boardCast0("&6&l公告! &7正在关闭服务器...");
        }
        System.out.println("Switching io executions to current thread");
        try {
            configManager.save();
            profileOperator.close();
        } catch (Exception e) {
            System.err.println("Failed to execute!");
        }
    }


    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        //setServerName
        if ("GetServer".equals(subchannel)) {
            setBungeeServerName(in.readUTF());
        }
    }

    public void connect(Player p, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (Exception e) {
            e.printStackTrace();
        }
        p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public static boolean callTimeChange(long time) {
        final OriginalTimeChangeEvent event = new OriginalTimeChangeEvent(time);
        event.callEvent();
        return event.isCancelled();
    }

    private void initPet() {
        this.petFactory = new PetFactory();
        this.petFactory.init();
        Bukkit.getPluginManager().registerEvents(this.petFactory, this);
    }

    private void initBossBar() {
        this.bossBar = new BossBarHandler();
        Bukkit.getPluginManager().registerEvents(this.bossBar, this);
    }

    private void loadSound() {
        this.soundFactory = new SoundFactory();
        this.soundFactory.init();
    }

    private void loadRedisLock() {
        PitWorldConfig pitWorldConfig = ThePit.getInstance().getPitConfig();
    }

    private void loadHologram() {
        this.hologramFactory = new HologramFactory();
        this.hologramFactory.init();
    }

    private void loadMenu() {
        this.getServer().getScheduler().runTaskTimer(this, new MenuUpdateTask(), 20L, 20L);
    }

    public void loadEnchantment() {
        this.enchantmentFactor = new EnchantmentFactor();
    }

    public void loadPerks() {
        this.perkFactory = new PerkFactory();
    }

    private void loadMedals() {
        this.medalFactory = new MedalFactory();
        this.medalFactory.init();
        getServer().getPluginManager().registerEvents(this.medalFactory, this);
    }

    private void loadBuffs() {
        this.buffFactory = new BuffFactory();
        this.buffFactory.init();
    }


    private void loadListener() {
        this.getServer().getPluginManager().registerEvents(new SafetyJoinListener(), this);
    }

    private void loadGame() {
        new ClearRunnable()
                .runTaskTimer(ThePit.getInstance(), 20, 20);

        new TradeMonitorRunnable()
                .runTaskTimer(ThePit.getInstance(), 20, 20);
    }

    @SneakyThrows
    private void loadMoveHandler() {
        this.movementHandler = new PlayerMoveHandler();
        iSpigot.INSTANCE.addMovementHandler(this.movementHandler);
    }

    public void loadQuest() {
        this.questFactory = new QuestFactory();
    }

    @Getter
    @Setter
    PitGlobalConfig globalConfig;
    @Getter
    @Setter
    ConfigManager configManager;

    private boolean loadConfig() throws IOException {
        log.info("Loading configuration...");
        ConfigManager cfgMan = new ConfigManager(this);
        configManager = cfgMan;
        PitGlobalConfig pitWorldConfig = cfgMan.getGlobal();
        pitWorldConfig.load();
        this.globalConfig = pitWorldConfig;
        log.info("Loaded configuration!");
        DEBUG_SERVER = pitWorldConfig.isDebugServer();
        if (DEBUG_SERVER) {
            this.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void permissionCheckOnJoin(PlayerLoginEvent event) {
                    final Player player = event.getPlayer();
                    if (pitWorldConfig.isDebugServerPublic()) {
                        final String name = RankUtil.getPlayerRealColoredName(player.getUniqueId());
                        if (name.contains("&7") || name.contains("§7")) {
                            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "你所在的用户组当前无法进入此分区!");
                        }
                    } else if (!player.isOp() && !player.hasPermission("thepit.admin")) {
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "此分区当前未开放,开放时间请关注官方公告!");
                    }
                }
            }, this);
        }

//        if (pitWorldConfig.isRedisEnable()) {
//            jedis = new JedisPool(
//                    new GenericObjectPoolConfig(),
//                    pitWorldConfig.getRedisAddress(),
//                    pitWorldConfig.getRedisPort(),
//                    Protocol.DEFAULT_TIMEOUT,
//                    pitWorldConfig.getRedisPassword(),
//                    false
//            );
//        }
        PitWorldConfig selectedWorldConfig = cfgMan.getSelectedWorldConfig();
        this.pitConfig = selectedWorldConfig;
        return selectedWorldConfig != null;
    }

    private void loadDatabase() {
        log.info("Loading mongodb...");
        this.mongoDB = new MongoDB();
        this.mongoDB.connect();
        log.info("Loaded mongodb!");
    }

    private void loadNpc() {
        log.info("Loading NPCFactory...");
        this.npcFactory = new NpcFactory();
        log.info("Loaded NPCFactory!");
        
        log.info("Loading CustomEntityNPCFactory...");
        this.customEntityNPCFactory = new net.mizukilab.pit.npc.CustomEntityNPCFactory();
        log.info("Loaded CustomEntityNPCFactory!");
    }

    public void loadEvents() {
        log.info("Loading Events...");
        this.eventFactory = new EventFactory();
        log.info("Loaded Events!");
    }


    public final void onLoad() {
        ImagineBreaker.openBootModules();
        ImagineBreaker.wipeMethodFilters();
        ImagineBreaker.wipeFieldFilters();
        instance = this;
        CommonLoader.preBootstrap(this);
        DependencyManager dependencyManager = new DependencyManager(this, new ReflectionClassLoader(this));
        dependencyManager.loadDependencies(
                new Dependency("fastutil", "it.unimi.dsi", "fastutil", "8.5.15", LoaderType.REFLECTION),
                new Dependency("kotlin", "org.jetbrains.kotlin", "kotlin-stdlib", "2.1.20", LoaderType.ISOLATED),
                new Dependency("adventure-platform-bukkit", "net.kyori", "adventure-platform-bukkit", "4.3.2", LoaderType.REFLECTION),
                new Dependency("adventure-platform-facet", "net.kyori", "adventure-platform-facet", "4.3.2", LoaderType.REFLECTION),
                new Dependency("adventure-text-serializer-legacy", "net.kyori", "adventure-text-serializer-legacy", "4.13.1", LoaderType.REFLECTION),
                new Dependency("adventure-text-serializer-gson", "net.kyori", "adventure-text-serializer-gson", "4.13.1", LoaderType.REFLECTION),
                new Dependency("adventure-text-serializer-gson-legacy-impl", "net.kyori", "adventure-text-serializer-gson-legacy-impl", "4.13.1", LoaderType.REFLECTION),

                new Dependency("adventure-nbt", "net.kyori", "adventure-nbt", "4.13.1", LoaderType.REFLECTION),
                new Dependency("adventure-platform-api", "net.kyori", "adventure-platform-api", "4.3.2", LoaderType.REFLECTION),
                new Dependency("adventure-key", "net.kyori", "adventure-key", "4.13.1", LoaderType.REFLECTION),
                new Dependency("adventure-api", "net.kyori", "adventure-api", "4.13.1", LoaderType.REFLECTION),
                new Dependency("hutool", "cn.hutool", "hutool-core", "5.8.36", LoaderType.REFLECTION),
                new Dependency("hutool-cry", "cn.hutool", "hutool-crypto", "5.8.36", LoaderType.REFLECTION),
                new Dependency(
                        "annotations",
                        "org.jetbrains",
                        "annotations",
                        "13.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib-common",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-common",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib-jdk7",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-jdk7",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "kotlin-stdlib-jdk8",
                        "org.jetbrains.kotlin",
                        "kotlin-stdlib-jdk8",
                        "1.4.32",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Junit",
                        "junit",
                        "junit",
                        "4.11",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Apache Http Client",
                        "org.apache.httpcomponents",
                        "httpclient",
                        "4.5.14",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Pool2",
                        "org.apache.commons",
                        "commons-pool2",
                        "2.4.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Apache Http Core",
                        "org.apache.httpcomponents",
                        "httpcore",
                        "4.4",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Apache Logging",
                        "commons-logging",
                        "commons-logging",
                        "1.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "MongoDB-Driver-Core",
                        "org.mongodb",
                        "mongodb-driver-core",
                        "5.2.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "MongoDB-Driver-Sync",
                        "org.mongodb",
                        "mongodb-driver-sync",
                        "5.2.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "MongoDB-Bson",
                        "org.mongodb",
                        "bson",
                        "5.2.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Jedis",
                        "redis.clients",
                        "jedis",
                        "2.9.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "MongoJack",
                        "org.mongojack",
                        "mongojack",
                        "5.0.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-Annotations",
                        "com.fasterxml.jackson.core",
                        "jackson-annotations",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-Databind",
                        "com.fasterxml.jackson.core",
                        "jackson-databind",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-Core",
                        "com.fasterxml.jackson.core",
                        "jackson-core",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "JackSon-DataType",
                        "com.fasterxml.jackson.datatype",
                        "jackson-datatype-jsr310",
                        "2.10.3",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Bson4Jackson",
                        "de.undercouch",
                        "bson4jackson",
                        "2.9.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Redisson",
                        "org.redisson",
                        "redisson",
                        "3.0.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Netty-common",
                        "io.netty",
                        "netty-common",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "Netty-codec",
                        "io.netty",
                        "netty-codec",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-buffer",
                        "io.netty",
                        "netty-buffer",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-transport",
                        "io.netty",
                        "netty-transport",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-handler",
                        "io.netty",
                        "netty-handler",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "reactor-core",
                        "io.projectreactor",
                        "reactor-core",
                        "3.3.9.RELEASE",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "rxjava",
                        "io.reactivex.rxjava2",
                        "rxjava",
                        "2.2.19",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "cache-api",
                        "javax.cache",
                        "cache-api",
                        "1.0.0",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "byte-buddy",
                        "net.bytebuddy",
                        "byte-buddy",
                        "1.10.14",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jboss-marshalling-river",
                        "org.jboss.marshalling",
                        "jboss-marshalling-river",
                        "2.0.9.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jodd-bean",
                        "org.jodd",
                        "jodd-bean",
                        "5.1.6",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "snakeyaml",
                        "org.yaml",
                        "snakeyaml",
                        "1.26",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "slf4j-api",
                        "org.slf4j",
                        "slf4j-api",
                        "1.7.30",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jboss-marshalling-river",
                        "org.jboss.marshalling",
                        "jboss-marshalling-river",
                        "2.0.9.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jboss-marshalling",
                        "org.jboss.marshalling",
                        "jboss-marshalling",
                        "2.0.9.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "jackson-dataformat-yaml",
                        "com.fasterxml.jackson.dataformat",
                        "jackson-dataformat-yaml",
                        "2.11.2",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "netty-all",
                        "io.netty",
                        "netty-all",
                        "4.0.42.Final",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "guava",
                        "com.google.guava",
                        "guava",
                        "29.0-jre",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm",
                        "org.ow2.asm",
                        "asm",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm-commons",
                        "org.ow2.asm",
                        "asm-commons",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm-tree",
                        "org.ow2.asm",
                        "asm-tree",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "asm-util",
                        "org.ow2.asm",
                        "asm-util",
                        "7.3.1",
                        LoaderType.REFLECTION
                ),
                new Dependency(
                        "nashorn-core",
                        "org.openjdk.nashorn",
                        "nashorn-core",
                        "15.3",
                        LoaderType.REFLECTION
                )
        );
    }

    /**
     * Validate that we have access to PlayerPoints
     *
     * @return True if we have PlayerPoints, else false.
     */
    private boolean hookPlayerPoints() {
        try {
            this.playerPoints = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
            return playerPoints != null;
        } catch (Throwable throwable){
            return false;
        }
    }

    private void hookMechanical() {
        isMechanical = Bukkit.getPluginManager().isPluginEnabled("ThePitMechanical");
    }

    private boolean hookLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        } else {
            luckPerms = LuckPermsProvider.get();
        }
        return luckPerms != null;
    }

    public static void setApi(PitInternalHook api) {
        ThePit.api = api;
    }

    public IItemFactory getItemFactory() {
        return factory;
    }

    public void setItemFactory(IItemFactory factory) {
        this.factory = factory;
    }

    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void disablePlugin() {
        onDisable();
    }

    @Override
    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }


    @Override
    public Resource getResourceType() {
        return Resource.CLEAR_LOWERCASE;
    }

}
