package tk.ardentbot.commands.botAdministration;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.commands.music.GuildMusicManager;
import tk.ardentbot.commands.music.Music;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.UsageUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.commands.botinfo.Status.getVoiceConnections;
import static tk.ardentbot.main.ShardManager.getShards;

public class Admin extends Command {
    private static int secondsWaitedForRestart = 0;

    public Admin(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void update(Command command, Language language, MessageChannel channel) throws Exception {
        channel.sendMessage("Updating now!").queue();
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
            Ardent.premiumProcess.destroy();
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
                    ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
                    ex.scheduleAtFixedRate(() -> {
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
                    }, 5, 5, TimeUnit.SECONDS);
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
                else if (args[1].equalsIgnoreCase("setgameurl")) {
                    Ardent.gameUrl = args[2];
                    sendTranslatedMessage("Updated the game URL", channel, user);
                }
                else if (args[1].equalsIgnoreCase("addmoney")) {
                    String[] raw = message.getRawContent().split(" ");
                    if (raw.length == 4) {
                        try {
                            User mentioned = message.getMentionedUsers().get(0);
                            double amount = Double.parseDouble(raw[3]);
                            Profile.get(mentioned).addMoney(amount);
                            sendTranslatedMessage("added " + amount + " to " + mentioned.getName(), channel, user);
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("bad", channel, user);
                        }
                    }
                    else sendTranslatedMessage("bad", channel, user);
                }
                else if (args[1].equalsIgnoreCase("test2")) {
                    interactiveOperation(language, channel, message, (returnedMessage) -> {
                        if (returnedMessage.getContent().equalsIgnoreCase("5")) {
                            sendTranslatedMessage("5", channel, user);
                        }
                    });
                }
                else if (args[1].equalsIgnoreCase("disable")) {
                    if (args.length == 2) {
                        StringBuilder disabledCommands = new StringBuilder();
                        disabledCommands.append("**Disabled Commands/Features:**\n");
                        Ardent.disabledCommands.forEach(s -> {
                            disabledCommands.append(" **>** ").append(s).append("\n");
                        });
                        sendTranslatedMessage(disabledCommands.toString(), channel, user);
                    }
                    else if (args.length == 4) {
                        try {
                            boolean disable = Boolean.parseBoolean(args[2]);
                            System.out.println(disable);
                            String identifier = args[3];
                            if (disable) {
                                if (Ardent.disabledCommands.contains(identifier))
                                    sendTranslatedMessage("This feature is already disabled", channel, user);
                                else {
                                    Ardent.disabledCommands.add(identifier);
                                    sendTranslatedMessage("disabled " + identifier, channel, user);
                                }
                            }
                            else {
                                if (!Ardent.disabledCommands.contains(identifier)) {
                                    sendTranslatedMessage("This feature is already enabled", channel, user);
                                }
                                else {
                                    Ardent.disabledCommands.remove(identifier);
                                    sendTranslatedMessage("enabled " + identifier, channel, user);
                                }
                            }
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("/admin restrict true/false command_identifier", channel, user);
                        }
                    }
                    else sendTranslatedMessage("/admin restrict true/false command_identifier", channel, user);
                }
            }
        }
        else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission", user);
    }

    @Override
    public void setupSubcommands() {
    }
}
