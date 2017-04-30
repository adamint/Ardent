package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.RestrictedUserModel;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.models.RestrictedUser;
import tk.ardentbot.utils.rpg.EntityGuild;

import java.util.ArrayList;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Restrict extends Command {
    public Restrict(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Prevent a user from using Ardent commands", "block @User", "block") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                Member member = guild.getMember(user);
                if (member.hasPermission(Permission.MANAGE_SERVER)) {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() > 0) {
                        for (User mentioned : mentionedUsers) {
                            Member mentionedMember = guild.getMember(mentioned);
                            if (mentionedMember.hasPermission(member.getPermissions()) || mentionedMember.hasPermission(Permission
                                    .MANAGE_SERVER) || mentionedMember.getUser().getId().equalsIgnoreCase(guild.getSelfMember().getUser()
                                    .getId()))
                            {
                                sendTranslatedMessage("You cannot modify this user because they have the same or higher permissions than " +
                                        "you.", channel, user);
                                return;
                            }
                            EntityGuild entityGuild = EntityGuild.get(guild);
                            boolean isRestricted = entityGuild.isRestricted(mentioned);
                            if (isRestricted) {
                                sendTranslatedMessage("This user has already been blocked from using commands!", channel, user);
                            }
                            else {
                                RestrictedUser restrictedUser = new RestrictedUser(mentioned.getId(), user.getId(), guild);
                                entityGuild.addRestricted(restrictedUser);
                                r.db("data").table("restricted").insert(r.json(gson.toJson(new RestrictedUserModel(guild.getId(),
                                        mentioned.getId(), user.getId())))).run(connection);
                                sendEditedTranslation("{1} has blocked {0} from sending commands", user, channel, mentioned.getName(), user
                                        .getName());
                            }
                        }
                    }
                    else sendTranslatedMessage("You need to mention one or more users", channel, user);
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("Allow a blocked user to run Ardent commands again", "unblock @User", "unblock") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() > 0) {
                        for (User mentioned : mentionedUsers) {
                            EntityGuild entityGuild = EntityGuild.get(guild);
                            boolean isRestricted = entityGuild.isRestricted(mentioned);
                            if (!isRestricted) {
                                sendEditedTranslation("{0} isn't restricted from sending commands!", user, channel, mentioned.getName());
                            }
                            else {
                                entityGuild.removeRestricted(mentioned.getId());
                                r.db("data").table("restricted").filter(row -> row.g("user_id").eq(mentioned.getId())
                                        .and(row.g("guild_id").eq(guild.getId()))).delete().run(connection);
                                sendEditedTranslation("{0} unrestricted {1}, allowing them to use commands again", user, channel,
                                        mentioned.getName(), user
                                                .getName());
                            }
                        }
                    }
                    else sendTranslatedMessage("You need to mention one or more users", channel, user);
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("View all the users who have been blocked in your server", "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                EntityGuild entityGuild = EntityGuild.get(guild);
                ArrayList<RestrictedUser> restrictedUsers = entityGuild.getRestrictedUsers();
                String title = "Restricted users";
                String restrictedBy = "restricted by";
                StringBuilder response = new StringBuilder();
                response.append("**" + title + "**");
                if (restrictedUsers.size() == 0) {
                    response.append("\nNo one");
                }
                else {
                    restrictedUsers.forEach(restrictedUser -> response.append("\n - " + guild.getMemberById(restrictedUser.getUserId())
                            .getUser().getName() + ", " + restrictedBy + " " + guild.getMemberById(restrictedUser.getRestrictedById())
                            .getUser().getName()));
                }
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                builder.setAuthor(title, getShard().url, guild.getSelfMember().getUser().getAvatarUrl());
                builder.setDescription(response.toString());
                sendEmbed(builder, channel, user);
            }
        });
    }
}
