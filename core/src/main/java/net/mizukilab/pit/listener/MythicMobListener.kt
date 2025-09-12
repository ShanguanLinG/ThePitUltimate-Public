package net.mizukilab.pit.listener

import net.mizukilab.pit.config.NewConfiguration
import cn.charlotte.pit.data.PlayerProfile
import net.mizukilab.pit.getPitProfile
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import nya.Skip
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
@Skip
object MythicMobListener : Listener {

    @EventHandler
    fun onMMDead(event: MythicMobDeathEvent) {
        val mob = event.mob
        val beKilled = mob.entity.bukkitEntity

        if (beKilled !is LivingEntity) return

        NewConfiguration.mythicMobs[mob.type.internalName] ?: return

        val killer = event.killer
        if (killer !is Player) return

        CombatListener.INSTANCE.handleKill(
            killer,
            killer.getPitProfile(),
            beKilled,
            PlayerProfile.NONE_PROFILE
        )
    }

}