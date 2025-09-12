package cn.charlotte.pit.util.hologram.packet

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.util.hologram.HologramAPI
import cn.charlotte.pit.util.hologram.touch.TouchHandler
import cn.charlotte.pit.util.hologram.view.ViewHandler
import eu.decentsoftware.holograms.api.nms.NMS
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * 2024/5/16<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */
typealias Parent = cn.charlotte.pit.util.hologram.Hologram

class PacketHologram(var displayText: String, var loc: Location) : Parent {
    val hologram = Hologram(displayText, loc)

    var above: cn.charlotte.pit.util.hologram.Hologram? = null

    var attachingEntity = -1

    var spawned = true
    var allPlayers = true

    override fun isSpawned(): Boolean {
        return spawned
    }

    override fun spawn(ticks: Long) {
        spawn()
    }

    override fun spawn(receivers: MutableCollection<out Player>?): Boolean {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance()) {
                spawn(receivers)
            }
            return true;
        }
        allPlayers = false
        receivers?.forEach {
            hologram.addUser(it)
        }

        return spawn()
    }

    override fun spawn(): Boolean {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ThePit.getInstance()) {
                spawn()
            }
            return true;
        }
        spawned = true
        PacketHologramRunnable.holograms.add(this)
        return true
    }

    override fun deSpawn(): Boolean {
        if (Bukkit.isPrimaryThread()) {
            hologram.removeAll()
            spawned = false
            HologramAPI.removeHologram(this)
        } else {
            Bukkit.getScheduler().runTask(ThePit.getInstance()) {
                deSpawn()
            }
        }
        return true
    }

    override fun getText(): String {
        return displayText
    }

    override fun setText(text: String?) {
        val after = text ?: ""
        if (after == this.displayText) return
        this.displayText = after
        hologram.setText(this.displayText)
    }

    override fun update() {
        if (!spawned) {
            return
        }
        hologram.update()
        if (allPlayers) {
            loc.world.players.forEach {
                hologram.addUser(it)
            }
        }
    }

    override fun update(interval: Long) {

    }

    override fun getLocation(): Location {
        return loc
    }

    override fun setLocation(loc: Location) {
        if (!Bukkit.isPrimaryThread()) {
            move(loc)
        } else {
            Bukkit.getScheduler().runTask(ThePit.getInstance()) {
                move(loc)
            }
        }
    }

    override fun move(loc: Location) {
        hologram.location(loc)
        this.loc = loc.clone();
    }

    override fun isTouchable(): Boolean {
        return false
    }

    override fun setTouchable(flag: Boolean) {

    }

    override fun addTouchHandler(handler: TouchHandler) {

    }

    override fun removeTouchHandler(handler: TouchHandler) {

    }

    override fun getTouchHandlers(): MutableCollection<TouchHandler> {
        TODO("Not yet implemented")
    }

    override fun clearTouchHandlers() {

    }

    override fun addViewHandler(handler: ViewHandler) {

    }

    override fun removeViewHandler(handler: ViewHandler) {

    }

    override fun getViewHandlers(): MutableCollection<ViewHandler> {
        return mutableListOf()
    }

    override fun clearViewHandlers() {

    }

    override fun addLineBelow(text: String): Parent {
        TODO("Not yet implemented")
    }

    override fun getLineBelow(): Parent? {
        return null
    }

    override fun removeLineBelow(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLinesBelow(): MutableCollection<cn.charlotte.pit.util.hologram.Hologram> {
        TODO("Not yet implemented")
    }

    override fun addLineAbove(text: String): Parent {
        return HologramAPI.createHologram(this.loc.add(0.0, 0.25, 0.0), text).apply {
            above = this
        }
    }

    override fun getLineAbove(): Parent? {
        return above
    }

    override fun removeLineAbove(): Boolean {
        above?.deSpawn()

        return true
    }

    override fun getLinesAbove(): MutableCollection<Parent> {
        TODO("Not yet implemented")
    }

    override fun getLines(): MutableCollection<Parent> {
        TODO("Not yet implemented")
    }

    override fun getAttachedTo(): Entity? {
        TODO("Not yet implemented")
    }

    override fun setAttachedTo(entity: Entity?) {
        entity ?: run {
            attachingEntity = -1
            return
        }

        attachingEntity = entity.entityId
        for (player in Bukkit.getOnlinePlayers()) {
            sendAttach(player)
        }
    }

    private fun sendAttach(player: Player) {
        val lineId = hologram.entity.entityId
        NMS.getInstance().attachFakeEntity(player, lineId, attachingEntity)
    }
}