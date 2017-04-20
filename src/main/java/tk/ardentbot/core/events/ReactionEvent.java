package tk.ardentbot.core.events;

import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;

public class ReactionEvent {
    public static ConcurrentHashMap<String, MessageReactionAddEvent> reactionEvents = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onReaction(MessageReactionAddEvent event) {
        if (reactionEvents.size() + 1 > 1000) {
            reactionEvents.clear();
        }
        reactionEvents.put(event.getChannel().getId(), event);
    }
}
