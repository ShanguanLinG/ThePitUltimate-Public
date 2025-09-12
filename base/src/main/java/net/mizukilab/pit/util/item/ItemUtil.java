package net.mizukilab.pit.util.item;

import cn.charlotte.pit.ThePit;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IItemFactory;
import net.mizukilab.pit.util.Log;
import net.mizukilab.pit.util.PublicUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 1:37
 */
public class ItemUtil {


    public static String getUUID(ItemStack item) {
        return getItemStringData(item, "uuid");
    }
    public static int getHashCodeForUUID(ItemStack item) {
        NBTTagCompound extra = getExtra(item);
        return getHashCodeForUUID0(item, extra);

    }

    public static int getHashCodeForUUID0(ItemStack item, NBTTagCompound extra) {
        if(extra == null) {
            return -1;
        }
        //        tag.setLong("up",PublicUtil.getMostSignificantBits(uuidseq));
        //        tag.setLong("do",PublicUtil.getLeastSignificantBits(uuidseq));
        long up = getLong(extra, "up");
        long down = getLong(extra, "do");
        if(up == 0 || down == 0) {
            UUID uuidObj = getUUIDObj(item);
            if (uuidObj != null) {
                Log.WriteLine("SET UUID OBJECT for " + up + " d:" + down + "by: " + item + " to: " + extra);
                setUUIDObj(extra, uuidObj);

                return uuidObj.hashCode();
            } else {
                return -1;
            }
        }
        return PublicUtil.hashTwoLong(up, down);
    }

    public static UUID getUUIDObj(ItemStack stack) {
        String uuid = getUUID(stack);
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }

    public static void setUUIDObj(NBTTagCompound extra, UUID uuid) {
        setUUID(extra, uuid.toString());
    }
    public static void setUUIDObj(ItemStack stack, UUID uuid) {
        setUUID(stack, uuid.toString());
    }

    public static void setUUID(ItemStack stack, String uuid) {
        NBTTagCompound extra = getExtra(stack);
        setUUID(extra,uuid);
    }
    public static void setUUID(NBTTagCompound compound, String uuid) {
        if (compound != null) {
            compound.setString("uuid", uuid);
            updateMagic(compound,uuid);
        }
    }
    public static long getLong(NBTTagCompound tag,String what) {
        return tag.getLong(what);
    }

    public static void updateMagic(NBTTagCompound tag,String uuidseq) {
        tag.set("up",new NBTTagLong(PublicUtil.getMostSignificantBits(uuidseq)));
        tag.set("do",new NBTTagLong(PublicUtil.getLeastSignificantBits(uuidseq)));

    }
    public static void setVer(ItemStack stack, String ver) {
        NBTTagCompound extra = getExtra(stack);
        if (extra != null) {
            extra.setString("version", ver);
        }
    }

    public static String getVer(ItemStack stack) {
        NBTTagCompound extra = getExtra(stack);
        if (extra != null) {
            NBTBase nbtBase = extra.get("version");
            return nbtBase instanceof NBTTagString ? ((NBTTagString) nbtBase).a_() : null;
        }
        return null;
    }

    public static boolean shouldUpdateItem(ItemStack stack) {
        String ver = getVer(stack);
        return ver == null || ver.equals("NULL") || !PublicUtil.itemVersion.equals(ver);
    }

    public static UUID randomUUIDItem(ItemStack stack) {
        UUID uuid;

        IItemFactory itemFactory = ThePit.getInstance().getItemFactory();
        if (itemFactory != null) {
            while (true) {
                uuid = UUID.randomUUID();
                AbstractPitItem item = itemFactory.getAbstractPitItem(uuid);
                if (item == null) {
                    break;
                }
            }
        } else {
            uuid = UUID.randomUUID();
        }
        setUUIDObj(stack, uuid); //async? I think never will have bugs on here
        return uuid;
    }

    public static void signVer(ItemStack stack) {
        setVer(stack, PublicUtil.itemVersion);
    }

    public static boolean shouldUpdateUUIDAndItem(ItemStack stack) {
        return shouldUpdateItem(stack) && shouldUpdateUUID();
    }

    public static boolean shouldUpdateUUID() {
        return PublicUtil.itemVersion.endsWith("uuid");
    }

    public static boolean isIllegalItem(ItemStack item) {
        NBTTagCompound extra = getExtra(item);
        if (extra == null) {
            return true;
        }

        return !extra.hasKey("internal") || getInternalName(item).endsWith("_reward");
    }

    public static boolean canDrop(ItemStack item) {
        return getItemBoolData(item, "tradeAllow");
    }

    public static boolean isHealingItem(ItemStack item) {
        return getItemBoolData(item, "isHealingItem");
    }

    public static boolean canTrade(ItemStack item) {
        NBTTagCompound extra = getExtra(item);
        if (extra == null) {

            return false;
        }

        if (forceCanTrade(item)) {
            return true;
        }
        String internalName = getInternalName(item);
        boolean b = extra.hasKey("canTrade") && extra.getBoolean("canTrade");
        boolean b1 = (internalName != null && internalName.startsWith("mythic_"));
        return b1 || b;
    }

    public static boolean forceCanTrade(ItemStack item) {
        return getItemBoolData(item, "forceCanTrade");
    }

    public static boolean canSaveEnderChest(ItemStack item) {
        return getItemBoolData(item, "enderChest");
    }

    public static boolean isDefaultItem(ItemStack item) {
        return getItemBoolData(item, "defaultItem");
    }

    @Nullable
    public static NBTTagCompound getExtra(ItemStack item) {
        if (item == null || item.getTypeId() == 0) { //Faster
            return null;
        }

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = PublicUtil.toNMStackQuick(item);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            return null;
        }
        if (tag.get("extra") instanceof NBTTagCompound extra) {
            return extra;
        }
        return null;
    }

    public static boolean isDeathDrop(ItemStack item) {
        return getItemBoolData(item, "deathDrop");
    }

    public static boolean isRemovedOnJoin(ItemStack item) {
        return getItemBoolData(item, "removeOnJoin");
    }
    public static boolean isMythicItem(ItemStack stack){
        return getVer(stack) != null;
    }
    public static String getInternalName(ItemStack item) {
        return getItemStringData(item, "internal");
    }

    public static Object[] getInternalNameAndUUID(ItemStack stack) {
        NBTTagCompound extra = getExtra(stack);
        Object[] objects = new Object[2];
        String internal = getInternalName0(extra);
        String uuid = getItemStringData0(extra, "uuid");
        objects[0] = internal;
        objects[1] = uuid;
        return objects;
    }

    public static Integer getItemIntData(ItemStack item, String key) {
        NBTTagCompound extra = getExtra(item);
        if (extra != null) {
            NBTBase nbtBase = extra.get(key);
            if (nbtBase instanceof NBTTagInt intData) {
                return intData.d();
            }
        }
        return null;
    }

    public static boolean getItemBoolData(ItemStack item, String key) {
        NBTTagCompound extra = getExtra(item);
        if (extra != null) {
            NBTBase nbtBase = extra.get(key);
            if (nbtBase instanceof NBTTagByte byted) {
                return byted.f() != 0;
            }
        }

        return false;
    }

    public static String getItemStringData0(NBTTagCompound extra, String key) {
        if (extra != null) {
            NBTBase nbtBase = extra.get(key);
            if (nbtBase instanceof NBTTagString string) {
                return string.a_();
            }
        }
        return null;
    }
    public static String getItemStringData(ItemStack item, String key) {
        return getItemStringData0(getExtra(item), key);
    }

    public static String getInternalName0(NBTTagCompound extra) {
        return getItemStringData0(extra,"internal");
    }
}
