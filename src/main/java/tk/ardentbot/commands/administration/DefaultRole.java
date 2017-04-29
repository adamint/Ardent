package tk.ardentbot.commands.administration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.DefaultRoleModel;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class DefaultRole extends Command {
    public DefaultRole(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static Role getDefaultRole(Guild guild) {
        Role returned = null;
        List<HashMap> defaultRoleModels = ((Cursor<HashMap>) r.db("data").table("defaultroles").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (defaultRoleModels.size() > 0) {
            DefaultRoleModel defaultRoleModel = asPojo(defaultRoleModels.get(0), DefaultRoleModel.class);
            String roleID = defaultRoleModel.getRole_id();
            if (!roleID.equalsIgnoreCase("none")) {
                returned = guild.getRoleById(roleID);
            }
        }
        return returned;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("View the current default role in this server", "view", "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendTranslatedMessage("There's no default role set up in this server!", channel, user);
                }
                else {
                    String reply = "The set default role is {0}".replace("{0}", role.getName());
                    sendTranslatedMessage(reply, channel, user);
                }
            }
        });

        subcommands.add(new Subcommand("Remove the default role", "remove", "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) {
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendTranslatedMessage("There's no default role set up in this server!", channel, user);
                }
                else {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                        removeDefaultRole(guild);
                        sendTranslatedMessage("Removed the set default role!", channel, user);
                    }
                    else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
                }
            }
        });

        subcommands.add(new Subcommand("Set the default role", "set", "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                    String roleName = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " "
                            + args[1] + " ", "");
                    List<Role> roles = guild.getRolesByName(roleName, true);
                    if (roles.size() == 0) {
                        sendTranslatedMessage("You need to type a role name!", channel, user);
                    }
                    else {
                        Role role = roles.get(0);
                        setDefaultRole(role, guild);
                        sendTranslatedMessage("Successfully set the default role as " + role.getName() + "!", channel, user);
                    }
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });
    }

    private void removeDefaultRole(Guild guild) {
        r.db("data").table("defaultroles").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap("channel_id", "none")).run
                (connection);
    }

    private void setDefaultRole(Role role, Guild guild) {
        List<HashMap> defaultRoleModels = ((Cursor<HashMap>) r.db("data").table("defaultroles").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (defaultRoleModels.size() > 0) {
            r.db("data").table("defaultroles").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap("role_id", role
                    .getId())).run(connection);
        }
        else {
            r.db("data").table("defaultroles").insert(r.json(gson.toJson(new DefaultRoleModel(guild.getId(), role.getId())))).run
                    (connection);
        }
    }
}
