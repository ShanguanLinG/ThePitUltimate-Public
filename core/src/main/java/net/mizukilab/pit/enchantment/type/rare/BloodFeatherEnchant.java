package net.mizukilab.pit.enchantment.type.rare;

import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.type.FunkyFeather;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.random.RandomUtil;
import nya.Skip;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
@Skip
@BowOnly
@WeaponOnly
@ArmorOnly
public class BloodFeatherEnchant extends AbstractEnchantment implements IPlayerKilledEntity {

    @Override
    public String getEnchantName() {
        return "羽毛掠夺者";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "bloodfeather_enchant";
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
        String color = getColorCode(enchantLevel);
        int chance = getChance(enchantLevel);

        return String.format("&7击杀玩家时有 %s%d%% &7的概率强制目标额外掉落 &f1x &3时髦的羽毛 %s",
                color, chance, enchantLevel >= 3 ? "&7并掠夺" : "");
    }

    @Override
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {

        if (RandomUtil.hasSuccessfullyByChance(0.01D * getChance(enchantLevel))) {
            Player player = (Player) target;
            if (InventoryUtil.getAmountOfItem(player, "funky_feather") >= 1) {
                InventoryUtil.removeItem((Player) target, "funky_feather", 1);

                if (enchantLevel > 3) {
                    myself.getInventory().addItem(FunkyFeather.toItemStack());
                    sendMessage(myself, target, "&7掠夺对方 &fx1 &3时髦的羽毛", "&7对方掠夺了你 &f1x &3时髦的羽毛");
                } else {
                    sendMessage(myself, target, "&7使对方强制掉落了一根 &f1x &3时髦的羽毛", "&7对方使你强制掉落了一根 &f1x &3时髦的羽毛");
                }
            }
        }
    }

    private int getChance(int enchantLevel) {
        return switch (enchantLevel) {
            case 2 -> 50;
            case 3 -> 35;
            default -> 25;
        };
    }

    private String getColorCode(int enchantLevel) {
        return switch (enchantLevel) {
            case 2 -> "&6";
            case 3 -> "&c";
            default -> "&e";
        };
    }

    private void sendMessage(Player myself, Entity target, String myselfMessage, String targetMessage) {
        myself.sendMessage(CC.translate("&c&l羽毛掠夺者! " + myselfMessage));
        target.sendMessage(CC.translate("&c&l羽毛掠夺者! " + targetMessage));
    }
}
