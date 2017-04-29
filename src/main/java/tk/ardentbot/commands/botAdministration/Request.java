package tk.ardentbot.commands.botAdministration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Ratelimitable;
import tk.ardentbot.utils.discord.GuildUtils;

import static tk.ardentbot.main.Ardent.botLogsShard;

public class Request extends Ratelimitable {
    public Request(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        String prefix = GuildUtils.getPrefix(guild);
        if (args.length == 1) {
            sendTranslatedMessage("Request a feature by typing /request and then your request", channel, user);
        }
        else {
            String request = message.getRawContent().replace(prefix + args[0] + " ", "");
            TextChannel ideasChannel = botLogsShard.jda.getTextChannelById("262810786186002432");
            ideasChannel.sendMessage("**Request** by " + user.getName() + "#" + user.getDiscriminator() + " (" +
                    user.getId() + "): " + request).queue();
            sendTranslatedMessage("Sent your request to the developers!", channel, user);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
