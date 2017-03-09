package tk.ardentbot.Commands.GuildAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;

public class Iam extends BotCommand {
    public Iam(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    String param = args[2];

                    if (param.equalsIgnoreCase("vc")) {

                    }
                    else if (param.equalsIgnoreCase("tc")) {

                    }
                    else {
                        sendRetrievedTranslation(channel, "iam", language, "mustspecifyvcortc");
                        return;
                    }

                }
                else sendRetrievedTranslation(channel, "iam", language, "mustspecifyvcortc");
            }
        });
    }
}
