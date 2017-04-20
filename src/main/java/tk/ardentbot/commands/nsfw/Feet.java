package tk.ardentbot.commands.nsfw;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translate.Language;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Feet extends Command {
    public Feet(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static String findString(String question, String ua) {
        String finRes = "";
        try {
            String googleUrl = "https://www.google.com/search?tbm=isch&q=" + question.replace(",", "");
            Document doc1 = Jsoup.connect(googleUrl).userAgent(ua).timeout(10 * 1000).get();
            Element media = doc1.select("[data-src]").first();
            String finUrl = media.attr("abs:data-src");

            finRes = "<a href=\"http://images.google.com/search?tbm=isch&q=" + question + "\"><img src=\"" + finUrl.replace("&quot", "")
                    + "\" border=1/></a>";

        }
        catch (Exception e) {
            System.out.println(e);
        }

        return finRes;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (NSFW.canSendNSFW(user, channel, guild, language, this)) {
            Document document = Jsoup.parse(new URL("https://www.google" +
                    ".com/search?q=hot+feet&rlz=1C1CHBF_enUS724US724&espv=2&source=lnms&tbm=isch&sa=X&ved" +
                    "=0ahUKEwijkYfj06LTAhXl8YMKHUS9AL0Q_AUIBygC&biw=1536&bih=759&dpr=1.25#tbm=isch&q=hot+feet"), 10000);
            ArrayList<String> images = new ArrayList<>();
            for (Element e : document.getElementsByTag("img")) {
                String src = e.attr("data-src");
                if (!src.contains("#") && !src.contains("google.com/search?")) {
                    images.add(src);
                }
            }
            try {
                sendTranslatedMessage(images.get(new SecureRandom().nextInt(45)), channel, user);
            }
            catch (Exception ignored) {
            }
        }
    }

    @Override
    public void setupSubcommands() throws Exception {

    }

}
