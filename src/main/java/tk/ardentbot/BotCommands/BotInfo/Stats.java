package tk.ardentbot.BotCommands.BotInfo;


import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Events.Join;
import tk.ardentbot.Core.Events.Leave;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Main.ShardManager;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.MapUtils;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Stats extends Command {
    private static final char ACTIVE_BLOCK = '\u2588';
    private static final char EMPTY_BLOCK = '\u200b';

    public Stats(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static String bar(int percentage) {
        if (percentage == 0) return null;
        int activeBlocks = Math.round((percentage * 15) / 100);
        int emptyBlocks = 15 - activeBlocks;

        StringBuilder bar = new StringBuilder();
        bar.append("`");
        for (int i = 0; i < activeBlocks; i++) bar.append(ACTIVE_BLOCK);
        for (int i = 0; i < emptyBlocks; i++) bar.append(' ');
        bar.append(EMPTY_BLOCK + "`");
        return bar.toString();
    }


    private static String generateGuild(int join, int leave) {
        int total = join + leave;
        StringBuilder layout = new StringBuilder();
        layout.append("Amount: " + total + "\n");
        if (total > 0) {
            int joinPercentage = Math.round(join * 100 / total);
            int leavePercentage = Math.round(leave * 100 / total);
            if (joinPercentage > 0) {
                layout.append(bar(joinPercentage) + " " + joinPercentage + "% **Joined** (" + join + ")\n");
            }
            if (leavePercentage > 0) {
                layout.append(bar(leavePercentage) + " " + leavePercentage + "% **Left** (" + leave + ")\n");
            }
        }
        else layout.append("No data for this period!");
        return layout.toString();
    }

    private static Map getCommandData(Shard[] shards) {
        HashMap<String, Long> finalized = new HashMap<>();
        for (Shard shard : shards) {
            HashMap<String, Long> localCmdUsage = shard.factory.getCommandUsages();
            localCmdUsage.forEach((key, value) -> {
                if (finalized.containsKey(key)) {
                    long old = finalized.get(key);
                    finalized.replace(key, old, (old + value));
                }
                else finalized.put(key, value);
            });
        }
        return MapUtils.sortByValue(finalized);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "commands") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                StringBuilder commandBars = new StringBuilder();

                Map<String, Long> commandsUsed = getCommandData(ShardManager.getShards());

                final int[] counter = {0};
                final int[] totalCommandsReceived = {0};
                commandsUsed.forEach((key, value) -> {
                    if (counter[0] < 7) totalCommandsReceived[0] += value;
                    counter[0]++;
                });
                counter[0] = 0;
                commandsUsed.forEach((key, value) -> {
                    if (counter[0] < 7) {
                        int percent = (int) (value * 100 / totalCommandsReceived[0]);
                        String bar = bar(percent);
                        if (bar != null) {
                            commandBars.append(bar + " " + percent + "% **" + key + "**\n");
                        }
                    }
                    counter[0]++;
                });
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Stats.this);
                builder.setAuthor("Command Statistics", getShard().url, getShard().bot.getAvatarUrl());
                builder.setColor(Color.GREEN);
                builder.setDescription("Command Usage\n" + commandBars.toString());
                sendEmbed(builder, channel, user);
            }
        });
        subcommands.add(new Subcommand(this, "guilds") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Stats.this);
                builder.setAuthor("Guild Statistics", getShard().url, getShard().bot.getAvatarUrl());
                builder.setColor(Color.GREEN);

                int lPH = 0;
                int lPD = 0;
                int lPS = 0;

                long epochSecond = Instant.now().getEpochSecond();
                long hBar = epochSecond - (60 * 60);
                long dBar = epochSecond - (60 * 60 * 24);
                long sBar = 0;
                for (Instant i : Leave.botLeaveEvents) {
                    if (i.getEpochSecond() >= hBar) lPH++;
                    if (i.getEpochSecond() >= dBar) lPD++;
                    if (i.getEpochSecond() >= sBar) lPS++; // Always true
                }
                int jPH = 0;
                int jPD = 0;
                int jPS = 0;
                for (Instant i : Join.botJoinEvents) {
                    if (i.getEpochSecond() >= hBar) jPH++;
                    if (i.getEpochSecond() >= dBar) jPD++;
                    if (i.getEpochSecond() >= sBar) jPS++; // Always true
                }
                builder.addField("Hourly", generateGuild(jPH, lPH), false);
                builder.addField("Daily", generateGuild(jPD, lPD), false);
                builder.addField("This Session", generateGuild(jPS, lPS), false);
                builder.addField("Guilds", String.valueOf(getShard().jda.getGuilds().size()), false);
                sendEmbed(builder, channel, user);
            }
        });
    }
}
