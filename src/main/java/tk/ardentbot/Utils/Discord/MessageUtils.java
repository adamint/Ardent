package tk.ardentbot.Utils.Discord;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.Translation.Language;

import java.awt.*;

public class MessageUtils {
    public static EmbedBuilder getDefaultEmbed(Guild guild, User author, BaseCommand baseCommand) throws Exception {
        Language language = GuildUtils.getLanguage(guild);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.ORANGE);
        builder.setFooter(baseCommand.getTranslation("other", language, "requestedby").getTranslation().replace
                ("{0}", author.getName() + "#" + author.getDiscriminator()), author.getAvatarUrl());
        return builder;
    }

}
