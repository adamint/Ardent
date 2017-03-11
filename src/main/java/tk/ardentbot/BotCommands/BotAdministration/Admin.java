package tk.ardentbot.BotCommands.BotAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.UsageUtils;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static tk.ardentbot.BotCommands.BotInfo.Status.getVoiceConnections;
import static tk.ardentbot.Main.Ardent.ardent;

public class Admin extends Command {
    private static int secondsWaitedForRestart = 0;

    public Admin(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void update(Command command, Language language, MessageChannel channel) throws Exception {
        channel.sendMessage("Updating...").queue();
        for (Guild g : ardent.jda.getGuilds()) {
            if (g.getAudioManager().isConnected()) {
                g.getPublicChannel().sendMessage(command.getTranslation("music", language, "restartingfiveminutes")
                        .getTranslation()).queue();
            }
        }
        shutdown();
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
                            (), channel);
                }
                else if (args[1].equalsIgnoreCase("usage")) {
                    Map<Guild, Integer> guildsByUsage = UsageUtils.sortedGuildsByCommandUsage(10);
                    StringBuilder sb = new StringBuilder();
                    guildsByUsage.forEach((key, value) -> {
                        sb.append(key.getName() + " (" + key.getId() + ") : " + value + "\n");
                    });
                    sendTranslatedMessage(sb.toString(), channel);
                }
                else if (args[1].equalsIgnoreCase("announce")) {
                    String msg = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                            args[1] + " ", "");
                    if (ardent.announcement != null) ardent.sentAnnouncement.clear();
                    ardent.announcement = "** == Important Announcement ==**\n" + msg;
                    ardent.jda.getGuilds().forEach(g -> {
                        ardent.sentAnnouncement.put(g.getId(), false);
                    });
                }
                else if (args[1].equalsIgnoreCase("stop")) {
                    ardent.jda.shutdown(true);
                    System.exit(0);
                }
            }
        }
        else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission");
    }

    @Override
    public void setupSubcommands() {
    }
}
