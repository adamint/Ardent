package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.Models.RestrictedUser;
import tk.ardentbot.Utils.RPGUtils.EntityGuild;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.util.ArrayList;
import java.util.HashMap;
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
                                sendRetrievedTranslation(channel, "roles", language, "cannotmodify", user);
                                return;
                            }
                            EntityGuild entityGuild = EntityGuild.get(guild);
                            boolean isRestricted = entityGuild.isRestricted(mentioned);
                            if (isRestricted) {
                                sendRetrievedTranslation(channel, "restrict", language, "alreadyrestricted", user);
                            }
                            else {
                                entityGuild.addRestricted(new RestrictedUser(mentioned.getId(), user.getId(), guild));
                                new DatabaseAction("INSERT INTO Restricted VALUES (?,?,?)").set(mentioned.getId()).set(user.getId())
                                        .set(guild.getId()).update();
                                sendEditedTranslation("restrict", language, "restricteduser", user, channel, mentioned.getName(), user
                                        .getName());
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
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() > 0) {
                        for (User mentioned : mentionedUsers) {
                            EntityGuild entityGuild = EntityGuild.get(guild);
                            boolean isRestricted = entityGuild.isRestricted(mentioned);
                            if (!isRestricted) {
                                sendEditedTranslation("restrict", language, "usernotrestricted", user, channel, mentioned.getName());
                            }
                            else {
                                entityGuild.removeRestricted(mentioned.getId());
                                new DatabaseAction("DELETE FROM Restricted WHERE UserID=? AND GuildID=?").set(mentioned.getId())
                                        .set(guild.getId()).update();
                                sendEditedTranslation("restrict", language, "unblockeduser", user, channel, mentioned.getName(), user
                                        .getName());
                            }
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "mentionuserorusers", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
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
                }
                else {
                    restrictedUsers.forEach(restrictedUser -> response.append("\n - " + guild.getMemberById(restrictedUser.getUserId())
                            .getUser().getName() + ", " + restrictedBy + " " + guild.getMemberById(restrictedUser.getRestrictedById())
                            .getUser().getName()));
                }
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Restrict.this);
                builder.setAuthor(title, getShard().url, guild.getSelfMember().getUser().getAvatarUrl());
                builder.setDescription(response.toString());
                sendEmbed(builder, channel, user);
            }
        });
    }
}
