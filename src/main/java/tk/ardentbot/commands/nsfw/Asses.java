package tk.ardentbot.commands.nsfw;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.translate.Language;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Asses extends Command {
    private final ArrayList<String> asses = new ArrayList<>();

    public Asses(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (asses.size() == 0) {
            addAsses("http://thechive.com/2017/04/11/im-in-the-business-of-booty-scoops-and-business-is-a-boomin-33-photos/");
            addAsses("http://thechive.com/2017/04/19/congrats-on-making-it-to-hump-day-here-is-your-reward-36-photos-2/");
            addAsses("http://thechive.com/2017/04/12/congrats-on-making-it-to-hump-day-here-is-your-reward-39-photos-3/");
            addAsses("http://thechive.com/2012/12/04/its-that-special-time-of-year-again-when-yoga-pants-run-rampant-65-photos/");
        }
        if (NSFW.canSendNSFW(user, channel, guild, language, this)) {
            sendTranslatedMessage(asses.get(new SecureRandom().nextInt(asses.size())), channel, user);
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }

    private void addAsses(String site) {
        try {
            Document assList = Jsoup.parse(new URL(site), 10000);
            Elements links = assList.getElementsByTag("img");
            links.forEach(img -> {
                String url = img.attr("src");
                if (url.contains(".jpeg")) asses.add(url);
            });
        }
        catch (IOException e) {
            new BotException(e);
        }
    }
}
