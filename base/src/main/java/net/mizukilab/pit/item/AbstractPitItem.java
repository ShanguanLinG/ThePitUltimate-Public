package net.mizukilab.pit.item;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.sub.EnchantmentRecord;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Setter;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.util.chat.RomanUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 2 * @Author: EmptyIrony
 * 3 * @Date: 2020/12/28 23:18
 * 4
 */
public abstract class AbstractPitItem {

    @Setter
    protected Object2IntOpenHashMap<AbstractEnchantment> enchantments = new Object2IntOpenHashMap<>();

    protected List<EnchantmentRecord> enchantmentRecords = new ObjectArrayList<>();
    public boolean boostedByGlobalGem;

    public AbstractPitItem() {
        enchantments.defaultReturnValue(-1);
    }

    public abstract String getInternalName();

    public abstract String getItemDisplayName();

    public abstract Material getItemDisplayMaterial();

    public int getEnchantmentLevel(String enchantment) {
        return -1;
    }

    public int getEnchantmentLevel(AbstractEnchantment enchantment) {
        return -1;
    }

    public boolean isEnchanted() {
        return !enchantments.isEmpty();
    }


    protected void getEnchantLore(List<String> lore, Map.Entry<AbstractEnchantment, Integer> entry, Set<Map.Entry<AbstractEnchantment, Integer>> entries) {
        if (lore == null || entry == null || entry.getKey() == null) {
            return;
        }
        lore.add(entry.getKey().getRarity().getPrefix() + "&9"
                + (entry.getKey().getRarity() == EnchantmentRarity.DISABLED || entry.getKey().getRarity() == EnchantmentRarity.REMOVED ? "&m" : "")
                + entry.getKey().getEnchantName() + " " + (entry.getValue() >= 2 ? RomanUtil.convert(entry.getValue()) : "") + "&r");
        if (entries.size() < 6) {
            String[] split = entry.getKey().getUsefulnessLore(entry.getValue()).split("/s");
            if (entry.getKey().getRarity() != EnchantmentRarity.REMOVED) {
                for (String s : split) {
                    lore.add("&7" + s);
                }
            } else {
                lore.add("&7此附魔已被移除. &8| " + ThePit.getApi().getWatermark());
            }
            if (entry.getKey().getRarity() == EnchantmentRarity.DISABLED) {
                lore.add("&7此附魔暂时被管理员停用. &8| " + ThePit.getApi().getWatermark());
            }
            lore.add(" ");
        }
    }

    public abstract ItemStack toItemStack();

    public abstract void loadFromItemStack(ItemStack item);

    public List<String> getEnchantLore() {
        List<String> lore = new ObjectArrayList<>();
        if (!isEnchanted()) {
            return lore;
        }
        ObjectSet<Map.Entry<AbstractEnchantment, Integer>> entries = enchantments.entrySet();

        enchantments.object2IntEntrySet().fastForEach(i ->
                getEnchantLore(lore, i, entries)

        );


        return lore;
    }


    public Object2IntOpenHashMap<AbstractEnchantment> getEnchantments() {
        return this.enchantments;
    }

    public void resetEnch() {
        this.enchantments = new Object2IntOpenHashMap<>();
        this.enchantments.defaultReturnValue(-1);
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AbstractPitItem)) return false;
        final AbstractPitItem other = (AbstractPitItem) o;
        if (!other.canEqual(this)) return false;
        final Object this$enchantments = this.getEnchantments();
        final Object other$enchantments = other.getEnchantments();
        if (this$enchantments == null ? other$enchantments != null : !this$enchantments.equals(other$enchantments))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AbstractPitItem;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $enchantments = this.getEnchantments();
        result = result * PRIME + ($enchantments == null ? 43 : $enchantments.hashCode());
        return result;
    }

    public String toString() {
        return "AbstractPitItem(enchantments=" + this.getEnchantments() + ")";
    }

    public List<EnchantmentRecord> getEnchantmentRecords() {
        return enchantmentRecords;
    }
}
