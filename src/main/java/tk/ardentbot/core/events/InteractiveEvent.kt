package tk.ardentbot.core.events

import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import tk.ardentbot.utils.javaAdditions.Triplet
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

class InteractiveEvent {
    /**
     * CHM<Text channel Id, Triplet<User id, Message id, Function to call with the consumable object type>>
     */
    val messageInteractivesQueue = CopyOnWriteArrayList<Pair<String, Triplet<String, String, Consumer<Message>>>>()
    val reactionInteractivesQueue = CopyOnWriteArrayList<Pair<String, Triplet<String, String, Consumer<MessageReaction>>>>()

    @SubscribeEvent
    fun miq(e: GuildMessageReceivedEvent) {
        val iterator = messageInteractivesQueue.iterator()
        while (iterator.hasNext()) {
            val interactive = iterator.next()
            if (interactive.first == e.channel.id) {
                val userMessageFunction: Triplet<String, String, Consumer<Message>> = interactive.second
                if (userMessageFunction.a == e.author.id) {
                    try {
                        e.channel.getMessageById(userMessageFunction.b).queue {
                            m ->
                            if (e.message.creationTime.isAfter(m.creationTime)) {
                                userMessageFunction.c.accept(e.message)
                                messageInteractivesQueue.remove(interactive)
                            }
                        }
                    } catch (ignored: Exception) {
                        messageInteractivesQueue.remove(interactive)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onReaction(e: GuildMessageReactionAddEvent) {
        val iterator = reactionInteractivesQueue.iterator()
        while (iterator.hasNext()) {
            val interactive = iterator.next()
            if (interactive.first == e.channel.id) {
                val userMessageFunction: Triplet<String, String, Consumer<MessageReaction>> = interactive.second
                if (userMessageFunction.a == e.user.id) {
                    try {
                        if (e.messageId == userMessageFunction.b) {
                            userMessageFunction.c.accept(e.reaction)
                            reactionInteractivesQueue.remove(interactive)
                        }
                    } catch (ignored: Exception) {
                        reactionInteractivesQueue.remove(interactive)
                    }
                }
            }
        }
    }
}