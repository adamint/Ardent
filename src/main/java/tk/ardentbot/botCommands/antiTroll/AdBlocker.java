package tk.ardentbot.botCommands.antiTroll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.commandExecutor.Subcommand;
import tk.ardentbot.core.translation.Language;

public class AdBlocker extends Command {
    public AdBlocker(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "allowdiscordlinks") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                try {
                    boolean allow = Boolean.parseBoolean(args[2]);
                }
                catch (Exception ex) {
                    sendRetrievedTranslation(channel, "other", language, "needspecifytrueorfalse", user);
                }
            }
        });
    }
}
