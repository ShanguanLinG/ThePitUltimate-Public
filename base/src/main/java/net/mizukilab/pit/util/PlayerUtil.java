package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PerkData;
import cn.charlotte.pit.event.PitPotionEffectEvent;
import cn.charlotte.pit.event.PitRegainHealthEvent;
import cn.charlotte.pit.events.trigger.type.IEpicEvent;
import cn.charlotte.pit.perk.AbstractPerk;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.UtilKt;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.item.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/30 22:30
 */
public class PlayerUtil {
    public static String BOT_NAME = "bot";

    public static String getActiveMegaStreak(Player player) {
        return CC.translate(getActiveMegaStreakObj(player).getDisplayName());
    }

    public static AbstractPerk getActiveMegaStreakObj(Player player) {
        final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        return profile.getActiveMegaStreakObj();
    }

    public static boolean isVenom(Player player) {
        return player.hasMetadata("combo_venom") && player.getMetadata("combo_venom").get(0).asLong() > System.currentTimeMillis();
    }

    public static void addAbsorptionHearts(Player player, float extraHearts) {
        if (player instanceof CraftPlayer) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            float currentHearts = craftPlayer.getHandle().getAbsorptionHearts();
            if (currentHearts < 40) {
                craftPlayer.getHandle().setAbsorptionHearts(currentHearts + extraHearts);
            }
        }
    }

    public static void clearAbsorptionHearts(Player player) {
        if (player instanceof CraftPlayer) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            craftPlayer.getHandle().setAbsorptionHearts(0);
        }
    }

    public static boolean isEquippingSomber(Player player) {
        PlayerProfile playerProfile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        return playerProfile.leggings != null && playerProfile.leggings.getEnchantmentLevel( "somber_enchant") > 0;
    }

    public static boolean isCritical(Player player) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return entityPlayer.fallDistance > 0.0F && !entityPlayer.onGround && !entityPlayer.k_() && !entityPlayer.V() && !entityPlayer.hasEffect(MobEffectList.BLINDNESS) && entityPlayer.vehicle == null;
    }

    //如果自身穿着黑裤/被毒则无法使用附魔 (适用于无目标附魔)
    public static boolean shouldIgnoreEnchant(Player self) {
        boolean sinkingMoonlight = PlayerUtil.isSinkingMoonlight(self);
        if (sinkingMoonlight) return true;
        return isEquippingSomber(self) || isVenom(self); //|| sinkingMoonlight; always false plz
    }

    public static boolean shouldIgnoreEnchant(Player self, org.bukkit.entity.Entity target) { //大合集
        boolean sinkingMoonlight = PlayerUtil.isSinkingMoonlight(self); //缓存结果
        if (sinkingMoonlight) return true;
        if (target instanceof Player player) { //对其他人使用
            boolean equipSomberTarget;
            boolean isVenom;
            if (isNPC(player)) {
                equipSomberTarget = false;
                isVenom = false;
            } else {
                equipSomberTarget = isEquippingSomber(player);
                isVenom = isVenom(player);
            }
            return isEquippingSomber(self) || isVenom(self) || (equipSomberTarget && !isEquippingArmageddon(self)) || isVenom;
        } else { //如果他不是人或者null
            return isEquippingSomber(self) || isVenom(self);
        }
    }

    /*  public static boolean shouldIgnoreEnchant(PlayerProfile selfPro,PlayerProfile targetProf,Player self, org.bukkit.entity.Entity target){ //大合集
          boolean sinkingMoonlight = PlayerUtil.isSinkingMoonlight(self); //缓存结果
          if (sinkingMoonlight) return true;
          if(target instanceof Player player){ //对其他人使用
              boolean equipSomberTarget;
              boolean isVenom;
              if(isNPC(player)){
                  equipSomberTarget = false;
                  isVenom = false;
              } else {
                  if(targetProf != null) {
                      equipSomberTarget = isEquippingSomberFast(targetProf);
                  } else {
                      equipSomberTarget = isEquippingSomber(player);
                  }
                  isVenom = isVenom(player);
              }
              return isEquippingSomberFast(selfPro) || isVenom(self) || (equipSomberTarget && !isEquippingArmageddon(self)) || isVenom;
          } else { //如果他不是人或者null
              return isEquippingSomberFast(selfPro) || isVenom(self);
          }
      }*/ //无影响
    //自身对其他人使用附魔时附魔是否应该失效 (适用于有目标附魔)
    public static boolean shouldIgnoreEnchant(Player self, Player target) {
        boolean sinkingMoonlight = PlayerUtil.isSinkingMoonlight(self);
        if (sinkingMoonlight) return true;

        //自身穿黑裤时必定失效 && 自身被沉默时必定生效 && 其他人穿黑裤且自身没有鞋子时失效 && 对方被沉默时失效
        return isEquippingSomber(self) || isVenom(self) || (isEquippingSomber(target) && !isEquippingArmageddon(self)) || isVenom(target);
    }

    //进行合并方法
    public static boolean isNPC(org.bukkit.entity.Entity entity) {
        if(entity.getName().equals("666")){
            return true;
        }
        return ((CraftEntity) entity).getHandle().getClass().getSuperclass().equals(EntityPlayer.class);
    }


    public static boolean isSinkingMoonlight(Player player) {
        return player.hasMetadata("sinking_moonlight") && player.getMetadata("sinking_moonlight").get(0).asLong() > System.currentTimeMillis();
    }

    public static boolean isEquippingArmageddon(Player player) {
        return "armageddon_boots".equals(ItemUtil.getInternalName(player.getInventory().getBoots()));
    }

    public static boolean isEquippingAngelChestplate(Player player) {
        return "angel_chestplate".equals(ItemUtil.getInternalName(player.getInventory().getChestplate()));
    }

    /**
     * @param player
     * @return if player is vanished (SuperVanish Plugin based)
     */
    public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    /**
     * 对玩家造成伤害,仅判断伤害能否被造成与造成伤害,不进行其他操作
     *
     * @param victim     受到伤害的目标
     * @param damageType 伤害类型
     * @param damage     伤害量
     * @param canImmune  此伤害能否被免疫 / 降低
     * @return 此伤害是否被免疫
     */
    public static boolean damage(Player victim, DamageType damageType, Double damage, boolean canImmune) {
        boolean immune = false;
        if (victim.getInventory().getLeggings() != null && victim.getInventory().getLeggings().getType() != Material.AIR) {
            immune = canImmune && ThePit.getApi().getItemEnchantLevel(victim.getInventory().getLeggings(), "Mirror") >= 1;
        }
        if (immune && damageType == DamageType.TRUE) return true;
        switch (damageType) {
            case NORMAL:
                victim.damage(damage);
                break;
            case TRUE:
                if (victim.getHealth() > damage) {
                    double max = Math.max(victim.getHealth() - damage, 0.0);
                    victim.setHealth(Math.min(victim.getMaxHealth(), max));
                } else {
                    victim.setNoDamageTicks(0);
                    victim.damage(victim.getMaxHealth() * 100);
                }
                break;
        }
        return false;
    }

    public static void damage(Entity victim, DamageType damageType, Double damage, Boolean canImmune) {

    }

    public static boolean cantIgnore(Player player) {
        return !(player.hasMetadata("true_damage_immune_ignore_immune") && player.getMetadata("true_damage_immune_ignore_immune").size() > 0 && player.hasMetadata("mirror_latest_active") && System.currentTimeMillis() < player.getMetadata("mirror_latest_active").get(0).asLong());
    }

    /**
     * 对玩家造成来源类型为玩家的伤害
     *
     * @param attacker   伤害来源(类型玩家)
     * @param victim     受到伤害的目标
     * @param damageType 伤害类型
     * @param damage     伤害量
     * @param canImmune  此伤害能否被免疫 / 降低
     * @return 此伤害是否被免疫
     */
    public static void damage(Player attacker, Player victim, DamageType damageType, double damage, boolean canImmune) {
        boolean immune = damage(victim, damageType, damage, canImmune);
        if (immune) {
            //Mirror附魔反弹伤害
            if (damageType == DamageType.TRUE && victim.getInventory().getLeggings() != null && victim.getInventory().getLeggings().getType() != Material.AIR) {
                int level = ThePit.getApi().getItemEnchantLevel(victim.getInventory().getLeggings(), "Mirror");
                if (level >= 2) damage(attacker, damageType, damage * (0.25 * level - 0.25), false);
            }
        }
        PublicUtil.processActionBarWithSetting(attacker, victim, (int) damage, damage);
    }


    public static boolean isStaffSpectating(Player player) {
        return false;
    }

    public static boolean isPlayerChosePerk(Player player, String internal) {
        if (player == null) return false;
        if (UtilKt.hasRealMan(player)) return false;
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        for (PerkData entry : profile.getChosePerk().values()) {
            if (entry.getPerkInternalName().equals(internal)) {
                return true;
            }
        }
        return false;
    }

    public static int getPlayerHealItemLimit(Player player) {
        int limit = 2;
        boolean vampirePresent = PlayerUtil.isPlayerChosePerk(player, "Vampire");
        boolean ramboPresent = PlayerUtil.isPlayerChosePerk(player, "rambo");
        boolean overHeal = isPlayerChosePerk(player, "OverHeal");
        boolean olympusPresent = PlayerUtil.isPlayerChosePerk(player, "Olympus");
        AbstractPitItem leggings = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId()).leggings;
        int overHealEnchant = leggings == null ? -1 : leggings.getEnchantmentLevel("over_heal_enchant");
        if (olympusPresent) {
            limit = 1;
        }
        if (overHeal) {
            limit += limit;
        }
        if (overHealEnchant > 0 && !isEquippingSomber(player) && !isVenom(player)) {
            limit += overHealEnchant;
        }
        if (ramboPresent || vampirePresent) {
            limit = -999;
        }
        return limit;
    }

    public static int getPlayerHealItemAmount(Player player) {
        int amount = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (ItemUtil.isHealingItem(itemStack)) {
                amount += itemStack.getAmount();
            }
        }
        return amount;
    }

    public static int getAmountOfActiveHealingPerk(Player player) {

        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if(!profile.isInArena()){
            return Integer.MAX_VALUE;
        }
        boolean vampirePresent = PlayerUtil.isPlayerChosePerk(player, "Vampire");
        if(vampirePresent){
            return Integer.MAX_VALUE;
        }
        boolean ramboPresent = PlayerUtil.isPlayerChosePerk(player, "rambo");
        if(ramboPresent){
            return Integer.MAX_VALUE;
        }
        boolean goldenHeadPresent = PlayerUtil.isPlayerChosePerk(player, "GoldenHead");
        boolean olympusPresent = PlayerUtil.isPlayerChosePerk(player, "Olympus");
        boolean tastySoupPresent = PlayerUtil.isPlayerChosePerk(player, "tasty_soup_perk");
        int amount = 0;
        if (goldenHeadPresent) {
            amount++;
        }
        if (olympusPresent) {
            amount++;
        }
        if (tastySoupPresent) {
            amount++;
        }
        return amount;
    }

    public static boolean isPlayerUnlockedPerk(Player player, String internal) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        return profile.getUnlockedPerkMap().get(internal) != null;
    }

    public static float getDistance(Player p1, Player p2) {
        Location lc1 = p1.getLocation();
        Location lc2 = p2.getLocation();
        return getDistance(lc1, lc2);
    }

    public static float getDistance(Location lc1, Location lc2) {
        if (lc1.getWorld() != lc2.getWorld()) {
            return Float.MAX_VALUE;
        }
        return (float) lc1.distance(lc2);
    }

    public static int getPlayerUnlockedPerkLevel(Player player, String internal) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        PerkData data = profile.getUnlockedPerkMap().get(internal);
        if (data == null) {
            return 0;
        }
        return data.getLevel();
    }

    public static boolean isPlayerBoughtPerk(Player player, String internal) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        return profile.getBoughtPerkMap().containsKey(internal);
    }

    public static int getPlayerArmorDefense(Player player) {
        int defense = 0;
        ItemStack[] is = player.getInventory().getArmorContents();
        for (ItemStack i : is) {
            Material type = i.getType();
            if (type == Material.LEATHER_HELMET || type == Material.LEATHER_BOOTS || type == Material.GOLD_BOOTS || type == Material.CHAINMAIL_BOOTS) {
                defense += 1;
            }
            if (type == Material.LEATHER_LEGGINGS || type == Material.GOLD_HELMET || type == Material.CHAINMAIL_HELMET || type == Material.IRON_HELMET) {
                defense += 2;
            }
            if (type == Material.LEATHER_CHESTPLATE || type == Material.GOLD_LEGGINGS || type == Material.DIAMOND_HELMET || type == Material.DIAMOND_BOOTS) {
                defense += 3;
            }
            if (type == Material.CHAINMAIL_LEGGINGS) {
                defense += 4;
            }
            if (type == Material.GOLD_CHESTPLATE || type == Material.CHAINMAIL_CHESTPLATE || type == Material.IRON_LEGGINGS) {
                defense += 5;
            }
            if (type == Material.IRON_CHESTPLATE || type == Material.DIAMOND_LEGGINGS) {
                defense += 6;
            }
            if (type == Material.DIAMOND_CHESTPLATE) {
                defense += 8;
            }
        }
        return Math.min(20, defense);
    }

    public static Player getStaffSpectating(Player player) {
        return player.hasMetadata("STAFF_SPECTATOR") ? (Player) player.getMetadata("STAFF_SPECTATOR").get(0).value() : null;
    }

    public static void clearStaffSpectateTarget(Player player) {
        if (isStaffSpectating(player)) {
            player.removeMetadata("STAFF_SPECTATOR", ThePit.getInstance());
        }
    }

    public static void setStaffSpectateTarget(Player player, Player target) {
        if (isStaffSpectating(player)) {
            player.setMetadata("STAFF_SPECTATOR", new FixedMetadataValue(ThePit.getInstance(), target));
        }
    }

    public static boolean isStaff(Player player) {
        return player.hasPermission(getStaffPermission()) || player.hasPermission("thepit.admin");
    }

    public static String getStaffPermission() {
        return "domcer.staff.default";
    }

    public static void setFirstSlotOfType(Player player, Material type, ItemStack itemStack) {
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack1 = player.getInventory().getContents()[i];
            if (itemStack1 == null || itemStack1.getType() == type || itemStack1.getType() == Material.AIR) {
                player.getInventory().setItem(i, itemStack);
                break;
            }
        }
    }


    public static void resetPlayer(Player player) {
        resetPlayer(player, true, true);
    }

    public static void resetPlayer(Player player, boolean closeInventor) {
        resetPlayer(player, closeInventor, true);
    }

    public static void resetPlayer(Player player, boolean closeInventory, boolean clearInventory) {
        player.setSaturation(12.8F);
        player.setMaximumNoDamageTicks(20);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        player.setLevel(0);
        player.setExp(0.0F);
        player.getInventory().setHeldItemSlot(0);
        player.setAllowFlight(false);
        player.setCanPickupItems(true);
        if (clearInventory) {
            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
            player.getEnderChest().clear();
        }
        if (closeInventory) {
            player.closeInventory();
        }
        player.setGameMode(GameMode.SURVIVAL);

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.getDataWatcher().watch(9, (byte) 0);
        entityPlayer.removeAllEffects();
        entityPlayer.setAbsorptionHearts(0.0F);
        //temp disable
        player.setWalkSpeed(0.2F);

        player.updateInventory();
    }

    public static void postResetPlayer(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        //apply stats - start
        player.setFoodLevel(profile.getFoodLevel());
        IEpicEvent activeEpicEvent = ThePit.getInstance().getEventFactory().getActiveEpicEvent();
        if (activeEpicEvent != null) {
            if (activeEpicEvent.processTrigger(IEpicEvent.TrigAction.CLEAR, player, profile)) {
                player.setMaxHealth(profile.getMaxHealth());
            }
        }
        //apply stats - end
        //heal player
        player.setHealth(player.getMaxHealth());
    }

    public static void sendMessage(String message, Player... players) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public static void sendMessage(String message, Set<Player> players) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public static void deadPlayer(Player player) {
        PlayerUtil.damage(player, PlayerUtil.DamageType.TRUE, player.getMaxHealth() * 100, false);
    }

    public static Collection<Player> getNearbyPlayers(Location location, double radius) {
        return getNearbyEntitiesByType(Player.class, location, radius, radius, radius, null);
    }

    public static Collection<LivingEntity> getNearbyPlayersAndChicken(Location location, double radius) {
        return getNearbyEntitiesByType(LivingEntity.class, location, radius, radius, radius, e -> e instanceof Chicken || e instanceof Player);
    }

    public static <T extends org.bukkit.entity.Entity> Collection<T> getNearbyEntitiesByType(Class<? extends org.bukkit.entity.Entity> clazz, Location loc, double xRadius, double yRadius, double zRadius, Predicate<T> predicate) {
        if (clazz == null) {
            clazz = org.bukkit.entity.Entity.class;
        }
        Collection<org.bukkit.entity.Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, xRadius, yRadius, zRadius);

        List<T> nearby = new ObjectArrayList<>(nearbyEntities.size() + 1);
        for (org.bukkit.entity.Entity bukkitEntity : nearbyEntities) {
            //noinspection unchecked
            if (clazz.isAssignableFrom(bukkitEntity.getClass()) && (predicate == null || predicate.test((T) bukkitEntity))) {
                //noinspection unchecked
                nearby.add((T) bukkitEntity);
            }
        }
        return nearby;
    }

    public static void heal(Player player, double heal) {
        PitRegainHealthEvent event = new PitRegainHealthEvent(player, heal);
        event.callEvent();
        if (event.isCancelled()) {
            return;
        }
        heal = Math.max(event.getAmount(), 0);
        player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
    }

    public static void food(Player player, int level) {
        player.setFoodLevel(
                Math.min(player.getFoodLevel() + level, 20));
    }

    public static void takeOneItemInHand(Player player) {
        ItemStack itemStack = player.getItemInHand();
        if (itemStack.getAmount() == 1) {
            player.setItemInHand(null);
            return;
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
        player.setItemInHand(itemStack);
    }

    public static void playThunderEffect(Location thunderLocation) {
        EntityLightning lightning = new EntityLightning(
                ((CraftWorld) thunderLocation.getWorld()).getHandle(), thunderLocation.getX(), thunderLocation.getY(), thunderLocation.getZ(), true, true
        );

        PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(lightning);
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void addPotionEffect(Player player, PotionEffect effect) {
        PlayerUtil.addPotionEffect(player, effect, false);
    }

    public static void addPotionEffect(Player player, PotionEffect effect, boolean force) {
        PitPotionEffectEvent event = new PitPotionEffectEvent(player, effect);
        event.callEvent();
        if (event.isCancelled()) {
            return;
        }
        event.getPlayer().addPotionEffect(event.getPotionEffect(), force);
    }

    public static void sendParticle(Location location, EnumParticle particle, int count) {
        new ParticleBuilder(location, particle).setCount(count).play();
    }

    public enum DamageType {
        NORMAL,
        TRUE
    }
}
