package real.nanoneko

import real.nanoneko.register.IMagicLicense

/**
 * @author Araykal
 * @since 2025/1/31
 */
@Deprecated("No use")
object PerkConstructor {
    private val perks: MutableList<Class<*>> = mutableListOf()

    fun getPerks(): List<Class<*>> {
        return perks
    }

    fun addPerk(enchantment: Class<*>) {

        println("This class is deprecated, please use EnchantmentFactor.init(?..), this feature will be removed in future!!!")
        if (IMagicLicense::class.java.isAssignableFrom(enchantment)) {
            perks.add(enchantment)
        } else {
            throw IllegalArgumentException("Only classes implementing IMagicLicense can be added as perk.")
        }
    }

    fun removePerk(enchantment: Class<*>) {
        perks.remove(enchantment)
    }
}