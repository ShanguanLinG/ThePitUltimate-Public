package net.mizukilab.pit.handler

import cn.charlotte.pit.ThePit
import org.bukkit.Location

object SewersHandler : Runnable {
    var lastClosedSewers = -1L
    var nowChest: Location? = null

    fun init() {

    }

    override fun run() {
        val locations = ThePit.getInstance()
            .pitConfig
            .sewersChestsLocations


    }

}