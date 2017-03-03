package tk.ardentbot.Commands.Fun;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.exception.GiphyException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class GIF extends BotCommand {
    public static Giphy giphy = new Giphy("dc6zaTOxFJmzC");
    public static ArrayList<String> categories = new ArrayList<>();
    ArrayList<User> cooldown = new ArrayList<>();
    static Timer timer = new Timer();

    public GIF(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        User author = message.getAuthor();
        if (cooldown.contains(author))
            sendRetrievedTranslation(channel, "other", language, "waitafewseconds");
        else {
            try {
                channel.sendMessage(giphy.searchRandom(randomMemeCategory()).getData().getImageUrl()).queue();
            }
            catch (GiphyException e) {
                e.printStackTrace();
            }
        }
        cooldown.add(author);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cooldown.remove(author);
            }
        }, 4000);

    }

    @Override
    public void setupSubcommands() {
    }

    public static void setupCategories() {
        categories.add("no");
        categories.add("funny");
        categories.add("cat");
        categories.add("puppy");
        categories.add("sherlock");
        categories.add("supernatural");
        categories.add("love");
        categories.add("happy");
    }

    private static String randomMemeCategory() {
        Random random = new Random();
        return categories.get(random.nextInt(categories.size()));
    }
}
