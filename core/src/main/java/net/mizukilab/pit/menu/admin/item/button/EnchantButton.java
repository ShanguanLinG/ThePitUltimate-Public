package net.mizukilab.pit.menu.admin.item.button;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.sub.EnchantmentRecord;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.ArmorOnly;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.param.item.RodOnly;
import net.mizukilab.pit.enchantment.param.item.WeaponOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.item.type.mythic.MagicFishingRod;
import net.mizukilab.pit.item.type.mythic.MythicBowItem;
import net.mizukilab.pit.item.type.mythic.MythicLeggingsItem;
import net.mizukilab.pit.item.type.mythic.MythicSwordItem;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.random.RandomUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/17 22:49
 */
public class EnchantButton extends Button {

    private final AbstractEnchantment enchantment;

    public EnchantButton(AbstractEnchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        boolean sword = enchantment.getClass().isAnnotationPresent(WeaponOnly.class);
        boolean bow = enchantment.getClass().isAnnotationPresent(BowOnly.class);
        boolean armor = enchantment.getClass().isAnnotationPresent(ArmorOnly.class);
        boolean rod = enchantment.getClass().isAnnotationPresent(RodOnly.class);
        EnchantmentRarity rarity = enchantment.getRarity();

        String s = "&7可用于: " + (sword ? "剑," : "") + (bow ? "弓," : "") + (armor ? "盔甲," : "") + (rod ? "鱼竿," : "");

        if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR) || ItemUtil.getInternalName(player.getItemInHand()) == null) {
            return new ItemBuilder(Material.AIR).build();
        }
        if ((ItemUtil.getInternalName(player.getItemInHand()).equalsIgnoreCase("mythic_sword") && !sword)
                || (ItemUtil.getInternalName(player.getItemInHand()).equalsIgnoreCase("mythic_bow") && !bow)
                || (ItemUtil.getInternalName(player.getItemInHand()).equalsIgnoreCase("mythic_leggings") && !armor)
                || (ItemUtil.getInternalName(player.getItemInHand()).equals("magic_fishing_rod") && !rod)) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&9" + enchantment.getEnchantName() + " &7(" + enchantment.getNbtName() + ")")
                    .lore("", s.substring(0, s.length() - 1))
                    .build();
        }
        ItemBuilder itemBuilder = new ItemBuilder(Material.WOOL)
                .name("&9" + enchantment.getEnchantName() + " &7(" + enchantment.getNbtName() + ")")
                .lore("", "&7点击附魔 (右键去除) 或者升级一级物品", s.substring(0, s.length() - 1))
                .durability(rarity.getItemColor());
        if (rarity.getParentType() == EnchantmentRarity.RarityType.RARE) {
            itemBuilder.shiny();
        }
        return itemBuilder.build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        ItemStack item = player.getItemInHand();
        boolean canApply = true;
        if ("mythic_sword".equals(ItemUtil.getInternalName(item))) {
            if (canApply) {
                MythicSwordItem swordItem = (MythicSwordItem) ((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItem(item);
                swordItem.setMaxLive(RandomUtil.random.nextInt(8) + 16);
                swordItem.setLive(swordItem.getMaxLive());
                int level;
                if (swordItem.getEnchantments().containsKey(enchantment)) {
                    level = swordItem.getEnchantments().getInt(enchantment) + (clickType.isRightClick() ? -1 : 1);
                    if (level <= 0) {
                        swordItem.getEnchantments().removeInt(enchantment);
                    } else {
                        if (level > enchantment.getMaxEnchantLevel() && !clickType.isShiftClick()) {
                            player.sendMessage(CC.translate("&c已到达最大等级!"));
                            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1, 99);
                            return;
                        }
                    }
                } else {
                    if (!clickType.isRightClick()) {
                        level = 1;
                    } else {
                        level = -1;
                    }
                }

                swordItem.getEnchantmentRecords().add(
                        new EnchantmentRecord(
                                player.getName(),
                                "管理员ench",
                                System.currentTimeMillis()
                        )
                );
                if (level != -1) {
                    swordItem.getEnchantments().put(enchantment, level);
                }
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 99);
                player.sendMessage(CC.translate("&a成功"));
                player.setItemInHand(swordItem.toItemStack());
                return;
            }
        } else if ("mythic_bow".equals(ItemUtil.getInternalName(item))) {
            if (canApply) {
                MythicBowItem bowItem = (MythicBowItem) ((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItem(item);
                bowItem.setMaxLive(RandomUtil.random.nextInt(8) + 16);
                bowItem.setLive(bowItem.getMaxLive());
                int level;
                if (bowItem.getEnchantments().containsKey(enchantment)) {
                    level = bowItem.getEnchantments().getInt(enchantment) + (clickType.isRightClick() ? -1 : 1);
                    if (level <= 0) {
                        bowItem.getEnchantments().removeInt(enchantment);
                    } else {
                        if (level > enchantment.getMaxEnchantLevel() && !clickType.isShiftClick()) {
                            player.sendMessage(CC.translate("&c已到达最大等级!"));
                            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1, 99);
                            return;
                        }
                    }
                } else {
                    if (!clickType.isRightClick()) {
                        level = 1;
                    } else {
                        level = -1;
                    }
                }
                bowItem.getEnchantmentRecords().add(
                        new EnchantmentRecord(
                                player.getName(),
                                "管理员ench",
                                System.currentTimeMillis()
                        )
                );

                if (level != -1) {
                    bowItem.getEnchantments().put(enchantment, level);
                }
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 99);
                player.sendMessage(CC.translate("&a成功"));
                player.setItemInHand(bowItem.toItemStack());
                IMythicItem.clearCache(item);
                return;
            }
        } else if ("mythic_leggings".equals(ItemUtil.getInternalName(item))) {
            if (canApply) {
                MythicLeggingsItem leggingsItem = (MythicLeggingsItem) ((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItem(item);
                leggingsItem.setMaxLive(RandomUtil.random.nextInt(17) + 24);
                leggingsItem.setLive(leggingsItem.getMaxLive());
                int level;
                if (leggingsItem.getEnchantments().containsKey(enchantment)) {
                    level = leggingsItem.getEnchantments().getInt(enchantment) + (clickType.isRightClick() ? -1 : 1);
                    if (level <= 0) {
                        leggingsItem.getEnchantments().removeInt(enchantment);
                    } else {
                        if (level > enchantment.getMaxEnchantLevel() && !clickType.isShiftClick()) {
                            player.sendMessage(CC.translate("&c已到达最大等级!"));
                            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1, 99);
                            return;
                        }
                    }
                } else {
                    if (!clickType.isRightClick()) {
                        level = 1;
                    } else {
                        level = -1;
                    }
                }
                leggingsItem.getEnchantmentRecords().add(
                        new EnchantmentRecord(
                                player.getName(),
                                "管理员ench",
                                System.currentTimeMillis()
                        )
                );

                if (level != -1) {
                    leggingsItem.getEnchantments().put(enchantment, level);
                }
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 99);
                player.sendMessage(CC.translate("&a成功"));
                player.setItemInHand(leggingsItem.toItemStack());
                return;
            }
        } else if ("magic_fishing_rod".equals(ItemUtil.getInternalName(item))) {
            if (canApply) {
                MagicFishingRod fishingRod = (MagicFishingRod) ((ItemFactory) ThePit.getInstance().getItemFactory()).getIMythicItem(item);
                fishingRod.setMaxLive(RandomUtil.random.nextInt(17) + 24);
                fishingRod.setLive(fishingRod.getMaxLive());
                int level;
                if (fishingRod.getEnchantments().containsKey(enchantment)) {
                    level = fishingRod.getEnchantments().getInt(enchantment) + (clickType.isRightClick() ? -1 : 1);
                    if (level <= 0) {
                        fishingRod.getEnchantments().removeInt(enchantment);
                    } else {
                        if (level > enchantment.getMaxEnchantLevel() && !clickType.isShiftClick()) {
                            player.sendMessage(CC.translate("&c已到达最大等级!"));
                            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1, 99);
                            return;
                        }
                    }
                } else {
                    if (!clickType.isRightClick()) {
                        level = 1;
                    } else {
                        level = -1;
                    }
                }

                if (level != -1) {
                    fishingRod.getEnchantments().put(enchantment, level);
                }
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 99);
                player.sendMessage(CC.translate("&a成功"));
                player.setItemInHand(fishingRod.toItemStack());
                return;
            }
        }
        player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1, 99);
        player.sendMessage(CC.translate("&c附魔失败!你使用了错误的附魔在这个物品上"));
    }
}
