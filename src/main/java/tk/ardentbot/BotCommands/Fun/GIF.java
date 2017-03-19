package tk.ardentbot.BotCommands.Fun;

import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.exception.GiphyException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class GIF extends Command {
    public static Giphy giphy = new Giphy("dc6zaTOxFJmzC");
    public static ArrayList<String> categories = new ArrayList<>();
    static Timer timer = new Timer();
    ArrayList<User> cooldown = new ArrayList<>();

    public GIF(CommandSettings commandSettings) {
        super(commandSettings);
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

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        User author = message.getAuthor();
        if (cooldown.contains(author))
            sendRetrievedTranslation(channel, "other", language, "waitafewseconds", user);
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
}
