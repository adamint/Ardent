package tk.ardentbot.Commands.BotInfo;


import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Events.Join;
import tk.ardentbot.Events.Leave;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.StringUtils;

import java.awt.*;
import java.time.Instant;

import static tk.ardentbot.Main.Ardent.ardent;

public class Stats extends BotCommand {
    public Stats(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static String generateGuild(int join, int leave) {
        int total = join + leave;
        double joinPercentage = 0;
        if (total > 0) joinPercentage = join / total;
        double leavePercentage = 0;
        if (total > 0) leavePercentage = leave / total;
        StringBuilder sb = new StringBuilder();
        sb.append("Amount: " + total + "\n");
        sb.append(StringUtils.bar(join / total, 15) + " " + ((int) joinPercentage * 100) + "% **Joined** (" + join +
                ")\n");
        sb.append(StringUtils.bar(leave / total, 15) + " " + ((int) leavePercentage * 100) + "% **Left** (" + leave +
                ")");
        return sb.toString();
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "guilds") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {

                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, Stats.this);
                builder.setAuthor("Guild Statistics", ardent.url, ardent.bot.getAvatarUrl());
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
                builder.addField("Guilds", String.valueOf(ardent.jda.getGuilds().size()), false);
                sendEmbed(builder, channel);
            }
        });
    }
}
