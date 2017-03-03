package Main;

import Backend.Commands.Category;
import Backend.Commands.Command;
import Backend.Commands.CommandFactory;
import Backend.Translation.Crowdin.PhraseUpdater;
import Backend.Translation.Crowdin.TranslationUpdater;
import Backend.Translation.LangFactory;
import Backend.Translation.Language;
import Backend.Web.WebServer;
import Commands.BotAdministration.*;
import Commands.BotInfo.*;
import Commands.Fun.*;
import Commands.Fun.Translate;
import Commands.GuildAdministration.*;
import Commands.GuildInfo.Botname;
import Commands.GuildInfo.GuildInfo;
import Commands.GuildInfo.Points;
import Commands.GuildInfo.Whois;
import Commands.Music.GuildMusicManager;
import Commands.Music.Music;
import Events.Join;
import Events.Leave;
import Events.OnMessage;
import Utils.GuildUtils;
import Utils.MuteDaemon;
import ch.qos.logback.classic.Level;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.apache.commons.io.IOUtils;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static Backend.Translation.LangFactory.languages;
import static Commands.GuildAdministration.EmojiPacks.registerEmojis;

public class Ardent {
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);

    public static ArrayList<Language> crowdinLanguages = new ArrayList<>();

    public static TextChannel botLogs;

    public static AudioPlayerManager playerManager;
    public static Map<Long, GuildMusicManager> musicManagers;

    public static ChatterBot cleverBot;
    public static ConcurrentHashMap<String, ChatterBotSession> cleverbots = new ConcurrentHashMap<>();

    public static ArrayList<String> developers = new ArrayList<>();
    public static ArrayList<String> translators = new ArrayList<>();

    public static Gson gson = new Gson();
    public static Connection conn;
    public static Timer timer = new Timer();
    public static CommandFactory factory;
    public static JDA jda;

    public static User ardent;
    private static int gameCounter = 0;
    private static int matureLanguages = 0;
    public static Twitter twitter;

    public static Command help;
    public static Command patreon;
    public static Command translateForArdent;

    public static final boolean testingBot = false;
    public static String url = "https://ardentbot.tk";

    public static ConcurrentHashMap<String, Boolean> sentAnnouncement = new ConcurrentHashMap<>();
    public static String announcement;

    public static void main(String[] args) {
        try {
           PhraseUpdater.ACCOUNT_KEY = IOUtils.toString(new FileReader(new File("/root/Ardent/crowdin_account_key.key")));
           PhraseUpdater.PROJECT_KEY = IOUtils.toString(new FileReader(new File("/root/Ardent/crowdin_project_key.key")));
            String token;
            if (testingBot) {
                token = IOUtils.toString(new FileReader(new File("/root/Ardent/testbottoken.key")));
            }
            else token = IOUtils.toString(new FileReader(new File("/root/Ardent/v2bottoken.key")));

            jda = new JDABuilder(AccountType.BOT)
                    .setEventManager(new AnnotatedEventManager())
                    .setToken(token)
                    .setAutoReconnect(true)
                    .setAudioEnabled(true)
                    .setGame(Game.of("- New Update! -"))
                    .setStatus(OnlineStatus.ONLINE)
                    .setBulkDeleteSplittingEnabled(true)
                    .setEnableShutdownHook(true)
                    .setAudioSendFactory(new NativeAudioSendFactory())
                    .buildBlocking();

            ardent = jda.getUserById(jda.getSelfUser().getId());

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("Fi9IjqqsGmOXqjR5uYK8YM2Pr")
                    .setOAuthConsumerSecret(IOUtils.toString(new FileReader(new File("/root/Ardent/twitterconsumersecret.key"))))
                    .setOAuthAccessToken("818984879018954752-aCzxyML6Xp0QcRpq5sjoe8wfp0sjVDt")
                    .setOAuthAccessTokenSecret(IOUtils.toString(new FileReader(new File("/root/Ardent/twitteroauthsecret.key"))));
            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();

            GIF.setupCategories();

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(IOUtils.toString(new FileReader(new File("/root/Ardent/v2url.key"))), IOUtils.toString(new FileReader(new File("/root/Ardent/v2user.key"))), IOUtils.toString(new FileReader(new File("/root/Ardent/v2password.key"))));
            if (conn.isClosed()) {
                System.out.println("Unable to connect to Database");
            }
            else {
                // Register event listeners
                jda.addEventListener(new OnMessage());
                jda.addEventListener(new Join());
                jda.addEventListener(new Leave());

                // Add languages
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
                languages.add(LangFactory.polish);
                languages.add(LangFactory.spanish);

                crowdinLanguages.add(LangFactory.croatian);
                crowdinLanguages.add(LangFactory.french);
                crowdinLanguages.add(LangFactory.turkish);
                crowdinLanguages.add(LangFactory.german);
                crowdinLanguages.add(LangFactory.cyrillicserbian);
                crowdinLanguages.add(LangFactory.spanish);
                crowdinLanguages.add(LangFactory.dutch);

                for (Language lang : languages) {
                    if (lang.getLanguageStatus() == Language.Status.MATURE) matureLanguages++;
                }

                factory = new CommandFactory();

                help = new Help(new Command.CommandSettings("help", true, true, Category.BOTINFO));
                patreon = new Patreon(new Command.CommandSettings("patreon", true, true, Category.GUILDINFO));
                translateForArdent = new TranslateForArdent(new Command.CommandSettings("translateforardent", true, true, Category.BOTINFO));

                // Register Commands
                factory.registerCommand(new AddEnglishBase(new Command.CommandSettings("addenglishbase", true, true, Category.BOTADMINISTRATION)));
                factory.registerCommand(new Todo(new Command.CommandSettings("todo", true, true, Category.BOTADMINISTRATION)));
                factory.registerCommand(new Tweet(new Command.CommandSettings("tweet", true, true, Category.BOTADMINISTRATION)));
                factory.registerCommand(new Admin(new Command.CommandSettings("admin", true, true, Category.BOTADMINISTRATION)));
                factory.registerCommand(new Eval(new Command.CommandSettings("eval", true, true, Category.BOTADMINISTRATION)));

                factory.registerCommand(new Ping(new Command.CommandSettings("ping", true, true, Category.BOTINFO)));
                factory.registerCommand(help);
                factory.registerCommand(new Website(new Command.CommandSettings("website", true, true, Category.BOTINFO)));
                factory.registerCommand(new Invite(new Command.CommandSettings("invite", true, true, Category.BOTINFO)));
                factory.registerCommand(new Joinmessage(new Command.CommandSettings("joinmessage", true, true, Category.BOTINFO)));
                factory.registerCommand(new Status(new Command.CommandSettings("status", true, true, Category.BOTINFO)));
                factory.registerCommand(new Request(new Command.CommandSettings("request", true, true, Category.BOTINFO)));
                factory.registerCommand(translateForArdent);
                factory.registerCommand(new Changelog(new Command.CommandSettings("changelog", true, true, Category.BOTINFO)));
                factory.registerCommand(patreon);
                factory.registerCommand(new Support(new Command.CommandSettings("support", true, true, Category.BOTINFO)));

                factory.registerCommand(new UD(new Command.CommandSettings("ud", true, true, Category.FUN)));
                factory.registerCommand(new GIF(new Command.CommandSettings("gif", true, true, Category.FUN)));
                factory.registerCommand(new FML(new Command.CommandSettings("fml", true, true, Category.FUN)));
                factory.registerCommand(new Yoda(new Command.CommandSettings("yoda", true, true, Category.FUN)));
                factory.registerCommand(new EightBall(new Command.CommandSettings("8ball", true, true, Category.FUN)));
                factory.registerCommand(new Tags(new Command.CommandSettings("tag", false, true, Category.FUN)));
                factory.registerCommand(new Translate(new Command.CommandSettings("translate", true, true, Category.FUN)));
                factory.registerCommand(new Roll(new Command.CommandSettings("roll", true, true, Category.FUN)));
                // factory.registerCommand(new Echo(new Command.CommandSettings("echo", true, true, Category.FUN)));
                // factory.registerCommand(new Coinflip(new Command.CommandSettings("coinflip", true, true, Category.FUN)));
                factory.registerCommand(new Music(new Command.CommandSettings("music", false, true, Category.FUN)));

                factory.registerCommand(new Prefix(new Command.CommandSettings("prefix", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new GuildLanguage(new Command.CommandSettings("language", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Setnickname(new Command.CommandSettings("setnickname", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Prune(new Command.CommandSettings("prune", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Kick(new Command.CommandSettings("kick", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Ban(new Command.CommandSettings("ban", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Clear(new Command.CommandSettings("clear", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Roles(new Command.CommandSettings("roles", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new DefaultRole(new Command.CommandSettings("defaultrole", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Unmute(new Command.CommandSettings("unmute", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Mute(new Command.CommandSettings("mute", false, true, Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Automessage(new Command.CommandSettings("automessage", false, true, Category.GUILDADMINISTRATION)));
                // factory.registerCommand(new EmojiPacks(new Command.CommandSettings("emojipacks", false, true, Category.GUILDADMINISTRATION)));
                // factory.registerCommand(new CustomCommands(new Command.CommandSettings("cc", false, true, Category.GUILDADMINISTRATION)));

                factory.registerCommand(new GuildInfo(new Command.CommandSettings("guildinfo", false, true, Category.GUILDINFO)));
                factory.registerCommand(new Whois(new Command.CommandSettings("whois", true, true, Category.GUILDINFO)));
                factory.registerCommand(new Points(new Command.CommandSettings("points", false, true, Category.GUILDINFO)));
                factory.registerCommand(new Botname(new Command.CommandSettings("botname", false, true, Category.GUILDINFO)));
                // factory.registerCommand(new Emojis(new Command.CommandSettings("emojis", true, true, Category.GUILDINFO)));

                cleverBot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "f5d922d97e345aa1");
                Statement statement = conn.createStatement();
                ResultSet set = statement.executeQuery("SELECT * FROM Staff");
                while (set.next()) {
                    if (set.getString("Role").equalsIgnoreCase("Developer")) developers.add(set.getString("UserID"));
                    else if (set.getString("Role").equalsIgnoreCase("Translator"))
                        translators.add(set.getString("UserID"));
                }

                set.close();

                registerEmojis();
                botLogs = jda.getTextChannelById("272865411471638528");

                musicManagers = new HashMap<>();

                playerManager = new DefaultAudioPlayerManager();
                playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.LOW);
                playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
                playerManager.registerSourceManager(new VimeoAudioSourceManager());
                playerManager.registerSourceManager(new BandcampAudioSourceManager());
                playerManager.registerSourceManager(new HttpAudioSourceManager());

                AudioSourceManagers.registerRemoteSources(playerManager);
                AudioSourceManagers.registerLocalSource(playerManager);

                jda.getGuilds().forEach((guild -> {
                    Status.commandsByGuild.put(guild.getId(), 0);
                    cleverbots.put(guild.getId(), cleverBot.createSession());
                    try {
                        ResultSet in = statement.executeQuery("SELECT * FROM Guilds WHERE GuildID='" + guild.getId() + "'");
                        if (!in.next()) {
                            statement.executeUpdate("INSERT INTO Guilds VALUES ('" + guild.getId() + "', 'english', '/')");
                        }
                        in.close();
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }));
                statement.close();

                GuildUtils.addAllPrefixes();

                Timer counter = new Timer();
                counter.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        jda.getPresence().setGame(Game.of("in 5 languages", "patreon.com/ardent"));
                    }
                }, 25000, 25000);

                PhraseUpdater phraseUpdater = new PhraseUpdater();
                TranslationUpdater translationUpdater = new TranslationUpdater();

                counter.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        translationUpdater.run();
                    }
                }, (1000 * 60 * 5), (1000 * 60 * 5));

                counter.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        phraseUpdater.run();
                    }
                }, (1000 * 60 * 17), (1000 * 60 * 17));

                MuteDaemon muteDaemon = new MuteDaemon();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        muteDaemon.run();
                    }
                }, 1000, 5000);

                Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
                Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF);
                Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.OFF);

                WebServer.setup();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
