package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;

import java.util.List;

public class Roles extends Command {
    public Roles(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Adds the specified role to a user", "add @User [Role name]", "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (args.length > 2) {
                    Member userMember = guild.getMember(user);
                    if (userMember.hasPermission(Permission.MANAGE_SERVER)) {
                        List<User> mentionedUsers = message.getMentionedUsers();
                        if (mentionedUsers.size() > 0) {
                            User mentioned = mentionedUsers.get(0);
                            Member mentionedMember = guild.getMember(mentioned);
                            if (!mentionedMember.hasPermission(userMember.getPermissions())) {
                                String[] split = message.getRawContent().split(" ");
                                StringBuilder role = new StringBuilder();
                                for (int i = 3; i < split.length; i++) {
                                    role.append(split[i]);
                                    if (i < (split.length - 1)) role.append(" ");
                                }
                                List<Role> roles = guild.getRolesByName(role.toString(), true);
                                if (roles.size() > 0) {
                                    try {
                                        Role r = roles.get(0);
                                        guild.getController().addRolesToMember(mentionedMember, r).queue(aVoid -> {
                                            try {
                                                String reply = "Added **{0}** to {1}".replace
                                                        ("{0}", r.getName()).replace("{1}", mentioned.getAsMention());
                                                sendTranslatedMessage(reply, channel, user);
                                            }
                                            catch (Exception e) {
                                                new BotException(e);
                                            }
                                        });
                                    }
                                    catch (PermissionException ex) {
                                        sendTranslatedMessage("I don't have permission to do this", channel, user);
                                    }
                                }
                                else sendTranslatedMessage("No roles with that name were found", channel, user);
                            }
                            else sendTranslatedMessage("You cannot modify this user!", channel, user);
                        }
                        else sendTranslatedMessage("You need to mention a user", channel, user);
                    }
                    else sendTranslatedMessage("You need the Manage Roles permission", channel, user);
                }
                else sendTranslatedMessage("Please mention a user and type a role name", channel, user);
            }
        });

        subcommands.add(new Subcommand("Removes the specified role from a user", "remove @user [Role name]", "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (args.length > 2) {
                    Member userMember = guild.getMember(user);
                    if (userMember.hasPermission(Permission.MANAGE_SERVER)) {
                        List<User> mentionedUsers = message.getMentionedUsers();
                        if (mentionedUsers.size() > 0) {
                            User mentioned = mentionedUsers.get(0);
                            Member mentionedMember = guild.getMember(mentioned);
                            if (!mentionedMember.hasPermission(userMember.getPermissions())) {
                                String[] split = message.getRawContent().split(" ");
                                StringBuilder role = new StringBuilder();
                                for (int i = 3; i < split.length; i++) {
                                    role.append(split[i]);
                                    if (i < (split.length - 1)) role.append(" ");
                                }
                                List<Role> roles = guild.getRolesByName(role.toString(), true);
                                if (roles.size() > 0) {
                                    try {
                                        Role r = roles.get(0);
                                        guild.getController().removeRolesFromMember(mentionedMember, r).queue(aVoid -> {
                                            try {
                                                String reply = "Successfully removed role {0} from {1}".replace
                                                        ("{0}", r.getName()).replace("{1}", mentioned.getAsMention());
                                                sendTranslatedMessage(reply, channel, user);
                                            }
                                            catch (Exception e) {
                                                new BotException(e);
                                            }
                                        });
                                    }
                                    catch (PermissionException ex) {
                                        sendTranslatedMessage("I don't have permission to do this", channel, user);
                                    }
                                }
                                else sendTranslatedMessage("No roles with that name were found", channel, user);
                            }
                            else sendTranslatedMessage("You cannot modify this user!", channel, user);
                        }
                        else sendTranslatedMessage("You need to mention a user", channel, user);
                    }
                    else sendTranslatedMessage("You need the Manage Roles permission", channel, user);
                }
                else sendTranslatedMessage("Please mention a user and type a role name", channel, user);
            }
        });
    }
}
