package tk.ardentbot.Updaters;

import net.dv8tion.jda.core.entities.Guild;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static tk.ardentbot.Main.Ardent.ardent;

public class GuildDaemon implements Runnable {
    @Override
    public void run() {
        try {
            ArrayList<String> guildIds = getAllGuildIds();
            for (Guild guild : ardent.jda.getGuilds()) {
                if (!guildIds.contains(guild.getId())) {
                    new DatabaseAction("INSERT INTO Guilds VALUES (?,?,?)").set(guild.getId())
                            .set("english").set("/").update();
                    ardent.botLanguageData.set(guild, "english");
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
