package tk.ardentbot.BotCommands.BotAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import twitter4j.Status;

import static tk.ardentbot.Main.Ardent.ardent;

public class Tweet extends Command {
    public Tweet(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (Ardent.developers.contains(user.getId())) {
            if (args.length == 1) {
                sendTranslatedMessage("Bro, you gotta include an actual tweet with that", channel);
            }
            else {
                String content = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0], "");
                boolean mentionEveryone = false;
                if (content.startsWith("{{mentionall}}")) {
                    mentionEveryone = true;
                    content = content.replace("{{mentionall}}", "");
                }
                Status status = ardent.twitter.tweets().updateStatus(content);
                StringBuilder sb = new StringBuilder();
                sb.append("**New Tweet** by Ardent\n" + content + "\n\nIf you liked this, follow us on twitter & like the post - https://twitter.com/ardentbot/status/" + status.getId());
                if (mentionEveryone) sb.append("\n@everyone");
                ardent.jda.getTextChannelById("272411413031419904").sendMessage(sb.toString()).queue();
            }
        }
        else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission");
    }

    @Override
    public void setupSubcommands() {
    }
}
