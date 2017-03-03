package tk.ardentbot.Utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.TranslationResponse;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Main.Ardent;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.conn;
import static tk.ardentbot.Main.Ardent.jda;
import static tk.ardentbot.Utils.SQLUtils.cleanString;

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
                            privateChannel.sendMessage(getTranslationForNonCommands("mute", GuildUtils.getLanguage(guild), "nowabletospeak").getTranslation().replace("{0}", guild.getName())).queue();
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    });
                }

            }
        }
    }

    public static TranslationResponse getTranslationForNonCommands(String cmdName, Language lang, String id) throws Exception {
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Translations WHERE CommandIdentifier='" + cleanString(cmdName) + "' AND Language='" + lang + "' AND ID='" + cleanString(id) + "'");
        if (set.next()) {
            String translation = set.getString("Translation").replace("{newline}", "\n");
            set.close();
            return new TranslationResponse(translation, lang, true, false, true);
        }
        else {
            ResultSet englishSet = statement.executeQuery("SELECT * FROM Translations WHERE CommandIdentifier='" + cleanString(cmdName) + "' AND Language='english' AND ID='" + cleanString(id) + "'");
            if (englishSet.next()) {
                String translation = englishSet.getString("Translation").replace("{newline}", "\n");
                englishSet.close();
                return new TranslationResponse(translation, lang, true, false, true);
            }
            return new TranslationResponse(null, lang, false, false, false);
        }
    }
}
