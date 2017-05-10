package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.commands.music.Music;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.InternalStats;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.javaAdditions.Pair;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import static tk.ardentbot.main.ShardManager.getShards;

public class Status extends Command {
    public static ConcurrentHashMap<String, Integer> commandsByGuild = new ConcurrentHashMap<>();

    public Status(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static int getUserAmount() {
        int amount = 0;
        for (Shard shard : getShards()) {
            amount += shard.jda.getUsers().size();
        }
        return amount;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        InternalStats internalStats = InternalStats.collect();

        StringBuilder uptime = new StringBuilder();
        long secondsUptime = internalStats.getUptime();
        long days = secondsUptime / 86400;
        long hours = (secondsUptime / 3600) % 24;
        long minutes = (secondsUptime / 60) % 60;
        long seconds = secondsUptime % 60;
        if (days > 0) uptime.append(String.valueOf(days) + " day(s), ");
        if (hours > 0) uptime.append(String.valueOf(hours) + " hour(s), ");
        if (minutes > 0) uptime.append(String.valueOf(minutes) + "minute(s), ");
        uptime.append(String.valueOf(seconds) + " second(s)");

        DecimalFormat formatter = new DecimalFormat("#,###");
        String cmds = formatter.format(internalStats.getCommandsReceived());

        Pair<Integer, Integer> musicStats = Music.getMusicStats();
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(user);

        embedBuilder.setAuthor("Ardent Status", "https://ardentbot.tk", shard.bot
                .getAvatarUrl());
        embedBuilder.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer.png");

        embedBuilder.addField("Loaded Commands", String.valueOf(shard.factory
                .getLoadedCommandsAmount()), true);

        embedBuilder.addField("Received Messages", String.valueOf(internalStats.getMessagesReceived
                ()), true);
        embedBuilder.addField("Commands Received", cmds, true);

        embedBuilder.addField("Servers", String.valueOf(internalStats.getGuilds()), true);
        embedBuilder.addField("Music Players", String.valueOf(musicStats.getK()), true);

        embedBuilder.addField("Queue Length", String.valueOf(musicStats.getV()), true);
        embedBuilder.addField("CPU Usage", internalStats.getCpu_usage() + "%", true);

        embedBuilder.addField("RAM Usage", internalStats.getUsed_ram() + " / " + internalStats.getTotal_ram() + " MB", true);
        embedBuilder.addField("Uptime", uptime.toString(), true);

        embedBuilder.addField("Website", "https://ardentbot.tk", true);
        embedBuilder.addField("Get Help", "https://ardentbot.tk/guild", true);

        sendEmbed(embedBuilder, channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
