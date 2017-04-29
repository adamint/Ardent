package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.translate.Translation;
import tk.ardentbot.core.translate.TranslationResponse;
import tk.ardentbot.rethink.models.RestrictedUserModel;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.models.RestrictedUser;
import tk.ardentbot.utils.rpg.EntityGuild;

import java.util.ArrayList;
import java.util.HashMap;
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
        subcommands.add(new Subcommand(this, "block") {
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
                                    .getId())) {
                                sendTranslatedMessage("You cannot modify this user because they have the same or higher permissions than you.", channel, user);
                                return;
                            }
                            EntityGuild entityGuild = EntityGuild.get(guild);
                            boolean isRestricted = entityGuild.isRestricted(mentioned);
                            if (isRestricted) {
                                sendTranslatedMessage("This user has already been blocked from using commands!", channel, user);
                            } else {
                                RestrictedUser restrictedUser = new RestrictedUser(mentioned.getId(), user.getId(), guild);
                                entityGuild.addRestricted(restrictedUser);
                                r.db("data").table("restricted").insert(r.json(gson.toJson(new RestrictedUserModel(guild.getId(),
                                        mentioned.getId(), user.getId())))).run(connection);
                                sendEditedTranslation("restrict", language, "restricteduser", user, channel, mentioned.getName(), user
                                        .getName());
                            }
                        }
                    } else sendRetrievedTranslation(channel, "other", language, "mentionuserorusers", user);
                } else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "unblock") {
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
                                sendEditedTranslation("restrict", language, "usernotrestricted", user, channel, mentioned.getName());
                            } else {
                                entityGuild.removeRestricted(mentioned.getId());
                                r.db("data").table("restricted").filter(row -> row.g("user_id").eq(mentioned.getId())
                                        .and(row.g("guild_id").eq(guild.getId()))).delete().run(connection);
                                sendEditedTranslation("restrict", language, "unblockeduser", user, channel, mentioned.getName(), user
                                        .getName());
                            }
                        }
                    } else sendRetrievedTranslation(channel, "other", language, "mentionuserorusers", user);
                } else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                EntityGuild entityGuild = EntityGuild.get(guild);
                ArrayList<RestrictedUser> restrictedUsers = entityGuild.getRestrictedUsers();
                HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("restrict",
                        "restrictedusers"), new Translation("restrict", "restrictedby"), new Translation("other", "none"));
                String title = translations.get(0).getTranslation();
                String restrictedBy = translations.get(1).getTranslation();
                StringBuilder response = new StringBuilder();
                response.append("**" + title + "**");
                if (restrictedUsers.size() == 0) {
                    response.append("\n" + translations.get(2).getTranslation());
                } else {
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
