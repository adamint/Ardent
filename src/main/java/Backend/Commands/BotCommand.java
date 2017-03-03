package Backend.Commands;

import Backend.Models.SubcommandTranslation;
import Backend.Translation.Language;
import Bot.BotException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public abstract class BotCommand extends Command {
    public int usages = 0;
    public ArrayList<Subcommand> subcommands = new ArrayList<>();


    /**
     * Instantiates a new BotCommand
     *
     * @param commandSettings settings to instantiate the command with
     */
    public BotCommand(CommandSettings commandSettings) {
        this.commandIdentifier = commandSettings.getCommandIdentifier();
        this.privateChannelUsage = commandSettings.isPrivateChannelUsage();
        this.guildUsage = commandSettings.isGuildUsage();
        this.category = commandSettings.getCategory();
        this.botCommand = this;
    }


    /**
     * Called when a user runs a command with only one argument
     *
     * @param guild The guild of the sent command
     * @param channel Channel of the sent command
     * @param user Command author
     * @param message Command message
     * @param args Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception
     */
    public abstract void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception;

    public abstract void setupSubcommands() throws Exception;

    public void sendHelp(Language language, MessageChannel channel) throws Exception {
        sendTranslatedMessage(getHelp(language), channel);
    }

    protected String getHelp(Language language) throws Exception {
        StringBuilder help = new StringBuilder();
        String name = getName(language);
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        help.append("**" + name + "**\n");
        help.append(getDescription(language) + "\n\n");

        if (subcommands.size() > 0) {
            help.append("**" + getTranslation("other", language, "subcommands").getTranslation() + "**\n");
            for (Subcommand subcommand : subcommands) {
                help.append("- " + subcommand.getSyntax(language) + ": " + subcommand.getDescription(language) + "\n");
            }
        }
        return help.toString();
    }

    /**
     * Called when the CommandFactory identifies the queried
     * command. Will either call a subcommand from the list
     * or BotCommand#noArgs
     *
     * @param guild The guild of the sent command
     * @param channel Channel of the sent command
     * @param user Command author
     * @param message Command message
     * @param args Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception
     */
    void onUsage(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1 || subcommands.size() == 0) noArgs(guild, channel, user, message, args, language);
        else {
            List<SubcommandTranslation> subcommandTranslations = language.getSubcommands(this);
            final boolean[] found = {false};
            subcommandTranslations.forEach(subcommandTranslation -> {
                if (subcommandTranslation.getTranslation().equalsIgnoreCase(args[1])) {
                    found[0] = true;
                    subcommands.forEach(subcommand -> {
                        if (subcommand.getIdentifier().equalsIgnoreCase(subcommandTranslation.getIdentifier())) {
                            try {
                                subcommand.onCall(guild, channel, user, message, args, language);
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        }
                    });
                }
            });
            if (!found[0]) {
                sendRetrievedTranslation(channel, "other", language, "subcommandnotfound");
            }
        }
    }
}
