package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.utils.discord.GuildUtils;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Prefix extends Command {
    public Prefix(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() {
        subcommands.add(new Subcommand("View the prefix of your server!", "view", "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                sendTranslatedMessage("The prefix for this server is **{0}**.".replace
                        ("{0}", GuildUtils.getPrefix(guild)), channel, user);
            }
        });
        subcommands.add(new Subcommand("Change the prefix of your server.", "change", "change") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
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
                            sendTranslatedMessage("Successfully updated the prefix,  {0}!".replace("{0}", newPrefix), channel, user);
                        } else
                            sendTranslatedMessage("Your supplied prefix contained invalid characters!", channel, user);
                    } else {
                        sendTranslatedMessage("You need ```Manage Server``` permissions.", channel, user);
                    }
                } else {
                    sendTranslatedMessage("You must include a prefix as your third argument!", channel, user);
                }
            }
        });
    }
}
