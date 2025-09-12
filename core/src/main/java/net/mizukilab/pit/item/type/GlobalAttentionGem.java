package net.mizukilab.pit.item.type;

import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.menu.gem.GlobalAttentionGemMenu;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GlobalAttentionGem extends AbstractPitItem implements Listener {

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if ("global_attention_gem".equals(ItemUtil.getInternalName(e.getItem()))) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            new GlobalAttentionGemMenu().openMenu(e.getPlayer());
            return;
        }
    }

    @Override
    public String getInternalName() {
        return "global_attention_gem";
    }

    /**
     * 举世瞩目的宝石
     * 物品类型为附魔钻石图标，并使神话物品生命后添加 ♦ 的字符（天蓝色）
     * 仅可为稀有附魔增加一级附魔（不可超过III级），使用后消失
     * 与遵纪守法的宝石向相冲突，一件神话物品最多只能镶嵌1个宝石
     *
     * @return
     */
    @Override
    public String getItemDisplayName() {
        return "&b举世瞩目的宝石";
    }

    @Override
    public Material getItemDisplayMaterial() {
        return Material.DIAMOND;
    }

    @Override
    public ItemStack toItemStack() {
        return new ItemBuilder(getItemDisplayMaterial()).name(getItemDisplayName()).internalName(getInternalName())
                .shiny()
                .removeOnJoin(false)
                .deathDrop(false)
                .canDrop(false)
                .canTrade(true)
                .enchant(Enchantment.DURABILITY, 1)
                .lore(
                        "&7死亡时保留",
                        "",
                        "&7增加附魔物品的一级 &d&l稀有! &7附魔, 并附加 &b♦ &7标识",
                        "&7(仅适用于特定附魔, 且不超出上限)",
                        "&8单个物品仅可使用一次",
                        "",
                        "&e右键使用"

                )
                .canSaveToEnderChest(true)
                .build();
    }

    @Override
    public void loadFromItemStack(ItemStack item) {

    }
}
