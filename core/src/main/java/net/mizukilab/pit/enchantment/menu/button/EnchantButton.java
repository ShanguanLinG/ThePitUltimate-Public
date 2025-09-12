package net.mizukilab.pit.enchantment.menu.button;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.EnchantmentRecord;
import cn.charlotte.pit.event.StartEnchantLogicEvent;
import cn.charlotte.pit.events.genesis.GenesisTeam;
import cn.hutool.core.lang.func.Consumer3;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.menu.MythicWellMenu;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.enchantment.type.limit.ILimit;
import net.mizukilab.pit.event.PitPlayerEnchantEvent;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.IMythicItem;
import net.mizukilab.pit.item.MythicColor;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.item.type.MythicEnchantingTable;
import net.mizukilab.pit.menu.shop.button.AbstractShopButton;
import net.mizukilab.pit.util.FuncsKt;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.PlusPlayer;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.chat.ChatComponentBuilder;
import net.mizukilab.pit.util.chat.RomanUtil;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.inventory.InventoryUtil;
import net.mizukilab.pit.util.item.ItemBuilder;
import net.mizukilab.pit.util.item.ItemUtil;
import net.mizukilab.pit.util.menu.Button;
import net.mizukilab.pit.util.random.RandomUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/11 16:51
 */
@Skip
public class EnchantButton extends Button {

    SecureRandom random = RandomUtil.random;
    private final ItemStack item;
    private final MythicWellMenu menu;

    public EnchantButton(ItemStack item, MythicWellMenu menu) {
        this.item = item;
        this.menu = menu;
    }

    private int getPrice(Player player, int level, MythicColor color) {
        int price;
        if (color == MythicColor.DARK) {
            price = switch (level) {
                case 1 -> 10000;
                case 2 -> 60000;
                default -> 99999;
            };
        } else {
            price = switch (level) {
                case 1 -> 1000;
                case 2 -> 4000;
                case 3 -> 8000;
                default -> 9999;
            };
        }
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (ThePit.getInstance().getPitConfig().isGenesisEnable() && profile.getGenesisData().getTeam() == GenesisTeam.DEMON && profile.getGenesisData().getTier() >= 3) {
            return (int) (0.35 * AbstractShopButton.getDiscountPrice(player, price));
        }
        return AbstractShopButton.getDiscountPrice(player, price);
    }

    public IMythicItem getMythicItem(ItemStack item) {
        return Utils.getMythicItem(item);
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        try {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            String enchantingItemStr = profile.getEnchantingItem();
            
            ItemStack currentItem = null;
            if (enchantingItemStr != null) {
                currentItem = InventoryUtil.deserializeItemStack(enchantingItemStr);
            }
            
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                currentItem = this.item;
            }
            
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                return getDefaultDisplayItem();
            }
            
            IMythicItem mythicItem = Utils.getMythicItem0(currentItem);
            MythicColor color = MythicColor.valueOf(ItemUtil.getItemStringData(mythicItem.toItemStack(), "mythic_color").toUpperCase());
            int level = mythicItem.getTier();

            List<String> lines = new LinkedList<>();
            if (level < (color == MythicColor.DARK ? 2 : 3)) {
                lines.add("&7升级至: &a" + RomanUtil.convert(level + 1) + " 阶");
                lines.add("&7价格: &6" + getPrice(player, level + 1, color) + " 硬币" + (level == (color == MythicColor.DARK ? 1 : 2) ? " &7+ " + color.getChatColor() + color.getDisplayName() + "色神话之甲" : ""));
                lines.add(" ");
                if (profile.getCoins() >= getPrice(player, level + 1, color)) {
                    if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "Mythicism") < 4 && level == 2) {
                        lines.add("&c天赋 &6神话附魔师 &c等级 &eIV &c后解锁此功能!");
                    } else {
                        String sinceItem = profile.getEnchantingScience();
                        ItemStack item = InventoryUtil.deserializeItemStack(sinceItem);
                        if ((item == null || item.getType() == Material.AIR) && level == 2) {
                            lines.add("&e选择背包内的神话之甲作为材料以继续...");
                        } else {
                            lines.add("&e点击进行附魔!");
                        }
                    }
                } else {
                    lines.add("&c你的硬币不足!");
                }
            } else {
                lines.add("&a此附魔物品已被提升至最大等级!");
            }
            if ((color == MythicColor.DARK || color == MythicColor.RAGE) && !PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
                if (!PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
                    lines.clear();
                    lines.add("&c请先解锁精通天赋 &6邪术 &c后重试!");
                }
                if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "heresy_perk") < 3 && level == 1) {
                    lines.clear();
                    lines.add("&c天赋 &6邪术 &c等级 &eIII &c后解锁此功能!");
                }
            }
            return new ItemBuilder(Material.ENCHANTMENT_TABLE)
                    .name("&d神话之井")
                    .lore(lines)
                    .build();
        } catch (Exception e) {
            CC.printError(player, e);
        }
        return getDefaultDisplayItem();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton, ItemStack currentItem) {
        // 从profile中获取最新的物品数据，而不是使用构造函数传入的旧数据
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        String enchantingItemStr = profile.getEnchantingItem();
        
        ItemStack actualItem = null;
        if (enchantingItemStr != null) {
            actualItem = InventoryUtil.deserializeItemStack(enchantingItemStr);
        }
        
        // 如果profile中没有物品或物品为空，则使用传入的item作为备用
        if (actualItem == null || actualItem.getType() == Material.AIR) {
            actualItem = this.item;
        }
        
        if (actualItem == null || actualItem.getType() == Material.AIR) {
            return;
        }

        IMythicItem mythicItem = ((ItemFactory)ThePit.getInstance().getItemFactory()).getIMythicItem(actualItem);

        if (mythicItem == null) return;

        final String mythicColor = ItemUtil.getItemStringData(actualItem, "mythic_color");
        if (mythicColor == null) {
            return;
        }

        MythicColor color = MythicColor.valueOf(mythicColor.toUpperCase());
        int level = mythicItem.getTier();

        if ((color == MythicColor.DARK || color == MythicColor.RAGE) && !PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
            if (!PlayerUtil.isPlayerUnlockedPerk(player, "heresy_perk")) {
                return;
            }
            if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "heresy_perk") < 3 && level == 1) {
                return;
            }
        }
        if (level >= (color == MythicColor.DARK ? 2 : 3)) {
            return;
        }
        if (profile.getCoins() < getPrice(player, level + 1, color)) {
            return;
        }
        if (level == (color == MythicColor.DARK ? 1 : 2)) {
            if (PlayerUtil.getPlayerUnlockedPerkLevel(player, "Mythicism") < 4) {
                return;
            }
            if (!removeMythicLegWithColor(player, color)) {
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(CC.translate("&c请放入一条额外的 " + color.getChatColor() + color.getDisplayName() + "色神话之甲 &c才能附魔!"));
                return;
            }
        }
        profile.setCoins(profile.getCoins() - getPrice(player, level + 1, color));
        //handle enchant - 使用最新的物品数据
        doEnchant(actualItem, player, mythicItem);

        menu.getAnimationData().setFinished(false);
        menu.getAnimationData().setStartEnchanting(true);
        menu.getAnimationData().setAnimationTick(0);

        menu.setClosedByMenu(true);
        menu.openMenu(player);
    }

    /**
     * 面对过程式 附魔, 害得我键盘也给附魔了
     *
     * @param item
     * @param player
     * @param mythicItem
     */
    private void doEnchant(ItemStack item, Player player, IMythicItem mythicItem) {

        StartEnchantLogicEvent startEnchantLogicEvent = new StartEnchantLogicEvent(player);
        startEnchantLogicEvent.callEvent();
        Consumer3<ItemStack, AbstractPitItem, Player> consumer = startEnchantLogicEvent.getConsumer();
        if(consumer != null){
            consumer.accept(item,mythicItem,player);
            if(!startEnchantLogicEvent.isCancelled()) {
                new PitPlayerEnchantEvent(player, mythicItem, mythicItem).callEvent();
                end(player, mythicItem);
            }
            return;
        }
        if(!startEnchantLogicEvent.isAllowEnchant()){
            return;
        }
        MythicColor color = mythicItem.getColor();
        int level = mythicItem.getTier();
        int maxLive = 0;
        if (level > 0) {
            maxLive = mythicItem.getMaxLive();
        }
        //根据附魔物品颜色的不同,maxLive也有所不同
        if (color == MythicColor.DARK) {
            switch (level) {
                case 0:
                    mythicItem.setMaxLive((Integer) RandomUtil.helpMeToChooseOne(20, 25, 30, 35));
                    break;
                case 1:
                    mythicItem.setMaxLive((Integer) RandomUtil.helpMeToChooseOne(40, 45, 50, 55, 60));
                    if (RandomUtil.hasSuccessfullyByChance(0.01)) {
                        mythicItem.setMaxLive(135);
                    }
                    break;
                default:
                    mythicItem.setMaxLive(random.nextInt(36) + 5); //5-40
                    break;
            }
        } else {
            if (color == MythicColor.RAGE && level == 0) {
                mythicItem.setMaxLive(((Integer) RandomUtil.helpMeToChooseOne(4, 5, 6, 7, 8, 9)));
            } else {
                switch (level) {
                    case 0:
                        mythicItem.setMaxLive(random.nextInt(7) + 3); //3-9
                        break;
                    case 1:
                        mythicItem.setMaxLive(random.nextInt(6) + 10); //10-15
                        break;
                    case 2:
                        if (RandomUtil.hasSuccessfullyByChance(0.01)) { //Artifact Prefix -> 100 Lives
                            mythicItem.setMaxLive(100);
                        } else {
                            mythicItem.setMaxLive(random.nextInt(8) + 16); //16-23
                        }
                        break;
                    default:
                        mythicItem.setMaxLive(random.nextInt(36) + 5); //5-40
                        break;
                }
            }
        }
        if (level > 0) {
            mythicItem.setLive(mythicItem.getLive() + mythicItem.getMaxLive() - maxLive);
        } else {

            mythicItem.setLive(mythicItem.getMaxLive());
        }
        level++;

        mythicItem.setTier(level);

        List<AbstractEnchantment> list = ThePit.getInstance()
                .getEnchantmentFactor()
                .getEnchantments()
                .stream()
                .filter(abstractEnchantment -> abstractEnchantment.canApply(item))
                .toList();
        List<AbstractEnchantment> enchantments = new ObjectArrayList<>();
        if (level > 1) {
            enchantments = new ObjectArrayList<>(mythicItem.getEnchantments().keySet());
        }
        boolean announcement = false;

        List<AbstractEnchantment> results = list.stream()
                .filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.NORMAL).collect(Collectors.toList());
        List<AbstractEnchantment> rareResults = list.stream()
                .filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.RARE).collect(Collectors.toList());

        //different type of mythic item have different rarity enchantments
        if (color == MythicColor.DARK) {
            results = list.stream()
                    .filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.DARK_NORMAL).collect(Collectors.toList());
            rareResults = list.stream()
                    .filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.DARK_RARE).collect(Collectors.toList());
        } else if (color == MythicColor.RAGE && level == 1) {
            results = list.stream()
                    .filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.RAGE).collect(Collectors.toList());
            rareResults = list.stream().filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.RAGE_RARE).collect(Collectors.toList());
        } else if (color == MythicColor.DARK_GREEN) {
            results = list.stream()
                    .filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.SEWER_NORMAL).collect(Collectors.toList());
            rareResults = list.stream().filter(abstractEnchantment -> abstractEnchantment.getRarity() == EnchantmentRarity.SEWER_RARE).collect(Collectors.toList());
        }

        rareResults = rareResults.stream().filter(abstractEnchantment -> !isBlackList(player, abstractEnchantment)).collect(Collectors.toList());
        results = results.stream().filter(abstractEnchantment -> !isBlackList(player, abstractEnchantment)).collect(Collectors.toList());
        //Enchant Start
        switch (color) {
            case DARK: {
                RandomUtil.switchSeed();
                if (level == 1) {
                    //t1 dark pants have only somber1
                    mythicItem.getEnchantments().put(ThePit.getInstance().getEnchantmentFactor().getEnchantmentMap().get("somber_enchant"), 1);
                    break;
                }
                if (level == 2) {
                    if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
                        AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                        mythicItem.getEnchantments().put(enchantment, 1);
                        announcement = true;
                    } else {
                        results.removeIf(enchant -> enchant.getNbtName().equals("somber_enchant"));
                        AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                        mythicItem.getEnchantments().put(enchantment, 1);
                    }
                }
                break;
            }
            case RAGE: {
                if (level == 1) {
                    if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
                        AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                        mythicItem.getEnchantments().put(enchantment, RandomUtil.random.nextInt(enchantment.getMaxEnchantLevel() - 1) + 1);
                        announcement = true;
                    } else {
                        AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                        mythicItem.getEnchantments().put(enchantment, RandomUtil.random.nextInt(enchantment.getMaxEnchantLevel() - 1) + 1);
                    }
                    break;
                }
            }
            default: {
                RandomUtil.switchSeed();
                if (level == 1) {
                    if(!NewConfiguration.INSTANCE.getAlwaysT2Enchant()) {
                        announcement = levelTier1MythicEnchantLogic(item, player, mythicItem,level, rareResults, announcement, results, enchantments);
                        //Tier 1 Enchant End
                    } else{
                        announcement = levelTier2MythicEnchantLogic(item, player, mythicItem,level, rareResults, announcement, color, enchantments,results);

                    }
                } else if (level == 2) {
                    announcement = levelTier2MythicEnchantLogic(item, player, mythicItem,level, rareResults, announcement, color, enchantments, results);
                } else if (level == 3) {
                    announcement = levelTier3MythicEnchantLogic(item, player, mythicItem,level, rareResults, announcement, results, enchantments, color);
                }
            }
        }

        if (mythicItem.color == MythicColor.DARK_GREEN && mythicItem.getEnchantmentLevel("trash_panda_enchant") >= 1) {
            int randomLive = RandomUtil.random.nextInt(30, 41);
            mythicItem.live = randomLive;
            mythicItem.maxLive = randomLive;
        }

        mythicItem.getEnchantmentRecords()
                .add(new EnchantmentRecord(
                        player.getName(),
                        "Enchant Table",
                        System.currentTimeMillis()
                ));

        //Check if the mythic item have a prefix and announce it
        //reload it , maybe trash code
        //mythicItem.loadFromItemStack(mythicItem.toItemStack());
        if(startEnchantLogicEvent.isCancelled()){
            return;
        }
        new PitPlayerEnchantEvent(player, mythicItem, mythicItem).callEvent();
        if (mythicItem.getPrefix() != null) {
            announcement = true;
        }

        if (announcement) {
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());

            beginAnnounceAsync(player, mythicItem, profile);
        }
        end(player, mythicItem);
    }
    public void writeOut(ItemStack stack,IMythicItem item){
        ItemStack itemStack = item.toItemStack();
        stack.setData(itemStack.getData());
        stack.setItemMeta(itemStack.getItemMeta());
    }
    private static BaseComponent[] toEmptyHover(IMythicItem mythicItem) {
        net.minecraft.server.v1_8_R3.ItemStack nms = Utils.toNMStackQuick(mythicItem.toItemStack());
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nms.save(nbtTagCompound);
        return new BaseComponent[]{
                new TextComponent(nbtTagCompound.toString())
        };
    }

    private void beginAnnounceAsync(Player player, IMythicItem mythicItem, PlayerProfile profile) {
        new BukkitRunnable() {
            final Cooldown cooldown = new Cooldown(10, TimeUnit.SECONDS);

            public void run() {
                if (menu.getAnimationData().isFinished()) {
                    this.cancel();
                    BaseComponent[] hoverEventComponents = toEmptyHover(mythicItem);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!FuncsKt.isSpecial(p)) {
                            if (!PlusPlayer.getPlusPlayer().contains(player.getName())) {
                                p.spigot().sendMessage(new ChatComponentBuilder(CC.translate("&d&l稀有附魔! &7" + profile.getFormattedNameWithRoman() + " &7在神话之井中获得了稀有物品: " + mythicItem.toItemStack().getItemMeta().getDisplayName() + " &e[查看]"))
                                        .setCurrentHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents)).create());
                                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                            }
                        }

                    }
                } else if (cooldown.hasExpired()) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(), 50, 5);
    }

    private static void end(Player player, IMythicItem mythicItem) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        profile.setEnchantingItem(InventoryUtil.serializeItemStack(mythicItem.toItemStack()));
    }

    private boolean levelTier3MythicEnchantLogic(ItemStack item, Player player, IMythicItem mythicItem,int level, List<AbstractEnchantment> rareResults, boolean announcement, List<AbstractEnchantment> results, List<AbstractEnchantment> enchantments, MythicColor color) {
        int amount = mythicItem.getEnchantments().size();
        if (amount == 1) { // If this item have only 1 enchantment
            AbstractEnchantment enchantment = null;
            if (Utils.canUseMythicBook(player, item)) { //定义不明
                AbstractEnchantment rareEnchant = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                mythicItem.getEnchantments().put(rareEnchant, 3);
                announcement = true;
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                profile.setEnchantingBook(null);
                //add logic TODO
                results.removeAll(enchantments);
                if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
                    announcement = true;
                    rareResults.removeAll(enchantments);
                    enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                } else {
                    enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                }
                enchantments.add(enchantment);
                mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b, 1));
                mythicItem.boostedByBook = true;
            } else {
                for (int i = 0; i < 2; i++) {
                    results.removeAll(enchantments);
                    if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
                        announcement = true;
                        rareResults.removeAll(enchantments);
                        enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                    } else {
                        enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                    }
                    enchantments.add(enchantment);
                    mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b, 1));
                }
                for (AbstractEnchantment abstractEnchantment : enchantments) {
                    mythicItem.getEnchantments().put(abstractEnchantment, max(mythicItem.getEnchantments().get(abstractEnchantment), getRandomLevel()));
                }
                //set level of a new enchant to 1/2 (3 excluded cuz the limit)
                int totalLevel = 0;
                for (AbstractEnchantment abstractEnchantment : mythicItem.getEnchantments().keySet()) {
                    totalLevel += mythicItem.getEnchantments().getInt(abstractEnchantment);
                }
                if ((totalLevel == 8 && RandomUtil.hasSuccessfullyByChance(0.9)) || totalLevel == 9) {
                    if (enchantment != null) {
                        mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b, RandomUtil.hasSuccessfullyByChance(0.1) ? 2 : 1));
                    }
                }
            }
        } else if (amount == 2) { //21 -> 311
            if (Utils.canUseMythicBook(player, item)) {
                AbstractEnchantment rareEnchant = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                Object2IntOpenHashMap<AbstractEnchantment> enchantments1 = mythicItem.getEnchantments();
                int levelCurrentlyEnchantment = 0;
                for (Integer value : enchantments1.values()) {
                    levelCurrentlyEnchantment += value;
                }
                final int lce = levelCurrentlyEnchantment;
                enchantments1.computeInt(rareEnchant, (a,b) -> max(b, lce >= 6 ? 2 : 3));
                announcement = true;
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                profile.setEnchantingBook(null);
                mythicItem.boostedByBook = true;
            } else {
                results.removeAll(enchantments);
                AbstractEnchantment enchantment;
                if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
                    announcement = true;
                    rareResults.removeAll(enchantments);
                    enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                } else {
                    enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                }
                enchantments.add(enchantment);
                mythicItem.getEnchantments().put(enchantment, 1);
                for (AbstractEnchantment abstractEnchantment : enchantments) {
                    final int currentLevel = max(mythicItem.getEnchantments().getInt(abstractEnchantment), getRandomLevel());
                    mythicItem.getEnchantments().put(abstractEnchantment, currentLevel);
                    if (currentLevel == 3 && (abstractEnchantment.getRarity() == EnchantmentRarity.RARE || abstractEnchantment.getRarity() == EnchantmentRarity.RAGE_RARE)) {
                        announcement = true;
                    }
                }
                int totalLevel = 0;
                for (AbstractEnchantment abstractEnchantment : mythicItem.getEnchantments().keySet()) {
                    totalLevel += mythicItem.getEnchantments().getInt(abstractEnchantment);
                }
                if ((totalLevel == 8 && RandomUtil.hasSuccessfullyByChance(0.9)) || totalLevel == 9) {
                    //set level of new enchant to 1/2 (3 excluded cuz the limit)
                    if (enchantment != null) {
                        mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b, RandomUtil.hasSuccessfullyByChance(0.1) ? 2 : 1));
                    }
                }
            }


        } else if (amount == 3) { // 111 -> 211/311
            for (AbstractEnchantment abstractEnchantment : enchantments) {
                final int currentLevel = max(mythicItem.getEnchantments().getInt(abstractEnchantment), getRandomLevel());
                mythicItem.getEnchantments().put(abstractEnchantment, currentLevel);
                if (currentLevel == 3 && (abstractEnchantment.getRarity() == EnchantmentRarity.RARE || abstractEnchantment.getRarity() == EnchantmentRarity.RAGE_RARE)) {
                    announcement = true;
                }
            }
            int totalLevel = 0;
            for (AbstractEnchantment abstractEnchantment : mythicItem.getEnchantments().keySet()) {
                totalLevel += mythicItem.getEnchantments().getInt(abstractEnchantment);
            }
            if ((totalLevel == 8 && RandomUtil.hasSuccessfullyByChance(0.9)) || totalLevel == 9) {
                mythicItem.getEnchantments().put((AbstractEnchantment) RandomUtil.helpMeToChooseOne(mythicItem.getEnchantments().keySet().toArray()), 1);
            }
        }
        boolean badLuck = true;
        for (AbstractEnchantment abstractEnchantment : mythicItem.getEnchantments().keySet()) {
            if (mythicItem.getEnchantments().getInt(abstractEnchantment) >= 3) {
                badLuck = false;
                break;
            }
        }
        if (badLuck) {
            AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(mythicItem.getEnchantments().keySet().toArray());
            mythicItem.getEnchantments().put(enchantment, 3);
            if ((enchantment.getRarity() == EnchantmentRarity.RARE || enchantment.getRarity() == EnchantmentRarity.RAGE_RARE)) {
                announcement = true;
            }
        }
        return announcement;
    }

    private boolean levelTier2MythicEnchantLogic(ItemStack item, Player player, IMythicItem mythicItem,int level, List<AbstractEnchantment> rareResults, boolean announcement, MythicColor color, List<AbstractEnchantment> enchantments, List<AbstractEnchantment> results) {
        int amount = mythicItem.getEnchantments().size();

        if (amount <= 1) { // If this item has only 1 enchantment

            boolean useBook = Utils.canUseMythicBook(player, item);

            if (useBook) {
                AbstractEnchantment rareEnchant = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                mythicItem.getEnchantments().put(rareEnchant, 3);
                announcement = true;
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                profile.setEnchantingBook(null);
                mythicItem.boostedByBook = true;
            } else {
                int singleLevel = 0;
                AbstractEnchantment enchantment = null;
                for (Integer i : mythicItem.getEnchantments().values()) {
                    singleLevel = i;
                }
                for (AbstractEnchantment ae : mythicItem.getEnchantments().keySet()) {
                    enchantment = ae;
                }
                if (singleLevel == 1) { //Condition: 1 (Only 1 Lv1 Enchantment)
                    int choice = random.nextInt(3);
                    switch (choice) {
                        case 0: { // 1->3
                            mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,3));
                            if (enchantment.getRarity() == EnchantmentRarity.RARE || enchantment.getRarity() == EnchantmentRarity.RAGE_RARE) {
                                announcement = true;
                            }
                            break;
                        }
                        case 1: { // 1->21
                            mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,2));
                            announcement = shouldAnnouncement(player, color, mythicItem,level, enchantments, announcement, results, rareResults);
                            break;
                        }
                        case 2: { // 1->211
                            mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,2));
                            for (int i = 0; i < 2; i++) {
                                announcement = shouldAnnouncement(player, color, mythicItem,level, enchantments, announcement, results, rareResults);
                            }
                        }
                        default:
                            break;
                    }
                } else if (singleLevel == 2) {
                    int choice = random.nextInt(2);
                    switch (choice) {
                        case 0: { // 2->3
                            mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,3));
                            if (enchantment.getRarity() == EnchantmentRarity.RARE || enchantment.getRarity() == EnchantmentRarity.RAGE_RARE) {
                                announcement = true;
                            }
                            break;
                        }
                        case 1: { // 2->21
                            announcement = shouldAnnouncement(player, color, mythicItem,level, enchantments, announcement, results, rareResults);
                            break;
                        }
                        default:
                            break;
                    }
                } else {
                    announcement = shouldAnnouncement(player, color, mythicItem,level, enchantments, announcement, results, rareResults);
                }
            }
        } else if (amount == 2) { //11
            int choice = random.nextInt(2);
            boolean useBook = Utils.canUseMythicBook(player, item);
            if (useBook) {
                choice = 3;
                AbstractEnchantment rareEnchant = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                mythicItem.getEnchantments().computeInt(rareEnchant, (a,b) -> max(b,3));
                announcement = true;
                PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
                profile.setEnchantingBook(null);
                mythicItem.boostedByBook = true;
            }

            switch (choice) {
                case 0: { // 11->21
                    AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(enchantments.toArray());
                    mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,2));
                    break;
                }
                case 1: { // 11->111
                    results.removeAll(enchantments);
                    AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                    if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
                        announcement = true;
                        rareResults.removeAll(enchantments);
                        enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
                    }
                    enchantments.add(enchantment);
                    mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,1));
                    break;
                }
                default:
                    break;
            }
        }
        return announcement;
    }
    public int max(Integer num1,int num2){
        if(num1 != null){
            return Math.max(num1,num2);
        }
        return num2;
    }

    private boolean levelTier1MythicEnchantLogic(ItemStack item, Player player, IMythicItem mythicItem,int level, List<AbstractEnchantment> rareResults, boolean announcement, List<AbstractEnchantment> results, List<AbstractEnchantment> enchantments) {
        //Tier 1 Enchant Start
        int choice = random.nextInt(4);

        boolean useBook = Utils.canUseMythicBook(player, item);
        if (useBook) {
            choice = 5;
            AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());

            mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,3));
            announcement = true;
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            profile.setEnchantingBook(null);
            mythicItem.boostedByBook = true;
        }

        switch (choice) {
            case 0: { //choice 0: 1 of Lv1 Enchantment

                AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                enchantments.add(enchantment);
                mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,1));
                break;
            }
            case 3: { //choice 0: 2 of Lv1 Enchantment

                for (int i = 0; i < 2; i++) {
                    results.removeAll(enchantments);
                    AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                    enchantments.add(enchantment);
                    mythicItem.getEnchantments().computeInt(enchantment, (a,b) -> max(b,1));
                }
                break;
            }
            case 1: { //choice 0: 1 of Lv2 Enchantment

                AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                enchantments.add(enchantment);
                mythicItem.getEnchantments().put(enchantment, Math.min(enchantment.getMaxEnchantLevel(), 2));
                break;
            }
            case 2: { //choice 0: 2 of Lv2 Enchantment

                AbstractEnchantment enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
                enchantments.add(enchantment);
                mythicItem.getEnchantments().put(enchantment, Math.min(enchantment.getMaxEnchantLevel(), 2));
                break;
            }
            default:
                break;
        }
        return announcement;
    }

    private static boolean isBlackList(Player player, AbstractEnchantment abstractEnchantment) {
        return abstractEnchantment instanceof ILimit;
    }


    private boolean shouldAnnouncement(Player player, MythicColor color, AbstractPitItem mythicItem,int level, List<AbstractEnchantment> enchantments, boolean announcement, List<AbstractEnchantment> results, List<AbstractEnchantment> rareResults) {
        AbstractEnchantment enchantment;
        results.removeAll(enchantments);
        if (RandomUtil.hasSuccessfullyByChance(NewConfiguration.INSTANCE.getChance(player, color,level))) {
            announcement = true;
            rareResults.removeAll(enchantments);
            enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(rareResults.toArray());
        } else {
            enchantment = (AbstractEnchantment) RandomUtil.helpMeToChooseOne(results.toArray());
        }
        enchantments.add(enchantment);
        mythicItem.getEnchantments().put(enchantment, 1);
        return announcement;
    }

    @Override
    public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
        return true;
    }

    public ItemStack getDefaultDisplayItem() {
        return ((MythicEnchantingTable) FuncsKt.getInstance(ThePit.getInstance().getItemFactor().getItemMap().get("enchant_table_mobile"))).toItemStack();
    }

    /**
     * 返回升级至T3为附魔提供的随机等级(1~3) 概率分布6:2:2
     *
     * @return int Level
     */
    private int getRandomLevel() {
        if (RandomUtil.hasSuccessfullyByChance(0.6)) {
            return 1;
        } else {
            return random.nextInt(2) + 2;
        }
    }

    /**
     * 移除玩家指定颜色的裤子
     *
     * @param player 玩家
     * @param color  需要移除的颜色
     * @return 是否移除，返回false为移除失败
     */
    private boolean removeMythicLegWithColor(Player player, MythicColor color) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getEnchantingScience() == null) return false;
        ItemStack itemStack = InventoryUtil.deserializeItemStack(profile.getEnchantingScience());
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        String mythic_color = ItemUtil.getItemStringData(itemStack, "mythic_color");
        for (MythicColor mythicColor : MythicColor.values()) {
            if (mythicColor.getInternalName().equals(mythic_color)) {
                profile.setEnchantingScience(InventoryUtil.serializeItemStack(new ItemStack(Material.AIR)));
                return true;
            }
        }

        return false;
    }

}
