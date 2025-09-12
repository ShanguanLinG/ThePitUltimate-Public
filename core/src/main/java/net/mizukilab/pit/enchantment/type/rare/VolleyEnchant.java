package net.mizukilab.pit.enchantment.type.rare;

import cn.charlotte.pit.ThePit;
import io.irina.backports.utils.SWMRHashTable;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.enchantment.AbstractEnchantment;
import net.mizukilab.pit.enchantment.param.item.BowOnly;
import net.mizukilab.pit.enchantment.rarity.EnchantmentRarity;
import net.mizukilab.pit.parm.AutoRegister;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import net.mizukilab.pit.util.item.ItemBuilder;
import nya.Skip;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Misoryan
 * @Created_In: 2021/3/8 16:55
 */

@AutoRegister
@BowOnly
@Skip
public class VolleyEnchant extends AbstractEnchantment implements Listener {

    private static final Map<UUID, Cooldown> cooldown = new SWMRHashTable<>();

    private final Field playerUsingFiled;
    private Object unreflected = null;

    @SneakyThrows
    public VolleyEnchant() {
        this.playerUsingFiled = EntityHuman.class.getDeclaredField("h");
        this.playerUsingFiled.setAccessible(true);
        try {
            VarHandle varHandle = MethodHandles.lookup().unreflectVarHandle(playerUsingFiled);
            unreflected = varHandle;
        } catch (Throwable e) {
            unreflected = null;
        }
    }

    @Override
    public String getEnchantName() {
        return "连续射击";
    }

    @Override
    public int getMaxEnchantLevel() {
        return 3;
    }

    @Override
    public String getNbtName() {
        return "volley_enchant";
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public Cooldown getCooldown() {
        return null;
    }

    @Override
    public String getUsefulnessLore(int enchantLevel) {
        return "&7射箭时同时射出 &e" + (enchantLevel + 3) + " &7支箭矢";
    }

    private Map<UUID, Boolean> isShooting = new SWMRHashTable<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        cooldown.remove(uniqueId);
        isShooting.remove(uniqueId);
    }

    @EventHandler
    @SneakyThrows
    public void onInteract(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (PlayerUtil.shouldIgnoreEnchant(player)) return;
        final org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) return;
        final int level = Math.min(3, this.getItemEnchantLevel(itemInHand));
        if (level == -1) {
            return;
        }
        if (isShooting.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        if(event.getProjectile().hasMetadata("volley_enchant_arrow")){
            return;
        }
        if (itemInHand.getType() == Material.BOW) {
            try {
                if (cooldown.getOrDefault(player.getUniqueId(), new Cooldown(0)).hasExpired()) {

                    //shoot 5 arrows need 400ms u suck why u set it to 200ms
                    if (player.isSneaking()) {
                        return;
                    }

                    final ItemStack item = Utils.toNMStackQuick(itemInHand);
                    final ItemBow bow = (ItemBow) item.getItem();

                    final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

                    final int value;
                    if (unreflected != null) {
                        value = (int) ((VarHandle) unreflected).get(entityPlayer);
                    } else {
                        value = (int) this.playerUsingFiled.get(entityPlayer);
                    }
                    ItemBuilder arrowBuilder = new ItemBuilder(Material.ARROW).internalName("default_arrow").defaultItem().canDrop(false).canSaveToEnderChest(false);

                    //let shooting multiple arrows cost 1 arrow at once
                    player.getInventory().addItem(arrowBuilder.amount(level + 1).build());

                    new BukkitRunnable() {
                        int tick = 0;

                        {
                            isShooting.put(player.getUniqueId(), true);
                        }

                        @Override
                        public void run() {
                            if (tick >= level + 2) {
                                cooldown.put(player.getUniqueId(), new Cooldown(15L, TimeUnit.MILLISECONDS));
                                isShooting.remove(player.getUniqueId());
                                this.cancel();
                            } else {
                                forkItemBowEntity(bow,item, entityPlayer.world, entityPlayer, value,1);
                            }
                            tick++;
                        }
                    }.runTaskTimer(ThePit.getInstance(), 0, 2);
                }
            } catch (Exception e) {
                CC.printError(player, e);
            }
        }
    }
    public void forkItemBowEntity(ItemBow bow,ItemStack itemstack, World world, EntityHuman entityhuman, int i,int count) {
        boolean flag = entityhuman.abilities.canInstantlyBuild || EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_INFINITE.id, itemstack) > 0;
        if (flag || entityhuman.inventory.b(Items.ARROW)) {
            int j = bow.d(itemstack) - i;
            float f = (float) j / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if ((double) f < 0.1) {
                return;
            }

            if (f > 1.0F) {
                f = 1.0F;
            }

            boolean shouldCritical = f == 1.0F;

            int enchantmentLevel = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, itemstack);
            boolean arrowDmg = enchantmentLevel > 0;
            double d0 = 2.5 + enchantmentLevel * 0.5;
            int enchantmentLevel1 = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, itemstack);
            for (int k = 0; k < count; k++) {
                EntityArrow entityarrow = new EntityArrow(world, entityhuman, f * 2.0F);
                if (EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, itemstack) > 0) {
                    CraftEntity bukkitEntity = entityarrow.getBukkitEntity();
                    bukkitEntity.setMetadata("volley_enchant_arrow", new FixedMetadataValue(ThePit.getInstance(), true));
                    Utils.pointMetadataAndRemove(bukkitEntity, 100, "volley_enchant");
                    EntityCombustEvent event = new EntityCombustEvent(bukkitEntity, 100);
                    entityarrow.world.getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        entityarrow.setOnFire(event.getDuration());
                    }
                }
                if (shouldCritical) {
                    entityarrow.setCritical(true);
                }
                if (arrowDmg) {
                    entityarrow.b(d0);
                }

                if (enchantmentLevel1 > 0) {
                    entityarrow.setKnockbackStrength(enchantmentLevel1);
                }

                world.addEntity(entityarrow);
                if (flag) {
                    entityarrow.fromPlayer = 2;
                } else {
                    entityhuman.inventory.a(Items.ARROW);
                }
            }

            itemstack.damage(count, entityhuman);
            //world.makeSound(entityhuman, "random.bow", 1.0F, 1.0F / (ThreadLocalRandom.current().nextFloat() * 0.4F + 1.2F) + f * 0.5F);


            entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(bow)]);
        }
    }
}
