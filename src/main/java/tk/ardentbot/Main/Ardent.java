package tk.ardentbot.Main;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.wrapper.spotify.Api;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.BotCommands.Music.StuckVoiceConnection;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Misc.WebServer.SparkServer;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Rethink.Database;
import tk.ardentbot.Utils.Premium.CheckIfPremiumGuild;
import tk.ardentbot.Utils.Premium.UpdatePremiumMembers;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Searching.GoogleSearch;
import tk.ardentbot.Utils.Updaters.BotlistUpdater;
import tk.ardentbot.Utils.Updaters.ProfileUpdater;
import tk.ardentbot.Utils.Updaters.SpotifyTokenRefresh;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Core.Translation.LangFactory.languages;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;
import static tk.ardentbot.Utils.Searching.GoogleSearch.GOOGLE_API_KEY;

public class Ardent {
    public static final Gson globalGson = new Gson();
    private static final String APPLICATION_NAME = "Ardent";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final File DATA_STORE_DIR = new java.io.File("/root/Ardent", ".credentials/sheets.googleapis.com-java-quickstart");
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);
    public static ArrayList<String> disabledCommands = new ArrayList<>();
    public static String cleverbotUser;
    public static String cleverbotKey;
    public static Process premiumProcess;
    public static boolean premiumBot = false;
    public static String premiumBotToken;
    public static boolean testingBot = true;
    public static Api spotifyApi;
    public static ArrayList<String> tierOnepatrons = new ArrayList<>();
    public static ArrayList<String> tierTwopatrons = new ArrayList<>();
    public static ArrayList<String> tierThreepatrons = new ArrayList<>();
    public static ArrayList<String> developers = new ArrayList<>();
    public static ArrayList<String> moderators = new ArrayList<>();
    public static ArrayList<String> translators = new ArrayList<>();
    public static String botsDiscordPwToken;
    public static String discordBotsOrgToken;
    public static Connection conn;
    public static ScheduledExecutorService globalExecutorService = Executors.newScheduledThreadPool(20);
    public static ScheduledExecutorService profileUpdateExecutorService = Executors.newScheduledThreadPool(20);
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
    static String node0Url;
    private static String credential;
    private static HttpTransport HTTP_TRANSPORT;
    private static DataStoreFactory DATA_STORE_FACTORY;

    static {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(new File("/root/Ardent"));
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
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

            dbPassword = IOUtils.toString(new FileReader(new File("/root/Ardent/v2password.key")));
            botsDiscordPwToken = IOUtils.toString(new FileReader(new File("/root/Ardent/botsdiscordpw.key")));
            discordBotsOrgToken = IOUtils.toString(new FileReader(new File("/root/Ardent/discordbotsorg.key")));
        }
        else {
            try {
                dbPassword = IOUtils.toString(new
                        FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbpassword.key")));

                conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop" +
                                "\\Ardent\\dburl.key"))),
                        IOUtils.toString(new FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbuser.key"))),
                        IOUtils.toString(new
                                FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\dbpassword.key"))));
            }
            catch (Exception ex) {
                dbPassword = IOUtils.toString(new FileReader(new File("/root/Ardent/v2password.key")));
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
            else if (id.equalsIgnoreCase("googlecredential")) credential = value;
        }
        getKeys.close();
        Database.setup();

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
        globalExecutorService.scheduleWithFixedDelay(profileUpdater, 1, 1, TimeUnit.MINUTES);

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

        int status = Unirest.post("https://cleverbot.io/1.0/create").field("user", cleverbotUser).field("key", cleverbotKey).field
                ("nick", "ardent")
                .asString().getStatus();
        if (status != 200) new BotException("Unable to connect to cleverbot!");

        disableSSLCertificateChecking();
        // sheetsApi = getSheetsApi();
        //sheetsApi.spreadsheets().get().execute().getSheets().get(0).getData();
        // TODO: 4/5/2017 complete, switch trivia to https://docs.google
        // .com/spreadsheets/d/1qm27kGVQ4BdYjvPSlF0zM64j7nkW4HXzALFNcan4fbs/edit#gid=0

        globalExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                for (String s : moderators) {
                    r.db("data").table("staff").insert(r.hashMap("user_id", s).with("role", "moderator")).run(connection);
                }
                for (String s : developers) {
                    r.db("data").table("patrons").insert(r.hashMap("user_id", s).with("tier", "developer")).run(connection);
                }
                for (String s : translators) {
                    r.db("data").table("patrons").insert(r.hashMap("user_id", s).with("tier", "translator")).run(connection);
                }
            }
        }, 10, TimeUnit.SECONDS);
    }

    private static Sheets getSheetsApi() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential authorize() throws IOException {
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new StringReader(credential));
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        return new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     * <p>
     * Credit to https://gist.github.com/aembleton/889392
     */
    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
