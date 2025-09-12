package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PlayerOption;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.World;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.*;
import net.mizukilab.pit.util.rank.RankUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class PublicUtil {
    public static final int TICK_OFF_MAGIC_CODE = -1;
    public static String signVer = "Loader";
    public static String itemVersion = "Loader";
    protected static VarHandle METHOD_;
    private static final int[] HEX_VALUES = new int[128];

    private static final DecimalFormat numFormat = new DecimalFormat("0.00");
    static {
        Arrays.fill(HEX_VALUES, -1);

        HEX_VALUES['0'] = 0x0;
        HEX_VALUES['1'] = 0x1;
        HEX_VALUES['2'] = 0x2;
        HEX_VALUES['3'] = 0x3;
        HEX_VALUES['4'] = 0x4;
        HEX_VALUES['5'] = 0x5;
        HEX_VALUES['6'] = 0x6;
        HEX_VALUES['7'] = 0x7;
        HEX_VALUES['8'] = 0x8;
        HEX_VALUES['9'] = 0x9;

        HEX_VALUES['a'] = 0xa;
        HEX_VALUES['b'] = 0xb;
        HEX_VALUES['c'] = 0xc;
        HEX_VALUES['d'] = 0xd;
        HEX_VALUES['e'] = 0xe;
        HEX_VALUES['f'] = 0xf;

        HEX_VALUES['A'] = 0xa;
        HEX_VALUES['B'] = 0xb;
        HEX_VALUES['C'] = 0xc;
        HEX_VALUES['D'] = 0xd;
        HEX_VALUES['E'] = 0xe;
        HEX_VALUES['F'] = 0xf;
    }
    public static void processActionBar(Player victim, Player damager, int damage,double finalDamage) {
        int absorptionHearts1 = (int) ((CraftPlayer) victim).getHandle().getAbsorptionHearts();
        int absorptionHearts = (int) (absorptionHearts1 / 2);
        int totalHearts = (int) victim.getMaxHealth() / 2;
        int nowHearts = (int) Math.ceil(victim.getHealth() / 2D);
        int damageHearts = (int) Math.ceil(finalDamage / 2D);
        StringBuilder builder = new StringBuilder();
        builder.append(RankUtil.getPlayerColoredName(victim.getUniqueId()));
        boolean venom = !PlayerUtil.    isNPC(victim) && PlayerUtil.isVenom(victim);
        builder.append(venom ? " &2" : " &4");
        builder.append("❤".repeat(Math.max(0, nowHearts - damageHearts)));

        if (absorptionHearts > 0) {
            builder.append("&e");
        }
        builder.append("❤".repeat(Math.max(0, absorptionHearts)));

        builder.append(venom ? "&a" : "&c");
        builder.append("❤".repeat(Math.max(0, damageHearts)));
        builder.append("&7");
        int heats = totalHearts - nowHearts;
        builder.append("❤".repeat(Math.max(0, heats)));
        ThePit.getInstance().getActionBarManager().addActionBarOnQueue(damager, "heart", builder + (PlayerUtil.isPlayerUnlockedPerk(damager, "raw_numbers_perk") ? " &c" + numFormat.format(finalDamage) + "HP" : ""), 3, false);
    }

    public static void processActionBarWithSetting(Player victim, Player damager, int damage,double finalDamage) {
        processActionBarWithSettingProvided(victim,damager,damage,finalDamage,PlayerProfile.getPlayerProfileByUuid(damager.getUniqueId()));
    }
    public static void processActionBarWithSettingProvided(Player victim, Player damager, int damage,double finalDamage,PlayerProfile damagerProfile) {
        if (damagerProfile.isLoaded() && damagerProfile.getPlayerOption().getBarPriority() != PlayerOption.BarPriority.ENCHANT_ONLY) {
            processActionBar(victim, damager, (int) damage,finalDamage);
        }
    }
    static int getHexValueForChar(final char c) {
        try {
            if (HEX_VALUES[c] < 0) {
                throw new IllegalArgumentException("Illegal hexadecimal digit: " + c);
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Illegal hexadecimal digit: " + c);
        }

        return HEX_VALUES[c];
    }
    private static final int UUID_STRING_LENGTH = 36;
    /**
     * 超快hashCode算法
     */
    public static final int _uuidHashCode(CharSequence uuidSequence) {
        if (uuidSequence.length() != UUID_STRING_LENGTH ||
                uuidSequence.charAt(8) != '-' ||
                uuidSequence.charAt(13) != '-' ||
                uuidSequence.charAt(18) != '-' ||
                uuidSequence.charAt(23) != '-') {

            throw new IllegalArgumentException("Illegal UUID string: " + uuidSequence);
        }

        long mostSignificantBits = getMostSignificantBits(uuidSequence);

        long leastSignificantBits = getLeastSignificantBits(uuidSequence);
        return hashTwoLong(mostSignificantBits, leastSignificantBits);
    }

    public static int hashTwoLong(long mostSignificantBits, long leastSignificantBits) {
        return Long.hashCode(mostSignificantBits ^ leastSignificantBits);
    }

    public static long getLeastSignificantBits(CharSequence uuidSequence) {
        long leastSignificantBits = getHexValueForChar(uuidSequence.charAt(19)) << 60;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(20)) << 56;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(21)) << 52;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(22)) << 48;

        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(24)) << 44;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(25)) << 40;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(26)) << 36;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(27)) << 32;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(28)) << 28;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(29)) << 24;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(30)) << 20;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(31)) << 16;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(32)) << 12;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(33)) << 8;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(34)) << 4;
        leastSignificantBits |= getHexValueForChar(uuidSequence.charAt(35));
        return leastSignificantBits;
    }
    public static final long combine(int a,int b){
        return ((long) a << 32) | (b & 0xFFFFFFFFL);
    }
    public static long getMostSignificantBits(CharSequence uuidSequence) {
        long mostSignificantBits = getHexValueForChar(uuidSequence.charAt(0)) << 60;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(1)) << 56;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(2)) << 52;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(3)) << 48;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(4)) << 44;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(5)) << 40;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(6)) << 36;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(7)) << 32;

        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(9)) << 28;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(10)) << 24;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(11)) << 20;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(12)) << 16;

        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(14)) << 12;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(15)) << 8;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(16)) << 4;
        mostSignificantBits |= getHexValueForChar(uuidSequence.charAt(17));
        return mostSignificantBits;
    }
    public static net.minecraft.server.v1_8_R3.ItemStack toNMStackQuick(ItemStack item) {
        if (item instanceof CraftItemStack) {
            try {
                if (METHOD_ != null) {
                    return (net.minecraft.server.v1_8_R3.ItemStack) METHOD_.get(item);
                }
                java.lang.reflect.Field handleField = CraftItemStack.class.getDeclaredField("handle");
                handleField.setAccessible(true);
                net.minecraft.server.v1_8_R3.ItemStack itemStack = (net.minecraft.server.v1_8_R3.ItemStack) handleField.get(item);
                if (itemStack != null) {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(CraftItemStack.class, MethodHandles.lookup());
                    VarHandle varHandle = lookup.unreflectVarHandle(handleField);
                    METHOD_ = varHandle;

                }
                return itemStack;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to access the handle field", e);
            }
        } else {
            return CraftItemStack.asNMSCopy(item);
        }
    }

    public static void removeFromWorld(Entity entity){
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> removeFromWorld(entity));
            return;
        }
        if(entity instanceof CraftEntity){
            net.minecraft.server.v1_8_R3.Entity entityRaw = ((CraftEntity) entity).getHandle();
            int i = entityRaw.ae;
            int j = entityRaw.ag;
            World world = entityRaw.world;
            Chunk chunkIfLoaded = world.getChunkIfLoaded(i, j);
            if (chunkIfLoaded != null) {
                chunkIfLoaded.b(entityRaw);
            }
        }
    }
    /**
     * 超级高效的split方法。
     *
     * @param line string
     * @return a array of strings
     */
    public static String[] splitByCharAt(final String line, final char delimiter) {
        String[] temp = new String[(line.length() / 2) + 1];
        int wordCount = 0;
        int i = 0;
        int j = line.indexOf(delimiter); // first substring

        while (j >= 0) {
            temp[wordCount++] = line.substring(i, j);
            i = j + 1;
            j = line.indexOf(delimiter, i); // rest of substrings
        }

        temp[wordCount++] = line.substring(i); // last substring

        String[] result = new String[wordCount];
        System.arraycopy(temp, 0, result, 0, wordCount);

        return result;
    }

    public static void register(Class<?> clazz, Object instance, List<IPlayerDamaged> playerDamageds, List<IAttackEntity> attackEntities, List<IItemDamage> iItemDamages, List<IPlayerBeKilledByEntity> playerBeKilledByEntities, List<IPlayerKilledEntity> playerKilledEntities, List<IPlayerRespawn> playerRespawns, List<IPlayerShootEntity> playerShootEntities) {
        if (instance instanceof Listener && instance.getClass().isAnnotationPresent(AutoRegister.class)) {
            Bukkit.getPluginManager().registerEvents((Listener) instance, ThePit.getInstance());
        }

        if (IPlayerDamaged.class.isAssignableFrom(clazz)) {
            playerDamageds.add((IPlayerDamaged) instance);
        }
        if (IAttackEntity.class.isAssignableFrom(clazz)) {
            attackEntities.add((IAttackEntity) instance);
        }
        if (IItemDamage.class.isAssignableFrom(clazz)) {
            iItemDamages.add((IItemDamage) instance);
        }
        if (IPlayerBeKilledByEntity.class.isAssignableFrom(clazz)) {
            playerBeKilledByEntities.add((IPlayerBeKilledByEntity) instance);
        }
        if (IPlayerKilledEntity.class.isAssignableFrom(clazz)) {
            playerKilledEntities.add((IPlayerKilledEntity) instance);
        }
        if (IPlayerRespawn.class.isAssignableFrom(clazz)) {
            playerRespawns.add((IPlayerRespawn) instance);
        }
        if (IPlayerShootEntity.class.isAssignableFrom(clazz)) {
            playerShootEntities.add((IPlayerShootEntity) instance);
        }
    }

    public static void unregister(Class<?> clazz, Object instance, List<IPlayerDamaged> playerDamageds, List<IAttackEntity> attackEntities, List<IItemDamage> iItemDamages, List<IPlayerBeKilledByEntity> playerBeKilledByEntities, List<IPlayerKilledEntity> playerKilledEntities, List<IPlayerRespawn> playerRespawns, List<IPlayerShootEntity> playerShootEntities) {
        if (instance instanceof Listener && instance.getClass().isAnnotationPresent(AutoRegister.class)) {
            HandlerList.unregisterAll((Listener) instance);
        }

        if (IPlayerDamaged.class.isAssignableFrom(clazz)) {
            playerDamageds.remove((IPlayerDamaged) instance);
        }
        if (IAttackEntity.class.isAssignableFrom(clazz)) {
            attackEntities.remove((IAttackEntity) instance);
        }
        if (IItemDamage.class.isAssignableFrom(clazz)) {
            iItemDamages.remove((IItemDamage) instance);
        }
        if (IPlayerBeKilledByEntity.class.isAssignableFrom(clazz)) {
            playerBeKilledByEntities.remove((IPlayerBeKilledByEntity) instance);
        }
        if (IPlayerKilledEntity.class.isAssignableFrom(clazz)) {
            playerKilledEntities.remove((IPlayerKilledEntity) instance);
        }
        if (IPlayerRespawn.class.isAssignableFrom(clazz)) {
            playerRespawns.remove((IPlayerRespawn) instance);
        }
        if (IPlayerShootEntity.class.isAssignableFrom(clazz)) {
            playerShootEntities.remove((IPlayerShootEntity) instance);
        }
    }
}
