package tk.ardentbot.Main;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.BotData.BotLanguageData;
import tk.ardentbot.Backend.BotData.BotMuteData;
import tk.ardentbot.Backend.BotData.BotPrefixData;
import tk.ardentbot.Backend.Commands.Command;
import tk.ardentbot.Backend.Commands.CommandFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Commands.Music.GuildMusicManager;
import twitter4j.Twitter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Config {
    public static final boolean testingBot = true;
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);
    public static BotMuteData botMuteData;
    public static BotPrefixData botPrefixData;
    public static BotLanguageData botLanguageData;
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
    public static Twitter twitter;
    public static Command help;
    public static Command patreon;
    public static Command translateForArdent;
    public static Command getDevHelp;
    public static String url = "https://ardentbot.tk";
    public static ConcurrentHashMap<String, Boolean> sentAnnouncement = new ConcurrentHashMap<>();
    public static String announcement;
    public static int gameCounter = 0;
    public static int matureLanguages = 0;
}
