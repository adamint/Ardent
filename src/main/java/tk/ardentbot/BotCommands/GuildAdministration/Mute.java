package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.CommandExecution.SubCmd;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import static tk.ardentbot.Main.Ardent.ardent;

public class Mute extends Cmd {
    public Mute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subCmds.add(new SubCmd(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Statement statement = ardent.conn.createStatement();
                ResultSet set = statement.executeQuery("SELECT * FROM Mutes WHERE GuildID='" + guild.getId() + "'");
                String until = getTranslation("mute", language, "until").getTranslation();
                StringBuilder sb = new StringBuilder();
                int amt = 0;
                while (set.next()) {
                    amt++;
                    sb.append(" - " + ardent.jda.getUserById(set.getString("UserID")).getAsMention() + " " + until +
                            " " +
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
        subCmds.add(new SubCmd(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Member author = guild.getMember(message.getAuthor());
                if (author.hasPermission(Permission.ADMINISTRATOR)) {
                    if (message.getMentionedUsers().size() > 0) {

                        Member mentioned = message.getGuild().getMember(message.getMentionedUsers().get(0));

                        if (ardent.botMuteData.isMuted(mentioned)) {
                            sendRetrievedTranslation(channel, "mute", language, "alreadymuted");
                        }
                        else {
                            if (ardent.botMuteData.wasMute(mentioned)) {
                                ardent.botMuteData.unmute(mentioned); // Do delete it from the list
                            }
                            String muteTime = args[3];
                            if (muteTime.endsWith("w") || muteTime.endsWith("h") || muteTime.endsWith("d") ||
                                    muteTime.endsWith("m"))
                            {
                                try {
                                    long now = System.currentTimeMillis() + StringUtils.commandeTime(muteTime);
                                    ardent.botMuteData.mute(mentioned, now, guild.getMember(message.getAuthor()));
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
