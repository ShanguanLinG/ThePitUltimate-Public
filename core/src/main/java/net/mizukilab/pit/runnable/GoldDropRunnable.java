package net.mizukilab.pit.runnable;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.util.random.RandomUtil;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/2 13:21
 */
@Skip
public class GoldDropRunnable extends BukkitRunnable {

    private final ObjectArrayList<Item> itemGarbageList = new ObjectArrayList<>();
    private long tick;

    @Override
    public void run() {
        tick++;
        for (int i = 0; i < 2; i++) {
            Location location = RandomUtil.generateRandomLocation();
            Item item = location.getWorld().dropItemNaturally(location, new ItemStack(Material.GOLD_INGOT, 1));
            item.setMetadata("gold", new FixedMetadataValue(ThePit.getInstance(), RandomUtil.random.nextInt(3) + 3));
            itemGarbageList.add(item);
        }
        if (tick % 20 == 0) {
            Iterator<Item> iterator = itemGarbageList.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                item.removeMetadata("gold", ThePit.getInstance());
                if (item.isValid()) {
                    item.remove();
                }
                iterator.remove();
            }
        }
    }
}
