package tk.ardentbot.commands.administration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;
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
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("See a list of setup Iams", "view", "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                Shard shard = GuildUtils.getShard(guild);
                String title = "Auto Roles | /iam";
                String givesYouRole = "will give you the role";

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
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
                msg.append("\n\nAdd an autorole by doing /iam role and going through the setup wizard");
                builder.setDescription(msg.toString());
                sendEmbed(builder, channel, user);
            }
        });
        subcommands.add(new Subcommand("Give yourself an autorole", "role", "role") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
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
                                sendTranslatedMessage("Hmm.. there's no role for this", channel, user);
                            else {
                                boolean failure = false;
                                try {
                                    guild.getController().addRolesToMember(guild.getMember(user), role).queue();
                                }
                                catch (Exception ex) {
                                    failure = true;
                                    sendTranslatedMessage("Please make sure I can add roles", channel, user);
                                }
                                if (!failure)
                                    sendTranslatedMessage("Successfully gave you the role {0}".replace("{0}", role.getName()), channel,
                                            user);
                            }
                        }
                    }
                    if (!found) sendTranslatedMessage("An autorole with that name wasn't found", channel, user);
                }
                else sendTranslatedMessage("You need to include an autorole name", channel, user);
            }
        });

        subcommands.add(new Subcommand("Remove autoroles", "remove", "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
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
                            sendTranslatedMessage("Deleted the autorole **{0}**".replace("{0}", rolePair.getK()), channel, user);
                        }
                    }
                    if (!found) sendTranslatedMessage("An autorole with that name wasn't found", channel, user);
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("Add an autorole", "add", "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                if (UserUtils.hasManageServerOrStaff(guild.getMember(user))) {
                    sendTranslatedMessage("Type the name of the autorole you want to add", channel, user);
                    interactiveOperation(channel, message, nameMessage -> {
                        String name = nameMessage.getContent();
                        sendTranslatedMessage("Type the name of the **discord role** you want this set to", channel, user);
                        interactiveOperation(channel, message, roleMessage -> {
                            try {
                                String role = roleMessage.getRawContent();
                                boolean found = false;
                                ArrayList<Pair<String, Role>> autoroles = getAutoRoles(guild);
                                for (Pair<String, Role> rolePair : autoroles) {
                                    if (rolePair.getK().equalsIgnoreCase(name)) {
                                        found = true;
                                    }
                                }
                                if (found) sendTranslatedMessage("An autorole with that name is already setup", channel, user);
                                else {
                                    List<Role> roleList = guild.getRolesByName(role, true);
                                    if (roleList.size() > 0) {
                                        Role toAdd = roleList.get(0);
                                        r.table("autoroles").insert(r.json(gson.toJson(new AutoroleModel(guild.getId(),
                                                name, toAdd.getId())))).run(connection);
                                        sendTranslatedMessage("Successfully added autorole **{0}** which gives the role **{1}**".replace
                                                ("{0}", name).replace("{1}", role), channel, user);
                                    }
                                    else sendTranslatedMessage("An role with that name wasn't found", channel, user);
                                }
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        });
                    });
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
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
