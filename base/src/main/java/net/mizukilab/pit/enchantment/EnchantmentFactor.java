package net.mizukilab.pit.enchantment;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.mizukilab.pit.parm.listener.*;
import net.mizukilab.pit.util.PublicUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/30 21:49
 */
@Getter
@Slf4j
public class EnchantmentFactor {


    private final Collection<AbstractEnchantment> enchantments;
    private final Map<String, AbstractEnchantment> enchantmentMap;
    private final List<IPlayerDamaged> playerDamageds;
    private final List<IAttackEntity> attackEntities;
    private final List<IItemDamage> iItemDamages;
    private final List<IPlayerBeKilledByEntity> playerBeKilledByEntities;
    private final List<IPlayerKilledEntity> playerKilledEntities;
    private final List<IPlayerRespawn> playerRespawns;
    private final List<IPlayerShootEntity> playerShootEntities;
    private final Map<String, ITickTask> tickTasks;
    private final List<IPlayerAssist> playerAssists;
    private final Map<String, IActionDisplayEnchant> actionDisplayEnchants;

    public EnchantmentFactor() {
        this.enchantmentMap = new ConcurrentHashMap<>();
        this.enchantments = enchantmentMap.values();
        this.playerDamageds = new ObjectArrayList<>();
        this.iItemDamages = new ObjectArrayList<>();
        this.attackEntities = new ObjectArrayList<>();
        this.playerBeKilledByEntities = new ObjectArrayList<>();
        this.playerKilledEntities = new ObjectArrayList<>();
        this.playerRespawns = new ObjectArrayList<>();
        this.tickTasks = new ConcurrentHashMap<>();
        this.playerShootEntities = new ObjectArrayList<>();
        this.playerAssists = new ObjectArrayList<>();
        this.actionDisplayEnchants = new ConcurrentHashMap<>(); //keep sync!!!
    }

    /**
     * 初始化附魔Entry
     * @param classes
     */
    public void init(Collection<Class<? extends AbstractEnchantment>> classes) {
        log.info("Loading enchantments...");
        for (Class<?> clazz : classes) {
            if (AbstractEnchantment.class.isAssignableFrom(clazz)) {
                try {
                    AbstractEnchantment enchantment = (AbstractEnchantment) clazz.getConstructor().newInstance();
                    registerEnchantment(enchantment);
                } catch (Exception e) {
                    log.error("{} exception on install enchantments.", String.valueOf(e));
                }
            }
        }
        log.info("{} enchantments loaded!", enchantmentMap.size());
    }

    public void registerEnchantment(AbstractEnchantment enchantment) {
        this.enchantmentMap.put(enchantment.getNbtName(), enchantment);
        PublicUtil.register(enchantment.getClass(), enchantment, playerDamageds, attackEntities, iItemDamages, playerBeKilledByEntities, playerKilledEntities, playerRespawns, playerShootEntities);
        registerTickTask(enchantment.getClass(), enchantment);
    }


    /**
     * 移除附魔
     * @param nbtName
     * @param enchantName
     */
    public void unregister(String nbtName, String enchantName) {
        if (nbtName == null) {
            nbtName = "NULL";
        }
        if (enchantName == null) {
            enchantName = "NULL";
        }
        String finalNbtName = nbtName;
        String finalEnchantName = enchantName;
        int size = this.enchantmentMap.size();
        Iterator<AbstractEnchantment> iterator = this.enchantmentMap
                .values().iterator();
        while (iterator.hasNext()) {
            AbstractEnchantment enchObj = iterator.next();
            String nbt = enchObj.getNbtName();
            boolean b = nbt.equalsIgnoreCase(finalNbtName) || enchObj.getEnchantName().equalsIgnoreCase(finalEnchantName);
            if (b) {
                removeEnchantment(enchObj, iterator);
            }
        }
        log.info("Enchantments {} -> {}", size, enchantmentMap.size());
    }

    public void removeEnchantment(AbstractEnchantment enchObj, Iterator<AbstractEnchantment> iterator) {
        log.info("Removing {}", enchObj.getNbtName());
        PublicUtil.unregister(enchObj.getClass(), enchObj, playerDamageds, attackEntities, iItemDamages, playerBeKilledByEntities, playerKilledEntities, playerRespawns, playerShootEntities);
        iterator.remove();
    }

    public void removeEnchantment(AbstractEnchantment enchObj) {
        log.info("Removing {}", enchObj.getNbtName());
        PublicUtil.unregister(enchObj.getClass(), enchObj, playerDamageds, attackEntities, iItemDamages, playerBeKilledByEntities, playerKilledEntities, playerRespawns, playerShootEntities);
        this.enchantmentMap.remove(enchObj.getNbtName());
    }


    private void registerTickTask(Class<?> clazz, AbstractEnchantment enchantment) {
        if (ITickTask.class.isAssignableFrom(clazz)) {
            tickTasks.put(enchantment.getNbtName(), (ITickTask) enchantment);
        }
        if (IPlayerAssist.class.isAssignableFrom(clazz)) {
            playerAssists.add((IPlayerAssist) enchantment);
        }
        if (IActionDisplayEnchant.class.isAssignableFrom(clazz)) {
            actionDisplayEnchants.put(enchantment.getNbtName(), (IActionDisplayEnchant) enchantment);
        }
    }

    public Collection<AbstractEnchantment> getEnchantments() {
        return enchantments;
    }

    public Map<String, AbstractEnchantment> getEnchantmentMap() {
        return enchantmentMap;
    }

    public List<IPlayerDamaged> getPlayerDamageds() {
        return playerDamageds;
    }

    public List<IAttackEntity> getAttackEntities() {
        return attackEntities;
    }

    public List<IItemDamage> getiItemDamages() {
        return iItemDamages;
    }

    public List<IPlayerBeKilledByEntity> getPlayerBeKilledByEntities() {
        return playerBeKilledByEntities;
    }

    public List<IPlayerKilledEntity> getPlayerKilledEntities() {
        return playerKilledEntities;
    }

    public List<IPlayerRespawn> getPlayerRespawns() {
        return playerRespawns;
    }

    public List<IPlayerShootEntity> getPlayerShootEntities() {
        return playerShootEntities;
    }

    public Map<String, ITickTask> getTickTasks() {
        return tickTasks;
    }

    public List<IPlayerAssist> getPlayerAssists() {
        return playerAssists;
    }

    public Map<String, IActionDisplayEnchant> getActionDisplayEnchants() {
        return actionDisplayEnchants;
    }
}
