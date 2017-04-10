package tk.ardentbot.BotCommands.GuildInfo;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Rethink.Models.ServerInfoModel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.Main.Ardent.globalGson;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

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
        String toPut = getServerInfo(guild) == null ? parsed : "none";
        r.db("data").table("serverinfo").filter(row -> row.g("guild_id").eq(guild.getId())).update(r.hashMap("message", toPut))
                .run(connection);
    }

    private String getServerInfo(Guild guild) throws SQLException {
        String toReturn = null;
        List<HashMap> serverinfo = ((Cursor<HashMap>) r.db("data").table("serverinfo").filter(row -> row.g("guild_id")
                .eq(guild.getId())).run(connection)).toList();
        if (serverinfo.size() > 0) {
            String info = asPojo(serverinfo.get(0), ServerInfoModel.class).getMessage();
            if (!info.equalsIgnoreCase("none")) toReturn = info;
        }
        else {
            r.db("data").table("serverinfo").insert(r.json(globalGson.toJson(new ServerInfoModel(guild.getId(), "none")))).run(connection);
        }
        return toReturn;
    }

}
