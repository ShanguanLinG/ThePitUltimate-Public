package cn.charlotte.pit.util.hologram.packet;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReflectUtil;
import net.minecraft.server.v1_8_R3.*;
import net.mizukilab.pit.util.PublicUtil;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 2022/2/26<br>
 * LimeCode<br>
 *
 * @author huanmeng_qwq
 */
public class PacketArmorStand {

    public static final double DISTANCE = 64;
    protected String text;
    public Location location;
    protected final ArmorStand entity;
    protected final Set<Player> users;
    protected final Set<Player> viewing;
    protected final Set<Player> hiding;

    public PacketArmorStand(String text, Location location) {
        this.text = text;
        this.location = location;
        this.entity = ArmorStandHelper.memoryEntity(location);
        this.users = new ConcurrentHashSet<>();
        this.viewing = new ConcurrentHashSet<>();
        this.hiding = new ConcurrentHashSet<>();
        init();
    }

    public void init() {
        if (!text.isEmpty()) {
            entity.setCustomName(text);
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomNameVisible(false);
        }
    }

    public void invisible() {
        entity.setVisible(false);
    }

    public void small() {
        entity.setSmall(true);
    }

    public void redisplay(Player user, boolean force) {
        hide(user, force);
        show(user, force);
    }

    public void addUser(Player user) {
        if (users.add(user)) {
            show(user, false);
        }
    }

    public void removeUser(Player user) {
        if (users.remove(user)) {
            hide(user, true);
        }
        hiding.remove(user);
        viewing.remove(user);
    }

    //MUST DO THIS AFTER THE USE IT CAN BE A FUCKING CRASHER for YOUR SERVER SILENT!!!!
    //The dumb huanmeng_qwq
    public void recycleEntity() {
        this.entity.remove(); //remove the memory entity from the list.
        PublicUtil.removeFromWorld(entity);
    }

    public void removeAll() {
        ArrayList<Player> list = new ArrayList<>(users);
        for (Player user : list) {
            removeUser(user);
        }
        viewing.clear();
        hiding.clear();
        users.clear();
    }

    public void show(Player user, boolean force) {
        if (!user.getWorld().equals(location.getWorld())) {
            hiding.add(user);
            viewing.remove(user);
            return;
        }
        if (user.getLocation().distance(location) >= DISTANCE) {
            hiding.add(user);
            viewing.remove(user);
            return;
        }
        if (viewing.add(user) || force) {
            sendEquipments(user);
            hiding.remove(user);
        }
    }

    public void sendEquipments(Player user) {
        PacketPlayOutSpawnEntityLiving spawnEntityLiving = new PacketPlayOutSpawnEntityLiving(((CraftArmorStand) entity).getHandle());
        EntityPlayer handle = ((CraftPlayer) user).getHandle();
        handle.playerConnection.sendPacket(spawnEntityLiving);
        {
            for (Pair<EquipmentSlot, ItemStack> pair : getItems(entity)) {
                PacketPlayOutEntityEquipment entityEquipment = new PacketPlayOutEntityEquipment(entity.getEntityId(), pair.getKey().ordinal(), PublicUtil.toNMStackQuick(pair.getValue()));
                handle.playerConnection.sendPacket(entityEquipment);
            }
        }
    }

    private List<Pair<EquipmentSlot, ItemStack>> getItems(LivingEntity entity) {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>(6);
        EntityEquipment equipment = entity.getEquipment();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            switch (equipmentSlot.name()) {
                case "HAND":
                    list.add(new Pair<>(equipmentSlot, equipment.getItemInHand()));
                    break;
                case "OFF_HAND":
                    list.add(new Pair<>(equipmentSlot, ReflectUtil.invoke(equipment, "getItemInOffHand")));
                    break;
                case "FEET":
                    list.add(new Pair<>(equipmentSlot, equipment.getBoots()));
                    break;
                case "LEGS":
                    list.add(new Pair<>(equipmentSlot, equipment.getLeggings()));
                    break;
                case "CHEST":
                    list.add(new Pair<>(equipmentSlot, equipment.getChestplate()));
                    break;
                case "HEAD":
                    list.add(new Pair<>(equipmentSlot, equipment.getHelmet()));
                    break;
            }
        }
        return list;
    }

    public void hide(Player user, boolean force) {
        if (!user.getWorld().equals(location.getWorld())) {
            hiding.add(user);
            viewing.remove(user);
            return;
        }
        if (!force && user.getLocation().distance(location) >= DISTANCE) {
            hiding.add(user);
            viewing.remove(user);
            return;
        }
        if (hiding.add(user) || force) {
            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entity.getEntityId());
            ((CraftPlayer) user).getHandle().playerConnection.sendPacket(destroy);
            viewing.remove(user);
        }
    }

    private void clean() {
        viewing.removeIf(e -> !e.isOnline());
        hiding.removeIf(e -> !e.isOnline());
        users.removeIf(e -> !e.isOnline());
    }

    public void update() {
        clean();
        updateView();
        setText(text, false);
        ArmorStand entity1 = this.entity;
        CraftArmorStand entity11 = (CraftArmorStand) entity1;
        PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(
                entity1.getEntityId(), entity11.getHandle().getDataWatcher(), true);
        for (Player user : viewing) {
            ((CraftPlayer) user).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata);
        }
    }

    public void updateView() {
        ArrayList<Player> viewingList = new ArrayList<>(viewing);
        ArrayList<Player> hidingList = new ArrayList<>(hiding);
        for (Player user : viewingList) {
            if (!user.getWorld().equals(location.getWorld())) {
                continue;
            }
            if (user.getLocation().distance(location) >= DISTANCE) {
                hide(user, false);
            } else {
                show(user, false);
            }
        }
        for (Player user : hidingList) {
            if (!user.getWorld().equals(location.getWorld())) {
                continue;
            }
            if (user.getLocation().distance(location) < DISTANCE) {
                show(user, false);
            }
        }
    }

    public void location(@NotNull Location location) {
        ArmorStandHelper.applyLocation(location, this);
    }

    public String text() {
        return text;
    }

    public PacketArmorStand setText(String text) {
        return this.setText(text, true);
    }

    public PacketArmorStand setText(String text, boolean update) {
        if (this.text.equals(text)) {
            return this;
        }
        this.text = text;
        if (!text.isEmpty()) {
            entity.setCustomName(text);
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomNameVisible(false);
        }
        if (update) {
            update();
        }
        return this;
    }

    public PacketArmorStand setNameVisible(boolean visible) {
        entity.setCustomNameVisible(visible);
        return this;
    }

    public Location location() {
        return location;
    }

    public Set<Player> users() {
        return users;
    }

    public ArmorStand entity() {
        return entity;
    }

    public Set<Player> viewing() {
        return viewing;
    }

    public void sendTo(Location to, boolean ground) {
        double v = to.getX() - this.location.getX();
        double p = to.getX() - this.location.getX();
        double a = to.getZ() - location.getZ();
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook entityMoveLook = new PacketPlayOutEntity
                .PacketPlayOutRelEntityMoveLook(entity.getEntityId(), (byte) v,
                (byte) p, (byte) a, (byte) MathHelper.floor(to.getYaw() * 256.0F / 360F), (byte) MathHelper.floor(to.getPitch() * 256.0F / 360.0F), ground);
        for (Player user : viewing) {
            ((CraftPlayer) user).getHandle().playerConnection.sendPacket(entityMoveLook);
        }
    }

    public void move(Location to, boolean ground) {
        double teleportThreshold = 4;
        double v = to.getX() - this.location.getX();
        double p = to.getY() - this.location.getY();
        double a = to.getZ() - location.getZ();

        double distance = Math.abs(v) +
                Math.abs(p) + Math.abs(a);


        if (distance > teleportThreshold) {
            this.sendTeleportPacket(to, ground);
        } else {
            PacketPlayOutEntity.PacketPlayOutRelEntityMove entityMove =
                    new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entity.getEntityId(), (byte) MathHelper.floor(v * 32D),
                            (byte) MathHelper.floor(p * 32D), (byte) MathHelper.floor(a * 32D), ground);
            for (Player user : viewing) {
                ;
                ((CraftPlayer) user).getHandle().playerConnection.sendPacket(entityMove);
            }
            senHeadYaw(to.getYaw());
        }
        ArmorStandHelper.setEntityLocation(entity, to.clone());
        this.location = to.clone();
    }

    public void sendTeleportPacket(Location location, boolean ground) {
        PacketPlayOutEntityTeleport entityTeleport = new PacketPlayOutEntityTeleport(entity.getEntityId(),
                MathHelper.floor(location.getX() * 32), MathHelper.floor(location.getY() * 32), MathHelper.floor(location.getZ() * 32)
                , getFixRotation(location.getYaw()), getFixRotation(location.getPitch()), ground);
        for (Player user : viewing) {
            ((CraftPlayer) user).getHandle().playerConnection.sendPacket(entityTeleport);
        }
    }


    public static byte getFixRotation(final float yawpitch) {
        return (byte) (yawpitch * 256.0F / 360.0F);
    }

    public void senHeadYaw(float yaw) {
        PacketPlayOutEntityHeadRotation entityHeadRotation = new PacketPlayOutEntityHeadRotation(((CraftArmorStand) entity).getHandle()
                , getFixRotation(yaw));
        for (Player user : viewing) {
            ((CraftPlayer) user).getHandle().playerConnection.sendPacket(entityHeadRotation);
        }
    }
}
