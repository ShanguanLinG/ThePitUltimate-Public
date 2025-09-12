package net.mizukilab.pit.item.type;

import cn.charlotte.pit.buff.impl.BountySolventBuff;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/28 18:39
 */

@AutoRegister
public class BountySolventPotion implements Listener {

    private static final BountySolventBuff buff = new BountySolventBuff();

    public static ItemStack toItemStack() {
        return new ItemBuilder(Material.POTION)
                .name("&6赏金溶剂 (01:00)")
                .lore(
                        "&7受到来自被悬赏 &6&l1000g &7以上的玩家攻击",
                        "&7受到的伤害 &9-30%",
                        "",
                        "&7获得赏金时赏金的获取量 &6+50%"
                )
                .internalName("bounty_solvent_potion")
                .canSaveToEnderChest(true)
                .deathDrop(true)
                .shiny()
                .build();
    }

    @EventHandler
    public void onPotionDrank(PlayerItemConsumeEvent event) {
        if (ItemUtil.getInternalName(event.getItem()) != null && ItemUtil.getInternalName(event.getItem()).equalsIgnoreCase("bounty_solvent_potion")) {
            event.setCancelled(true);
            PlayerUtil.takeOneItemInHand(event.getPlayer());
            buff.stackBuff(event.getPlayer(), 60 * 1000L);
        }
    }
}
