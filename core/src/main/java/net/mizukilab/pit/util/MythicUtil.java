package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.factory.ItemFactory;
import nya.Skip;
import org.bukkit.inventory.ItemStack;
@Skip
public class MythicUtil {

    /**
     * @param itemStack
     * @return 返回物品读取为IMythicItem后的形式, 如此物品不是MythicItem则返回null
     * (此物品需要能被附魔)
     */
    public static IMythicItem getMythicItem(ItemStack itemStack) {//sync
        return ((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItemSync(itemStack);
    }


}
