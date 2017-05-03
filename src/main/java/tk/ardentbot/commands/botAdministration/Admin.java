package tk.ardentbot.commands.botAdministration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.commands.music.GuildMusicManager;
import tk.ardentbot.commands.music.Music;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.main.ShardManager;
import tk.ardentbot.rethink.models.Staff;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.InternalStats;
import tk.ardentbot.utils.discord.UsageUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.main.Ardent.developers;
import static tk.ardentbot.main.Ardent.moderators;
import static tk.ardentbot.main.Ardent.translators;
import static tk.ardentbot.main.ShardManager.getShards;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Admin extends Command {
    private static int secondsWaitedForRestart = 0;

    public Admin(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void update(Command command, MessageChannel channel) throws Exception {
        channel.sendMessage("Updating now!").queue();
        for (Shard shard : getShards()) {
            for (Guild g : shard.jda.getGuilds()) {
                if (g.getAudioManager().isConnected()) {
                    GuildMusicManager manager = Music.getGuildAudioPlayer(g, null);
                    TextChannel ch = manager.scheduler.manager.getChannel();
                    if (ch == null) {
                        g.getPublicChannel().sendMessage("I'm restarting in **5** minutes for updates!").queue();
                    }
                    else {
                        ch.sendMessage("I'm restarting in **5** minutes for updates!").queue();
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
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (Ardent.developers.contains(user.getId())) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("update")) {
                    update(this, channel);
                }
                else if (args[1].equalsIgnoreCase("softupdate")) {
                    ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
                    ex.scheduleAtFixedRate(() -> {
                        long connections = InternalStats.collect().getMusicPlayers();
                        if (connections <= 1 || (secondsWaitedForRestart >= (60 * 60 * 3))) {
                            if (connections <= 3) {
                                try {
                                    update(Admin.this, channel);
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
                else if (args[1].equalsIgnoreCase("addpatron")) {
                    List<User> mentioned = message.getMentionedUsers();
                    if (mentioned.size() == 1) {
                        User u = mentioned.get(0);
                        String[] uArgs = message.getRawContent().split(" ");
                        if (uArgs.length == 4) {
                            String role = uArgs[3];
                            if (role.equalsIgnoreCase("tier1") || role.equalsIgnoreCase("tier2") || role.equalsIgnoreCase("tier3")) {
                                r.table("patrons").insert(r.hashMap("user_id", u.getId()).with("tier", role)).run(connection);
                                channel.sendMessage("done").queue();
                            }
                        }
                    }
                }
                else if (args[1].equalsIgnoreCase("addstaff")) {
                    Cursor<HashMap> staff = r.db("data").table("staff").run(connection);
                    staff.forEach(hashMap -> {
                        Staff staffMember = asPojo(hashMap, Staff.class);
                        List<User> mentioned = message.getMentionedUsers();
                        User u = mentioned.get(0);
                        String roleToAdd = args[3];
                        if (staffMember.getRole().equalsIgnoreCase("Developer")) {
                            if (!developers.contains(staffMember.getId())) developers.add(staffMember.getId());
                        }
                        else if (staffMember.getRole().equalsIgnoreCase("Moderator")) {
                            if (!moderators.contains(staffMember.getId())) moderators.add(staffMember.getId());
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
                            User mentioned1 = message.getMentionedUsers().get(0);
                            double amount = Double.parseDouble(raw[3]);
                            Profile.get(mentioned1).addMoney(amount);
                            sendTranslatedMessage("added " + amount + " to " + mentioned1.getName(), channel, user);
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("bad", channel, user);
                        }
                    }
                    else sendTranslatedMessage("bad", channel, user);
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
                else if (args[1].equals("shards")) {
                    // All credit to Kodehawa @ Mantaro
                    StringBuilder builder = new StringBuilder();
                    for (Shard shard : ShardManager.getShards()) {
                        builder.append(shard.jda.getShardInfo()).append(" | STATUS: ").append(shard.jda.getStatus()).append(" | U: ")
                                .append(shard.jda.getUsers().size()).append(" | G: ").append(shard.jda.getGuilds().size()).append(" | L: ")
                                .append(" | MC: ")
                                .append(shard.jda.getVoiceChannels().stream().filter
                                        (voiceChannel -> voiceChannel.getMembers().contains(voiceChannel.getGuild().getSelfMember()))
                                        .count());

                        if (shard.jda.getShardInfo() != null && shard.jda.getShardInfo().equals(guild.getJDA().getShardInfo())) {
                            builder.append(" <- CURRENT");
                        }

                        builder.append("\n");
                    }
                    channel.sendMessage(String.format("```prolog\n%s```", builder.toString())).queue();
                }
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
