package tk.ardentbot.core.misc.web;

import com.google.gson.Gson;
import tk.ardentbot.core.executor.CommandFactory;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.misc.web.models.Command;
import tk.ardentbot.core.misc.web.models.Status;
import tk.ardentbot.core.misc.web.models.User;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.utils.discord.InternalStats;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Random;

import static spark.Spark.get;
import static spark.Spark.port;
import static tk.ardentbot.main.Ardent.shard0;

public class SparkServer {
    private static final Gson webGson = new Gson();

    /**
     * Sets up the web server and the endpoints
     */
    public static void setup() {
        port(666);
        get("/api/commands", (rq, rs) -> {
            CommandFactory factory = shard0.factory;
            ArrayList<Command> commands = new ArrayList<>();
            factory.getBaseCommands().forEach(command -> {
                try {
                    commands.add(new Command(command.getName(), command.getCategory(), command
                            .getDescription()));
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
            return shard0.gson.toJson(commands);
        });

        get("/api/staff", (rq, rs) -> {
            ArrayList<User> developers = new ArrayList<>();
            for (String id : Ardent.developers) {
                net.dv8tion.jda.core.entities.User user = shard0.jda.getUserById(id);
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl == null) avatarUrl = getDefaultImage();

                developers.add(new User(id, user.getName(), user.getDiscriminator(), avatarUrl, "developer"));
            }
            ArrayList<User> translators = new ArrayList<>();
            for (String id : Ardent.translators) {
                net.dv8tion.jda.core.entities.User user = shard0.jda.getUserById(id);
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl == null) avatarUrl = getDefaultImage();

                translators.add(new User(id, user.getName(), user.getDiscriminator(), avatarUrl, "translator"));
            }
            ArrayList<ArrayList<User>> staff = new ArrayList<>();
            staff.add(developers);
            staff.add(translators);
            return shard0.gson.toJson(staff);
        });
        get("/api/status", (rq, rs) -> {
            InternalStats internalStats = InternalStats.collect();
            return shard0.gson.toJson(new Status(internalStats.getMessagesReceived(),
                    internalStats.getCommandsReceived(), ManagementFactory.getRuntimeMXBean().getUptime() / 1000,
                    internalStats.getLoadedCommands(), internalStats.getGuilds(), internalStats.getUsers()));
        });
    }


    private static String getDefaultImage() {
        String avatarUrl = null;
        int random = new Random().nextInt(4);

        if (random == 0)
            avatarUrl = "https://i.gyazo.com/41c854b8f366402cd75a4450becd178a.jpg";
        else if (random == 1)
            avatarUrl = "https://i.gyazo.com/5b07238cf478a02c9565d28ed6bb2b1f.jpg";
        else if (random == 2)
            avatarUrl = "https://i.gyazo.com/65ab76aa4c70f3b7e85b1cfcc74370df.jpg";
        else if (random == 3)
            avatarUrl = "https://i.gyazo.com/249ad1d26af8b388ea3b42fc23f52daa.jpg";
        return avatarUrl;
    }
}
