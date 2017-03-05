package tk.ardentbot.Utils.SQL;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.ardent;


public class MuteDaemon implements Runnable {
    @Override
    public void run() {
        HashMap<String, HashMap<String, Long>> mutes = ardent.botMuteData.getMutes();
        for(String guildID : mutes.keySet()){

            Guild guild = ardent.jda.getGuildById(guildID);

            for(String userID : mutes.get(guildID).keySet()){

                Member member = guild.getMember(ardent.jda.getUserById(userID));

                if (!ardent.botMuteData.isMuted(member) && ardent.botMuteData.wasMute(member)) {
                    ardent.botMuteData.unmute(member);
                    member.getUser().openPrivateChannel().queue(privateChannel -> {
                        try {
                            privateChannel.sendMessage(ardent.help.getTranslation("mute", GuildUtils.getLanguage
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
