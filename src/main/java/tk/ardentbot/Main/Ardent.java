package tk.ardentbot.Main;

import com.google.code.chatterbotapi.ChatterBotSession;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.Core.WebServer.SparkServer;
import tk.ardentbot.Utils.Updaters.BotlistUpdater;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Ardent {
    public static final boolean testingBot = false;
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

    public static void main(String[] args) throws Exception {
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

        ShardManager.register(shardCount);
        SparkServer.setup();

        BotlistUpdater updater = new BotlistUpdater();
        globalExecutorService.scheduleAtFixedRate(updater, 1, 1, TimeUnit.HOURS);
    }
}
