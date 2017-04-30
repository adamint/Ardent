package tk.ardentbot.main;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.gson.Gson;
import com.rethinkdb.net.Cursor;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.apache.commons.io.IOUtils;
import tk.ardentbot.commands.administration.*;
import tk.ardentbot.commands.antitroll.AdBlock;
import tk.ardentbot.commands.botAdministration.Admin;
import tk.ardentbot.commands.botAdministration.Eval;
import tk.ardentbot.commands.botAdministration.Request;
import tk.ardentbot.commands.botAdministration.Tweet;
import tk.ardentbot.commands.botinfo.*;
import tk.ardentbot.commands.fun.*;
import tk.ardentbot.commands.games.Blackjack;
import tk.ardentbot.commands.games.Trivia;
import tk.ardentbot.commands.guildinfo.*;
import tk.ardentbot.commands.music.GuildMusicManager;
import tk.ardentbot.commands.music.Music;
import tk.ardentbot.commands.music.Play;
import tk.ardentbot.commands.nsfw.Asses;
import tk.ardentbot.commands.nsfw.Feet;
import tk.ardentbot.commands.nsfw.NSFW;
import tk.ardentbot.commands.nsfw.Tits;
import tk.ardentbot.commands.rpg.*;
import tk.ardentbot.core.data.BotMuteData;
import tk.ardentbot.core.data.BotPrefixData;
import tk.ardentbot.core.events.*;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.core.executor.Category;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.CommandFactory;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.rethink.models.GuildModel;
import tk.ardentbot.rethink.models.RestrictedUserModel;
import tk.ardentbot.utils.models.RestrictedUser;
import tk.ardentbot.utils.rpg.EntityGuild;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.core.executor.BaseCommand.asPojo;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Shard {
    public boolean testingBot;
    public ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    public BotMuteData botMuteData;
    public BotPrefixData botPrefixData;
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public ChatterBot cleverBot;
    public CommandFactory factory;
    public JDA jda;
    public User bot;
    public Command help;
    public BaseCommand patreon;
    public BaseCommand request;
    public String url = "https://ardentbot.tk";
    public Gson gson = new Gson();
    @Getter
    @Setter
    private long LAST_EVENT;
    private int gameCounter = 0;
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
                try {
                    token = IOUtils.toString(new FileReader(new File("/root/Ardent/v2bottoken.key")));
                }
                catch (Exception e) {
                    token = IOUtils.toString(new
                            FileReader(new File("C:\\Users\\AMR\\Desktop\\Ardent\\v2bottoken.key")));
                }
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
        if (jda.getGuildById("260841592070340609") != null) {
            Ardent.botLogsShard = this;
        }

        executorService.schedule(() -> {
            try {
                // Register event listeners
                jda.addEventListener(new OnMessage());
                jda.addEventListener(new InteractiveOnMessage());
                jda.addEventListener(new Join());
                jda.addEventListener(new Leave());
                jda.addEventListener(new VoiceLeaveEvent());
                jda.addEventListener(new ReactionEvent());

                // Adding the "handler" for mutes
                botMuteData = new BotMuteData();

                // Adding the handler for prefixes
                botPrefixData = new BotPrefixData();

                // Adding the handler for languages

                factory = new CommandFactory(this);

                help = new Help(new BaseCommand.CommandSettings(true, true, Category.BOTINFO, "See command help", "help", "watH"));
                patreon = new Patreon(new BaseCommand.CommandSettings(true, true, Category.BOTINFO, "View Ardent's patreon", "patreon"));
                request = new Request(new BaseCommand.CommandSettings(true, true, Category.BOTADMINISTRATION, "Request a feature for " +
                        "Ardent", "request"));

                factory.registerCommand(new Tweet(new BaseCommand.CommandSettings(true, true, Category
                        .BOTADMINISTRATION, "Send tweets to the Ardent twitter", "tweet")));
                factory.registerCommand(new Admin(new BaseCommand.CommandSettings(true, true, Category
                        .BOTADMINISTRATION, "admin stuff", "admin")));
                factory.registerCommand(new Eval(new BaseCommand.CommandSettings(true, true, Category
                        .BOTADMINISTRATION, "moar admin stuff", "eval", "j")));
                factory.registerCommand(new Manage(new BaseCommand.CommandSettings(true, true, Category
                        .BOTADMINISTRATION, "Send tweets to the Ardent twitter", "manage")));
                factory.registerCommand(new Botname(new BaseCommand.CommandSettings(true, true, Category
                        .BOTADMINISTRATION, "Set Ardent's nickname", "botname")));
                factory.registerCommand(request);
                factory.registerCommand(new About(new BaseCommand.CommandSettings(true, true, Category
                        .BOTINFO, "See why Ardent was started and the awesome people who make it run", "about")));
                factory.registerCommand(new Website(new BaseCommand.CommandSettings(true, true, Category
                        .BOTINFO, "See our website", "website")));
                factory.registerCommand(new Invite(new BaseCommand.CommandSettings(true, true, Category
                        .BOTINFO, "Invite Ardent to your server", "invite", "inv")));
                factory.registerCommand(patreon);
                factory.registerCommand(new Status(new BaseCommand.CommandSettings(true, true, Category
                        .BOTINFO, "View the current status of Ardent", "status", "statut")));
                factory.registerCommand(new Ping(new BaseCommand.CommandSettings(true, true, Category
                        .BOTINFO, "Checks and displays the bot response time", "ping")).with(30));
                factory.registerCommand(help);
                factory.registerCommand(new Stats(new BaseCommand.CommandSettings(true, true, Category
                        .BOTINFO, "View interesting stats about Ardent", "stats")));

                factory.registerCommand(new Music(new BaseCommand.CommandSettings(false, true, Category.FUN,
                        "Play music from youtube, soundcloud, or even search for songs!", "music", "m", "moosic"), this));
                factory.registerCommand(new Play(new BaseCommand.CommandSettings(false, true, Category.FUN,
                        "Shortcut for /music play", "play", "p")));
                factory.registerCommand(new UD(new BaseCommand.CommandSettings(false, true, Category.FUN,
                        "Retrieves the urban dictionary definition for a word", "ud", "urban")));
                factory.registerCommand(new GIF(new BaseCommand.CommandSettings(false, true, Category.FUN,
                        "Sends funny gifs!", "gif")));
                factory.registerCommand(new FML(new BaseCommand.CommandSettings(false, true, Category.FUN,
                        "Feeling down? Someone's having a worse day. View a FML here!", "fml")));
                factory.registerCommand(new Fortune(new BaseCommand.CommandSettings(false, true, Category.FUN,
                        "Displays a random Unix fortune ^_^ good luck!", "fortune")));
                factory.registerCommand(new Yoda(new BaseCommand.CommandSettings(true, true, Category.FUN,
                        "Type something and make yourself sound like yoda!", "yoda")));
                factory.registerCommand(new EightBall(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Get responses from the omnipotent 8ball", "8ball")));
                factory.registerCommand(new Translate(new BaseCommand.CommandSettings
                        (true, true, Category.FUN, "Translate a sentence from the given language to a target language", "translate")));
                factory.registerCommand(new Define(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Get the definition of a word", "define")));
                factory.registerCommand(new Coinflip(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Flip a coin", "coinflip")));
                factory.registerCommand(new Roll(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Roll a die", "roll", "dice")));
                factory.registerCommand(new RandomNum(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Generate a random number", "randomnum")));
                factory.registerCommand(new GSearch(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Search google!", "google", "g")));
                factory.registerCommand(new Wiki(new BaseCommand.CommandSettings(true, true, Category
                        .FUN, "Search wikipedia", "wiki")));

                factory.registerCommand(new Prefix(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "View and change the bot prefix for your server!", "prefix")));
                factory.registerCommand(new Restrict(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Prevent a user from using Ardent commands through this command.", "restrict")));
                factory.registerCommand(new Iam(new BaseCommand.CommandSettings(false, true,
                        Category.GUILDADMINISTRATION, "Automatically gives users a set role when they type /iam role [set autorole name " +
                        "here]", "iam")));
                factory.registerCommand(new Setnickname(new BaseCommand.CommandSettings(false, true,
                        Category.GUILDADMINISTRATION, "Set peoples' nicknames!", "setnickname")));
                factory.registerCommand(new Prune(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Kicks users who haven't been active for the specified amount of days", "prune")));
                factory.registerCommand(new Kick(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Kick one or multiple users with just one command", "kick")));
                factory.registerCommand(new Ban(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Ban one or multiple users with just one command", "ban")));
                factory.registerCommand(new Clear(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Clear messages from the current channel you're in", "clear")));
                factory.registerCommand(new Roles(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Add or remove roles to & from users", "roles", "role")));
                factory.registerCommand(new DefaultRole(new BaseCommand.CommandSettings(false, true,
                        Category.GUILDADMINISTRATION, "Add a role that all users will be given when they join your server",
                        "defaultrole")));
                factory.registerCommand(new Unmute(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDADMINISTRATION, "Unmute users who have been muted!", "unmute")));
                factory.registerCommand(new Automessage(new BaseCommand.CommandSettings(false, true,
                        Category.GUILDADMINISTRATION, "Set an automated join & leave message for your server!", "automessage")));

                factory.registerCommand(new GuildInfo(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDINFO, "Displays server-specific settings and information", "serverinfo", "guildinfo")));
                factory.registerCommand(new ServerInfo(new BaseCommand.CommandSettings(false, true, Category
                        .GUILDINFO, "View server-specific information that your administrators have written!", "info")));
                factory.registerCommand(new Whois(new BaseCommand.CommandSettings(true, true, Category
                        .GUILDINFO, "Displays information about a user", "whois")));
                factory.registerCommand(new Roleinfo(new BaseCommand.CommandSettings(false, true,
                        Category.GUILDINFO, "View information about specific roles", "roleinfo", "ri")));

                factory.registerCommand(new Blackjack(new BaseCommand.CommandSettings(false, true, Category.RPG, "Play a game of " +
                        "blackjack, betting money!", "blackjack")));
                factory.registerCommand(new RPGMoney(new BaseCommand.CommandSettings(false, true, Category.RPG, "See how much money you " +
                        "have, or compare to your server and friends!", "money", "balance", "bal")));
                factory.registerCommand(new Pay(new BaseCommand.CommandSettings(false, true, Category.RPG, "Give some of your hard-earned" +
                        " money to someone else", "pay", "givemoney")));
                factory.registerCommand(new Bet(new BaseCommand.CommandSettings(false, true, Category.RPG, "Bet money in the hopes of " +
                        "winning more! 55% chance of winning", "bet")));
                factory.registerCommand(new Trivia(new BaseCommand.CommandSettings(false, true, Category.RPG, "Play trivia games and win" +
                        " money", "trivia")));
                factory.registerCommand(new Marry(new BaseCommand.CommandSettings(false, true, Category.RPG, "Found that special someone?" +
                        " Marry them and you'll both get money multiplier boosts and have a nice life ^_^", "marry", "propose")));
                factory.registerCommand(new Divorce(new BaseCommand.CommandSettings(false, true, Category.RPG, "Want a divorce? Get one " +
                        "using this command", "divorce")));
                factory.registerCommand(new Tinder(new BaseCommand.CommandSettings(false, true, Category.RPG, "Get matched with people " +
                        "you like :wink:", "tinder")));
                factory.registerCommand(new UserProfile(new BaseCommand.CommandSettings(true, true, Category
                        .RPG, "View your Ardent profile using this!", "profile")));
                factory.registerCommand(new Daily(new BaseCommand.CommandSettings(true, true, Category
                        .RPG, "Get a daily stipend of money", "daily")));

                factory.registerCommand(new NSFW(new BaseCommand.CommandSettings(false, true, Category.NSFW, "See and set the " +
                        "NSFW settings for your server :wink: ", "nsfw")));
                factory.registerCommand(new Asses(new BaseCommand.CommandSettings(false, true, Category.NSFW, "See some nice asses " +
                        ":kissing_heart: - NSFW", "ass")));
                factory.registerCommand(new Tits(new BaseCommand.CommandSettings(false, true, Category.NSFW, "See some bosoms - NSFW!",
                        "tits", "boobs")));
                factory.registerCommand(new Feet(new BaseCommand.CommandSettings(false, true, Category.NSFW, "Feet o.o", "feet")));

                factory.registerCommand(new AdBlock(new BaseCommand.CommandSettings(false, true, Category
                        .ANTI_TROLL, "Prevent users from advertising other servers", "adblock")));

                musicManagers = new HashMap<>();

                playerManager = new DefaultAudioPlayerManager();
                playerManager.useRemoteNodes(Ardent.node0Url + ":8080", Ardent.node1Url + ":8080");

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

                Cursor<HashMap> guildData = r.db("data").table("guilds").run(connection);
                guildData.forEach(hashMap -> {
                    GuildModel g = asPojo(hashMap, GuildModel.class);
                    botPrefixData.set(g.getGuild_id(), g.getPrefix());
                });
                guildData.close();

                Cursor<HashMap> restrictedData = r.db("data").table("restricted").run(connection);
                restrictedData.forEach(hashMap -> {
                    RestrictedUserModel r = asPojo(hashMap, RestrictedUserModel.class);
                    Guild temp = jda.getGuildById(r.getGuild_id());
                    if (temp != null) {
                        EntityGuild entityGuild = EntityGuild.get(temp);
                        entityGuild.addRestricted(new RestrictedUser(r.getUser_id(), r.getRestricter_id(), temp));
                    }
                });
                restrictedData.close();

                executorService.scheduleAtFixedRate(() -> {
                    String game = null;
                    switch (gameCounter) {
                        case 0:
                            game = "ardentbot.tk";
                            break;
                        case 1:
                            game = "music | /help";
                            break;
                        case 2:
                            game = "fun | try /bet!";
                            break;
                        case 3:
                            game = "data | share Ardent w/friends!";
                    }
                    if (game != null) {
                        jda.getPresence().setGame(Game.of(game, "https://twitch.tv/ "));
                    }

                    if (gameCounter == 3) gameCounter = 0;
                    else gameCounter++;

                }, 5, 25, TimeUnit.SECONDS);

                // On hold for a bit
                // PhraseUpdater phraseUpdater = new PhraseUpdater();
                // TranslationUpdater translationUpdater = new TranslationUpdater();
            }
            catch (Exception ex) {
                new BotException(ex);
            }
        }, 5, TimeUnit.SECONDS);
        cleverBot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "f5d922d97e345aa1");
    }

    public int getId() {
        return id;
    }
}
