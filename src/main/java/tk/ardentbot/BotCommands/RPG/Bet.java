package tk.ardentbot.BotCommands.RPG;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;
import tk.ardentbot.Utils.RPGUtils.RPGUtils;

public class Bet extends Command {
    public Bet(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "all") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                Profile profile = Profile.get(user);
                sendEditedTranslation("bet", language, "areyousure", user, channel, RPGUtils.formatMoney(profile.getMoneyAmount()));
                interactivate(language, channel, message, (returnedMessage) -> {
                    if (returnedMessage.getContent().equalsIgnoreCase("yes")) {

                    }
                });
            }
        });
    }
}
