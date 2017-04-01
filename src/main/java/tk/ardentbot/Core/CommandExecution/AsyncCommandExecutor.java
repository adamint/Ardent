package tk.ardentbot.Core.CommandExecution;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.concurrent.TimeUnit;

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
            Shard shard = GuildUtils.getShard(guild);
            if (Ardent.announcement != null) {
                if (!Ardent.sentAnnouncement.get(guild.getId())) {
                    Ardent.sentAnnouncement.replace(guild.getId(), true);
                    shard.executorService.schedule(() -> {
                        try {
                            command.sendTranslatedMessage(Ardent.announcement, channel, user);
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    }, 5, TimeUnit.SECONDS);
                }
            }

            command.getBotCommand().onUsage(guild, channel, author, message, args, language, null);
            shard.factory.addCommandUsage(command.getCommandIdentifier());
        }
        catch (Exception e) {
            new BotException(e);
        }
    }
}

