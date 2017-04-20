package tk.ardentbot.main;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.mashape.unirest.http.Unirest;
import com.rethinkdb.net.Cursor;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.wrapper.spotify.Api;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import tk.ardentbot.commands.fun.GIF;
import tk.ardentbot.commands.music.Music;
import tk.ardentbot.commands.music.StuckVoiceConnection;
import tk.ardentbot.commands.rpg.Trivia;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.core.misc.webServer.SparkServer;
import tk.ardentbot.core.translation.LangFactory;
import tk.ardentbot.rethink.Database;
import tk.ardentbot.utils.models.TriviaQuestion;
import tk.ardentbot.utils.premium.CheckIfPremiumGuild;
import tk.ardentbot.utils.premium.UpdatePremiumMembers;
import tk.ardentbot.utils.searching.GoogleSearch;
import tk.ardentbot.utils.updaters.*;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.core.translation.LangFactory.languages;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;
import static tk.ardentbot.utils.searching.GoogleSearch.GOOGLE_API_KEY;

public class Ardent {
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/sheets.googleapis" +
            ".com.json");
    public static ArrayList<String> disabledCommands = new ArrayList<>();
    public static String cleverbotUser;
    public static String cleverbotKey;
    public static Process premiumProcess;
    public static boolean premiumBot = false;
    public static String premiumBotToken;
    public static boolean testingBot = false;
    public static Api spotifyApi;
    public static ArrayList<String> tierOnepatrons = new ArrayList<>();
    public static ArrayList<String> tierTwopatrons = new ArrayList<>();
    public static ArrayList<String> tierThreepatrons = new ArrayList<>();
    public static ArrayList<String> developers = new ArrayList<>();
    public static ArrayList<String> moderators = new ArrayList<>();
    public static ArrayList<String> translators = new ArrayList<>();
    public static String botsDiscordPwToken;
    public static String discordBotsOrgToken;
    public static ScheduledExecutorService globalExecutorService = Executors.newScheduledThreadPool(20);
    public static Shard shard0;
    public static Shard botLogsShard;
    public static int shardCount = 2;
    public static ConcurrentHashMap<String, ChatterBotSession> cleverbots = new ConcurrentHashMap<>();
    public static String announcement;
    public static ConcurrentHashMap<String, Boolean> sentAnnouncement = new ConcurrentHashMap<>();
    public static String mashapeKey;
    public static String gameUrl = "https://ardentbot.tk";
    public static String testBotToken;
    public static Sheets sheetsApi;
    public static String node1Url;
    public static String dbPassword;
    public static ValueRange triviaSheet;
    public static Twitter twitter;
    public static boolean started = false;
    static String node0Url;
    private static HttpTransport transport;
    private static JacksonFactory jsonFactory;
    private static FileDataStoreFactory dataStoreFactory;
    private static List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
    private static HttpTransport HTTP_TRANSPORT;
    private static DataStoreFactory DATA_STORE_FACTORY;
    private static String clientSecret;

    public static void main(String[] args) throws Exception {

        new DefaultAudioPlayerManager();
        Logger root1 = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root1.setLevel(Level.OFF);

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
            dbPassword = IOUtils.toString(new FileReader(new File("/root/Ardent/v2password.key")));
            botsDiscordPwToken = IOUtils.toString(new FileReader(new File("/root/Ardent/botsdiscordpw.key")));
            discordBotsOrgToken = IOUtils.toString(new FileReader(new File("/root/Ardent/discordbotsorg.key")));
        }
        else {
            try {
                dbPassword = IOUtils.toString(new
                        FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbpassword.key")));
            }
            catch (Exception ex) {
                dbPassword = IOUtils.toString(new FileReader(new File("/root/Ardent/v2password.key")));
            }
        }
        Database.setup();

        Cursor<HashMap> apiKeys = r.db("data").table("api_keys").run(connection);
        apiKeys.forEach((outside) -> {
            outside.forEach((uId, uValue) -> {
                String id = (String) uId;
                String value = (String) uValue;
                if (id.equalsIgnoreCase("mashape")) mashapeKey = value;
                else if (id.equalsIgnoreCase("google")) GOOGLE_API_KEY = value;
                else if (id.equalsIgnoreCase("node0")) node0Url = value;
                else if (id.equalsIgnoreCase("node1")) node1Url = value;
                else if (id.equalsIgnoreCase("cleverbotuser")) cleverbotUser = value;
                else if (id.equalsIgnoreCase("cleverbotkey")) cleverbotKey = value;
                else if (id.equalsIgnoreCase("premiumbottoken")) premiumBotToken = value;
                else if (id.equalsIgnoreCase("testbottoken")) testBotToken = value;
                else if (id.equalsIgnoreCase("googlesecret")) clientSecret = value;
            });
        });

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

        ShardManager.register(shardCount);

        BotlistUpdater updater = new BotlistUpdater();
        globalExecutorService.scheduleAtFixedRate(updater, 1, 1, TimeUnit.HOURS);

        StuckVoiceConnection playerStuckDaemon = new StuckVoiceConnection();
        globalExecutorService.scheduleAtFixedRate(playerStuckDaemon, 10, 10, TimeUnit.SECONDS);

        UpdatePremiumMembers updatePremiumMembers = new UpdatePremiumMembers();
        globalExecutorService.scheduleAtFixedRate(updatePremiumMembers, 0, 1, TimeUnit.MINUTES);

        ProfileUpdater profileUpdater = new ProfileUpdater();
        globalExecutorService.scheduleWithFixedDelay(profileUpdater, 5, 5, TimeUnit.MINUTES);

        if (!premiumBot) {
            SparkServer.setup();
            /*Class currentClass = new Object() {
            }.getClass().getEnclosingClass();
            premiumProcess = Runtime.getRuntime().exec("java -jar " + currentClass.getProtectionDomain().getCodeSource().getLocation()
                    .getPath() + " -premium");*/

        }
        else {
            CheckIfPremiumGuild checkIfPremiumGuild = new CheckIfPremiumGuild();
            globalExecutorService.scheduleAtFixedRate(checkIfPremiumGuild, 1, 5, TimeUnit.MINUTES);
        }

        GoogleSearch.setup(GOOGLE_API_KEY);

        int status = Unirest.post("https://cleverbot.io/1.0/create").field("user", cleverbotUser).field("key", cleverbotKey).field
                ("nick", "ardent")
                .asString().getStatus();
        if (status != 200) new BotException("Unable to connect to cleverbot!");

        WebsiteDaemon websiteDaemon = new WebsiteDaemon();
        globalExecutorService.scheduleAtFixedRate(websiteDaemon, 5, 15, TimeUnit.SECONDS);

        PermissionsDaemon permissionsDaemon = new PermissionsDaemon();
        globalExecutorService.scheduleAtFixedRate(permissionsDaemon, 0, 60, TimeUnit.SECONDS);

        GuildDaemon guildDaemon = new GuildDaemon();
        globalExecutorService.scheduleAtFixedRate(guildDaemon, 0, 10, TimeUnit.SECONDS);

        MuteDaemon muteDaemon = new MuteDaemon();
        globalExecutorService.scheduleAtFixedRate(muteDaemon, 1, 5, TimeUnit.SECONDS);

        Music.checkMusicConnections();

        GIF.setupCategories();

        if (!testingBot) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("Fi9IjqqsGmOXqjR5uYK8YM2Pr")
                    .setOAuthConsumerSecret(IOUtils.toString(new FileReader(new File
                            ("/root/Ardent/twitterconsumersecret.key"))))
                    .setOAuthAccessToken("818984879018954752-aCzxyML6Xp0QcRpq5sjoe8wfp0sjVDt")
                    .setOAuthAccessTokenSecret(IOUtils.toString(new FileReader(new File
                            ("/root/Ardent/twitteroauthsecret.key"))));
            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();
        }

        java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);

        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            jsonFactory = JacksonFactory.getDefaultInstance();
            sheetsApi = getSheetsService();
            globalExecutorService.schedule(() -> {
                try {
                    triviaSheet = sheetsApi.spreadsheets().values().get("1qm27kGVQ4BdYjvPSlF0zM64j7nkW4HXzALFNcan4fbs", "A2:C").execute();
                }
                catch (IOException e) {
                    new BotException(e);
                }
                List<List<Object>> values = triviaSheet.getValues();
                values.forEach(row -> {
                    String category = (String) row.get(0);
                    String q = (String) row.get(1);
                    String answerUnparsed = (String) row.get(2);
                    TriviaQuestion triviaQuestion = new TriviaQuestion();
                    triviaQuestion.setQuestion(q);
                    triviaQuestion.setCategory(category);
                    for (String answer : answerUnparsed.split("~")) {
                        triviaQuestion.withAnswer(answer);
                    }
                    Trivia.triviaQuestions.add(triviaQuestion);
                });
            }, 60, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        started = true;
    }

    public static Credential authorize() throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(clientSecret));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        transport, jsonFactory, clientSecrets, scopes)
                        .setDataStoreFactory(dataStoreFactory)
                        .build();
        Credential credential;
        if (!testingBot)
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setHost("ardentbot.tk").setPort
                    (1337).build()).authorize("703818195441-no5nh31a0rfcogq8k9ggsvsvkq5ai0ih.apps.googleusercontent.com");
        else {
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setHost("localhost").setPort
                    (1337).build()).authorize("703818195441-no5nh31a0rfcogq8k9ggsvsvkq5ai0ih.apps.googleusercontent.com");
        }
        //System.out.println(credential.refreshToken());
        return credential;
    }

    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("Ardent")
                .build();
    }
}
