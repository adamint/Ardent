package tk.ardentbot.Utils.Updaters;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Main.ShardManager;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.HashMap;


public class MuteDaemon implements Runnable {
    @Override
    public void run() {
        for (Shard shard : ShardManager.getShards()) {
            HashMap<String, HashMap<String, Long>> mutes = shard.botMuteData.getMutes();
            for (String guildID : mutes.keySet()) {

                Guild guild = shard.jda.getGuildById(guildID);

                for (String userID : mutes.get(guildID).keySet()) {

                    Member member = guild.getMember(shard.jda.getUserById(userID));

                    if (!shard.botMuteData.isMuted(member) && shard.botMuteData.wasMute(member)) {
                        shard.botMuteData.unmute(member);
                        member.getUser().openPrivateChannel().queue(privateChannel -> {
                            try {
                                privateChannel.sendMessage(shard.help.getTranslation("mute", GuildUtils.getLanguage
                                        (guild), "nowabletospeak").getTranslation().replace("{0}", guild.getName()))
                                        .queue();
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        });
                    }
                }
            }
        }
    }
}
