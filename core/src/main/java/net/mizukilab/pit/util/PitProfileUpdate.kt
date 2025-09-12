package net.mizukilab.pit.util

import cn.charlotte.pit.data.PlayerProfile
import nya.Skip

@Suppress("DEPRECATION")
@Skip
object PitProfileUpdater {

    @JvmStatic
    fun updateVersion0(profile: PlayerProfile) {
        for (perkData in profile.boughtPerk) {
            profile.boughtPerkMap[perkData.perkInternalName] = perkData
        }
        profile.boughtPerk.clear()

        for (perkData in profile.unlockedPerk) {
            profile.unlockedPerkMap[perkData.perkInternalName] = perkData
        }

        profile.unlockedPerk.clear()

        profile.profileFormatVersion = 1
    }

}