package tk.ardentbot.commands.botAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.rethink.models.CommandModel;
import tk.ardentbot.rethink.models.SubcommandModel;
import tk.ardentbot.rethink.models.TranslationModel;
import tk.ardentbot.utils.discord.GuildUtils;

import static tk.ardentbot.main.Ardent.globalGson;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class AddEnglishBase extends Command {
    public AddEnglishBase(CommandSettings settings) {
        super(settings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk.ardentbot
            .core.translation.Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() {
        subcommands.add(new Subcommand(this, "basic") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, tk
                    .ardentbot.core.translation.Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length >= 4) {
                        String commandID = args[2];
                        String id = args[3];
                        String lang = "english";
                        String translation = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " "
                                + args[2] + " " + args[3] + " ", "");
                        r.db("data").table("translations").insert(r.json(globalGson.toJson(new TranslationModel(commandID, translation,
                                id, lang, true)))).run
                                (connection);
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
                    .ardentbot.core.translation.Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length > 4) {
                        String commandID = args[2];
                        String translation = args[3];
                        String lang = "english";
                        String description = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " "
                                + args[2] + " " + args[3] + " ", "");
                        r.db("data").table("commands").insert(r.json(globalGson.toJson(new CommandModel(commandID, lang, translation,
                                description)))).run(connection);
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
                    .ardentbot.core.translation.Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length >= 6) {
                        String commandID = args[2];
                        String identifier = args[3];
                        String lang = "english";
                        String translation = args[4];
                        String needsDb = args[5];
                        String left = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " " +
                                args[2] + " " + args[3] + " " + args[4] + " " + args[5] + " ", "");
                        String[] syntaxDescription = left.split("//");
                        if (syntaxDescription.length == 2) {
                            r.db("data").table("subcommands").insert(r.json(globalGson.toJson(new SubcommandModel(commandID,
                                    syntaxDescription[1], identifier,
                                    lang, Boolean.parseBoolean(needsDb), syntaxDescription[0], translation)))).run(connection);
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
