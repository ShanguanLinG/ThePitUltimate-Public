package net.mizukilab.pit.enchantment.rarity;

/**
 * @Author: Misoryan
 * @Created_In: 2020/12/30 17:54
 */
public enum EnchantmentRarity {
    NORMAL("", RarityType.NORMAL, 5), //1 0.95 0.9
    RARE("&d稀有! ", RarityType.RARE, 2), //0 0.05 0.1

    DARK_NORMAL("", RarityType.NORMAL, 15),
    DARK_RARE("&d稀有! ", RarityType.RARE, 2),

    SEWER_NORMAL("", RarityType.NORMAL, 9),
    SEWER_RARE("&d稀有! ", RarityType.RARE, 9),

    FISH_NORMAL("", RarityType.NORMAL, 3),
    FISH_RARE("&d稀有! ", RarityType.RARE, 3),
    RAGE("", RarityType.NORMAL, 14),
    RAGE_RARE("&d稀有! ", RarityType.RARE, 14),

    GENESIS("&fGenesis! ", RarityType.LIMITED, 0), //Enchantments for event

    DISABLED("&c禁!", RarityType.LIMITED, 8),
    REMOVED("&c删!", RarityType.LIMITED, 7),

    SPECIAL("", RarityType.LIMITED, 14),
    OP("&c限时! ", RarityType.LIMITED, 14),

    AUCTION_LIMITED("&6拍限! ", RarityType.NORMAL, 1),
    AUCTION_LIMITED_RARE("&6拍限! ", RarityType.RARE, 1),


    UBER_LIMITED("", RarityType.NORMAL, 4),
    UBER_LIMITED_RARE("&3登封!", RarityType.NORMAL, 2),

    NOSTALGIA("", RarityType.NORMAL, 14),
    NOSTALGIA_RARE("&d稀有! ", RarityType.RARE, 14);

    public final int itemColor;
    private final String prefix;
    private final RarityType parentType;

    EnchantmentRarity(String prefix, RarityType parentType, int itemColor) {
        this.prefix = prefix;
        this.parentType = parentType;
        this.itemColor = itemColor;
    }

    public String getPrefix() {
        return prefix;
    }

    public RarityType getParentType() {
        return parentType;
    }

    public int getItemColor() {
        return itemColor;
    }

    public enum RarityType {
        NORMAL,
        RARE,
        LIMITED
    }
}
