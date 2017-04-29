package tk.ardentbot.core.executor;

import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public abstract class Subcommand {
    @Getter
    private String syntax;
    @Getter
    private String description;
    private String[] aliases;

    public Subcommand(String description, String syntax, String... aliases) {
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
     * @throws Exception
     */
    public abstract void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception;

    public boolean containsAlias(String query) {
        for (String s : aliases) if (s.equalsIgnoreCase(query)) return true;
        return false;
    }
}
