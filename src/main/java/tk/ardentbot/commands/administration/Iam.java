package tk.ardentbot.commands.administration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.core.translation.Translation;
import tk.ardentbot.core.translation.TranslationResponse;
import tk.ardentbot.main.Shard;
import tk.ardentbot.rethink.models.AutoroleModel;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.javaAdditions.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

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
                Shard shard = GuildUtils.getShard(guild);
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("iam", "autoroleslist"));
                translations.add(new Translation("iam", "givesyou"));
                translations.add(new Translation("iam", "howtouse"));

                HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

                String title = responses.get(0).getTranslation();
                String givesYouRole = responses.get(1).getTranslation();

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Iam.this);
                builder.setAuthor(title, shard.url, shard.bot.getAvatarUrl());
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
                            r.db("data").table("autoroles").filter(row -> row.g("name").eq(query).and(row.g("guild_id").eq(guild.getId())
                            )).delete().run(connection);
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
                if (UserUtils.hasManageServerOrStaff(guild.getMember(user))) {
                    sendRetrievedTranslation(channel, "iam", language, "typenameofiam", user);
                    interactiveOperation(language, channel, message, nameMessage -> {
                        String name = nameMessage.getContent();
                        sendRetrievedTranslation(channel, "iam", language, "typerolenamenow", user);
                        interactiveOperation(language, channel, message, roleMessage -> {
                            try {
                                String role = roleMessage.getRawContent();
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
                                        r.table("autoroles").insert(r.json(gson.toJson(new AutoroleModel(guild.getId(),
                                                name, toAdd.getId())))).run(connection);
                                        sendTranslatedMessage(getTranslation("iam", language, "addedautorole").getTranslation()
                                                .replace("{0}", name).replace("{1}", role), channel, user);
                                    }
                                    else sendRetrievedTranslation(channel, "iam", language, "namenotfound", user);
                                }
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        });
                    });
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });
    }

    private ArrayList<Pair<String, Role>> getAutoRoles(Guild guild) {
        ArrayList<Pair<String, Role>> autoRoles = new ArrayList<>();
        Cursor<HashMap> cursor = r.table("autoroles").filter(r.hashMap("guild_id", guild.getId())).run(connection);
        cursor.forEach(hashMap -> {
            AutoroleModel autoroleModel = asPojo(hashMap, AutoroleModel.class);
            autoRoles.add(new Pair<>(autoroleModel.getName(), guild.getRoleById(autoroleModel.getRole_id())));
        });
        return autoRoles;
    }
}
