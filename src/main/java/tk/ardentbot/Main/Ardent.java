package tk.ardentbot.Main;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.mashape.unirest.http.Unirest;
import com.wrapper.spotify.Api;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.BotCommands.Music.StuckVoiceConnection;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.WebServer.SparkServer;
import tk.ardentbot.Utils.Premium.CheckIfPremiumGuild;
import tk.ardentbot.Utils.Premium.UpdatePremiumMembers;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;
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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Core.Translation.LangFactory.languages;
import static tk.ardentbot.Utils.Searching.GoogleSearch.GOOGLE_API_KEY;

public class Ardent {
    public static HashMap<String, Profile> userProfiles = new HashMap<>();

    public static ArrayList<String> disabledCommands = new ArrayList<>();

    public static String cleverbotUser;
    public static String cleverbotKey;
    public static Process premiumProcess;
    public static boolean premiumBot = false;
    public static String premiumBotToken;
    public static boolean testingBot = false;
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
    public static ScheduledExecutorService globalExecutorService = Executors.newScheduledThreadPool(125);
    public static Shard shard0;
    public static Shard botLogsShard;
    public static int shardCount = 2;
    public static ConcurrentHashMap<String, ChatterBotSession> cleverbots = new ConcurrentHashMap<>();
    public static String announcement;
    public static ConcurrentHashMap<String, Boolean> sentAnnouncement = new ConcurrentHashMap<>();
    public static String mashapeKey;
    public static String gameUrl = "https://ardentbot.tk";
    public static String testBotToken;
    static String node0Url;
    static String node1Url;

    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 100; i++)
            if (i % 3 == 0 && i % 5 != 0) System.out.println("Fizz");
            else if (i % 5 == 0 && i % 3 != 0) System.out.println("Buzz");
            else if (i % 5 == 0 && i % 3 == 0) System.out.println("FizzBuzz");

        for (String s : args) {
            if (s.contains("premium")) premiumBot = true;
        }

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
            try {
                conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop" +
                                "\\Ardent\\dburl.key"))),
                        IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbuser.key"))),
                        IOUtils.toString(new
                                FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbpassword.key"))));
            }
            catch (Exception ex) {
                conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("/root/Ardent/v2url.key"))),
                        IOUtils.toString(new FileReader(new File("/root/Ardent/v2user.key"))), IOUtils.toString(new
                                FileReader(new File("/root/Ardent/v2password.key"))));
            }
        }

        DatabaseAction getKeys = new DatabaseAction("SELECT * FROM APIKeys");
        ResultSet keys = getKeys.request();
        while (keys.next()) {
            String id = keys.getString("Identifier");
            String value = keys.getString("Value");
            if (id.equalsIgnoreCase("mashape")) mashapeKey = value;
            else if (id.equalsIgnoreCase("google")) GOOGLE_API_KEY = value;
            else if (id.equalsIgnoreCase("node0")) node0Url = value;
            else if (id.equalsIgnoreCase("node1")) node1Url = value;
            else if (id.equalsIgnoreCase("cleverbotuser")) cleverbotUser = value;
            else if (id.equalsIgnoreCase("cleverbotkey")) cleverbotKey = value;
            else if (id.equalsIgnoreCase("premiumbottoken")) premiumBotToken = value;
            else if (id.equalsIgnoreCase("testbottoken")) testBotToken = value;
        }
        getKeys.close();

        ShardManager.register(shardCount);

        BotlistUpdater updater = new BotlistUpdater();
        globalExecutorService.scheduleAtFixedRate(updater, 1, 1, TimeUnit.HOURS);

        StuckVoiceConnection playerStuckDaemon = new StuckVoiceConnection();
        globalExecutorService.scheduleAtFixedRate(playerStuckDaemon, 5, 10, TimeUnit.SECONDS);

        UpdatePremiumMembers updatePremiumMembers = new UpdatePremiumMembers();
        globalExecutorService.scheduleAtFixedRate(updatePremiumMembers, 0, 1, TimeUnit.MINUTES);

        if (!premiumBot) {
            SparkServer.setup();
            /*Class currentClass = new Object() {
            }.getClass().getEnclosingClass();
            premiumProcess = Runtime.getRuntime().exec("java -jar " + currentClass.getProtectionDomain().getCodeSource().getLocation()
                    .getPath() + " -premium");*/

        }
        else {
            CheckIfPremiumGuild checkIfPremiumGuild = new CheckIfPremiumGuild();
            globalExecutorService.scheduleAtFixedRate(checkIfPremiumGuild, 1, 1, TimeUnit.MINUTES);
        }

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

        Profile.startProfileChecking();

        int status = Unirest.post("https://cleverbot.io/1.0/create").field("user", cleverbotUser).field("key", cleverbotKey).field
                ("nick", "ardent")
                .asString().getStatus();
        if (status != 200) new BotException("Unable to connect to cleverbot!");
    }
}
