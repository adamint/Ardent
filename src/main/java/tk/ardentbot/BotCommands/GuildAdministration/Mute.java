package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.StringUtils;

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
                String until = getTranslation("mute", language, "until").getTranslation();
                StringBuilder sb = new StringBuilder();
                final int[] amt = {0};
                StringBuilder finalSb = sb;
                shard.botMuteData.getMutes().forEach((s, stringLongMap) -> {
                    amt[0]++;
                    finalSb.append(" - " + shard.jda.getUserById(s).getAsMention() + " " + until +
                            " " + new Date(stringLongMap.get(s)) + "\n");
                });
                if (amt[0] == 0) {
                    sb = new StringBuilder();
                    sb.append(getTranslation("mute", language, "nomutes").getTranslation());
                }
                sendTranslatedMessage(sb.toString(), channel, user);
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
                            String[] rawContent = message.getRawContent().split(" ");
                            if (rawContent.length <= 3) {
                                sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                                return;
                            }
                            String muteTime = rawContent[3];
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
