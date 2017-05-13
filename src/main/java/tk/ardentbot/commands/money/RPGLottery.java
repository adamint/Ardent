package tk.ardentbot.commands.money;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.Lottery;
import tk.ardentbot.utils.discord.MessageUtils;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class RPGLottery extends Command {
    public RPGLottery(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("View current lotteries", "current", "current", "view") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                ArrayList<Lottery> lotteries = queryAsArrayList(Lottery.class, r.table("lottery").filter(r -> r.g("inAction").eq(true))
                        .run(connection));
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user).setAuthor("Lotteries in Session", "https://ardentbot.tk", guild
                        .getSelfMember().getUser().getEffectiveAvatarUrl());
                StringBuilder description = new StringBuilder();
                lotteries.forEach(lottery -> {
                    description.append("**ID**: " + lottery.getId() + " | **Total Pot:** " + lottery.getPot() + " | **Ticket cost**: " +
                            lottery.getTicketCost() + " | " +
                            "**End Time**: " + Date.from(Instant.ofEpochSecond(lottery.getEndTime())).toLocaleString() + "\n");
                });
                description.append("\nUse /ticket buy ");
                builder.setDescription(description.toString());
                sendEmbed(builder, channel, user);
            }
        });
    }
}
