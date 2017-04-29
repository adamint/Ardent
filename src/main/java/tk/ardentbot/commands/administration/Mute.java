package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.StringUtils;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.Date;

public class Mute extends Command {
    public Mute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
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
        subcommands.add(new Subcommand(this, "person") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                Member author = guild.getMember(message.getAuthor());
                if (author.hasPermission(Permission.MANAGE_SERVER)) {
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
                            sendRetrievedTranslation(channel, "mute", language, "typeforhowlong", user);
                            interactiveOperation(language, channel, message, howLongMessage -> {
                                String howLong = howLongMessage.getRawContent();
                                if (howLong.endsWith("w") || howLong.endsWith("h") || howLong.endsWith("d") ||
                                        howLong.endsWith("m"))
                                {
                                    try {
                                        long muteUntil = System.currentTimeMillis() + StringUtils.commandeTime(howLong);
                                        shard.botMuteData.mute(mentioned, muteUntil, guild.getMember(message.getAuthor()));
                                        String reply = getTranslation("mute", language, "nowmuteduntil").getTranslation()
                                                .replace("{0}", mentioned.getUser().getName())
                                                .replace("{1}", String.valueOf(new Date(muteUntil)));
                                        sendTranslatedMessage(reply, channel, user);
                                    }
                                    catch (NumberFormatException ex) {
                                        sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                                    }
                                    catch (Exception e) {
                                        new BotException(e);
                                    }
                                }
                                else {
                                    sendRetrievedTranslation(channel, "mute", language, "invalidtimeperiod", user);
                                }
                            });
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });
    }
}
