package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.ArdentLang.ReturnWrapper;
import tk.ardentbot.Utils.Models.RestrictedUser;
import tk.ardentbot.Utils.Premium.EntityGuild;

import java.util.List;

public class Restrict extends Command {
    public Restrict(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "block") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() > 0) {
                        for (User mentioned : mentionedUsers) {
                            ReturnWrapper restrictedUserReturnWrapper = isRestricted(mentioned, guild);
                            if (restrictedUserReturnWrapper.getFailureReason() != null) {

                            }
                            else {
                                RestrictedUser restrictedUser = (RestrictedUser) restrictedUserReturnWrapper.getReturnValue();

                            }
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "mentionuserorusers", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });
        subcommands.add(new Subcommand(this, "unblock") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {

            }
        });
    }

    private ReturnWrapper isRestricted(User user, Guild guild) {
        return EntityGuild.getGuildPatronStatus(guild).isRestricted(user);
    }
}
