package net.mizukilab.pit.item.factory;

import com.google.common.annotations.Beta;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IItemFactory;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.util.ItemGlobalReference;
import net.mizukilab.pit.util.Log;
import net.mizukilab.pit.util.PublicUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.item.ItemUtil;
import nya.Skip;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
@Skip
public class ItemFactory implements IItemFactory {

    public boolean clientSide = false;
    //这里是存玩家的
    ItemGlobalReference theReference = new ItemGlobalReference(() -> Bukkit.getOnlinePlayers().size() * 60L);

    //简易LRU
    public boolean hasItem(UUID uuid) {
        return theReference.containsKey(uuid.hashCode());
    }

    @Override
    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
    }

    @Override
    public boolean getClientSide() {
        return clientSide;
    }

    @Override
    public AbstractPitItem getAbstractPitItem(UUID uuid) {
        return getItem(uuid);
    }

    @Override
    public AbstractPitItem getItemFromStack(ItemStack stack) {
        return getIMythicItem(stack);
    }

    public IMythicItem getItem(UUID uuid) {
        return theReference.getValue(uuid);
    }

    Runnable EMPTY_RUNNABLE = () -> {
    };

    public void lru() {
        theReference.executeLRU();
    }

    @Beta
    public IMythicItem getIMythicItem(ItemStack stack) {
        return getIMythicItem(stack, EMPTY_RUNNABLE);
    }

    @Beta
    public IMythicItem getIMythicItem(ItemStack stack, Runnable runnable) {
        return getIMythicItem(stack, runnable,clientSide);
    }
    @Beta
    public IMythicItem getIMythicItem(ItemStack stack,boolean clientSide) {
        return getIMythicItem(stack,EMPTY_RUNNABLE,clientSide);
    }
    @Beta
    public IMythicItem getIMythicItem(ItemStack stack, Runnable runnable,boolean clientSide) {
        NBTTagCompound extra = ItemUtil.getExtra(stack);
        String internalName = ItemUtil.getInternalName0(extra);
        if(internalName == null){
            return null;
        }
        int hashCodeForUUID = ItemUtil.getHashCodeForUUID0(stack,extra);
        if(hashCodeForUUID == -1){
            return null;
        }
        IMythicItem iMythicItem = getIMythicItem(hashCodeForUUID);

        if (iMythicItem == null || clientSide) { //会导致不掉命bug, 有点厉害
            runnable.run();

            return getIMythicItem0(stack, internalName,clientSide);
        } else {
            runnable.run();
            return iMythicItem;
        }

    }

    public Object[] readIMythicItemUUIDAndInternalName(ItemStack stack) {
        Object[] objects = ItemUtil.getInternalNameAndUUID(stack); //提前判断节约0.6%
        if (objects[0] == null || objects[1] == null) {
            return null;
        }
        return objects;
    }

    public IMythicItem getIMythicItemFromUUIDString(String uuidString) {
        return getIMythicItem(PublicUtil._uuidHashCode(uuidString));
    }

    private IMythicItem getIMythicItem(int hashcode) {
        IMythicItem iMythicItem;
        if (!Bukkit.isPrimaryThread()) { //async get sucks
            iMythicItem = theReference.get(hashcode);
        } else {
            iMythicItem = theReference.getValue(hashcode);
        }
        return iMythicItem;
    }

    public IMythicItem getIMythicItemSync(ItemStack stack) {
        return getIMythicItem(stack);
    }

    public IMythicItem getIMythicItem0(ItemStack stack, String internalName,boolean clientSide) {
        UUID itemUUID = null;
        if(!clientSide) {
            if (ItemUtil.shouldUpdateItem(stack)) {
                if (ItemUtil.shouldUpdateUUID()) {
                    itemUUID = ItemUtil.randomUUIDItem(stack);
                }

                ItemUtil.signVer(stack);
            }
        }

        IMythicItem mythicItem = Utils.getMythicItem0(stack, internalName);
        if (mythicItem != null) {

            if (mythicItem.uuid != null) {
                boolean sameAsDefault = mythicItem.uuid.equals(IMythicItem.getDefUUID());
                if(!clientSide) {
                    if (sameAsDefault) {
                        logic(stack, itemUUID, mythicItem);
                    }
                }
                if(!sameAsDefault){
                    theReference.putValue(mythicItem.uuid, mythicItem);
                }
            } else {
                logic(stack, itemUUID, mythicItem);
            }
        }
        return mythicItem;
    }

    private static void logic(ItemStack stack, UUID itemUUID, IMythicItem mythicItem) {
        if (itemUUID == null) {
            itemUUID = ItemUtil.randomUUIDItem(stack);
        }
        mythicItem.uuid = itemUUID;
    }
}
