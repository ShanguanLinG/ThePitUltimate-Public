package net.mizukilab.pit.enchantment.type.dark_rare;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoRegister
@ArmorOnly
public class ComboDustEnchant extends AbstractEnchantment implements IAttackEntity, Listener {

    @Override
    public String getEnchantName() {
        return "强力击: 混沌";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 1;
    }

    @Override
    public String getNbtName() {
        return "combo_dust";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.DARK_NORMAL;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7每 &e3 &7次击中玩家时/s你的下一次攻击将会附带 &8失明 II &f(00:04) &c虚弱 II &f(00:04) &7效果";
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.attacks.removeLong(e.getPlayer().getUniqueId());
    }

    PotionEffect L1 = PotionEffectType.BLINDNESS.createEffect(80, 1);

    PotionEffect L2 = PotionEffectType.WEAKNESS.createEffect(80, 1);
    Object2LongOpenHashMap<UUID> attacks = new Object2LongOpenHashMap<>();

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        UUID uniqueId = attacker.getUniqueId();
        long aLong = attacks.getLong(uniqueId);
        attacks.put(uniqueId, ++aLong);
        if (aLong == 4) {
            if (target instanceof LivingEntity e) {
                e.addPotionEffect(L1, true);
                e.addPotionEffect(L2, true);
            }
            attacks.removeLong(uniqueId);
        }
    }
}
