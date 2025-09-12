package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.random.RandomUtil;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@WeaponOnly
public class ElementalFuryEnchant extends AbstractEnchantment implements Listener,IAttackEntity, IActionDisplayEnchant {
    private static final HashMap<UUID, Cooldown> cooldown = new HashMap<>();
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        cooldown.remove(e.getPlayer().getUniqueId());
    }
    @Override
    public String getEnchantName() {
        return "元素";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "elemental_fury_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        int chance = enchantLevel * 6 + 8;
        double lightningDamage = enchantLevel * 0.5 + 1.5; // 2, 2.5, 3 hearts
        return "&7攻击时有 &e" + chance + "% &7概率触发随机元素效果" +
               "/s&f▶ &c火焰元素&7: 点燃目标 &c" + (enchantLevel + 2) + "秒" +
               "/s&f▶ &b冰霜元素&7: 缓慢目标 &b" + (enchantLevel + 1) + "秒" +
               "/s&f▶ &e雷电元素&7: 召唤雷击造成 &e" + lightningDamage + "♥ &7伤害" +
               "/s&8冷却时间: 10秒";
    }

    @Override
    public String getText(int level, Player player) {
        return cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired() ? 
               "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).getRemaining()).replace(" ", "");
    }

    @Override
    @PlayerOnly
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player targetPlayer = (Player) target;
        
        if (cooldown.getOrDefault(attacker.getUniqueId(), new Cooldown(0)).hasExpired()) {
            int chance = enchantLevel * 6 + 8;
            if (RandomUtil.hasSuccessfullyByChance(chance / 100.0)) {

                int elementType = (int) (Math.random() * 3);
                
                switch (elementType) {
                    case 0:
                        targetPlayer.setFireTicks((enchantLevel + 2) * 20);
                        attacker.playSound(attacker.getLocation(), Sound.GHAST_FIREBALL, 0.8f, 1.2f);
                        break;
                        
                    case 1:
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (enchantLevel + 1) * 20, 1), true);
                        attacker.playSound(attacker.getLocation(), Sound.GLASS, 1.0f, 0.8f);
                        break;
                        
                    case 2:
                        targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                        double lightningDamage = (enchantLevel * 0.5 + 1.5) * 2;
                        targetPlayer.damage(lightningDamage);
                        attacker.playSound(attacker.getLocation(), Sound.AMBIENCE_THUNDER, 1.0f, 1.0f);
                        break;
                }
                
                cooldown.put(attacker.getUniqueId(), new Cooldown(10, TimeUnit.SECONDS));
            }
        }
    }
} 