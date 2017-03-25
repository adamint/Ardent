package tk.ardentbot.Main;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.wrapper.spotify.Api;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.BotCommands.Music.StuckVoiceConnection;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.WebServer.SparkServer;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Searching.GoogleSearch;
import tk.ardentbot.Utils.Updaters.BotlistUpdater;
import tk.ardentbot.Utils.Updaters.SpotifyTokenRefresh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Core.Translation.LangFactory.languages;
import static tk.ardentbot.Utils.Searching.GoogleSearch.GOOGLE_API_KEY;

public class Ardent {
    public static boolean testingBot = true;
    public static Api spotifyApi;
    /**
     * Sharded
     */
    public static ArrayList<String> tierOnepatrons = new ArrayList<>();
    public static ArrayList<String> tierTwopatrons = new ArrayList<>();
    public static ArrayList<String> tierThreepatrons = new ArrayList<>();
    public static ArrayList<String> developers = new ArrayList<>();
    public static ArrayList<String> moderators = new ArrayList<>();
    public static ArrayList<String> translators = new ArrayList<>();
    public static String botsDiscordPwToken;
    public static String discordBotsOrgToken;
    public static Connection conn;
    public static ScheduledExecutorService globalExecutorService = Executors.newScheduledThreadPool(100);
    public static Shard shard0;
    public static Shard botLogsShard;
    public static int shardCount = 2;

    public static ConcurrentHashMap<String, ChatterBotSession> cleverbots = new ConcurrentHashMap<>();

    public static String announcement;
    public static ConcurrentHashMap<String, Boolean> sentAnnouncement = new ConcurrentHashMap<>();

    public static String mashapeKey;
    public static String gameUrl = "https://ardentbot.tk";


    public static void main(String[] args) throws Exception {
        for (String s : args) if (s.equalsIgnoreCase("test")) testingBot = true;
        if (!testingBot) {
            spotifyApi = Api.builder()
                    .clientId("471f277107704e3b89d489284b65c6c6")
                    .clientSecret(IOUtils.toString(new FileInputStream(new File("/root/Ardent/spotifysecret.key"))))
                    .redirectURI("https://ardentbot.tk")
                    .build();
            SpotifyTokenRefresh spotifyTokenRefresh = new SpotifyTokenRefresh();
            spotifyTokenRefresh.run();
            globalExecutorService.scheduleAtFixedRate(spotifyTokenRefresh, 10, 10, TimeUnit.MINUTES);
        }
        else spotifyApi = Api.DEFAULT_API;

        if (!testingBot) {
            conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("/root/Ardent/v2url.key"))),
                    IOUtils.toString(new FileReader(new File("/root/Ardent/v2user.key"))), IOUtils.toString(new
                            FileReader(new File("/root/Ardent/v2password.key"))));

            botsDiscordPwToken = IOUtils.toString(new FileReader(new File("/root/Ardent/botsdiscordpw.key")));
            discordBotsOrgToken = IOUtils.toString(new FileReader(new File("/root/Ardent/discordbotsorg.key")));
        }
        else {
            conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop" +
                            "\\Ardent\\dburl.key"))),
                    IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbuser.key"))),
                    IOUtils.toString(new
                            FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbpassword.key"))));
        }

        DatabaseAction getKeys = new DatabaseAction("SELECT * FROM APIKeys");
        ResultSet keys = getKeys.request();
        while (keys.next()) {
            String id = keys.getString("Identifier");
            String value = keys.getString("Value");
            if (id.equalsIgnoreCase("mashape")) mashapeKey = value;
            else if (id.equalsIgnoreCase("google")) GOOGLE_API_KEY = value;
        }
        getKeys.close();

        ShardManager.register(shardCount);
        SparkServer.setup();

        BotlistUpdater updater = new BotlistUpdater();
        globalExecutorService.scheduleAtFixedRate(updater, 1, 1, TimeUnit.HOURS);

        StuckVoiceConnection playerStuckDaemon = new StuckVoiceConnection();
        globalExecutorService.scheduleAtFixedRate(playerStuckDaemon, 10, 15, TimeUnit.SECONDS);

        GoogleSearch.setup(GOOGLE_API_KEY);

        languages = new ArrayList<>();
        languages.add(LangFactory.english);
        languages.add(LangFactory.french);
        languages.add(LangFactory.turkish);
        languages.add(LangFactory.croatian);
        languages.add(LangFactory.romanian);
        languages.add(LangFactory.portugese);
        languages.add(LangFactory.german);
        languages.add(LangFactory.cyrillicserbian);
        languages.add(LangFactory.dutch);
        languages.add(LangFactory.emoji);
        languages.add(LangFactory.arabic);
        languages.add(LangFactory.hindi);
        languages.add(LangFactory.spanish);
        languages.add(LangFactory.polish);
    }
}
