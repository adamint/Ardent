package tk.ardentbot.core.executor;

import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.translate.Language;

public abstract class Subcommand {
    @Getter
    private String syntax;
    @Getter
    private String description;
    @Getter
    private String[] aliases;

    public Subcommand(String syntax, String description, String... aliases) {
        this.syntax = syntax;
        this.description = description;
        this.aliases = aliases;
    }

    /**
     * Calls the overriden method when the BaseCommand has
     * identified the subcommand
     *
     * @param guild    The guild of the sent baseCommand
     * @param channel  Channel of the sent baseCommand
     * @param user     BaseCommand author
     * @param message  BaseCommand message
     * @param args     Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception
     */
    public abstract void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
            Exception;
}
