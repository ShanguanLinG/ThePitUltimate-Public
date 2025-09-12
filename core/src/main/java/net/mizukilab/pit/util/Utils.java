package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;
import net.mizukilab.pit.config.PitWorldConfig;
import net.mizukilab.pit.data.operator.PackedOperator;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.item.type.*;
import net.mizukilab.pit.item.type.mythic.MagicFishingRod;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.util.aabb.AABB;
import net.mizukilab.pit.util.arithmetic.IntegerUtils;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
@Skip
public class Utils {

    /**
     * 需要Paper支持。
     *
     * @return
     */
    public static net.minecraft.server.v1_8_R3.ItemStack toNMStackQuick(ItemStack item) {
        return PublicUtil.toNMStackQuick(item);
    }
    public static final long toUnsignedInt(int data){
        return data & 0xFFFFFFFFL;
    }


    public static boolean shouldTick(long tick, int b) {
        return tick % Math.max(1, b) == 0;
    }
    /**
     * 随机color
     */
    private static final ChatColor[] CHAT_COLORS = ChatColor.values();
    public static ChatColor randomColor() {
        return CHAT_COLORS[ThreadLocalRandom.current().nextInt(Math.max(0, CHAT_COLORS.length - 1))];
    }

    /**
     * 标记GC and Exit
     *
     * @param projectile current Entity
     */
    public static void pointMetadataAndRemove(Entity projectile, int later, String... metadata) {
        Bukkit.getScheduler().runTaskLater(ThePit.getInstance(), () -> {
            for (String metadatum : metadata) {
                projectile.removeMetadata(metadatum, ThePit.getInstance());
            }
        }, later);
    }

    /**
     * 超级快，nano respond o(":".length())
     * 0 - len
     * 原理: Enchantment NBT always prepend number at the last char
     */
    @SneakyThrows
    public static void readEnchantments(Object2IntMap<AbstractEnchantment> ment, NBTTagList nbtTagList) {
        for (int i = 0, size = nbtTagList.size(); i < size; i++) {
            String s = nbtTagList.getString(i);
            int length = s.length();
            int splitIndex = s.lastIndexOf(':',length - 1);

            if (splitIndex != -1) {
                String enchantmentName = s.substring(0, splitIndex);
                try {
                    int level = IntegerUtils.fastParse0(s,splitIndex + 1,length);
                    AbstractEnchantment enchantment = ThePit.getInstance()
                            .getEnchantmentFactor()
                            .getEnchantmentMap()
                            .get(enchantmentName);

                    if (enchantment != null) {
                        ment.put(enchantment, level);
                    }
                } catch (NumberFormatException e) {
                    String levelString = s.substring(splitIndex + 1);
                    ThePit.getInstance().getLogger().warning("Can't serialize level: " + levelString);
                }
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
        return PublicUtil.splitByCharAt(line, delimiter);
    }

    /**
     * 返回-1为没有
     *
     * @param item
     * @param enchantName
     * @return
     */
    public static int getEnchantLevel(ItemStack item, String enchantName) {
        final IMythicItem mythicItem = getMythicItem(item);
        if (mythicItem == null) {
            return -1;
        }

        return getEnchantLevel(mythicItem, enchantName);
    }

    public static int getEnchantLevel(ItemStack item, AbstractEnchantment enchObj) {
        final IMythicItem mythicItem = getMythicItem(item);
        if (mythicItem == null) {
            return -1;
        }
        return mythicItem.getEnchantments().getInt(enchObj);
    }

    public static int getEnchantLevel(AbstractPitItem item, AbstractEnchantment enchObj) {
        if (item == null) {
            return -1;
        }

        return item.getEnchantmentLevel(enchObj);
    }

    public static int getEnchantLevel(AbstractPitItem item, String enchantName) {
        if (item == null) {
            return -1;
        }
        return item.getEnchantmentLevel(enchantName);
    }

    public static String dumpNBTOnString(ItemStack stack) {
        NBTTagCompound tag = Utils.toNMStackQuick(stack).getTag();
        return tag.toString();
    }

    public static IMythicItem getMythicItem(ItemStack item) {
        ThePit instance = ThePit.getInstance();
        if (instance != null) {
            ItemFactory itemFactory = (ItemFactory) instance.getItemFactory();
            if (itemFactory != null) {
                return itemFactory.getIMythicItem(item);
            }
        }
        return getMythicItem0(item);
    }
    public static void playBlockBreak(Location location, Material material) {
        PacketPlayOutWorldEvent ppowe = new PacketPlayOutWorldEvent(2001, new BlockPosition(location.getX(), location.getY(), location.getZ()), material.getId(), false);
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(ppowe));
    }
    public static PackedOperator constructUnsafeOperator(String searchName) {
        PlayerProfile playerProfile = PlayerProfile.loadPlayerProfileByName(searchName);
        PackedOperator packedOperator = new PackedOperator(ThePit.getInstance());
        if (playerProfile == null) {
            return packedOperator;
        }
        packedOperator.loadAs(playerProfile);
        return packedOperator;
    }

    public static IMythicItem getMythicItem0(ItemStack item, String internalName) {
        IMythicItem mythicItem = null;
        if (internalName == null) { //提前skip, 不需要name。
            return null;
        }
        switch (internalName) {
            case "mythic_sword" -> mythicItem = new MythicSwordItem();
            case "mythic_bow" -> mythicItem = new MythicBowItem();
            case "mythic_leggings" -> mythicItem = new MythicLeggingsItem();
            case "angel_chestplate" -> mythicItem = new AngelChestplate();
            case "armageddon_boots" -> mythicItem = new ArmageddonBoots();
            case "kings_helmet" -> mythicItem = new GoldenHelmet();
            case "lucky_chestplate" -> mythicItem = new LuckyChestplate();
            case "jewel_sword" -> mythicItem = new JewelSword();
            case "magic_fishing_rod" -> mythicItem = new MagicFishingRod();
            default -> {
                return null;
            }
        }

        mythicItem.loadFromItemStack(item);

        return mythicItem;
    }

    public static IMythicItem getMythicItem0(ItemStack item) {
        final String internalName = ItemUtil.getInternalName(item);
        return getMythicItem0(item, internalName);
    }

    public static boolean canUseGen(ItemStack item) {
        if (item == null) {
            return false;
        }

        final IMythicItem mythicItem = (IMythicItem) FuncsKt.toMythicItem(item);
        if (mythicItem == null || !mythicItem.isEnchanted() || mythicItem.isBoostedByGem() || mythicItem.isBoostedByGlobalGem()) {
            return false;
        }

        if (mythicItem.getColor() == MythicColor.DARK) {
            return false;
        }

        for (Map.Entry<AbstractEnchantment, Integer> entry : mythicItem.getEnchantments().entrySet()) {
            if (entry.getKey().getRarity().getParentType() != EnchantmentRarity.RarityType.RARE && entry.getValue() < entry.getKey().getMaxEnchantLevel()) {
                return true;
            }
        }

        return false;
    }

    public static boolean canUseGlobalAttGem(ItemStack item) {
        if (item == null) {
            return false;
        }

        final IMythicItem mythicItem = (IMythicItem) FuncsKt.toMythicItem(item);
        if (mythicItem == null || !mythicItem.isEnchanted() || mythicItem.isBoostedByGem() || mythicItem.isBoostedByGlobalGem()) {
            return false;
        }
        if (mythicItem.getColor() == MythicColor.DARK) {
            return false;
        }

        for (Map.Entry<AbstractEnchantment, Integer> entry : mythicItem.getEnchantments().entrySet()) {
            if (entry.getKey().getRarity().getParentType() == EnchantmentRarity.RarityType.RARE && entry.getValue() < entry.getKey().getMaxEnchantLevel()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNPC(org.bukkit.entity.Entity entity) {
        return PlayerUtil.isNPC(entity);
    }
    public static void serializePlayer(Player player){
        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(copy(inventory.getArmorContents()));
        inventory.setContents(copy(inventory.getContents()));
    }

    private static ItemStack[] copy(ItemStack[] armorContents) {
        ItemStack[] itemStacks = new ItemStack[armorContents.length];
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorContent = armorContents[i];
            if(armorContent != null){
                IMythicItem mythicItem = Utils.getMythicItem(armorContent);
                if(mythicItem != null){
                    itemStacks[i] = mythicItem.toItemStack();
                } else {
                    itemStacks[i] = armorContent;
                }
            }
        }
        return itemStacks;
    }

    public static ItemStack subtractLive(ItemStack item) {
        if (item == null) {
            return null;
        }
        return subtractLive(((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItemSync(item));
    }

    public static ItemStack subtractLive(IMythicItem item) {
        if (item == null) return null;
        if (item.isEnchanted()) {
            if (item.getLive() <= 1) {
                return new ItemStack(Material.AIR);
            } else {
                item.setLive(item.getLive() - 1);
                return item.toItemStack();
            }
        }
        return item.toItemStack();
    }

    public static boolean check(Material material) {
        return material == Material.HOPPER || material == Material.ENDER_CHEST;
    }

    public static boolean isInArena(Player player) {
        PitWorldConfig config = ThePit.getInstance().getPitConfig();
        final AABB aabb = new AABB(config.getPitLocA().getX(), config.getPitLocA().getY(), config.getPitLocA().getZ(), config.getPitLocB().getX(), config.getPitLocB().getY(), config.getPitLocB().getZ());

        Location location = player.getLocation();
        final AABB playerAABB = new AABB(location.getX(), location.getY(), location.getZ(), location.getX() + 0.8, location.getY() + 2, location.getZ() + 0.8);
        final boolean inArena = !aabb.intersectsWith(playerAABB);
        return inArena;
    }

    //获取玩家是否可在该物品上使用神话之书 可则返回书物品 否则返回null
    public static boolean canUseMythicBook(Player player, ItemStack item) {
        //设置神话之书物品
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        IMythicItem mythicItem = getMythicItem(item);

        if (mythicItem.getColor() == MythicColor.RAGE) {
            return false;
        }

        if (mythicItem.boostedByBook) {
            return false;
        }

        if (profile.getEnchantingBook() != null) {
            return "mythic_reel".equals(ItemUtil.getInternalName(InventoryUtil.deserializeItemStack(profile.getEnchantingBook())));
        }
        return false;
    }
}
