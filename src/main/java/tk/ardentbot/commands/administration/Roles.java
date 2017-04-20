package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.translate.Language;

import java.util.List;

public class Roles extends Command {
    public Roles(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
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
                                                String reply = getTranslation("roles", language, "addedrole").getTranslation().replace
                                                        ("{0}", r.getName()).replace("{1}", mentioned.getName());
                                                sendTranslatedMessage(reply, channel, user);
                                            }
                                            catch (Exception e) {
                                                new BotException(e);
                                            }
                                        });
                                    }
                                    catch (PermissionException ex) {
                                        sendRetrievedTranslation(channel, "other", language, "needproperpermissions",
                                                user);
                                    }
                                }
                                else sendRetrievedTranslation(channel, "roles", language, "norolesfound", user);
                            }
                            else sendRetrievedTranslation(channel, "roles", language, "cannotmodify", user);
                        }
                        else sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageroles", user);
                }
                else sendRetrievedTranslation(channel, "roles", language, "mentionuserandrole", user);
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
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
                                                String reply = getTranslation("roles", language, "removedrole").getTranslation().replace
                                                        ("{0}", r.getName()).replace("{1}", mentioned.getName());
                                                sendTranslatedMessage(reply, channel, user);
                                            }
                                            catch (Exception e) {
                                                new BotException(e);
                                            }
                                        });
                                    }
                                    catch (PermissionException ex) {
                                        sendRetrievedTranslation(channel, "other", language, "needproperpermissions", user);
                                    }
                                }
                                else sendRetrievedTranslation(channel, "roles", language, "norolesfound", user);
                            }
                            else sendRetrievedTranslation(channel, "roles", language, "cannotmodify", user);
                        }
                        else sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageroles", user);
                }
                else sendRetrievedTranslation(channel, "roles", language, "mentionuserandrole", user);
            }
        });
    }
}
