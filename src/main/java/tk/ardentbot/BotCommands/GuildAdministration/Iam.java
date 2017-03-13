package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Tuples.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.Main.Ardent.ardent;

public class Iam extends Command {
    public Iam(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("iam", "autoroleslist"));
                translations.add(new Translation("iam", "givesyou"));
                translations.add(new Translation("iam", "howtouse"));

                HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

                String title = responses.get(0).getTranslation();
                String givesYouRole = responses.get(1).getTranslation();

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Iam.this);
                builder.setAuthor(title, ardent.url, ardent.bot.getAvatarUrl());
                StringBuilder msg = new StringBuilder();
                msg.append("**" + title + "**");

                for (Pair<String, Role> autorole : getAutoRoles(guild)) {
                    String roleName;
                    Role role = autorole.getV();
                    if (role == null) roleName = "deleted-role";
                    else roleName = role.getName();

                    msg.append("\n**" + autorole.getK() + "** " + givesYouRole + " **" + roleName + "**");
                }
                msg.append("\n\n" + responses.get(2).getTranslation());
                builder.setDescription(msg.toString());
                sendEmbed(builder, channel, user);
            }
        });
        subcommands.add(new Subcommand(this, "role") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                            args[1] + " ", "");
                    boolean found = false;
                    ArrayList<Pair<String, Role>> autoroles = getAutoRoles(guild);
                    for (Pair<String, Role> rolePair : autoroles) {
                        if (rolePair.getK().equalsIgnoreCase(query)) {
                            found = true;
                            Role role = rolePair.getV();
                            if (role == null)
                                sendRetrievedTranslation(channel, "iam", language, "thereisnoroleforthis", user);
                            else {
                                boolean failure = false;
                                try {
                                    guild.getController().addRolesToMember(guild.getMember(user), role).queue();
                                }
                                catch (Exception ex) {
                                    failure = true;
                                    sendRetrievedTranslation(channel, "iam", language, "makesureicanaddroles", user);
                                }
                                if (!failure)
                                    sendTranslatedMessage(getTranslation("iam", language, "gaverole").getTranslation()
                                            .replace("{0}", role.getName()), channel, user);
                            }
                        }
                    }
                    if (!found) sendRetrievedTranslation(channel, "iam", language, "namenotfound", user);
                }
                else sendRetrievedTranslation(channel, "iam", language, "includeautorolename", user);
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (UserUtils.hasManageServerOrStaff(guild.getMember(user))) {
                    String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                            args[1] + " ", "");
                    boolean found = false;
                    ArrayList<Pair<String, Role>> autoroles = getAutoRoles(guild);
                    for (Pair<String, Role> rolePair : autoroles) {
                        if (rolePair.getK().equalsIgnoreCase(query)) {
                            found = true;
                            new DatabaseAction("DELETE FROM Autoroles WHERE Name=? AND GuildID=?").set(query).set
                                    (guild.getId()).update();
                            sendTranslatedMessage(getTranslation("iam", language, "deletedautorole").getTranslation()
                                    .replace("{0}", rolePair.getK()), channel, user);
                        }
                    }
                    if (!found) sendRetrievedTranslation(channel, "iam", language, "namenotfound", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length == 4) {
                    if (UserUtils.hasManageServerOrStaff(guild.getMember(user))) {
                        String name = args[2];
                        String role = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                args[1] + " " + args[2] + " ", "");
                        boolean found = false;
                        ArrayList<Pair<String, Role>> autoroles = getAutoRoles(guild);
                        for (Pair<String, Role> rolePair : autoroles) {
                            if (rolePair.getK().equalsIgnoreCase(name)) {
                                found = true;
                            }
                        }
                        if (found)
                            sendRetrievedTranslation(channel, "iam", language, "autorolewithnamealreadyfound", user);
                        else {
                            List<Role> roleList = guild.getRolesByName(role, true);
                            if (roleList.size() > 0) {
                                Role toAdd = roleList.get(0);
                                new DatabaseAction("INSERT INTO Autoroles VALUES (?,?,?)").set(guild.getId())
                                        .set(name).set(toAdd.getId()).update();
                                sendTranslatedMessage(getTranslation("iam", language, "addedautorole").getTranslation()
                                        .replace("{0}", name).replace("{1}", role), channel, user);
                            }
                            else sendRetrievedTranslation(channel, "iam", language, "namenotfound", user);
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
                }
                else sendRetrievedTranslation(channel, "iam", language, "includenameandrole", user);
            }
        });
    }

    private ArrayList<Pair<String, Role>> getAutoRoles(Guild guild) throws SQLException {
        ArrayList<Pair<String, Role>> autoRoles = new ArrayList<>();
        DatabaseAction getRoles = new DatabaseAction("SELECT * FROM Autoroles WHERE GuildID=?").set(guild.getId());
        ResultSet set = getRoles.request();
        while (set.next()) {
            autoRoles.add(new Pair<>(set.getString("Name"), guild.getRoleById(set.getString("RoleID"))));
        }
        getRoles.close();
        return autoRoles;
    }
}
