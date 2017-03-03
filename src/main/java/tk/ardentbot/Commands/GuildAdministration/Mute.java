package tk.ardentbot.Commands.GuildAdministration;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import static tk.ardentbot.Main.Ardent.conn;
import static tk.ardentbot.Main.Ardent.jda;

public class Mute extends BotCommand {
    public Mute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                Statement statement = conn.createStatement();
                ResultSet set = statement.executeQuery("SELECT * FROM Mutes WHERE GuildID='" + guild.getId() + "'");
                String until = getTranslation("mute", language, "until").getTranslation();
                StringBuilder sb = new StringBuilder();
                int amt = 0;
                while (set.next()) {
                    amt++;
                    sb.append(" - " + jda.getUserById(set.getString("UserID")).getAsMention() + " " + until + " " + new Date(set.getLong("UnmuteEpochSecond")) + "\n");
                }
                if (amt == 0) {
                    sb = new StringBuilder();
                    sb.append(getTranslation("mute", language, "nomutes").getTranslation());
                }
                sendTranslatedMessage(sb.toString(), channel);
                set.close();
                statement.close();
            }
        });
        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                Member author = guild.getMember(message.getAuthor());
                if (author.hasPermission(Permission.ADMINISTRATOR)) {
                    if (message.getMentionedUsers().size() > 0) {
                        User mentioned = message.getMentionedUsers().get(0);
                        try (Statement statement = conn.createStatement()) {
                            ResultSet set = statement.executeQuery("SELECT * FROM Mutes WHERE GuildID='" + guild.getId() + "' AND UserID='" + mentioned.getId() + "'");
                            if (set.next()) {
                                if ((System.currentTimeMillis()) > set.getLong("UnmuteEpochSecond")) {
                                    statement.executeUpdate("DELETE FROM Mutes WHERE GuildID='" + guild.getId() + "' AND UserID='" + mentioned.getId() + "'");
                                }
                                else {
                                    sendRetrievedTranslation(channel, "mute", language, "alreadymuted");
                                    set.close();
                                    statement.close();
                                    return;
                                }
                            }
                            set.close();
                            String muteTime = args[3];
                            if (muteTime.endsWith("w") || muteTime.endsWith("h") || muteTime.endsWith("d") || muteTime.endsWith("m")) {
                                try {
                                    int time = Integer.parseInt(muteTime.replace("w", "").replace("d", "").replace("h", "").replace("m", ""));
                                    long now = 0;
                                    if (muteTime.endsWith("m")) {
                                        now = (System.currentTimeMillis() + ((long) time * 1000 * 60));
                                    }
                                    else if (muteTime.endsWith("h")) {
                                        now = (System.currentTimeMillis() + ((long) time * 1000 * 60 * 60));
                                    }
                                    else if (muteTime.endsWith("d")) {
                                        now = (System.currentTimeMillis() + ((long) time * 1000 * 60 * 60 * 24));
                                    }
                                    else if (muteTime.endsWith("w")) {
                                        now = (System.currentTimeMillis() + ((long) time * 1000 * 60 * 60 * 24 * 7));
                                    }
                                    statement.executeUpdate("INSERT INTO Mutes VALUES ('" + guild.getId() + "','" + message.getAuthor().getId() + "', '" + now + "', '" + message.getMentionedUsers().get(0).getId() + "')");
                                    String reply = getTranslation("mute", language, "nowmuteduntil").getTranslation().replace("{0}", mentioned.getName())
                                            .replace("{1}", String.valueOf(new Date(now)));
                                    sendTranslatedMessage(reply, channel);
                                }
                                catch (NumberFormatException ex) {
                                    sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                                }
                            }
                            else {
                                sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                            }
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
