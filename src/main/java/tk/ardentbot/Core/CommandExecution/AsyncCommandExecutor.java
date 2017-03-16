package tk.ardentbot.Core.CommandExecution;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.Language;

import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

class AsyncCommandExecutor implements Runnable {

    private Command command;
    private Guild guild;
    private MessageChannel channel;
    private User author;
    private Message message;
    private String[] args;
    private Language language;
    private User user;

    AsyncCommandExecutor(Command command, Guild guild, MessageChannel channel, User author, Message message, String[]
            args, Language language, User user) {
        this.command = command;
        this.guild = guild;
        this.channel = channel;
        this.author = author;
        this.message = message;
        this.args = args;
        this.language = language;
        this.user = user;
    }

    /**
     * Send the announcement if one exists, else run
     * the command
     */
    @Override
    public void run() {
        try {
            if (ardent.announcement != null) {
                if (!ardent.sentAnnouncement.get(guild.getId())) {
                    ardent.sentAnnouncement.replace(guild.getId(), true);
                    ardent.executorService.schedule(() -> {
                        try {
                            command.sendTranslatedMessage(ardent.announcement, channel, user);
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    }, 5, TimeUnit.SECONDS);
                }
            }

            command.getBotCommand().onUsage(guild, channel, author, message, args, language, null);
            ardent.factory.addCommandUsage(command.getCommandIdentifier());
        }
        catch (Exception e) {
            new BotException(e);
        }
    }
}

