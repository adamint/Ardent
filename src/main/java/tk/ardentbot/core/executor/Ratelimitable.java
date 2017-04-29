package tk.ardentbot.core.executor;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.translate.Language;

import java.time.Instant;
import java.util.HashMap;

public abstract class Ratelimitable extends Command {
    private int time;
    private HashMap<String, Long> ratelimited = new HashMap<>();

    public Ratelimitable(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public Ratelimitable with(int time) {
        this.time = time;
        return this;
    }

    @Override
    void onUsage(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language,
                 Language oldLang)
            throws Exception {
        long now = Instant.now().getEpochSecond();
        if (ratelimited.get(user.getId()) != null) {
            if (ratelimited.get(user.getId()) > now) {
                sendEditedTranslation("other", language, "holdonthere", user, channel, user.getAsMention(), String.valueOf(
                        ratelimited.get(user.getId()) - now));
                return;
            }
            else ratelimited.remove(user.getId());
        }
        ratelimited.put(user.getId(), now + time);
        super.onUsage(guild, channel, user, message, args, language, oldLang);
    }
}
