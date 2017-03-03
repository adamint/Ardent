package Utils;

import Backend.Translation.Language;
import Backend.Translation.TranslationResponse;
import Bot.BotException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static Main.Ardent.conn;
import static Main.Ardent.jda;
import static Utils.SQLUtils.cleanString;

public class MuteDaemon implements Runnable {
    @Override
    public void run() {
        try {
            if (!conn.isClosed()) {
                Statement statement = conn.createStatement();
                ResultSet mutes = statement.executeQuery("SELECT * FROM Mutes");
                while (mutes.next()) {
                    if (System.currentTimeMillis() > mutes.getLong("UnmuteEpochSecond")) {
                        String guildID = mutes.getString("GuildID");
                        String userID = mutes.getString("UserID");
                        statement.executeUpdate("DELETE FROM Mutes WHERE GuildID='" + guildID + "' AND " +
                                "UserID='" + userID + "'");
                        Guild guild = jda.getGuildById(guildID);
                        User user = jda.getUserById(userID);
                        user.openPrivateChannel().queue(privateChannel -> {
                            try {
                                privateChannel.sendMessage(getTranslationForNonCommands("mute", GuildUtils.getLanguage(guild), "nowabletospeak").getTranslation().replace("{0}", guild.getName())).queue();
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        });
                    }
                }
                mutes.close();
                statement.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
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
