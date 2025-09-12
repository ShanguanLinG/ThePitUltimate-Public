package net.mizukilab.pit.item;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface IItemFactory {

    boolean hasItem(UUID uuid);

    void setClientSide(boolean clientSide);

    boolean getClientSide();

    AbstractPitItem getAbstractPitItem(UUID uuid);

    AbstractPitItem getItemFromStack(ItemStack stack);
}
