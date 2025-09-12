package net.mizukilab.pit.menu.offer;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import net.mizukilab.pit.menu.offer.button.OfferButton;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.menu.Menu;
import net.mizukilab.pit.util.menu.buttons.DisplayButton;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @Creator Misoryan
 * @Date 2021/5/29 11:36
 */
public class OfferMenu extends Menu {

    private final Player target;
    private final String PATTEN_DEFAULT_YMD = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(PATTEN_DEFAULT_YMD);

    public OfferMenu(Player target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        return "来自 " + target.getName() + " 的交易报价";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        PlayerProfile targetProfile = ThePit.getInstance().getProfileOperator().getOrConstructIOperator(target).profile();
        if (targetProfile.getOfferData().getBuyer() == null || !targetProfile.getOfferData().getBuyer().equals(player.getUniqueId())) {
            player.closeInventory();
        } else {
            if (targetProfile.getOfferData().hasUnclaimedOffer()) {
                player.closeInventory();
                player.sendMessage(CC.translate("&c此交易报价已经过期!"));
            }
            buttons.put(11, new DisplayButton(targetProfile.getOfferData().getItemStack(), true));
            buttons.put(15, new OfferButton(target));
        }
        return buttons;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    @Override
    public int getSize() {
        return 3 * 9;
    }
}
