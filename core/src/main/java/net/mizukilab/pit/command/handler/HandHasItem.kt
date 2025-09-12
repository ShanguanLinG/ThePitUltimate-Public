package net.mizukilab.pit.command.handler

import dev.rollczi.litecommands.argument.suggester.input.SuggestionInput
import dev.rollczi.litecommands.flow.Flow
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.meta.MetaHolder
import dev.rollczi.litecommands.meta.MetaKey
import dev.rollczi.litecommands.validator.Validator
import net.mizukilab.pit.util.Utils
import net.mizukilab.pit.util.isAir
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * 2024/5/15<br>
 * ThePitPlus<br>
 * @author huanmeng_qwq
 */
annotation class HandHasItem(val mythic: Boolean = false)

val metaKey = MetaKey.of("req_hand_has_item", HandHasItem::class.java)

class HandHasItemValidator : Validator<CommandSender> {
    override fun validate(inv: Invocation<CommandSender>, meta: MetaHolder): Flow {
        if (inv.arguments() is SuggestionInput) {
            return Flow.continueFlow()
        }
        val handHasItem = meta.meta().get(metaKey, meta.parentMeta()?.meta()?.get(metaKey, null))
            ?: return Flow.continueFlow()
        val player = inv.sender() as Player
        if (handHasItem.mythic && Utils.getMythicItem(player.itemInHand) == null) {
            return Flow.stopCurrentFlow("§c请手持神话物品!")
        }
        if (player.itemInHand.isAir()) {
            return Flow.stopCurrentFlow("§c请手持物品!")
        }
        return Flow.continueFlow()
    }
}