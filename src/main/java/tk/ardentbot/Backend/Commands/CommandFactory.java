package tk.ardentbot.Backend.Commands;

import com.google.code.chatterbotapi.ChatterBotSession;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import tk.ardentbot.Backend.Models.CommandTranslation;
import tk.ardentbot.Backend.Translation.LangFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Commands.BotInfo.Status;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.UsageUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.*;

public class CommandFactory {
    private ArrayList<String> emojiCommandTags = new ArrayList<>();

    private ConcurrentArrayQueue<Command> commands = new ConcurrentArrayQueue<>();
    private long messagesReceived = 0;
    private long commandsReceived = 0;

    /**
     * Schedules emoji command updates with 150 second intervals,
     * as emoji parsing doesn't otherwise work with the existing system
     */
    public CommandFactory() {
        executorService.scheduleAtFixedRate(new EmojiCommandUpdater(), 1, 150, TimeUnit.SECONDS);
    }

    public static ChatterBotSession getBotSession(Guild guild) {
        return Ardent.cleverbots.get(guild.getId());
    }

    public ConcurrentArrayQueue<Command> getCommands() {
        return commands;
    }

    public int getLoadedCommandsAmount() {
        return commands.size();
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getCommandsReceived() {
        return commandsReceived;
    }

    /**
     * Registers a command to the factory, provides a simple check for duplicates
     *
     * @param command command to be added
     * @throws Exception
     */
    public void registerCommand(Command command) throws Exception {
        BotCommand botCommand = command.botCommand;
        for (Command cmd : commands) {
            if (StringUtils.stripAccents(cmd.getCommandIdentifier()).equalsIgnoreCase(StringUtils.stripAccents
                    (botCommand.getCommandIdentifier())))
            {
                System.out.println("Multiple commands cannot be registered under the same name. Ignoring new instance" +
                        ".\n" +
                        "Name: " + command.toString());
                return;
            }
        }
        botCommand.setupSubcommands();
        commands.add(command);
        System.out.println("Successfully registered " + command.toString());
    }

    /**
     * Handles generic message events, parses message content
     * and creates a new Runner that will execute the command
     *
     * @param event the MessageReceivedEvent to be handled
     * @throws Exception this will create a BotException
     */
    public void pass(MessageReceivedEvent event) throws Exception {
        try {
            Message message = event.getMessage();
            MessageChannel channel = event.getChannel();
            String[] args = message.getContent().split(" ");
            Guild guild = event.getGuild();
            Language language = GuildUtils.getLanguage(guild);
            if (message.getRawContent().startsWith(ardent.getAsMention())) {
                BotCommand cmd = help.botCommand;
                if (message.getRawContent().replace(ardent.getAsMention(), "").length() == 0) {
                    cmd.sendTranslatedMessage(cmd.getTranslation("other", language, "mentionedhelp").getTranslation()
                            .replace("{0}", GuildUtils.getPrefix(guild) +
                                    cmd.getName(language)), channel);
                }
                else {
                    if (guild != null) {
                        if (message.getRawContent().equalsIgnoreCase(ardent.getAsMention() + " english")) {
                            if (GuildUtils.hasManageServerPermission(guild.getMember(event.getAuthor()))) {
                                DatabaseAction updateLanguage = new DatabaseAction("UPDATE Guilds SET Language=? " +
                                        "WHERE" +
                                        " GuildID=?").set("english").set(guild.getId());
                                updateLanguage.update();
                                cmd.sendRetrievedTranslation(channel, "language", LangFactory.getLanguage("english"),
                                        "changedlanguage");
                            }
                            else cmd.sendRetrievedTranslation(channel, "other", language, "needmanageserver");
                        }
                        // On hold until I can find a suitable pandorabot or other chatbot api
                        /* else {
                            String query = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ",
                            "");
                            ChatterBotSession session = getBotSession(guild);
                            cmd.sendTranslatedMessage(session.think(query), channel);
                        }*/
                    }
                }
            }
            else {
                Queue<CommandTranslation> commandNames = language.getCommandTranslations();
                if (event.getAuthor().isBot()) return;
                if (channel instanceof PrivateChannel) {
                    if (args[0].startsWith("/")) {
                        args[0] = args[0].replace("/", "");
                        commandNames.forEach(commandTranslation -> {
                            String translation = commandTranslation.getTranslation();
                            String identifier = commandTranslation.getIdentifier();
                            if (StringUtils.stripAccents(translation).equalsIgnoreCase(StringUtils.stripAccents
                                    (args[0])))
                            {
                                for (Command command : commands) {
                                    if (StringUtils.stripAccents(command.getCommandIdentifier()).equalsIgnoreCase
                                            (StringUtils.stripAccents(identifier)))
                                    {
                                        try {
                                            if (command.isPrivateChannelUsage()) {
                                                command.botCommand.usages++;
                                                executorService.execute(new Runner(command.botCommand, guild,
                                                        channel, event.getAuthor(), message, args, language));
                                            }
                                            else {
                                                command.sendRetrievedTranslation(channel, "other", language,
                                                        "notavailableinprivatechannel");
                                            }
                                            commandsReceived++;
                                        }
                                        catch (Exception e) {
                                            new BotException(e);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
                else {
                    String prefix = GuildUtils.getPrefix(guild);
                    if (args[0].startsWith(prefix)) {
                        args[0] = args[0].replaceFirst(prefix, "");
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
                            String translation = commandTranslation.getTranslation().replace(" ", "").replace(":", "");
                            String identifier = commandTranslation.getIdentifier();
                            if (translation.equalsIgnoreCase(args[0])) {
                                commands.stream().filter(command -> command.getCommandIdentifier().equalsIgnoreCase
                                        (identifier)).forEach(command -> {
                                    try {
                                        command.botCommand.usages++;
                                        boolean beforeCmdFirst = UsageUtils.isGuildFirstInCommands(guild);
                                        int oldCommandAmount = Status.commandsByGuild.get(guild.getId());
                                        Status.commandsByGuild.replace(guild.getId(), oldCommandAmount,
                                                oldCommandAmount + 1);
                                        boolean afterCmdFirst = UsageUtils.isGuildFirstInCommands(guild);

                                        if (!beforeCmdFirst && afterCmdFirst) {
                                            command.botCommand.sendRetrievedTranslation(channel, "other",
                                                    language, "firstincommands");
                                        }

                                        executorService.execute(new Runner(command.botCommand, guild, channel,
                                                event.getAuthor(), message, args, language));
                                        commandsReceived++;

                                        new DatabaseAction("INSERT INTO CommandsReceived " +
                                                "VALUES (?,?,?,?)").set(guild.getId()).set(event.getAuthor().getId())
                                                .set(command.getCommandIdentifier()).set(Timestamp.from(Instant.now()
                                        )).update();
                                    }
                                    catch (Exception e) {
                                        new BotException(e);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }
        catch (Exception ex) {
            new BotException(ex);
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
            try {
                Statement statement = conn.createStatement();
                ResultSet emojis = statement.executeQuery("SELECT * FROM Commands WHERE Language='emoji'");
                while (emojis.next()) {
                    emojiCommandTags.add(emojis.getString("Translation").replace("\\:", ""));
                }
                emojis.close();
                statement.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
