package tk.ardentbot.Core.BotData;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.Rethink.Models.GuildModel;

import java.sql.SQLException;
import java.util.HashMap;

import static tk.ardentbot.Core.CommandExecution.BaseCommand.asPojo;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class BotPrefixData {
    private HashMap<String, String> guildPrefixes = new HashMap<>();

    public BotPrefixData() throws SQLException {
        Cursor<HashMap> guilds = r.db("data").table("guilds").run(connection);
        guilds.forEach(hashMap -> {
            GuildModel guildModel = asPojo(hashMap, GuildModel.class);
            guildPrefixes.put(guildModel.getGuild_id(), guildModel.getPrefix());
        });
        guilds.close();
    }

    public String getPrefix(Guild guild) {
        return guildPrefixes.get(guild.getId());
    }

    public void set(String guildId, String language) {
        guildPrefixes.putIfAbsent(guildId, language);
    }

    public void set(Guild guild, String prefix) {
        guildPrefixes.put(guild.getId(), prefix);
    }

    public void remove(Guild guild) {
        guildPrefixes.remove(guild.getId());
    }
}
