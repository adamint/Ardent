package tk.ardentbot.utils.discord;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.translate.Language;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class MessageUtils {
    public static EmbedBuilder getDefaultEmbed(Guild guild, User author, BaseCommand baseCommand) {
        try {
            final Random random = new Random();
            final float hue = random.nextFloat();
            final float saturation = (random.nextInt(2000) + 1000) / 10000f;
            final float luminance = 2f;
            final Color color = Color.getHSBColor(hue, saturation, luminance);

            Language language = GuildUtils.getLanguage(guild);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(color);
            builder.setFooter(baseCommand.getTranslation("other", language, "requestedby").getTranslation().replace
                    ("{0}", author.getName() + "#" + author.getDiscriminator()), author.getAvatarUrl());
            return builder;
        }
        catch (Exception e) {
            new BotException(e);
            return null;
        }
    }

    public static String listWithCommas(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i));
            if (i < (strings.size() - 1)) sb.append(", ");
        }
        return sb.toString();
    }
}
