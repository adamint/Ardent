package tk.ardentbot.Utils.Updaters;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Main.ShardManager;

public class BotlistUpdater implements Runnable {
    @Override
    public void run() {
        try {
            int guilds = 0;
            for (Shard shard : ShardManager.getShards()) {
                guilds += shard.jda.getGuilds().size();
            }
            Unirest.post("https://bots.discord.pw/api/bots/247093143160356865/stats").header("Authorization",
                    Ardent.botsDiscordPwToken).header("Content-Type", "application/json")
                    .body(new JSONObject().append("server_count", guilds))
                    .asString();
            Unirest.post("https://discordbots.org/api/bots/247093143160356865/stats").header("Authorization",
                    Ardent.discordBotsOrgToken).header("Content-Type", "application/json")
                    .body(new JSONObject().append("server_count", guilds)).asString();
        }
        catch (UnirestException e) {
        }
    }
}
