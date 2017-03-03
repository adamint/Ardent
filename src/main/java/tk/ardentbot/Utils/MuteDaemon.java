package tk.ardentbot.Utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Main.Ardent;

import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.help;
import static tk.ardentbot.Main.Ardent.jda;

public class MuteDaemon implements Runnable {
    @Override
    public void run() {
        HashMap<String, HashMap<String, Long>> mutes = Ardent.botMuteData.getMutes();
        for(String guildID : mutes.keySet()){

            Guild guild = jda.getGuildById(guildID);

            for(String userID : mutes.get(guildID).keySet()){

                Member member = guild.getMember(jda.getUserById(userID));

                if(!Ardent.botMuteData.isMuted(member) && Ardent.botMuteData.wasMute(member)){
                    Ardent.botMuteData.unmute(member);
                    member.getUser().openPrivateChannel().queue(privateChannel -> {
                        try {
                            privateChannel.sendMessage(help.getTranslation("mute", GuildUtils.getLanguage(guild), "nowabletospeak").getTranslation().replace("{0}", guild.getName())).queue();
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
