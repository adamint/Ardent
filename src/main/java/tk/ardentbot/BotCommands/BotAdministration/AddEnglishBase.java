package tk.ardentbot.BotCommands.BotAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.sql.Statement;

import static tk.ardentbot.Utils.SQL.SQLUtils.cleanString;

public class AddEnglishBase extends Command {
    public AddEnglishBase(CommandSettings settings) {
        super(settings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk.ardentbot
            .Core.Translation.Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() {
        subcommands.add(new Subcommand(this, "basic") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk
                    .ardentbot.Core.Translation.Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length >= 4) {
                        String commandID = args[2];
                        String id = args[3];
                        String lang = "english";
                        String translation = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " ", "");
                        Statement statement = Ardent.conn.createStatement();
                        statement.executeUpdate("INSERT INTO Translations VALUES ('" + commandID + "', '" + cleanString(translation) + "', '" +
                                id + "', '" + lang + "', '1')");
                        statement.close();
                        sendTranslatedMessage("Inserted new translation successfully.", channel, user);
                    }
                    else sendTranslatedMessage("/shrug Incorrect arguments", channel, user);
                }
                else sendTranslatedMessage("You need to be a developer to run this command", channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "commands") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk
                    .ardentbot.Core.Translation.Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length > 4) {
                        String commandID = args[2];
                        String translation = args[3];
                        String lang = "english";
                        String description = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " ", "");
                        Statement statement = Ardent.conn.createStatement();
                        statement.executeUpdate("INSERT INTO Commands VALUES ('" + commandID + "', '" + lang + "', '" +
                                cleanString(translation) + "', '" + cleanString(description) + "')");
                        statement.close();
                        sendTranslatedMessage("Inserted new command successfully.", channel, user);
                    }
                    else sendTranslatedMessage("/shrug Incorrect arguments", channel, user);
                }
                else sendTranslatedMessage("You need to be a developer to run this command", channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "subcommands") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk
                    .ardentbot.Core.Translation.Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length >= 6) {
                        String commandID = args[2];
                        String identifier = args[3];
                        String lang = "english";
                        String translation = args[4];
                        String needsDb = args[5];
                        String left = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5] + " ", "");
                        String[] syntaxDescription = left.split("//");
                        if (syntaxDescription.length == 2) {
                            Ardent.conn.prepareStatement("INSERT INTO Subcommands VALUES ('" + commandID + "', '" +
                                    identifier + "', '" +
                                    lang + "', '" + cleanString(translation) + "', '" + cleanString(syntaxDescription[0]) + "', '" +
                                    cleanString(syntaxDescription[1]) + "', '" + needsDb + "')").executeUpdate();
                            sendTranslatedMessage("Inserted new subcommand successfully.", channel, user);
                        }
                        else sendTranslatedMessage("You didn't have the correct syntax :thinking:", channel, user);
                    }
                    else sendTranslatedMessage("/shrug Incorrect arguments", channel, user);
                }
                else sendTranslatedMessage("You need to be a developer to run this command", channel, user);
            }
        });
    }
}
