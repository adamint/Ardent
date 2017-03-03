package tk.ardentbot.Utils;

import tk.ardentbot.Backend.Commands.Command;
import tk.ardentbot.Backend.Translation.Language;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

public class MessageUtils {
    public static EmbedBuilder getDefaultEmbed(Guild guild, User author, Command command) throws Exception {
        Language language = GuildUtils.getLanguage(guild);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#48D1CC"));
        builder.setFooter(command.getTranslation("other", language, "requestedby").getTranslation().replace("{0}", author.getName() + "#" + author.getDiscriminator()), author.getAvatarUrl());
        return builder;
    }

}
