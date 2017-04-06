package tk.ardentbot.Core.BotData;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Rethink.Models.GuildModel;

import java.sql.SQLException;
import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.botLogsShard;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class BotLanguageData {
    private HashMap<String, String> guildLanguages = new HashMap<>();

    public BotLanguageData() throws SQLException {
        Cursor<GuildModel> guilds = r.db("data").table("guilds").run(connection);
        guilds.forEach(guildModel -> {
            guildLanguages.put(guildModel.getGuild_id(), guildModel.getLanguage());
        });
        guilds.close();
    }

    public void set(String guildId, String language) {
        guildLanguages.put(guildId, language);
    }
    public void set(Guild guild, String language) {
        guildLanguages.put(guild.getId(), language);
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
                botLogsShard.botLogs.sendMessage(guild.getId() + " - failed to retrieve their language, returned " +
                        "english").queue();
            }
            return LangFactory.english;
        }
    }

    public void remove(Guild guild) {
        guildLanguages.remove(guild.getId());
    }
}
