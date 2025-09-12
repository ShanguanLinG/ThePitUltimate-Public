package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.data.PlayerProfile;
import com.google.common.util.concurrent.AtomicDouble;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.event.PlayerOnly;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.type.ChunkOfVileItem;
import net.mizukilab.pit.parm.listener.IPlayerKilledEntity;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.random.RandomUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@WeaponOnly
@ArmorOnly
public class MinersEnchant extends AbstractEnchantment implements IPlayerKilledEntity {

    @Override
    public String getEnchantName() {
        return "矿工";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "miners_enchant";
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
        return new StringBuilder().insert(0, "&7每达到 &e1000 &7连杀有 &b").append((enchantLevel + 2) * 10).append("% &7的概率获得 &f1x &5暗聚块.").toString();
    }

    @PlayerOnly
    @Override
    public void handlePlayerKilled(int enchantLevel, Player myself, Entity target, AtomicDouble coins, AtomicDouble experience) {
        if (PlayerProfile.getPlayerProfileByUuid(myself.getUniqueId()).getStreakKills() % 1000.0D == 0.0D) {
            if (RandomUtil.hasSuccessfullyByChance(0.01D * (enchantLevel + 2) * 10.0D)) {
                myself.getInventory().addItem(ChunkOfVileItem.toItemStack());
                myself.sendMessage(CC.translate("&5&l矿工! &7你获得了 &f1x &5暗聚块."));
                return;
            }
            myself.sendMessage(CC.translate("&5&l矿工! &c看起来你的运气不太好! :("));
        }
    }
}
