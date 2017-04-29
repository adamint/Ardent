package tk.ardentbot.core.executor;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.models.RestrictedUser;
import tk.ardentbot.utils.rpg.EntityGuild;

import java.util.HashMap;

public class CommandFactory {
    private Shard shard;

    private HashMap<String, Long> commandUsages = new HashMap<>();

    private ConcurrentArrayQueue<BaseCommand> baseCommands = new ConcurrentArrayQueue<>();
    private long messagesReceived = 0;
    private long commandsReceived = 0;

    /**
     * Schedules emoji command updates with 150 second intervals,
     * as emoji parsing doesn't otherwise work with the existing system
     */
    public CommandFactory(Shard shard) {
        this.shard = shard;
    }

    public static ChatterBotSession getBotSession(Guild guild) {
        return Ardent.cleverbots.get(guild.getId());
    }

    public ConcurrentArrayQueue<BaseCommand> getBaseCommands() {
        return baseCommands;
    }

    public int getLoadedCommandsAmount() {
        return baseCommands.size();
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getCommandsReceived() {
        return commandsReceived;
    }

    public HashMap<String, Long> getCommandUsages() {
        return commandUsages;
    }

    public Shard getShard() {
        return shard;
    }

    public void addCommandUsage(String identifier) {
        long old = commandUsages.get(identifier);
        commandUsages.replace(identifier, old, old + 1);
    }

    /**
     * Registers a baseCommand to the factory, provides a simple check for duplicates
     *
     * @param baseCommand baseCommand to be added
     * @throws Exception
     */
    public void registerCommand(BaseCommand baseCommand) throws Exception {
        baseCommand.setShard(shard);
        Command botCommand = baseCommand.botCommand;
        botCommand.setupSubcommands();
        baseCommands.add(baseCommand);
        commandUsages.put(baseCommand.getName(), (long) 0);
    }

    /**
     * Handles generic message events, parses message content
     * and creates a new AsyncCommandExecutor that will execute the command
     *
     * @param event the MessageReceivedEvent to be handled
     * @throws Exception this will create a BotException
     */
    public void pass(MessageReceivedEvent event, String prefix) throws
            Exception {
        try {
            User user = event.getAuthor();
            Message message = event.getMessage();
            MessageChannel channel = event.getChannel();
            String[] args = message.getContent().split(" ");
            Guild guild = event.getGuild();
            String rawContent = message.getRawContent();
            String mentionedContent = null;
            if (rawContent.startsWith("<@!" + shard.bot.getId() + ">")) {
                mentionedContent = rawContent.replace("<@!" + shard.bot.getId() + ">", "");
            }
            else if (rawContent.startsWith("<@" + shard.bot.getId() + ">")) {
                mentionedContent = rawContent.replace("<@" + shard.bot.getId() + ">", "");
            }
            if (mentionedContent != null) {
                mentionedContent = mentionedContent.replace(" ", "");
                if (mentionedContent.length() == 0) {
                    channel.sendMessage("Type @Ardent [msg] to talk to the bot\n" +
                            "FYI: The help command name is /help in your server").queue();
                }
                else {
                    if (!Ardent.disabledCommands.contains("cleverbot")) {
                        channel.sendMessage(Unirest.post("https://cleverbot.io/1.0/ask").field("user", Ardent.cleverbotUser)
                                .field("key", Ardent.cleverbotKey).field("nick", "ardent").field("text", mentionedContent).asJson()
                                .getBody().getObject().getString("response")).queue();
                    }
                    else {
                        channel.sendMessage("Cleverbot is currently disabled, sorry.").queue();
                    }
                }
            }
            else {
                if (event.getAuthor().isBot()) return;
                if (channel instanceof PrivateChannel) {
                    channel.sendMessage("Private channel integration will be re-added soon, please type this command in a guild!").queue();
                }
                else {
                    final boolean[] ranCommand = {false};
                    String pre = StringEscapeUtils.escapeJava(prefix);
                    if (args[0].startsWith(pre)) {
                        args[0] = args[0].replaceFirst(pre, "");
                        baseCommands.forEach(command -> {
                            if (command.getBotCommand().containsAlias(args[0])) {
                                command.botCommand.usages++;
                                if (!Ardent.disabledCommands.contains(command.getName())) {
                                    EntityGuild entityGuild = EntityGuild.get(guild);
                                    for (RestrictedUser u : entityGuild.getRestrictedUsers()) {
                                        if (u.getUserId().equalsIgnoreCase(user.getId())) {
                                            command.sendRestricted(user);
                                            return;
                                        }
                                    }
                                    shard.executorService.execute(new AsyncCommandExecutor(command.botCommand,
                                            guild, channel,
                                            event.getAuthor(), message, args, user));
                                    commandsReceived++;
                                    ranCommand[0] = true;
                                    UserUtils.addMoney(user, 1);
                                }
                                else {
                                    command.sendTranslatedMessage("Sorry, this command is currently disabled and will be re-enabled soon."
                                            , channel, user);
                                    ranCommand[0] = true;
                                }
                            }
                        });
                    }
                    if (!ranCommand[0]) {
                        if (!prefix.equalsIgnoreCase("/")) {
                            pass(event, "/");
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            if (ex instanceof PermissionException) {
                event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("I don't have permission to send a message in this channel, please " +
                            "tell a server administrator").queue();
                });
            }
            else {
                new BotException(ex);
            }
        }
    }

    public void incrementMessagesReceived() {
        messagesReceived += 1;
    }
}
