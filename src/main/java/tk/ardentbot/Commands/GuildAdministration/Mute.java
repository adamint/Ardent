package tk.ardentbot.Commands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import static tk.ardentbot.Main.Ardent.conn;
import static tk.ardentbot.Main.Ardent.jda;

public class Mute extends BotCommand {
    public Mute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Statement statement = conn.createStatement();
                ResultSet set = statement.executeQuery("SELECT * FROM Mutes WHERE GuildID='" + guild.getId() + "'");
                String until = getTranslation("mute", language, "until").getTranslation();
                StringBuilder sb = new StringBuilder();
                int amt = 0;
                while (set.next()) {
                    amt++;
                    sb.append(" - " + jda.getUserById(set.getString("UserID")).getAsMention() + " " + until + " " +
                            new Date(set.getLong("UnmuteEpochSecond")) + "\n");
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
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Member author = guild.getMember(message.getAuthor());
                if (author.hasPermission(Permission.ADMINISTRATOR)) {
                    if (message.getMentionedUsers().size() > 0) {

                        Member mentioned = message.getGuild().getMember(message.getMentionedUsers().get(0));

                        if (Ardent.botMuteData.isMuted(mentioned)) {
                            sendRetrievedTranslation(channel, "mute", language, "alreadymuted");
                        }
                        else {
                            if (Ardent.botMuteData.wasMute(mentioned)) {
                                Ardent.botMuteData.unmute(mentioned); // Do delete it from the list
                            }
                            String muteTime = args[3];
                            if (muteTime.endsWith("w") || muteTime.endsWith("h") || muteTime.endsWith("d") ||
                                    muteTime.endsWith("m"))
                            {
                                try {
                                    long now = StringUtils.commandeTime(muteTime);
                                    Ardent.botMuteData.mute(mentioned, now, guild.getMember(message.getAuthor()));
                                    String reply = getTranslation("mute", language, "nowmuteduntil").getTranslation()
                                            .replace("{0}", mentioned.getUser().getName())
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
                    }
                }
            }
        });
    }
}
