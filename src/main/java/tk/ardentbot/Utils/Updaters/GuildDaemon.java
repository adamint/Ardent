package tk.ardentbot.Utils.Updaters;

import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Main.ShardManager;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GuildDaemon implements Runnable {
    @Override
    public void run() {
        try {
            for (Shard shard : ShardManager.getShards()) {
                ArrayList<String> guildIds = getAllGuildIds();
                for (Guild guild : shard.jda.getGuilds()) {
                    if (!guildIds.contains(guild.getId())) {
                        new DatabaseAction("INSERT INTO Guilds VALUES (?,?,?)").set(guild.getId())
                                .set("english").set("/").update();
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
        DatabaseAction getGuild = new DatabaseAction("SELECT * FROM Guilds");
        ResultSet set = getGuild.request();
        while (set.next()) {
            ids.add(set.getString("GuildID"));
        }
        getGuild.close();
        return ids;
    }

}
