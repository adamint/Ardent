package tk.ardentbot.Main;

import ch.qos.logback.classic.Level;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.BotCommands.BotAdministration.*;
import tk.ardentbot.BotCommands.BotInfo.*;
import tk.ardentbot.BotCommands.Fun.*;
import tk.ardentbot.BotCommands.GuildAdministration.*;
import tk.ardentbot.BotCommands.GuildInfo.Botname;
import tk.ardentbot.BotCommands.GuildInfo.GuildInfo;
import tk.ardentbot.BotCommands.GuildInfo.ServerInfo;
import tk.ardentbot.BotCommands.GuildInfo.Whois;
import tk.ardentbot.BotCommands.Music.GuildMusicManager;
import tk.ardentbot.BotCommands.Music.Music;
import tk.ardentbot.BotCommands.Music.Play;
import tk.ardentbot.BotCommands.RPG.*;
import tk.ardentbot.Core.BotData.BotLanguageData;
import tk.ardentbot.Core.BotData.BotMuteData;
import tk.ardentbot.Core.BotData.BotPrefixData;
import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.CommandExecution.Category;
import tk.ardentbot.Core.CommandExecution.CommandFactory;
import tk.ardentbot.Core.Events.*;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.InternalStats;
import tk.ardentbot.Utils.Models.RestrictedUser;
import tk.ardentbot.Utils.RPGUtils.EntityGuild;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.SQL.MuteDaemon;
import tk.ardentbot.Utils.Updaters.GuildDaemon;
import tk.ardentbot.Utils.Updaters.PermissionsDaemon;
import tk.ardentbot.Utils.Updaters.PhraseUpdater;
import tk.ardentbot.Utils.Updaters.WebsiteDaemon;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static tk.ardentbot.Core.Translation.LangFactory.languages;

public class Shard {
    public boolean testingBot;
    public ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);
    public BotMuteData botMuteData;
    public BotPrefixData botPrefixData;
    public BotLanguageData botLanguageData;
    public ArrayList<Language> crowdinLanguages = new ArrayList<>();
    public TextChannel botLogs;
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public ChatterBot cleverBot;
    public CommandFactory factory;
    public JDA jda;
    public User bot;
    public Twitter twitter;
    public BaseCommand help;
    public BaseCommand patreon;
    public BaseCommand translateForArdent;
    public BaseCommand getDevHelp;
    public BaseCommand request;
    public String url = "https://ardentbot.tk";
    public Gson gson = new Gson();
    private int gameCounter = 0;
    private int matureLanguages = 0;
    private int id;

    public Shard(boolean testingBot, int shardNumber, int totalShardCount) throws Exception {
        this.testingBot = testingBot;
        this.id = shardNumber;
        String token;
        if (testingBot && !Ardent.premiumBot) {
            token = Ardent.testBotToken;
        }
        else {
            if (Ardent.premiumBot) {
                token = Ardent.premiumBotToken;
            }
            else {
                token = IOUtils.toString(new FileReader(new File("/root/Ardent/v2bottoken.key")));
            }
        }

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
                .useSharding(shardNumber, totalShardCount)
                .buildBlocking();

        bot = jda.getUserById(jda.getSelfUser().getId());
        if (jda.getGuildById("260841592070340609") != null) Ardent.botLogsShard = this;

        Class.forName("com.mysql.jdbc.Driver");

        if (!testingBot) {
            PhraseUpdater.ACCOUNT_KEY = IOUtils.toString(new FileReader(new File("/root/Ardent/crowdin_account_key" +
                    ".key")));
            PhraseUpdater.PROJECT_KEY = IOUtils.toString(new FileReader(new File("/root/Ardent/crowdin_project_key" +
                    ".key")));

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

        executorService.schedule(() -> {
            try {
                GIF.setupCategories();

                // Register event listeners
                jda.addEventListener(new OnMessage());
                jda.addEventListener(new InteractiveOnMessage());
                jda.addEventListener(new Join());
                jda.addEventListener(new Leave());
                jda.addEventListener(new VoiceLeaveEvent());

                crowdinLanguages.add(LangFactory.croatian);
                crowdinLanguages.add(LangFactory.french);
                crowdinLanguages.add(LangFactory.turkish);
                crowdinLanguages.add(LangFactory.german);
                crowdinLanguages.add(LangFactory.cyrillicserbian);
                crowdinLanguages.add(LangFactory.spanish);
                crowdinLanguages.add(LangFactory.dutch);
                crowdinLanguages.add(LangFactory.arabic);
                crowdinLanguages.add(LangFactory.hindi);

                // Adding the "handler" for mutes
                botMuteData = new BotMuteData();

                // Adding the handler for prefixes
                botPrefixData = new BotPrefixData();

                // Adding the handler for languages
                botLanguageData = new BotLanguageData();

                languages.stream().filter(lang -> lang.getLanguageStatus() == Language.Status.MATURE).forEach(lang ->
                        matureLanguages++);

                factory = new CommandFactory(this);

                help = new Help(new BaseCommand.CommandSettings("help", true, true, Category.BOTINFO));
                patreon = new Patreon(new BaseCommand.CommandSettings("patreon", true, true, Category.GUILDINFO));
                translateForArdent = new TranslateForArdent(new BaseCommand.CommandSettings("translateforardent", true,
                        true, Category.BOTINFO));
                getDevHelp = new Bug(new BaseCommand.CommandSettings("getdevhelp", false, true, Category.BOTINFO));
                request = new Request(new BaseCommand.CommandSettings("request", true, true, Category
                        .BOTADMINISTRATION));

                factory.registerCommand(new AddEnglishBase(new BaseCommand.CommandSettings("addenglishbase", true, true,
                        Category.BOTADMINISTRATION)));
                factory.registerCommand(new Todo(new BaseCommand.CommandSettings("todo", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Tweet(new BaseCommand.CommandSettings("tweet", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Admin(new BaseCommand.CommandSettings("admin", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Eval(new BaseCommand.CommandSettings("eval", true, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(new Manage(new BaseCommand.CommandSettings("manage", false, true, Category.BOTADMINISTRATION)));
                factory.registerCommand(new Botname(new BaseCommand.CommandSettings("botname", false, true, Category
                        .BOTADMINISTRATION)));
                factory.registerCommand(request);

                factory.registerCommand(new About(new BaseCommand.CommandSettings("about", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Support(new BaseCommand.CommandSettings("support", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Website(new BaseCommand.CommandSettings("website", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Invite(new BaseCommand.CommandSettings("invite", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(translateForArdent);
                factory.registerCommand(new Changelog(new BaseCommand.CommandSettings("changelog", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(patreon);
                factory.registerCommand(new Joinmessage(new BaseCommand.CommandSettings("joinmessage", true, true,
                        Category.BOTINFO)));
                factory.registerCommand(new Status(new BaseCommand.CommandSettings("status", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(new Ping(new BaseCommand.CommandSettings("ping", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(help);
                factory.registerCommand(new Stats(new BaseCommand.CommandSettings("stats", true, true, Category
                        .BOTINFO)));
                factory.registerCommand(getDevHelp);

                factory.registerCommand(new Music(new BaseCommand.CommandSettings("music", false, true, Category.FUN)
                        , this));
                factory.registerCommand(new Play(new BaseCommand.CommandSettings("play", false, true, Category
                        .FUN)));
                factory.registerCommand(new UD(new BaseCommand.CommandSettings("ud", true, true, Category.FUN)));
                factory.registerCommand(new GIF(new BaseCommand.CommandSettings("gif", true, true, Category.FUN)));
                factory.registerCommand(new FML(new BaseCommand.CommandSettings("fml", true, true, Category.FUN)));
                factory.registerCommand(new Fortune(new BaseCommand.CommandSettings("fortune", true, true, Category
                        .FUN)));
                factory.registerCommand(new Yoda(new BaseCommand.CommandSettings("yoda", true, true, Category.FUN)));
                factory.registerCommand(new EightBall(new BaseCommand.CommandSettings("8ball", true, true, Category
                        .FUN)));
                factory.registerCommand(new Tags(new BaseCommand.CommandSettings("tag", false, true, Category.FUN)));
                factory.registerCommand(new tk.ardentbot.BotCommands.Fun.Translate(new BaseCommand.CommandSettings
                        ("translate", true, true, Category.FUN)));
                factory.registerCommand(new Define(new BaseCommand.CommandSettings("define", true, true, Category
                        .FUN)));
                factory.registerCommand(new Roll(new BaseCommand.CommandSettings("roll", true, true, Category.FUN)));
                factory.registerCommand(new Coinflip(new BaseCommand.CommandSettings("coinflip", true, true, Category
                        .FUN)));
                factory.registerCommand(new RandomNum(new BaseCommand.CommandSettings("random", true, true, Category
                        .FUN)));
                factory.registerCommand(new GSearch(new BaseCommand.CommandSettings("google", true, true, Category
                        .FUN)));
                factory.registerCommand(new Wiki(new BaseCommand.CommandSettings("wiki", true, true, Category
                        .FUN)));
                factory.registerCommand(new Deeplearning(new BaseCommand.CommandSettings("deeplearning", false, true, Category
                        .FUN)));

                factory.registerCommand(new Prefix(new BaseCommand.CommandSettings("prefix", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new GuildLanguage(new BaseCommand.CommandSettings("language", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Restrict(new BaseCommand.CommandSettings("restrict", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Iam(new BaseCommand.CommandSettings("iam", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Setnickname(new BaseCommand.CommandSettings("setnickname", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Prune(new BaseCommand.CommandSettings("prune", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Kick(new BaseCommand.CommandSettings("kick", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Ban(new BaseCommand.CommandSettings("ban", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Clear(new BaseCommand.CommandSettings("clear", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Roles(new BaseCommand.CommandSettings("roles", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new DefaultRole(new BaseCommand.CommandSettings("defaultrole", false, true,
                        Category.GUILDADMINISTRATION)));
                factory.registerCommand(new Unmute(new BaseCommand.CommandSettings("unmute", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Mute(new BaseCommand.CommandSettings("mute", false, true, Category
                        .GUILDADMINISTRATION)));
                factory.registerCommand(new Automessage(new BaseCommand.CommandSettings("automessage", false, true,
                        Category.GUILDADMINISTRATION)));

                factory.registerCommand(new GuildInfo(new BaseCommand.CommandSettings("guildinfo", false, true, Category
                        .GUILDINFO)));
                factory.registerCommand(new ServerInfo(new BaseCommand.CommandSettings("info", false, true, Category
                        .GUILDINFO)));
                factory.registerCommand(new Whois(new BaseCommand.CommandSettings("whois", true, true, Category
                        .GUILDINFO)));
                /*
                factory.registerCommand(new Points(new BaseCommand.CommandSettings("points", false, true, Category
                        .GUILDINFO)));
                factory.registerCommand(new Emojis(new BaseCommand.CommandSettings("emojis", true, true, Category
                .GUILDINFO)));
                */

                factory.registerCommand(new RPGMoney(new BaseCommand.CommandSettings("money", false, true, Category.RPG)));
                factory.registerCommand(new Pay(new BaseCommand.CommandSettings("pay", false, true, Category.RPG)));
                factory.registerCommand(new Bet(new BaseCommand.CommandSettings("bet", false, true, Category.RPG)));
                factory.registerCommand(new Badges(new BaseCommand.CommandSettings("badges", false, true, Category.RPG)));
                factory.registerCommand(new UserProfile(new BaseCommand.CommandSettings("profile", true, true, Category
                        .RPG)));

                cleverBot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "f5d922d97e345aa1");

                musicManagers = new HashMap<>();

                playerManager = new DefaultAudioPlayerManager();
                playerManager.useRemoteNodes(Ardent.node1Url + ":8080");

                playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.LOW);
                playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                playerManager.registerSourceManager(new SoundCloudAudioSourceManager());

                AudioSourceManagers.registerRemoteSources(playerManager);
                AudioSourceManagers.registerLocalSource(playerManager);

                jda.getGuilds().forEach((guild -> {
                    Status.commandsByGuild.put(guild.getId(), 0);
                    Ardent.sentAnnouncement.put(guild.getId(), false);
                    Ardent.cleverbots.put(guild.getId(), cleverBot.createSession());
                }));

                DatabaseAction putGuildData = new DatabaseAction("SELECT * FROM Guilds");
                ResultSet guildDataSet = putGuildData.request();
                while (guildDataSet.next()) {
                    String guildId = guildDataSet.getString("GuildID");
                    String lang = guildDataSet.getString("Language");
                    String prefix = guildDataSet.getString("Prefix");
                    botLanguageData.set(guildId, lang);
                    botPrefixData.set(guildId, prefix);
                }
                putGuildData.close();

                DatabaseAction getRestrictions = new DatabaseAction("SELECT * FROM Restricted");
                ResultSet restrictionSet = getRestrictions.request();
                while (restrictionSet.next()) {
                    String restrictedId = restrictionSet.getString("UserID");
                    String restrictedBy = restrictionSet.getString("RestrictedByID");
                    String guildId = restrictionSet.getString("GuildID");
                    Guild temp = jda.getGuildById(guildId);
                    if (temp != null) {
                        EntityGuild entityGuild = EntityGuild.get(temp);
                        entityGuild.addRestricted(new RestrictedUser(restrictedId, restrictedBy, temp));
                    }
                }
                getRestrictions.close();

                executorService.scheduleAtFixedRate(() -> {
                    String game = null;
                    switch (gameCounter) {
                        case 0:
                            game = "Shard [" + shardNumber + "] - " + jda.getGuilds().size() + " guilds";
                            break;
                        case 1:
                            game = "Serving " + InternalStats.collect().getGuilds() + " guilds";
                            break;
                        case 2:
                            game = "Music for " + Music.getMusicStats().getK() + " servers (on this shard)";
                            break;
                    }
                    jda.getPresence().setGame(Game.of(game, Ardent.gameUrl));

                    if (gameCounter == 2) gameCounter = 0;
                    else gameCounter++;

                }, 10, 25, TimeUnit.SECONDS);

                // On hold for a bit
                // PhraseUpdater phraseUpdater = new PhraseUpdater();
                // TranslationUpdater translationUpdater = new TranslationUpdater();

                WebsiteDaemon websiteDaemon = new WebsiteDaemon();
                executorService.scheduleAtFixedRate(websiteDaemon, 5, 15, TimeUnit.SECONDS);

                PermissionsDaemon permissionsDaemon = new PermissionsDaemon();
                executorService.scheduleAtFixedRate(permissionsDaemon, 0, 60, TimeUnit.SECONDS);

                GuildDaemon guildDaemon = new GuildDaemon();
                executorService.scheduleAtFixedRate(guildDaemon, 1, 5, TimeUnit.SECONDS);

                MuteDaemon muteDaemon = new MuteDaemon();
                executorService.scheduleAtFixedRate(muteDaemon, 1, 5, TimeUnit.SECONDS);

                Music.checkMusicConnections();

                Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
                Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF);
                Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                        .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.OFF);
            }
            catch (Exception ex) {
                new BotException(ex);
            }
        }, 5, TimeUnit.SECONDS);
    }

    public int getId() {
        return id;
    }
}
