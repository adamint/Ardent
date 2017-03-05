package tk.ardentbot.Commands.BotAdministration;

import net.dv8tion.jda.core.entities.*;
import org.apache.commons.io.FileUtils;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Commands.Music.Music;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.UsageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static tk.ardentbot.Commands.BotInfo.Status.getVoiceConnections;
import static tk.ardentbot.Main.Ardent.*;

public class Admin extends BotCommand {
    private static int secondsWaitedForRestart = 0;
    public Admin(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void update(BotCommand command, Language language, MessageChannel channel) {
        channel.sendMessage("Updating...").queue();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Guild g : jda.getGuilds()) {
                    if (g.getAudioManager().isConnected()) {
                        TextChannel tch = g.getTextChannelById(Music.textChannels.get(g.getId()));
                            if (tch.canTalk()) {
                                try {
                                    tch.sendMessage(command.getTranslation("other", language, "restartmusic").getTranslation()).complete();
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                                break;
                            }
                    }
                }
                jda.getPresence().setGame(Game.of("- UPDATING! -", "https://www.ardentbot.tk"));
                shutdown();
                System.exit(0);
            }
        }, 5000);
    }

    private static void shutdown() {
        try {
            File jar = new File("/root/Ardent_main.jar");
            File ardentloc1 = new File("/root/Ardent/update/Ardent1/Ardent_main.jar");
            File ardentloc2 = new File("/root/Ardent/update/Ardent2/Ardent_main.jar");
            boolean useLoc1 = true;
            if (Admin.class.getProtectionDomain().getCodeSource().getLocation().getPath().contains("Ardent1")) {
                moveAndOverwrite(jar, ardentloc2);
                useLoc1 = false;
            }
            else {
                moveAndOverwrite(jar, ardentloc1);
            }
            if (useLoc1) Runtime.getRuntime().exec("java -jar /root/Ardent/update/Ardent1/Ardent_main.jar");
            else Runtime.getRuntime().exec("java -jar /root/Ardent/update/Ardent2/Ardent_main.jar");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void moveAndOverwrite(File source, File dest) throws IOException {
        File backup = getNonExistingTempFile(dest);
        FileUtils.copyFile(dest, backup);
        FileUtils.copyFile(source, dest);
        if (!source.delete()) {
            throw new IOException("Failed to delete " + source.getName());
        }
        if (!backup.delete()) {
            throw new IOException("Failed to delete " + backup.getName());
        }
    }

    private static File getNonExistingTempFile(File inputFile) {
        File tempFile = new File(inputFile.getParentFile(), inputFile.getName() + "_temp");
        if (tempFile.exists()) {
            return getNonExistingTempFile(tempFile);
        }
        else {
            return tempFile;
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
                                    update(Admin.this, language, channel);
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
                    if (announcement != null) sentAnnouncement.clear();
                    announcement = "** == Important Announcement ==**\n" + msg;
                    jda.getGuilds().forEach(g -> {
                        sentAnnouncement.put(g.getId(), false);
                    });
                }
            }
        }
        else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission");
    }

    @Override
    public void setupSubcommands() {
    }
}
