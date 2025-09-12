package net.mizukilab.pit.menu.option.button;

import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Araykal
 * @since 2025/5/4
 */
public class LevelBarOption extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        List<String> lines = new ArrayList<>();
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&7是否切换为进度条?");
        lines.add(" ");
        lines.add("&7当前: " + (profile.getPlayerOption().isLevelBar() ? "&a是" : "&c否"));
        lines.add("&e点击切换此选项!");
        return new ItemBuilder(Material.EXP_BOTTLE).name("&a显示选项: 经验").lore(lines).build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        profile.getPlayerOption().setLevelBar(!profile.getPlayerOption().isLevelBar());
    }

    @Override
    public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
        return true;
    }
}
