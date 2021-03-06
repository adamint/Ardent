package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.discord.UserUtils;

import java.util.ArrayList;

public class About extends Command {
    public About(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
        builder.setAuthor("Ardent About", getShard().url, getShard().bot.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append("**What's this embed? ;O**\n\n")
                .append("Ardent was a small project started by Adam#9261 in December of 2016 to have some " +
                        "fun programming and play music.\n\n")
                .append("In March, we had our second developer, Akio, join us, and we've been blessed to have had an " +
                        "amazing community of staff and members, who are listed below.\n\n");
        String devs = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.developers));
        String moderators = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.moderators));
        String translators = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.translators));
        ArrayList<String> patronsList = new ArrayList<>();
        patronsList.addAll(Ardent.tierOnepatrons);
        patronsList.addAll(Ardent.tierTwopatrons);
        patronsList.addAll(Ardent.tierThreepatrons);

        String patrons = MessageUtils.listWithCommas(UserUtils.getNamesById(patronsList));

        description.append("**Developers**: *" + devs + "*\n")
                .append("**Moderators**: *" + moderators + "*\n")
                .append("**Translators**: *" + translators + "*\n")
                .append("**Patrons**: *" + patrons + "*");
        builder.setDescription(description);
        sendEmbed(builder, channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
