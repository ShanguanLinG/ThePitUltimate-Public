package net.mizukilab.pit.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PerkData;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.park.Parker;
import net.mizukilab.pit.parm.listener.ITickTask;
import net.mizukilab.pit.util.PublicUtil;
import net.mizukilab.pit.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.SneakyThrows;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

import static net.mizukilab.pit.util.Utils.shouldTick;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/5 0:30
 */
@Skip
public class TickHandler extends BukkitRunnable {
    //@Getter
    //private final static ObjectArrayList<TradeRequest> tradeRequests = new ObjectArrayList<>();

    final Map<String, ITickTask> enchantTicks = ThePit.getInstance().getEnchantmentFactor().getTickTasks();

    final Map<String, ITickTask> ticksPerk = ThePit.getInstance().getPerkFactory().getTickTasks();

    private int tick = 0;

    @SneakyThrows
    @Override
    public void run() {
        long currentTickTime = Utils.toUnsignedInt(tick);
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            if (!profile.isLoaded()) {
                continue;
            }
            tickPerks(player, profile,currentTickTime);
            tickItemInHand(player, tickLeggings(player, profile,currentTickTime), profile,currentTickTime); //别问我为什么这样写 lol
        }
        ThePit.getInstance().getMapSelector().tick();
        ((Parker)ThePit.getInstance().getParker()).tick();
        incTickTime();
    }
    private void incTickTime(){
        tick++;
    }
    private void tickItemInHand(Player player, PlayerInventory inventory, PlayerProfile profile,long tick) {
        ItemStack itemInHand = inventory.getItemInHand();
        if (itemInHand != null) {
            Material type = itemInHand.getType();
            if (type != Material.AIR && type != Material.LEATHER_LEGGINGS && type != Material.PAPER) {
                ItemStack heldItemStack = profile.heldItemStack;
                if (heldItemStack != null) {
                    if (heldItemStack == itemInHand) { //only compare java object equal
                        if (profile.heldItem instanceof IMythicItem ii)
                            tickIMythicItem(player, ii,tick);
                    } else {
                        tickItemStackHand(player, profile, itemInHand,tick);
                    }
                } else {
                    tickItemStackHand(player, profile, itemInHand,tick);
                }
            } else {
                profile.heldItem = null;
                profile.heldItemStack = null;
            }
        }
    }

    @NotNull
    private PlayerInventory tickLeggings(Player player, PlayerProfile profile,long tick) {
        //裤子
        PlayerInventory inventory = player.getInventory();
        final ItemStack leggings = inventory.getLeggings();
        if (leggings != null) {
            ItemStack leggingItemStack = profile.leggingItemStack;
            if (leggingItemStack != null) {
                if (leggingItemStack == leggings) { //only compare java object equal
                    if (profile.leggings instanceof IMythicItem ii)
                        tickIMythicItem(player, ii,tick);
                } else {
                    tickItemStack(player, profile, leggings,tick);
                }
            } else {
                tickItemStack(player, profile, leggings,tick);
            }

        } else {
            profile.leggingItemStack = null;
            profile.leggings = null;
        }
        return inventory;
    }

    private void tickItemStack(Player player, PlayerProfile profile, ItemStack leggings,long tick) {
        profile.leggingItemStack = leggings;
        profile.leggings = handleIMythicItemTickTasks(leggings, player,tick);
    }

    private void tickItemStackHand(Player player, PlayerProfile profile, ItemStack leggings,long tick) {
        profile.heldItemStack = leggings;
        profile.heldItem = handleIMythicItemTickTasks(leggings, player,tick);
    }

    private void tickPerks(Player player, PlayerProfile profile,long tick) {
        for (Map.Entry<Integer, PerkData> entry : profile.getChosePerk().entrySet()) {
            final ITickTask task = entry.getValue().getITickTask(ticksPerk);
            if (task != null) {
                int b = task.loopTick(entry.getValue().getLevel());
                if(b == PublicUtil.TICK_OFF_MAGIC_CODE){
                    return;
                }
                if (shouldTick(tick, b)) {
                    task.handle(entry.getValue().getLevel(), player);
                }
            }
        }

        for (Map.Entry<String, PerkData> entry : profile.getUnlockedPerkMap().entrySet()) {
            final ITickTask task = ticksPerk.get(entry.getValue().getPerkInternalName());
            if (task != null) {
                int b = task.loopTick(entry.getValue().getLevel());
                if(b == PublicUtil.TICK_OFF_MAGIC_CODE){
                    return;
                }
                if (shouldTick(tick, b)) {
                    task.handle(entry.getValue().getLevel(), player);
                }
            }
        }
    }

    public IMythicItem handleIMythicItemTickTasks(ItemStack stack, Player player,long tick) {

        final IMythicItem imythicItem = Utils.getMythicItem(stack);

        //U can set it on your profile
        tickIMythicItem(player, imythicItem,tick);
        return imythicItem;
    }

    private void tickIMythicItem(Player player, IMythicItem imythicItem,long tick) {
        if (imythicItem != null) {
            for (Object2IntMap.Entry<AbstractEnchantment> entry : imythicItem.getEnchantments().object2IntEntrySet()) {
                final ITickTask task = enchantTicks.get(entry.getKey().getNbtName());
                if (task == null) {
                    continue;
                }

                final int level = entry.getIntValue();
                int b = task.loopTick(level);
                if(b == PublicUtil.TICK_OFF_MAGIC_CODE){
                    return;
                }
                if (shouldTick(tick,b)) {
                    task.handle(level, player);
                }
            }
        }
    }

}