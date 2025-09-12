package net.mizukilab.pit.enchantment.menu.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.enchantment.menu.MythicWellMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/11 17:03
 */
public class EnchantDisplayButton extends Button {

    private final ItemStack item;
    private final MythicWellMenu menuInstance;
    private final boolean onlyView;
    public EnchantDisplayButton(ItemStack item, MythicWellMenu menu) {
        this(item,menu,false);
    }
    public EnchantDisplayButton(ItemStack item, MythicWellMenu menu,boolean onlyView) {
        this.item = item;
        this.menuInstance = menu;
        this.onlyView = onlyView;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return item;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        if (onlyView || InventoryUtil.isInvFull(player)) {
            return;
        }
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

        if (profile.getEnchantingScience() != null) {
            ItemStack itemStack = InventoryUtil.deserializeItemStack(profile.getEnchantingScience());
            if (itemStack != null && itemStack.getType() != null) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1.2F);
                player.sendMessage(CC.translate("&c请先取下附魔的消耗材料!"));
                return;
            }
        }


        profile.setEnchantingItem(InventoryUtil.serializeItemStack(new ItemStack(Material.AIR)));
        player.getInventory().addItem(item);
        if (item != null && item.getType() != Material.AIR) {
            player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 0.65F);
        }

        menuInstance.openMenu(player);
        menuInstance.getAnimationData().reset();
    }


}
