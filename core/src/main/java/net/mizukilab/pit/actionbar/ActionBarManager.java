package net.mizukilab.pit.actionbar;

import cn.hutool.core.lang.mutable.MutablePair;
import io.irina.backports.utils.SWMRHashTable;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.ActionBarUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 不要使用SimpleEntry, 会污染HashMap
 */
@Skip
public class ActionBarManager implements IActionBarManager {
    int tick = 0;
    Map<UUID, MutablePair<StringBuilder,Map<String, MutablePair<String, Integer>>>> multiMap = new SWMRHashTable<>();
    public void addActionBarOnQueue(Player player, String arg, String val, int repeat,boolean flush) {
        UUID uniqueId = player.getUniqueId();
        MutablePair<StringBuilder,Map<String, MutablePair<String, Integer>>> stringStringMap = multiMap.get(uniqueId);
        if (stringStringMap == null) {
            stringStringMap = new MutablePair<>(new StringBuilder(),new SWMRHashTable<>());
            multiMap.put(uniqueId, stringStringMap);
        }
        Map<String, MutablePair<String, Integer>> value = stringStringMap.getValue();
        value.put(arg, new MutablePair<>(val, repeat));
    }

    public void addActionBarOnQueue(Player player, String arg, String val, int repeat) {
        addActionBarOnQueue(player, arg, val, repeat, false);
    }

    public void tick() {
        ((SWMRHashTable<UUID, MutablePair<StringBuilder,Map<String, MutablePair<String, Integer>>>>) multiMap).removeIf((uuid, mappedString) -> { //forEach as multimap
            return tickPiece(uuid, mappedString.getValue(),mappedString.getKey());
        });
    }

    private boolean tickPiece(UUID uuid, Map<String, MutablePair<String, Integer>> mappedString,StringBuilder builder) {
        tick++;
        long tick = Utils.toUnsignedInt(this.tick);
        Player player = Bukkit.getPlayer(uuid); //get Players
        if (mappedString.isEmpty() || player == null || !player.isOnline()) {
            //remove immediately
            return true;
        }
        AtomicBoolean ab = new AtomicBoolean(false);
        AtomicInteger index = new AtomicInteger();
        int size = mappedString.size();

        ((SWMRHashTable<String, MutablePair<String, Integer>>) mappedString).removeIf((key, value) -> {
            String rawString = value.getKey();
            Integer repeat = value.getValue();
            boolean reduce = false;
            if(repeat < 0) {
                reduce = true;
                repeat = -repeat;
            } else {
                if ((tick % 5) != 0) {
                    return false;
                }
            }
            builder.append(rawString);
            ab.set(true);
            if (index.incrementAndGet() < size) {
                builder.append("&7| ");
            }

            int i1 = !reduce ? --repeat : repeat;
            if (i1 <= 0) {
                return true;
            }
            //always set it seems not expensive only memory operation
            value.setValue(i1); //setting the value instead create new one, do not use SimpleEntry, because of its rehashing operation
            return false;
        });
        if (ab.get()) {
            ActionBarUtil.sendActionBar0(player, builder.toString());
            ; //clear it
        }
        if (!builder.isEmpty()) {
            builder.setLength(0);
        }

        return false;
    }
}
