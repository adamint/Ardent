package tk.ardentbot.BotCommands.GuildAdministration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Rethink.Models.DefaultRoleModel;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.Main.Ardent.globalGson;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class DefaultRole extends Command {
    public DefaultRole(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static Role getDefaultRole(Guild guild) throws SQLException {
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
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendRetrievedTranslation(channel, "defaultrole", language, "nodefaultrole", user);
                }
                else {
                    String reply = getTranslation("defaultrole", language, "currentdefaultrole").getTranslation()
                            .replace("{0}", role.getName());
                    sendTranslatedMessage(reply, channel, user);
                }
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendRetrievedTranslation(channel, "defaultrole", language, "nodefaultrole", user);
                }
                else {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                        removeDefaultRole(guild);
                        sendRetrievedTranslation(channel, "defaultrole", language, "removeddefaultrole", user);
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
                }
            }
        });

        subcommands.add(new Subcommand(this, "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                    String roleName = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " "
                            + args[1] + " ", "");
                    List<Role> roles = guild.getRolesByName(roleName, true);
                    if (roles.size() == 0) {
                        sendRetrievedTranslation(channel, "defaultrole", language, "needtotyperole", user);
                    }
                    else {
                        Role role = roles.get(0);
                        setDefaultRole(role, guild);
                        String reply = getTranslation("defaultrole", language, "setdefaultrole").getTranslation()
                                .replace("{0}", role.getName());
                        sendTranslatedMessage(reply, channel, user);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });
    }

    private void removeDefaultRole(Guild guild) throws SQLException {
        r.db("data").table("defaultroles").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap("channel_id", "none")).run
                (connection);
    }

    private void setDefaultRole(Role role, Guild guild) throws SQLException {
        List<HashMap> defaultRoleModels = ((Cursor<HashMap>) r.db("data").table("defaultroles").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (defaultRoleModels.size() > 0) {
            r.db("data").table("defaultroles").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap("role_id", role
                    .getId())).run(connection);
        }
        else {
            r.db("data").table("defaultroles").insert(r.json(globalGson.toJson(new DefaultRoleModel(guild.getId(), role.getId())))).run
                    (connection);
        }
    }
}
