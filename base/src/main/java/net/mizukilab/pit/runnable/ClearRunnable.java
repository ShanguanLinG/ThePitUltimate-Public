package net.mizukilab.pit.runnable;

import cn.charlotte.pit.data.sub.DroppedEntityData;
import cn.charlotte.pit.data.sub.PlacedBlockData;
import io.irina.backports.utils.SWMRHashTable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 22:12
 */
@Getter
public class ClearRunnable extends BukkitRunnable {

    private static ClearRunnable clearRunnable;
    public final SWMRHashTable<Location, PlacedBlockData> placedBlock;
    private final List<DroppedEntityData> entityData;

    public ClearRunnable() {
        clearRunnable = this;
        this.placedBlock = new SWMRHashTable<>();
        this.entityData = new ObjectArrayList<>();
    }

    @Override
    public void run() {
        this.placedBlock.removeIf((i, a) -> {
            if (a.getCooldown().hasExpired()) {
                Location location = a.getLocation();
                location.getBlock().setType(Material.AIR);
                return true;
            }
            return false;
        });

        entityData.removeIf(i -> {
            if (i.getTimer().hasExpired()) {
                i.getEntity().remove();
                return true;
            }
            return false;
        });
    }

    public void placeBlock(Location location) {
        this.placeBlock(location, new Cooldown(360, TimeUnit.SECONDS));
    }

    public void placeBlock(Location location, Cooldown cooldown) {
        this.placedBlock.put(location, new PlacedBlockData(location, cooldown));
    }

    public static ClearRunnable getClearRunnable() {
        return clearRunnable;
    }
}
