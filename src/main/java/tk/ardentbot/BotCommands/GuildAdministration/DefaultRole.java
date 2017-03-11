package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.CommandExecution.SubCmd;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DefaultRole extends Cmd {
    public DefaultRole(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static Role getDefaultRole(Guild guild) throws SQLException {
        Role returned = null;
        DatabaseAction retrieve = new DatabaseAction("SELECT * FROM DefaultRole WHERE GuildID=?").set(guild.getId());
        ResultSet set = retrieve.request();
        if (set.next()) {
            String roleID = set.getString("RoleID");
            if (!roleID.equalsIgnoreCase("none")) {
                returned = guild.getRoleById(roleID);
            }
        }
        retrieve.close();
        return returned;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subCmds.add(new SubCmd(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendRetrievedTranslation(channel, "defaultrole", language, "nodefaultrole");
                }
                else {
                    String reply = getTranslation("defaultrole", language, "currentdefaultrole").getTranslation()
                            .replace("{0}", role.getName());
                    sendTranslatedMessage(reply, channel);
                }
            }
        });

        subCmds.add(new SubCmd(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendRetrievedTranslation(channel, "defaultrole", language, "nodefaultrole");
                }
                else {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                        removeDefaultRole(guild);
                        sendRetrievedTranslation(channel, "defaultrole", language, "removeddefaultrole");
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
                }
            }
        });

        subCmds.add(new SubCmd(this, "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    String roleName = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " "
                            + args[1] + " ", "");
                    List<Role> roles = guild.getRolesByName(roleName, true);
                    if (roles.size() == 0) {
                        sendRetrievedTranslation(channel, "defaultrole", language, "needtotyperole");
                    }
                    else {
                        Role role = roles.get(0);
                        setDefaultRole(role, guild);
                        String reply = getTranslation("defaultrole", language, "setdefaultrole").getTranslation()
                                .replace("{0}", role.getName());
                        sendTranslatedMessage(reply, channel);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });
    }

    private void removeDefaultRole(Guild guild) throws SQLException {
        new DatabaseAction("UPDATE DefaultRole SET RoleID=? WHERE GuildID=?").set("none").set(guild.getId()).update();
    }

    private void setDefaultRole(Role role, Guild guild) throws SQLException {
        DatabaseAction retrieve = new DatabaseAction("SELECT * FROM DefaultRole WHERE GuildID=?").set(guild.getId());
        ResultSet isIn = retrieve.request();
        if (isIn.next()) {
            new DatabaseAction("UPDATE DefaultRole SET RoleID=? WHERE GuildID=?").set(role.getId()).set(guild.getId()
            ).update();
        }
        else {
            new DatabaseAction("INSERT INTO DefaultRole VALUES (?,?)").set(guild.getId()).set(role.getId()).update();
        }
        retrieve.close();
    }
}
