package tk.ardentbot.BotCommands.BotInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.Discord.UserUtils;

import static tk.ardentbot.Main.Ardent.ardent;

public class About extends Command {
    public About(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
        builder.setAuthor("Ardent About", ardent.url, ardent.bot.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append("**What's this?**\n\n")
                .append("Ardent was a small project started by Adam#9261 in December of 2016 to have some " +
                        "fun programming and play music.\n\n")
                .append("In March, we had our second developer, Akio, join us, and we've been blessed to have had an " +
                        "amazing community of staff and members, who are listed below.\n\n");
        String devs = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.developers));
        String moderators = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.moderators));
        String translators = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.translators));
        String patrons = MessageUtils.listWithCommas(UserUtils.getNamesById(Ardent.patrons));

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
