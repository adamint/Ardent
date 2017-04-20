package tk.ardentbot.commands.botAdministration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.translate.Language;
import tk.ardentbot.utils.discord.GuildUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.main.Ardent.botLogsShard;
import static tk.ardentbot.main.Ardent.globalExecutorService;

public class Request extends Command {
    private static ArrayList<RequestUtil> usersUnableToRequest = new ArrayList<>();

    public Request(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        String prefix = GuildUtils.getPrefix(guild);
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("request", language, "requesthelp").getTranslation().replace("{0}",
                    prefix + args[0]), channel, user);
        }
        else {
            if (canRequest(user)) {
                String request = message.getRawContent().replace(prefix + args[0] + " ", "");
                TextChannel ideasChannel = botLogsShard.jda.getTextChannelById("262810786186002432");
                ideasChannel.sendMessage("**Request** by " + user.getName() + "#" + user.getDiscriminator() + " (" +
                        user.getId() + "): " + request).queue();
                usersUnableToRequest.add(new RequestUtil(Instant.now(), user));
                sendRetrievedTranslation(channel, "request", language, "successfullyrequested", user);
            }
            else sendTranslatedMessage(getRequestTime(user, language), channel, user);
        }
    }

    @Override
    public void setupSubcommands() {
    }

    private boolean canRequest(User user) {
        for (RequestUtil r : usersUnableToRequest) {
            if (r.id.equalsIgnoreCase(user.getId())) return false;
        }
        return true;
    }

    private String getRequestTime(User user, Language language) throws Exception {
        for (RequestUtil r : usersUnableToRequest) {
            if (r.id.equalsIgnoreCase(user.getId())) {
                int minutes = (int) ((r.ableToRequest.getEpochSecond() - Instant.now().getEpochSecond()) / 60);
                return Request.this.getTranslation("request", language, "requestin").getTranslation().replace("{0}",
                        String.valueOf(minutes));
            }
        }
        return null;
    }

    private class RequestUtil {
        private Instant ableToRequest;
        private String id;

        RequestUtil(Instant requestedAt, User user) {
            this.ableToRequest = requestedAt.plusSeconds(60 * 5);
            this.id = user.getId();
            globalExecutorService.schedule(() -> usersUnableToRequest.remove(RequestUtil.this), ableToRequest.getEpochSecond() -
                    requestedAt.getEpochSecond(), TimeUnit.MILLISECONDS);
            usersUnableToRequest.add(this);
        }
    }
}
