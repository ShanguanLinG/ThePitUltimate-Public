package net.mizukilab.pit.item.type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/22 17:14
 */

public class SpireArmor  {

    public SpireArmor() {

    }

    public static ItemStack toItemStack(Material material) {
        final List<String> lore = new ArrayList<>();
        lore.add(0, "");
        lore.add(0, "&7事件物品");

        return new ItemBuilder(material)
                .name(material.name())
                .internalName("spire_armor")
                .removeOnJoin(true)
                .deathDrop(true)
                .lore(lore)
                .buildWithUnbreakable();
    }


}
