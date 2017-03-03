package Backend.Commands;

import Backend.Translation.Language;
import Bot.BotException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.TimeUnit;

import static Main.Ardent.*;

class Runner implements Runnable {

    private BotCommand command;
    private Guild guild;
    private MessageChannel channel;
    private User author;
    private Message message;
    private String[] args;
    private Language language;

    Runner(BotCommand command, Guild guild, MessageChannel channel, User author, Message message, String[] args, Language language) {
        this.command = command;
        this.guild = guild;
        this.channel = channel;
        this.author = author;
        this.message = message;
        this.args = args;
        this.language = language;
    }

    /**
     * Send the announcement if one exists, else run
     * the command
     */
    @Override
    public void run() {
        try {
            if (announcement != null) {
                if (!sentAnnouncement.get(guild.getId())) {
                    sentAnnouncement.replace(guild.getId(), true);

                    command.sendTranslatedMessage(announcement, channel);

                    executorService.schedule(() -> {
                        try {
                            command.getBotCommand().onUsage(guild, channel, author, message, args, language);
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    }, 1, TimeUnit.SECONDS);
                }
                else {
                    command.getBotCommand().onUsage(guild, channel, author, message, args, language);
                }
            }
            else {
                command.getBotCommand().onUsage(guild, channel, author, message, args, language);
            }
        }
        catch (Exception e) {
            new BotException(e);
        }
    }
}

