package net.mizukilab.pit.enchantment.type.normal;

import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.parm.listener.IPlayerRespawn;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.item.ItemBuilder;
import com.google.common.util.concurrent.AtomicDouble;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/31 21:19
 */

@ArmorOnly
@Skip
public class CreativeEnchant extends AbstractEnchantment implements IPlayerKilledEntity, IPlayerRespawn {

    @Override
    public String getEnchantName() {
        return "手艺人";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "creative_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.NORMAL;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7每次死亡后复活立刻获得 &f" + (16 * enchantLevel) + " * &6木板"
                + "/s&7击杀玩家时获得 &f" + (6 * enchantLevel) + " * &6木板"
                + "/s&7木板放置后存在时间降低至 &e30 秒 &7且无法被延长.";
    }


    @Override
    @PlayerOnly
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        ItemBuilder woodBuilder = new ItemBuilder(Material.WOOD).canDrop(false).canSaveToEnderChest(false).deathDrop(true).canTrade(false).removeOnJoin(true).internalName("creative_enchantment_item");
        myself.getInventory().addItem(woodBuilder.amount(6 * enchantLevel).build());
    }

    @Override
    public void handleRespawn(int enchantLevel, Player myself) {
        ItemBuilder woodBuilder = new ItemBuilder(Material.WOOD).canDrop(false).canSaveToEnderChest(false).deathDrop(true).canTrade(false).removeOnJoin(true).internalName("creative_enchantment_item");
        myself.getInventory().addItem(woodBuilder.amount(16 * enchantLevel).build());
    }
}
