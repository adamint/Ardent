package tk.ardentbot.commands.guildinfo;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.ServerInfoModel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class ServerInfo extends Command {
    public ServerInfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Set server info for new useres", "set", "set") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                String serverInfo = getServerInfo(guild);
                if (serverInfo == null) {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_CHANNEL)) {
                        if (args.length > 2) {
                            String parsed = replace(message.getContent(), 2);
                            setInfo(guild, parsed);
                            sendTranslatedMessage("Successfully set the server info message! Invoke it by doing /server info", channel,
                                    user);
                        } else {
                            sendTranslatedMessage("You need to include some text in the info!", channel, user);
                        }
                    } else sendTranslatedMessage("You need the Manage Server permission to use this!", channel, user);
                } else {
                    sendTranslatedMessage("Server info has already been added! Remove it before you can change it.", channel, user);
                }
            }
        });

        subcommands.add(new Subcommand("Remove the set server info", "remove", "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                String serverInfo = getServerInfo(guild);
                if (serverInfo != null) {
                    if (guild.getMember(user).hasPermission(Permission.MANAGE_CHANNEL)) {
                        setInfo(guild, null);
                        sendTranslatedMessage("Successfully removed server info.", channel, user);
                    } else sendTranslatedMessage("You need the Manage Server permission to use this!", channel, user);
                } else {
                    sendTranslatedMessage("Your server doesn't have a setup info message.", channel, user);

                }
            }
        });

        subcommands.add(new Subcommand("View the server info settings", "view", "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                String serverInfo = getServerInfo(guild);
                if (serverInfo == null) {
                    sendTranslatedMessage("Your server doesn't have a setup info message.", channel, user);
                } else {
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
        } else {
            r.db("data").table("serverinfo").insert(r.json(gson.toJson(new ServerInfoModel(guild.getId(), "none")))).run(connection);
        }
        return toReturn;
    }

}
