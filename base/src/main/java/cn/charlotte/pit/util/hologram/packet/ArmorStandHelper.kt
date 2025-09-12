package cn.charlotte.pit.util.hologram.packet

import net.minecraft.server.v1_8_R3.EntityArmorStand
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity

/**
 * 2024/5/16<br></br>
 * ThePitPlus<br></br>
 *
 * @author huanmeng_qwq
 */
object ArmorStandHelper {

    @JvmStatic
    fun applyLocation(location: Location, armorStand: PacketArmorStand) {
        armorStand.move(location, true)
    }

    //傻逼幻梦。
    @JvmStatic
    fun memoryEntity(location: Location): ArmorStand {
        val worldServer = (location.world as CraftWorld).handle
        val entityArmorStand = EntityArmorStand(worldServer, location.x, location.y, location.z)
        return entityArmorStand.bukkitEntity as ArmorStand
    }

    @JvmStatic
    fun setEntityLocation(entity: Entity, to: Location) {
        entity as CraftEntity
        entity.handle.setLocation(to.x, to.y, to.z, to.yaw, to.pitch) //crasher code -->
        //locate to public void entityJoinedWorld(Entity entity, boolean flag)
        //it will not add to Minecraft Server Entity System, but it will be added to the chunk, that is bug from bukkit
    }
}
