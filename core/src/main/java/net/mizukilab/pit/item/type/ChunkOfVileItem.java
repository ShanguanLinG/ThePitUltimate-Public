package net.mizukilab.pit.item.type;

import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class ChunkOfVileItem {


    public static ItemStack toItemStack() {
        return new ItemBuilder(Material.COAL)
                .name("&5暗聚块")
                .lore(
                        "&7死亡后保留",
                        "",
                        "&c邪术收藏品"
                )
                .canSaveToEnderChest(true)
                .canTrade(true)
                .internalName("chunk_of_vile_item").build();
    }

    public static String getInternalName() {
        return "chunk_of_vile_item";
    }

}
