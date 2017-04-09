package tk.ardentbot.Core.CommandExecution;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.mashape.unirest.http.Unirest;
import com.rethinkdb.net.Cursor;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Models.CommandTranslation;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Rethink.Models.CommandModel;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.Models.RestrictedUser;
import tk.ardentbot.Utils.RPGUtils.EntityGuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class CommandFactory {
    private Shard shard;

    private HashMap<String, Long> commandUsages = new HashMap<>();

    private ArrayList<String> emojiCommandTags = new ArrayList<>();

    private ConcurrentArrayQueue<BaseCommand> baseCommands = new ConcurrentArrayQueue<>();
    private long messagesReceived = 0;
    private long commandsReceived = 0;

    /**
     * Schedules emoji command updates with 150 second intervals,
     * as emoji parsing doesn't otherwise work with the existing system
     */
    public CommandFactory(Shard shard) {
        this.shard = shard;
        shard.executorService.scheduleAtFixedRate(new EmojiCommandUpdater(), 1, 300, TimeUnit.SECONDS);
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
        for (BaseCommand cmd : baseCommands) {
            if (StringUtils.stripAccents(cmd.getCommandIdentifier()).equalsIgnoreCase(StringUtils.stripAccents
                    (botCommand.getCommandIdentifier())))
            {
                System.out.println("Multiple baseCommands cannot be registered under the same name. Ignoring new " +
                        "instance" +
                        ".\n" +
                        "Name: " + baseCommand.toString());
                return;
            }
        }
        botCommand.setupSubcommands();
        baseCommands.add(baseCommand);
        commandUsages.put(baseCommand.commandIdentifier, (long) 0);
    }

    /**
     * Handles generic message events, parses message content
     * and creates a new AsyncCommandExecutor that will execute the command
     *
     * @param event the MessageReceivedEvent to be handled
     * @throws Exception this will create a BotException
     */
    public void pass(MessageReceivedEvent event, Language language, String prefix) throws
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
                Language toChange = null;
                Command command = shard.help.botCommand;
                for (Language l : LangFactory.languages) {
                    if (l.getIdentifier().equalsIgnoreCase(mentionedContent)) toChange = l;
                }
                if (toChange != null) {
                    if (GuildUtils.hasManageServerPermission(guild.getMember(event.getAuthor()))) {
                        r.db("data").table("guilds").filter(r.hashMap("guild_id", guild.getId())).update(r.hashMap("language", toChange
                                .getIdentifier()));
                        command.sendRetrievedTranslation(channel, "language", LangFactory.getLanguage("english"),
                                "changedlanguage", user);
                        getShard().botLanguageData.set(guild, toChange.getIdentifier());
                    }
                    else command.sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
                }
                else {
                    if (mentionedContent.length() == 0) {
                        command.sendTranslatedMessage(command.getTranslation("other", language, "mentionedhelp")
                                .getTranslation()
                                .replace("{0}", GuildUtils.getPrefix(guild) +
                                        command.getName(language)), channel, user);
                    }
                    else {
                        if (!Ardent.disabledCommands.contains("cleverbot")) {
                            command.sendTranslatedMessage(Unirest.post("https://cleverbot.io/1.0/ask").field("user", Ardent.cleverbotUser)
                                    .field("key", Ardent.cleverbotKey).field("nick", "ardent").field("text", mentionedContent).asJson()
                                    .getBody()
                                    .getObject().getString("response"), channel, user);
                        }
                        else {
                            command.sendRetrievedTranslation(channel, "other", language, "disabledfeature", user);
                        }
                    }
                }
            }
            else {
                Queue<CommandTranslation> commandNames = language.getCommandTranslations();

                if (event.getAuthor().isBot()) return;
                if (channel instanceof PrivateChannel) {
                    channel.sendMessage("Private channel integration will be re-added soon, please type this command in a guild!").queue();
                }
                else {
                    final boolean[] ranCommand = {false};
                    String pre = StringEscapeUtils.escapeJava(prefix);
                    if (args[0].startsWith(pre)) {
                        args[0] = args[0].replaceFirst(pre, "");
                        Emoji emoji = EmojiManager.getByUnicode(args[0]);
                        if (emoji != null) {
                            emoji.getAliases().forEach(alias -> {
                                emojiCommandTags.forEach(f -> {
                                    String converted = f.replace(":", "").replace(" ", "");
                                    if (converted.equals(alias)) {
                                        args[0] = alias;
                                    }
                                });
                            });
                        }
                        commandNames.forEach(commandTranslation -> {
                            String translation = StringUtils.stripAccents(commandTranslation.getTranslation().replace
                                    (" ", "").replace(":", ""));
                            String identifier = commandTranslation.getIdentifier();
                            if (translation.equalsIgnoreCase(StringUtils.stripAccents(args[0]))) {
                                baseCommands.stream().filter(command -> command.getCommandIdentifier().equalsIgnoreCase
                                        (identifier)).forEach(command -> {
                                    try {
                                        command.botCommand.usages++;
                                        /*boolean beforeCmdFirst = false;
                                        int oldCommandAmount = Status.commandsByGuild.get(guild.getId());
                                        Status.commandsByGuild.replace(guild.getId(), oldCommandAmount,
                                                oldCommandAmount + 1);
                                        boolean afterCmdFirst = UsageUtils.isGuildFirstInCommands(guild);

                                        if (!beforeCmdFirst && afterCmdFirst) {
                                            command.botCommand.sendRetrievedTranslation(channel, "other",
                                                    language, "firstincommands", user);
                                        }*/
                                        if (!Ardent.disabledCommands.contains(command.getCommandIdentifier())) {
                                            EntityGuild entityGuild = EntityGuild.get(guild);
                                            for (RestrictedUser u : entityGuild.getRestrictedUsers()) {
                                                if (u.getUserId().equalsIgnoreCase(user.getId())) {
                                                    command.sendRestricted(user);
                                                    return;
                                                }
                                            }
                                            shard.executorService.execute(new AsyncCommandExecutor(command.botCommand,
                                                    guild, channel,
                                                    event.getAuthor(), message, args, GuildUtils.getLanguage(guild), user));
                                            commandsReceived++;
                                            ranCommand[0] = true;
                                            UserUtils.addMoney(user, 1);
                                        }
                                        else {
                                            command.sendRetrievedTranslation(channel, "other", language, "disabledfeature", user);
                                            ranCommand[0] = true;
                                        }
                                    }
                                    catch (Exception e) {
                                        new BotException(e);
                                    }
                                });
                            }
                        });
                    }
                    if (!ranCommand[0]) {
                        if (language != LangFactory.english) {
                            pass(event, LangFactory.english, prefix);
                        }
                        else if (!prefix.equalsIgnoreCase("/")) {
                            pass(event, language, "/");
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

    /**
     * Simple Runnable to update command names for emojis
     */
    private class EmojiCommandUpdater implements Runnable {
        @Override
        public void run() {
            emojiCommandTags.clear();
            Cursor<CommandModel> emojiCommands = r.db("data").table("commands").filter(r.hashMap("language", "emoji")).run(connection);
            emojiCommands.forEach(commandModel -> {
                emojiCommandTags.add(commandModel.getTranslation().replace("\\:", ""));
            });
        }
    }
}
