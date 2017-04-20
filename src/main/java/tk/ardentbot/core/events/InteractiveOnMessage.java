package tk.ardentbot.core.events;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.ConcurrentHashMap;

public class InteractiveOnMessage {
    /**
     * First param is the text channel id, second is the user id
     */
    public static ConcurrentHashMap<String, String> queuedInteractives = new ConcurrentHashMap<>();

    /**
     * Pair params are Pair(Channel ID, User ID) and then its corresponding message
     */
    public static ConcurrentHashMap<Message, TextChannel> lastMessages = new ConcurrentHashMap<>();

    public static void onMessage(MessageReceivedEvent event) {
        if (lastMessages.size() + 1 > 1000) {
            lastMessages.clear();
        }
        lastMessages.put(event.getMessage(), (TextChannel) event.getChannel());
    }
}
