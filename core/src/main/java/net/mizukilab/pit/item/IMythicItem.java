package net.mizukilab.pit.item;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.sub.EnchantmentRecord;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.type.ArmageddonBoots;
import net.mizukilab.pit.item.type.mythic.MagicFishingRod;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.random.RandomUtil;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/23 23:45
 */
@Data()
@Getter
@Setter
@Skip
public abstract class IMythicItem extends AbstractPitItem {

    public int maxLive;
    public int live;
    public int tier;
    public MythicColor color;
    public DyeColor dyeColor;
    public String version;
    public String prefix;
    public boolean boostedByGem = false;
    @Getter
    private final static String defUUIDString = "00000000-0000-0000-0000-000000000001";
    @Getter
    private final static UUID defUUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public String customName = null;

    public boolean boostedByBook = false;

    public UUID uuid;

    // 0=false 1=true -1=unset

    public int forceCanTrade = -1;

    public IMythicItem() {
    }

    public static void clearCache(ItemStack e) {
    }

    public void resetUUID() {
        this.uuid = defUUID;
    }

    @Override
    public ItemStack toItemStack() {
        List<String> lore = new ObjectArrayList<>();
        String name = getItemDisplayName();
        if (this.color == null) {
            this.color = (MythicColor) RandomUtil.helpMeToChooseOne(MythicColor.RED
                    , MythicColor.ORANGE, MythicColor.BLUE, MythicColor.GREEN, MythicColor.YELLOW);
        }

        //Guardian Enchant for Archangel Chestplate

        if (isEnchanted()) {
            if (tier == 0) {
                tier = 3;
            }
        }

        if (this instanceof MythicLeggingsItem) {
            name = color.getChatColor() + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + color.getDisplayName() + "色" + getItemDisplayName();
            if (dyeColor != null) {
                name = dyeColor.getChatColor() + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + "染色神话之甲";
            }
            if (color == MythicColor.DARK) {
                name = color.getChatColor() + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + "暗黑之甲";
            }
            if (color == MythicColor.RAGE) {
                name = color.getChatColor() + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + "暴怒之甲";
            }
            if (color == MythicColor.DARK_GREEN) {
                name = color.getChatColor() + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + "下水道之甲";
            }
        } else if (this instanceof MythicSwordItem) {
            name = (tier >= 3 ? "&c" : "&e") + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + getItemDisplayName();
        } else if (this instanceof MythicBowItem) {
            name = (tier >= 3 ? "&c" : "&b") + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + getItemDisplayName();
        } else if (this instanceof MagicFishingRod) {
            name = (tier >= 3 ? "&c" : "&a") + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + getItemDisplayName();
        }

        int rareAmount = 0;
        int opAmount = 0;
        int uberAmount = 0;
        int enchantTotalLevel = 0;


        for (AbstractEnchantment abstractEnchantment : enchantments.keySet()) {
            int enchantLevel = enchantments.get(abstractEnchantment);
            enchantTotalLevel += enchantLevel;

            if (abstractEnchantment.getRarity() == EnchantmentRarity.RARE) {
                rareAmount++;
                if (color == MythicColor.RAGE && abstractEnchantment.getMaxEnchantLevel() == enchantLevel) {
                    this.prefix = "不可思议的";
                }
            } else if (abstractEnchantment.getRarity() == EnchantmentRarity.OP) {
                opAmount++;
            } else if (abstractEnchantment.getRarity() == EnchantmentRarity.UBER_LIMITED_RARE || abstractEnchantment.getRarity() == EnchantmentRarity.UBER_LIMITED) {
                uberAmount++;
            }
        }

        if (enchantTotalLevel >= 8) {
            this.prefix = switch (color) {
                case RAGE -> "狂躁的";
                case DARK -> "邪恶的";
                default -> "传说中的";
            };
        }

        if (this.maxLive >= 100) {
            if (color == MythicColor.DARK) {
                name = color.getChatColor() + (tier > 0 ? RomanUtil.convert(tier) + " 阶" : "") + "恶魔之甲";
            } else {
                this.prefix = "精制的";
            }
        }

        if (rareAmount >= 3 && this.maxLive >= 100) {
            this.prefix = "万里挑一的";
        } else if (rareAmount == 2 && this.maxLive >= 100) {
            this.prefix = "奇迹般的";
        } else if (rareAmount >= 3) {
            this.prefix = "不朽的";
        } else if (rareAmount == 2) {
            this.prefix = "不凡的";
        }

        if (enchantTotalLevel >= 7 && this.maxLive >= 100) {
            this.prefix = "强大的";
        }

        if (uberAmount >= 1) {
            this.prefix = "登峰造极的";
        }
        if (opAmount >= 1) {
            this.prefix = "可怕的";
        }
        if (this.prefix != null) {
            name = name.substring(0, 2) + this.prefix + " " + name;
        }


        if (this.customName != null) {
            name = customName;
        }

        if (maxLive != 0) {
            lore.add(("&7生命: " + (live / (maxLive * 1.0) <= 0.6 ? (live / (maxLive * 1.0) <= 0.3 ? "&c" : "&e") : "&a") + live + "&7/" + maxLive)
                    + (isBoostedByGem() ? "&a ♦" : "") + (isBoostedByGlobalGem() ? "&b ♦" : "") + (boostedByBook ? "&6 ᥀" : ""));
            lore.add("");
        }

        if (isEnchanted()) {
            final AbstractEnchantment somber = ThePit.getInstance().getEnchantmentFactor().getEnchantmentMap().get("somber_enchant");
            if (color == MythicColor.DARK && !enchantments.containsKey(somber)) {
                enchantments.put(somber, 1);
            }
            boolean genesisFound = false;

            for (Map.Entry<AbstractEnchantment, Integer> entry : enchantments.entrySet()) {
                getEnchantLore(lore, entry, enchantments.entrySet());
                if (entry.getKey().getRarity() == EnchantmentRarity.GENESIS) {
                    genesisFound = true;
                }
            }

            if (this instanceof MythicLeggingsItem) {
                if (color != MythicColor.DARK) {
                    lore.add((dyeColor == null ? color.getChatColor() : dyeColor.getChatColor()) + "穿着时提供与铁护腿相同的伤害减免效果 &8| " + NewConfiguration.INSTANCE.getWatermarks());
                } else {
                    lore.add((dyeColor == null ? color.getChatColor() : dyeColor.getChatColor()) + "穿着时提供与皮革护腿相同的伤害减免效果 &8| " + NewConfiguration.INSTANCE.getWatermarks());
                }
            } else {
                lore.add(NewConfiguration.INSTANCE.getWatermarks());
            }

            if (genesisFound) {
                lore.add(color.getChatColor() + "阵营活动奖励");
            }

        } else {
            lore.add("&7死亡后保留");
            lore.add("");
            if (this instanceof MythicLeggingsItem) {
                lore.add((dyeColor == null ? color.getChatColor() : dyeColor.getChatColor()) + "在神话之井中附魔");
                lore.add((dyeColor == null ? color.getChatColor() : dyeColor.getChatColor()) + "同时,也是一种象征 &8| " + NewConfiguration.INSTANCE.getWatermarks());
            } else {
                lore.add("&7在神话之井中附魔 &8| " + ThePit.getApi().getWatermark());
            }
            this.tier = 0;
        }

        if (dyeColor != null && this instanceof MythicLeggingsItem) {
            lore.add("&7原: " + color.getChatColor() + color.getDisplayName() + "色神话之甲");
        }
        //Dark Pants
        ItemBuilder builder = new ItemBuilder(this.getItemDisplayMaterial());
        if (name != null) {
            builder.name(name);
        }
        if (customName != null) {
            builder.customName(customName);
        }
        if (uuid != null) {
            boolean equals = uuid == null || defUUID.equals(uuid);
            lore.add("&8" + (equals ? "Refresh on table" : uuid.toString()));
        }

        if (this instanceof IMythicSword mythicSword) {
            builder
                    .lore(lore)
                    .internalName(getInternalName())
                    .deathDrop(false)
                    .version(version == null ? "NULL" : version)
                    .canSaveToEnderChest(true)
                    .removeOnJoin(false)
                    .uuid(uuid == null ? defUUID : uuid)
                    .canDrop(false)
                    .canTrade(true)
                    .enchant(enchantments)
                    .itemDamage(mythicSword.getItemDamage())
                    .maxLive(this.maxLive)
                    .tier(this.tier)
                    .makeBoostedByGem(this.boostedByGem)
                    .makeBoostedByGlobalGem(this.boostedByGlobalGem)
                    .makeBoostedByBook(boostedByBook)
                    .live(this.live)
                    .recordEnchantments(enchantmentRecords);
        } else {
            builder
                    .lore(lore)
                    .internalName(getInternalName())
                    .deathDrop(false)
                    .version(version == null ? "NULL" : version)
                    .uuid(uuid == null ? defUUID : uuid)
                    .canDrop(false)
                    .canTrade(true)
                    .canSaveToEnderChest(true)
                    .removeOnJoin(false)
                    .enchant(enchantments)
                    .maxLive(this.maxLive)
                    .live(this.live)
                    .makeBoostedByGem(this.boostedByGem)
                    .makeBoostedByGlobalGem(this.boostedByGlobalGem)
                    .makeBoostedByBook(boostedByBook)
                    .tier(this.tier)
                    .recordEnchantments(enchantmentRecords);
        }

        if (dyeColor != null) {
            builder.dyeColor(dyeColor.name());
        }

        if (isEnchanted()) {
            builder.shiny();
        }
        this.setMythicColor(builder, color);
        if (this instanceof MythicLeggingsItem || this instanceof ArmageddonBoots) {
            builder.setLetherColor(dyeColor == null ? color.getLeatherColor() : dyeColor.getColor());
        }

        if (this.prefix != null) {
            builder.prefix(prefix);
        }
        if (forceCanTrade == 0) {
            builder.forceCanTrade(false);
        } else if (forceCanTrade == 1) {
            builder.forceCanTrade(true);
        } else {
            builder.unsetForceCanTrade();
        }

        return builder
                .buildWithUnbreakable();
    }

    public boolean isBoostedByGlobalGem() {
        return this.boostedByGlobalGem;
    }

    @Override
    public void loadFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = Utils.toNMStackQuick(item);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            return;
        }
        NBTTagCompound extra = tag.getCompound("extra");
        if (extra == null) {
            return;
        }

        //natives
        this.boostedByGem = extra.getBoolean("boostedByGem");

        this.boostedByGlobalGem = extra.getBoolean("boostedByGlobalGem");

        this.boostedByBook = extra.getBoolean("boostedByBook");

        //for raw type opti
        NBTBase prefix1 = extra.get("prefix");
        if (prefix1 instanceof NBTTagString) {
            this.prefix = ((NBTTagString) prefix1).a_();
        }
        //0.12% -> 0.06%

        NBTBase customName1 = extra.get("customName");
        if (customName1 instanceof NBTTagString) {
            this.customName = ((NBTTagString) customName1).a_();
        }
        NBTBase version = extra.get("version");
        if (version instanceof NBTTagString verStr) {
            this.version = verStr.a_();
        }


        NBTBase dyeColor1 = extra.get("dyeColor");
        if (dyeColor1 instanceof NBTTagString) {
            this.dyeColor = DyeColor.valueOf(((NBTTagString) dyeColor1).a_());

        }

        NBTBase uuid1 = extra.get("uuid");
        if (uuid1 instanceof NBTTagString gg) {
            this.uuid = UUID.fromString(gg.a_());
        }

        NBTBase mythicColor = extra.get("mythic_color");
        if (mythicColor == null) {
            this.color = (MythicColor) RandomUtil.helpMeToChooseOne(
                    MythicColor.RED, MythicColor.ORANGE, MythicColor.BLUE, MythicColor.GREEN, MythicColor.YELLOW);
        } else {
            if (mythicColor instanceof NBTTagString) {
                String internalColor = ((NBTTagString) mythicColor).a_();
                this.color = MythicColor.valueOfInternalName(internalColor);
                if (color == null) {
                    this.color = (MythicColor) RandomUtil.helpMeToChooseOne(
                            MythicColor.RED, MythicColor.ORANGE, MythicColor.BLUE, MythicColor.GREEN, MythicColor.YELLOW);
                }
            }
        }

        this.maxLive = extra.getInt("maxLive");
        this.live = extra.getInt("live");
        if (extra.hasKey("forceCanTrade")) {
            this.forceCanTrade = extra.getBoolean("forceCanTrade") ? 1 : 0;
        }

        if (!extra.hasKey("ench")) { //TODO
            return;
        }

        final NBTBase recordsStringRaw = extra.get("records");
        if (recordsStringRaw instanceof NBTTagString) {
            String recordsString = ((NBTTagString) recordsStringRaw).a_();

            for (String recordString : Utils.splitByCharAt(recordsString, ';')) {
                final String[] split = Utils.splitByCharAt(recordString,'|');
                if (split.length >= 3) {
                    enchantmentRecords.add(
                            new EnchantmentRecord(
                                    split[0],
                                    split[1],
                                    Long.parseLong(split[2])
                            )
                    );
                }
            }
        }
        //nano
        NBTTagList ench = extra.getList("ench", 8);
        this.enchantments = new Object2IntOpenHashMap<>();
        this.enchantments.defaultReturnValue(-1);
        Utils.readEnchantments(enchantments, ench);
        if (!extra.hasKey("tier") && isEnchanted()) {
            if (color == MythicColor.DARK) {
                this.tier = 2;
            } else {
                this.tier = 3;
            }
        } else {
            if (extra.hasKey("tier")) {
                if (!isEnchanted()) {
                    this.tier = 0;
                } else {
                    this.tier = extra.getInt("tier");
                }
            }
        }
    }

    protected void setMythicColor(ItemBuilder builder, MythicColor color) {
        builder.changeNbt("mythic_color", color.getInternalName());
    }

    public int getEnchantmentLevel(String name) {
        AbstractEnchantment abstractEnchantment = ThePit.getInstance().getEnchantmentFactor().getEnchantmentMap().get(name);
        if (abstractEnchantment != null) {
            return this.enchantments.getInt(abstractEnchantment);
        }
        return -1;
    }

    public int getEnchantmentLevel(AbstractEnchantment abstractEnchantment) {
        return this.enchantments.getInt(abstractEnchantment);
    }

    public String toString() {
        return "IMythicItem(maxLive=" +
                this.getMaxLive() + ", live=" +
                this.getLive() + ", tier=" + this.getTier() + "" +
                ", color=" + this.getColor() + ", dyeColor=" + this.getDyeColor() + ", prefix=" + this.getPrefix() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof IMythicItem)) return false;
        final IMythicItem other = (IMythicItem) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.getMaxLive() != other.getMaxLive()) return false;
        if (this.getLive() != other.getLive()) return false;
        if (this.getTier() != other.getTier()) return false;
        final Object this$color = this.getColor();
        final Object other$color = other.getColor();
        if (this$color == null ? other$color != null : !this$color.equals(other$color)) return false;
        final Object this$dyeColor = this.getDyeColor();
        final Object other$dyeColor = other.getDyeColor();
        if (this$dyeColor == null ? other$dyeColor != null : !this$dyeColor.equals(other$dyeColor)) return false;
        final Object this$prefix = this.getPrefix();
        final Object other$prefix = other.getPrefix();
        if (this$prefix == null ? other$prefix != null : !this$prefix.equals(other$prefix)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof IMythicItem;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + this.getMaxLive();
        result = result * PRIME + this.getLive();
        result = result * PRIME + this.getTier();
        final Object $color = this.getColor();
        result = result * PRIME + ($color == null ? 43 : $color.hashCode());
        final Object $dyeColor = this.getDyeColor();
        result = result * PRIME + ($dyeColor == null ? 43 : $dyeColor.hashCode());
        final Object $prefix = this.getPrefix();
        result = result * PRIME + ($prefix == null ? 43 : $prefix.hashCode());
        return result;
    }

}
