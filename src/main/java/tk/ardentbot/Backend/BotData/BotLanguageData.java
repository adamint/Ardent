package tk.ardentbot.Backend.BotData;

import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.Backend.Translation.LangFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.ardent;

public class BotLanguageData {
    private HashMap<String, String> guildLanguages = new HashMap<>();

    public BotLanguageData() throws SQLException {
        DatabaseAction getLanguages = new DatabaseAction("SELECT * FROM Guilds");
        ResultSet languages = getLanguages.request();
        while (languages.next()) {
            guildLanguages.put(languages.getString("GuildID"), languages.getString("Language"));
        }
        getLanguages.close();
    }

    public void set(Guild guild, String language) {
        guildLanguages.putIfAbsent(guild.getId(), language);
    }

    public Language getLanguage(Guild guild) {
        try {
            return LangFactory.getLanguage(guildLanguages.get(guild.getId()));
        }
        catch (NullPointerException ex) {
            try {
                set(guild, "english");
            }
            catch (NullPointerException e) {
                ardent.botLogs.sendMessage(guild.getId() + " - failed to retrieve their language, returned english")
                        .queue();
            }
            return LangFactory.english;
        }
    }
}
