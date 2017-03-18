package tk.ardentbot.BotCommands.BotAdministration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.BotCommands.Music.GuildMusicManager;
import tk.ardentbot.BotCommands.Music.Music;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.UsageUtils;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.BotCommands.BotInfo.Status.getVoiceConnections;
import static tk.ardentbot.Main.ShardManager.getShards;

public class Admin extends Command {
    private static int secondsWaitedForRestart = 0;

    public Admin(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void update(Command command, Language language, MessageChannel channel) throws Exception {
        channel.sendMessage("Updating...").queue();
        for (Shard shard : getShards()) {
            for (Guild g : shard.jda.getGuilds()) {
                if (g.getAudioManager().isConnected()) {
                    GuildMusicManager manager = Music.getGuildAudioPlayer(g, null);
                    TextChannel ch = manager.scheduler.manager.getChannel();
                    if (ch == null) {
                        g.getPublicChannel().sendMessage(command.getTranslation("music", language,
                                "restartingfiveminutes")

                                .getTranslation()).queue();
                    }
                    else {
                        ch.sendMessage(command.getTranslation("music", language, "restartingfiveminutes")
                                .getTranslation()).queue();
                    }
                }
            }
            shard.executorService.schedule(() -> {
                shard.jda.getGuilds().stream().filter(g -> g.getAudioManager().isConnected()).forEach(g -> {
                    GuildMusicManager manager = Music.getGuildAudioPlayer(g, null);
                    TextChannel ch = manager.scheduler.manager.getChannel();
                    if (ch == null) {
                        g.getPublicChannel().sendMessage("Updating, I'll be online in a minute!").queue();
                    }
                    else {
                        ch.sendMessage("Updating, I'll be online in a minute!").queue();
                    }
                });
                shutdown();
            }, 4, TimeUnit.MINUTES);
        }
    }

    private static void shutdown() {
        try {
            System.exit(0);
            boolean useLoc1 = true;
            if (Admin.class.getProtectionDomain().getCodeSource().getLocation().getPath().contains("Ardent1")) {
                useLoc1 = false;
            }
            if (useLoc1) Runtime.getRuntime().exec("java -jar /root/Ardent/update/Ardent1/Ardent_main.jar");
            else Runtime.getRuntime().exec("java -jar /root/Ardent/update/Ardent2/Ardent_main.jar");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (Ardent.developers.contains(user.getId())) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("update")) {
                    update(this, language, channel);
                }
                else if (args[1].equalsIgnoreCase("softupdate")) {
                    Timer t = new Timer();
                    t.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (getVoiceConnections() <= 1 || (secondsWaitedForRestart >= (60 * 60 * 3))) {
                                if (getVoiceConnections() <= 3) {
                                    try {
                                        update(Admin.this, language, channel);
                                    }
                                    catch (Exception e) {
                                        new BotException(e);
                                    }
                                }
                            }
                            secondsWaitedForRestart += 5;
                        }
                    }, 5000, 5000);
                }
                else if (args[1].equalsIgnoreCase("getloc")) {
                    sendTranslatedMessage(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath
                            (), channel, user);
                }
                else if (args[1].equalsIgnoreCase("usage")) {
                    Map<Guild, Integer> guildsByUsage = UsageUtils.sortedGuildsByCommandUsage(10);
                    StringBuilder sb = new StringBuilder();
                    guildsByUsage.forEach((key, value) -> {
                        sb.append(key.getName() + " (" + key.getId() + ") : " + value + "\n");
                    });
                    sendTranslatedMessage(sb.toString(), channel, user);
                }
                else if (args[1].equalsIgnoreCase("announce")) {
                    String msg = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                            args[1] + " ", "");
                    if (Ardent.announcement != null) Ardent.sentAnnouncement.clear();
                    Ardent.announcement = "** == Important Announcement ==**\n" + msg;
                    for (Shard shard : getShards()) {
                        shard.jda.getGuilds().forEach(g -> {
                            Ardent.sentAnnouncement.put(g.getId(), false);
                        });
                    }
                }
                else if (args[1].equalsIgnoreCase("stop")) {
                    for (Shard shard : getShards()) {
                        shard.jda.shutdown(true);
                    }
                    System.exit(0);
                }
            }
        }
        else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission", user);
    }

    @Override
    public void setupSubcommands() {
    }
}
