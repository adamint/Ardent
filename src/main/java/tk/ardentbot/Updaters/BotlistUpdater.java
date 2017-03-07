package tk.ardentbot.Updaters;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import static tk.ardentbot.Main.Ardent.ardent;

public class BotlistUpdater implements Runnable {
    @Override
    public void run() {
        try {
            int guilds = ardent.jda.getGuilds().size();
            Unirest.post("https://bots.discord.pw/api/bots/247093143160356865/stats").header("Authorization",
                    ardent.botsDiscordPwToken).header("Content-Type", "application/json")
                    .body(new JSONObject().append("server_count", guilds))
                    .asString();
            Unirest.post("https://discordbots.org/api/bots/247093143160356865/stats").header("Authorization",
                    ardent.discordBotsOrgToken).header("Content-Type", "application/json")
                    .body(new JSONObject().append("server_count", guilds)).asString();
        }
        catch (UnirestException e) {
        }
    }
}
