package tk.ardentbot.utils.updaters;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.main.Shard;
import tk.ardentbot.main.ShardManager;
import tk.ardentbot.rethink.models.GuildModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.core.commandExecutor.BaseCommand.asPojo;
import static tk.ardentbot.main.Ardent.globalGson;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class GuildDaemon implements Runnable {
    @Override
    public void run() {
        try {
            for (Shard shard : ShardManager.getShards()) {
                ArrayList<String> guildIds = getAllGuildIds();
                for (Guild guild : shard.jda.getGuilds()) {
                    if (!guildIds.contains(guild.getId())) {
                        r.db("data").table("guilds").insert(r.json(globalGson.toJson(new GuildModel(guild.getId(), "english", "/"))));
                        shard.botLanguageData.set(guild, "english");
                    }
                }
            }
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }

    public ArrayList<String> getAllGuildIds() throws SQLException {
        ArrayList<String> ids = new ArrayList<>();
        Cursor<HashMap> guilds = r.db("data").table("guilds").run(connection);
        guilds.forEach(g -> ids.add(asPojo(g, GuildModel.class).getGuild_id()));
        guilds.close();
        return ids;
    }

}
