package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.utils.discord.GuildUtils;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Prefix extends Command {
    public Prefix(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() {
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                sendTranslatedMessage(getTranslation("prefix", language, "viewprefix").getTranslation().replace
                        ("{0}", GuildUtils.getPrefix(guild)), channel, user);
            }
        });
        subcommands.add(new Subcommand(this, "change") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                        String newPrefix = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                "" + args[1] + " ", "").replace(" ", "");
                        if (newPrefix.length() == message.getRawContent().length()) {
                            newPrefix = message.getRawContent().replace("/" + args[0] + " " + args[1] + " ", "");
                        }
                        if (!newPrefix.contains(" ") && !newPrefix.contains("$")) {
                            GuildUtils.updatePrefix(newPrefix, guild);
                            r.db("data").table("guilds").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap("prefix",
                                    newPrefix)).run(connection);
                            sendTranslatedMessage(getTranslation("prefix", language, "successfullyupdated")
                                    .getTranslation().replace("{0}", newPrefix), channel, user);
                        }
                        else sendRetrievedTranslation(channel, "prefix", language, "invalidcharacters", user);
                    }
                    else {
                        sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
                    }
                }
                else {
                    sendRetrievedTranslation(channel, "prefix", language, "mustincludeargument", user);
                }
            }
        });
    }
}
