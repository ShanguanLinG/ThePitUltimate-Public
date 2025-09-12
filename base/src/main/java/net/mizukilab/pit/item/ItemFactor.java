package net.mizukilab.pit.item;

import cn.charlotte.pit.ThePit;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/31 12:57
 */
public class ItemFactor {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ItemFactor.class);
    private final Map<String, Class<? extends AbstractPitItem>> itemMap;

    public ItemFactor() {
        this.itemMap = new HashMap<>();
    }

    public void registerItem(AbstractPitItem item) {
        itemMap.put(item.getInternalName(), item.getClass());
    }
    @SneakyThrows
    public void registerItem(Class<? extends AbstractPitItem> itemClass) {
        try {
            AbstractPitItem pitItem = itemClass.getDeclaredConstructor().newInstance();
            if (pitItem instanceof Listener listen) {
                Bukkit.getPluginManager().registerEvents(listen, ThePit.getInstance());
            }
            ThePit.getInstance().getItemFactor().registerItem(pitItem);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    public void registerItemsPreparedObj(Collection<AbstractPitItem> items) {
        for (AbstractPitItem item : items) {
            registerItem(item);
        }
    }
    public void registerItems(AbstractPitItem... items) {
        for (AbstractPitItem item : items) {
            registerItem(item);
        }
    }
    public void registerItems(Collection<Class<? extends AbstractPitItem>> itemClass) {
        for (Class<? extends AbstractPitItem> item : itemClass) {
            registerItem(item);
        }
    }

    public Map<String, Class<? extends AbstractPitItem>> getItemMap() {
        return this.itemMap;
    }


    public void registerItems(@NotNull List<Class<? extends Object>> clazzList) {
        clazzList.forEach(i -> {
            if(AbstractPitItem.class.isAssignableFrom(i)){
                registerItem((Class<? extends AbstractPitItem>) i);
            }
        });
    }
}
