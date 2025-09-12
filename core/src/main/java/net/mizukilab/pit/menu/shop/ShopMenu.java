package net.mizukilab.pit.menu.shop;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.menu.shop.button.type.*;
import net.mizukilab.pit.menu.shop.button.type.server.CloverPixelShopButton;
import net.mizukilab.pit.menu.supporter.button.SupporterEntranceButton;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 16:57
 */
public class ShopMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "商店";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> button = new HashMap<>();
        if (ThePit.getInstance().getGlobalConfig().getToken().equals("CloverPixel")) {
            button.put(4, new CloverPixelShopButton());
        }
        button.put(10, new DiamondSwordShopButton());
        button.put(11, new DiamondChestplateShopButton());
        button.put(12, new DiamondBootsShopButton());
        button.put(13, new BountySolventShopButton());
        button.put(14, new ObsidianShopButton());
        button.put(15, new ObsidianStackShopButton());
        button.put(16, new GoldPickaxeShopButton());

        button.put(19, new CombatSpadeShopButton());
        button.put(20, new DiamongLeggingsShopButton());
        button.put(21, new IronPackShopButton());

        button.put(23, new FirstAidEggShopButton());
        button.put(24, new JumpBoostShopButton());
        button.put(25, new PantsBundleShopButton());

        button.put(28, new SwordBundleShopButton());
        button.put(29, new BowBundleShopButton());

        if (!ThePit.isDEBUG_SERVER() || PlayerUtil.isStaff(player)) {
            button.put(44, new SupporterEntranceButton());
        }
        return button;
    }

    @Override
    public int getSize() {
        return 5 * 9;
    }

}
