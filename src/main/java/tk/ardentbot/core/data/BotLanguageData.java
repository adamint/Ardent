package tk.ardentbot.core.data;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.core.translate.LangFactory;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.rethink.models.GuildModel;

import java.sql.SQLException;
import java.util.HashMap;

import static tk.ardentbot.core.executor.BaseCommand.asPojo;
import static tk.ardentbot.main.Ardent.botLogsShard;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class BotLanguageData {
    private HashMap<String, String> guildLanguages = new HashMap<>();

    public BotLanguageData() throws SQLException {
        Cursor<HashMap> guilds = r.db("data").table("guilds").run(connection);
        guilds.forEach(hashMap -> {
            GuildModel guildModel = asPojo(hashMap, GuildModel.class);
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
