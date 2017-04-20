package tk.ardentbot.commands.fun;

import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.exception.GiphyException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.translate.Language;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.main.Ardent.globalExecutorService;


public class GIF extends Command {
    public static Giphy giphy = new Giphy("dc6zaTOxFJmzC");
    public static ArrayList<String> categories = new ArrayList<>();
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
        globalExecutorService.schedule(() -> cooldown.remove(author), 4, TimeUnit.SECONDS);
    }

    @Override
    public void setupSubcommands() {
    }
}
