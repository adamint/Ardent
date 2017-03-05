package tk.ardentbot.Commands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static tk.ardentbot.Main.Config.conn;

public class DefaultRole extends BotCommand {
    public DefaultRole(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static Role getDefaultRole(Guild guild) throws SQLException {
        Role returned;
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM DefaultRole WHERE GuildID='" + guild.getId() + "'");
        if (set.next()) {
            String id = set.getString("RoleID");
            if (id.equalsIgnoreCase("none")) {
                returned = null;
            }
            else {
                returned = guild.getRoleById(id);
            }
            set.close();
            statement.close();
            return returned;
        }
        else {
            set.close();
            statement.close();
            return null;
        }
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                Role role = getDefaultRole(guild);
                if (role == null) {
                    sendRetrievedTranslation(channel, "defaultrole", language, "nodefaultrole");
                }
                else {
                    String reply = getTranslation("defaultrole", language, "currentdefaultrole").getTranslation().replace("{0}", role.getName());
                    sendTranslatedMessage(reply, channel);
                }
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
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

        subcommands.add(new Subcommand(this, "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    String roleName = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " ", "");
                    List<Role> roles = guild.getRolesByName(roleName, true);
                    if (roles.size() == 0) {
                        sendRetrievedTranslation(channel, "defaultrole", language, "needtotyperole");
                    }
                    else {
                        Role role = roles.get(0);
                        setDefaultRole(role, guild);
                        String reply = getTranslation("defaultrole", language, "setdefaultrole").getTranslation().replace("{0}", role.getName());
                        sendTranslatedMessage(reply, channel);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });
    }

    public void removeDefaultRole(Guild guild) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate("UPDATE DefaultRole SET RoleID='none' WHERE GuildID='" + guild.getId() + "'");
        statement.close();
    }

    public void setDefaultRole(Role role, Guild guild) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet isIn = statement.executeQuery("SELECT * FROM DefaultRole WHERE GuildID='" + guild.getId() + "'");
        if (isIn.next()) {
            statement.executeUpdate("UPDATE DefaultRole SET RoleID='" + role.getId() + "' WHERE GuildID='" + guild.getId() + "'");
        }
        else {
            statement.executeUpdate("INSERT INTO DefaultRole VALUES ('" + guild.getId() + "', '" + role.getId() + "')");
        }
        isIn.close();
        statement.close();
    }
}
