package net.mizukilab.pit.item.type;

import net.mizukilab.pit.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: Misoryan
 * @Created_In: 2021/4/3 16:57
 */

public class SuperPackage {

    public static ItemStack toItemStack() {
        return new ItemBuilder(Material.MINECART)
                .name("&b超级收纳箱")
                .lore(
                        "&e特殊物品",
                        "",
                        "&7可储存大量物品!"
                )
                .shiny()
                .canSaveToEnderChest(true)
                .internalName("super_package")
                .build();
    }
}
