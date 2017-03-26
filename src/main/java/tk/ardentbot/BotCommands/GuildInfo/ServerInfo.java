package tk.ardentbot.BotCommands.GuildInfo;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerInfo extends Command {
    public ServerInfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                String serverInfo = getServerInfo(guild);
                if (serverInfo == null) {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_CHANNEL)) {
                        if (args.length > 2) {
                            String parsed = replace(message.getContent(), 2);
                            setInfo(guild, parsed);
                            sendRetrievedTranslation(channel, "info", language, "successfullyset", user);
                        }
                        else {
                            sendRetrievedTranslation(channel, "other", language, "includesometext", user);
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
                }
                else {
                    sendRetrievedTranslation(channel, "info", language, "alreadysetup", user);
                }
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                String serverInfo = getServerInfo(guild);
                if (serverInfo != null) {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_CHANNEL)) {
                        setInfo(guild, null);
                        sendRetrievedTranslation(channel, "info", language, "successfullyremoved", user);
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
                }
                else {
                    sendRetrievedTranslation(channel, "info", language, "nonesetup", user);
                }
            }
        });

        subcommands.add(new Subcommand(this, "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                String serverInfo = getServerInfo(guild);
                if (serverInfo == null) {
                    sendRetrievedTranslation(channel, "info", language, "nonesetup", user);
                }
                else {
                    sendTranslatedMessage("**" + guild.getName() + "**:\n" + serverInfo, channel, user);
                }
            }
        });
    }

    private void setInfo(Guild guild, String parsed) throws SQLException {
        if (getServerInfo(guild) == null) {
            new DatabaseAction("UPDATE ServerInfo SET Message=? WHERE GuildID=?").set(parsed).set(guild.getId()).update();
        }
        else {
            new DatabaseAction("UPDATE ServerInfo SET Message=? WHERE GuildID=?").set("none").set(guild.getId()).update();
        }
    }

    private String getServerInfo(Guild guild) throws SQLException {
        String toReturn = null;
        DatabaseAction getMessage = new DatabaseAction("SELECT * FROM ServerInfo WHERE GuildID=?").set(guild.getId());
        ResultSet set = getMessage.request();
        if (set.next()) {
            String info = set.getString("Message");
            if (!info.equalsIgnoreCase("none")) toReturn = info;
        }
        else {
            new DatabaseAction("INSERT INTO ServerInfo VALUES (?,?)").set(guild.getId()).set("none").update();
        }
        getMessage.close();
        return toReturn;
    }

}
