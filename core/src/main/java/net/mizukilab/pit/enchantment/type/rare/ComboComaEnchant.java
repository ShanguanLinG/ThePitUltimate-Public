package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IAttackEntity;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@WeaponOnly
@Skip
public class ComboComaEnchant extends AbstractEnchantment implements IAttackEntity, IActionDisplayEnchant, Listener {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private final HashMap<UUID, Cooldown> Cooldown = new HashMap();
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Cooldown.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public String getEnchantName() {
        return "强力击: 昏迷";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "combocoma_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Nullable
    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return new StringBuilder()
                .insert(0, "&7每 &e5 &7次攻击对敌人施加以下效果 (持续")
                .append(decimalFormat.format(1.6 * (double) enchantLevel))
                .append("秒) &7(8秒冷却)/s  &f▶ &8失明/s  &f▶ &8缓慢/s  &f▶ &c虚弱/s  &f▶ &6挖掘疲劳")
                .toString();
    }

    @Override
    public String getText(int level, Player player) {
        level = (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW) ? PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getBowHit() : PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getMeleeHit();
        byte b = 5;
        if ((Cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0L))).hasExpired())
            return (level % b == 0) ? "&a&l✔" : (new StringBuilder()).insert(0, "&e&l").append(b - level % b).toString();
        return getCooldownActionText(Cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0L)));
    }

    @Override
    public void handleAttackEntity(int enchantLevel, Player attacker, Entity target, double damage, AtomicDouble finalDamage, AtomicDouble boostDamage, AtomicBoolean cancel) {
        Player player1 = (Player) target;
        if ((Cooldown.getOrDefault(player1.getUniqueId(), new Cooldown(0L))).hasExpired() && PlayerProfile.getPlayerProfileByUuid(attacker.getUniqueId()).getMeleeHit() % 5 == 0) {
            Cooldown.put(attacker.getUniqueId(), new Cooldown(8L, TimeUnit.SECONDS));
            player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 32 * enchantLevel, 9));
            player1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 32 * enchantLevel, 9));
            player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 32 * enchantLevel, 1));
            player1.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 32 * enchantLevel, 3));
        }
    }
}
