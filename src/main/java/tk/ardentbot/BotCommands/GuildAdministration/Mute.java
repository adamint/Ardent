package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

public class Mute extends Command {
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
        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Shard shard = GuildUtils.getShard(guild);
                Statement statement = Ardent.conn.createStatement();
                ResultSet set = statement.executeQuery("SELECT * FROM Mutes WHERE GuildID='" + guild.getId() + "'");
                String until = getTranslation("mute", language, "until").getTranslation();
                StringBuilder sb = new StringBuilder();
                int amt = 0;
                while (set.next()) {
                    amt++;
                    sb.append(" - " + shard.jda.getUserById(set.getString("UserID")).getAsMention() + " " + until +
                            " " +
                            new Date(set.getLong("UnmuteEpochSecond")) + "\n");
                }
                if (amt == 0) {
                    sb = new StringBuilder();
                    sb.append(getTranslation("mute", language, "nomutes").getTranslation());
                }
                sendTranslatedMessage(sb.toString(), channel, user);
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
                        Shard shard = GuildUtils.getShard(guild);
                        Member mentioned = message.getGuild().getMember(message.getMentionedUsers().get(0));

                        if (shard.botMuteData.isMuted(mentioned)) {
                            sendRetrievedTranslation(channel, "mute", language, "alreadymuted", user);
                        }
                        else {
                            if (shard.botMuteData.wasMute(mentioned)) {
                                shard.botMuteData.unmute(mentioned); // Do delete it from the list
                            }
                            String muteTime = args[3];
                            if (muteTime.endsWith("w") || muteTime.endsWith("h") || muteTime.endsWith("d") ||
                                    muteTime.endsWith("m"))
                            {
                                try {
                                    long now = System.currentTimeMillis() + StringUtils.commandeTime(muteTime);
                                    shard.botMuteData.mute(mentioned, now, guild.getMember(message.getAuthor()));
                                    String reply = getTranslation("mute", language, "nowmuteduntil").getTranslation()
                                            .replace("{0}", mentioned.getUser().getName())
                                            .replace("{1}", String.valueOf(new Date(now)));
                                    sendTranslatedMessage(reply, channel, user);
                                }
                                catch (NumberFormatException ex) {
                                    sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                                }
                            }
                            else {
                                sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                            }

                        }
                    }
                }
            }
        });
    }
}
