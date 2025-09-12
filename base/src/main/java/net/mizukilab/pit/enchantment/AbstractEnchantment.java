package net.mizukilab.pit.enchantment;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.RodOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.util.PublicUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.time.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * 2 * @Author: EmptyIrony
 * 3 * @Date: 2020/12/28 23:21
 * 4
 */

@Getter
public abstract class AbstractEnchantment {
    public abstract String getEnchantName();

    public abstract int getMaxEnchantLevel();

    public abstract String getNbtName();

    //Todo: 对重制后的附魔系统的所有附魔稀有度分级
    public abstract EnchantmentRarity getRarity();

    //Todo: 附魔触发后的冷却时间
    @Nullable
    public abstract Cooldown getCooldown();
    //填写此玩家触发附魔的所需冷却时间
    public String getCooldownActionText(Cooldown cooldown) {
        return (cooldown.hasExpired() ? "&a&l✔" : "&c&l" + TimeUtil.millisToRoundedTime(cooldown.getRemaining()).replace(" ", ""));
    }

    //填写每x次攻击触发
    public String getHitActionText(Player player, int activeHit) {
        int hit = (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW ? PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getBowHit() : PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).getMeleeHit());
        return (hit % activeHit == 0 ? "&a&l✔" : "&e&l" + (activeHit - hit % activeHit));
    }

    //Todo: 需要一个判断玩家身上附魔是否生效中(持续时间内)的方法 (虽然也许不应该写在这里)
    public int getItemEnchantLevel(AbstractPitItem im) {
        if (im == null)
            return -1;
        return im.getEnchantments().getInt(this);
    }

    public int getItemEnchantLevel(ItemStack item) {
        AbstractPitItem iMythicItem = ThePit.getInstance().getItemFactory().getItemFromStack(item); //更快Or 更慢
        if (iMythicItem != null) {
            return iMythicItem.getEnchantments().getInt(this);
        } //更快的解析
        return -1;
    }

    public boolean canApply(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = PublicUtil.toNMStackQuick(item);
        if (nmsItem.getItem() instanceof ItemSword) {
            return this.getClass().isAnnotationPresent(WeaponOnly.class);
        }
        if (nmsItem.getItem() instanceof ItemBow) {
            return this.getClass().isAnnotationPresent(BowOnly.class);
        }
        if (nmsItem.getItem() instanceof ItemArmor) {
            return this.getClass().isAnnotationPresent(ArmorOnly.class);
        }
        if (nmsItem.getItem() instanceof ItemFishingRod) {
            return this.getClass().isAnnotationPresent(RodOnly.class);
        }
        return false;
    }

    public boolean isItemHasEnchant(ItemStack itemStack) {
        return this.getItemEnchantLevel(itemStack) != -1;
    }

    public boolean isItemHasEnchant(AbstractPitItem itemStack) {
        return this.getItemEnchantLevel(itemStack) != -1;
    }


    public abstract String getUsefulnessLore(int enchantLevel);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEnchantment that = (AbstractEnchantment) o;
        return that.getNbtName().equals(this.getNbtName());
    }

    //return the hashcode ewe
    int hashCode = 0, cached = -1;

    @Override
    public int hashCode() {
        if (cached == -1) {
            hashCode = this.getNbtName().hashCode();
            cached = 0;
        }
        return hashCode;
    }

}
