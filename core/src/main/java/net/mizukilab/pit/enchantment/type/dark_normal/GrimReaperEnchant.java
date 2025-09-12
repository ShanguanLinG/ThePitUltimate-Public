package net.mizukilab.pit.enchantment.type.dark_normal;

import cn.charlotte.pit.ThePit;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.SpecialUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Skip
@ArmorOnly
public class GrimReaperEnchant extends AbstractEnchantment implements IPlayerKilledEntity {

    @Override
    public String getEnchantName() {
        return "死神";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "grim_reaper_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DARK_NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7击杀玩家时释放冲击波,对周围10格内所有玩家造成 &c2❤ &7普通伤害"
                + "/s&c击杀获得的奖励 -80%";
    }

    Set<UUID> taskMap = new ObjectOpenHashSet<>();

    @Override
    @PlayerOnly
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        if (SpecialUtil.isPrivate(myself)) {
            return;
        }
        coins.getAndAdd(-0.8 * coins.get());
        experience.getAndAdd(-0.8 * experience.get());
        Player targetPlayer = (Player) target;
        if (!taskMap.contains(myself.getUniqueId())) {
            Bukkit.getScheduler().runTaskAsynchronously(ThePit.getInstance(), () -> {
                Collection<LivingEntity> nearbyPlayers = PlayerUtil.getNearbyPlayersAndChicken(myself.getLocation(), 10);
                int shouldDamageForCount = 4;

                BukkitRunnable task = new BukkitRunnable() {
                    final Iterator<LivingEntity> iterator = nearbyPlayers.iterator();

                    {
                        taskMap.add(myself.getUniqueId());
                    }

                    @Override
                    public void run() {
                        boolean flag = false;
                        for (int i = 0; i < shouldDamageForCount; i++) {
                            if (!iterator.hasNext()) {
                                flag = true;
                                break;
                            }
                            LivingEntity player = iterator.next();
                            if (player instanceof Player && !myself.canSee((Player) player)) {
                                iterator.remove();
                                continue;
                            }
                            if (!player.isDead() && player != myself && player != targetPlayer) {
                                player.damage(player instanceof Chicken ? 0.75 : 4, myself);
                            }
                        }
                        if (flag) {
                            this.cancel();
                            taskMap.remove(myself.getUniqueId());
                        }
                    }
                };

                task.runTaskTimer(ThePit.getInstance(), 1L, 1L);
            });
        }

    }
}
