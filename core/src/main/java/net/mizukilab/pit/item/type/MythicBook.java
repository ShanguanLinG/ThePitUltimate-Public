package net.mizukilab.pit.item.type;

import net.mizukilab.pit.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MythicBook {

    public static ItemStack toItemStack() {
        return new ItemBuilder(Material.PAPER)
                .name("&d附魔卷轴")
                .deathDrop(false)
                .canDrop(true)
                .canSaveToEnderChest(true)
                .internalName("mythic_reel")
                .uuid(UUID.randomUUID())
                .lore(
                        "",
                        "&7将&6神话物品&7和&d附魔卷轴&7放入神话之井",
                        "&7将会为该&6神话物品&7带来一个随机的三级 &d&l稀有! &7附魔",
                        "",
                        "&7在神话之井使用"
                )
                .shiny()
                .dontStack()
                .build();
    }
}
