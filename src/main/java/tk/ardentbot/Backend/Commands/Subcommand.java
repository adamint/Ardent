package tk.ardentbot.Backend.Commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Models.SubcommandTranslation;
import tk.ardentbot.Backend.Translation.Language;

import java.util.List;
import java.util.Optional;

import static tk.ardentbot.Backend.Translation.LangFactory.english;

public abstract class Subcommand {
    private String identifier;
    private Command command;

    public Subcommand(Command command, String identifier) {
        this.identifier = identifier;
        this.command = command;
    }

    /**
     * Calls the overriden method when the Command has
     * identified the subcommand
     *
     * @param guild    The guild of the sent command
     * @param channel  Channel of the sent command
     * @param user     Command author
     * @param message  Command message
     * @param args     Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception
     */
    public abstract void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception;

    /**
     * Gets the identifier of the subcommand
     *
     * @return the identifier
     */

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Retrieves the represented translations for this subcommand
     *
     * @param language specified language
     * @return SubcommandTranslation object containing translations
     * or null
     */
    public SubcommandTranslation getTranslations(Language language) {
        List<SubcommandTranslation> subcommandTranslations = language.getSubcommands(command);
        Optional<SubcommandTranslation> currentSubcommand = subcommandTranslations.stream()
                .filter(subcommandTranslation -> subcommandTranslation.getIdentifier()
                        .equals(identifier)).distinct().findFirst();
        if (currentSubcommand.isPresent()) {
            return currentSubcommand.get();
        }
        else {
            return (language != english) ? getTranslations(english) : null;
        }
    }

    /**
     * Retrieves the translated name of the subcommand
     *
     * @param language guild language
     * @return a string representing the translation of the subcommand
     * @throws Exception
     */
    public String getName(Language language) throws Exception {
        SubcommandTranslation translations = getTranslations(language);
        return (translations != null) ? translations.getTranslation() : "invalidsubcommand";
    }

    /**
     * Retrieves the translated syntax of the subcommand
     *
     * @param language guild language
     * @return a string representing the syntax of the subcommand
     * @throws Exception
     */
    String getSyntax(Language language) throws Exception {
        SubcommandTranslation translations = getTranslations(language);
        return (translations != null) ? translations.getSyntax() : "invalidsubcommand";
    }

    /**
     * Retrieves the translated description of the subcommand
     *
     * @param language guild language
     * @return a string representing the translated description of the subcommand
     * @throws Exception
     */
    String getDescription(Language language) throws Exception {
        SubcommandTranslation translations = getTranslations(language);
        return (translations != null) ? translations.getDescription() : "invalidsubcommand";
    }
}
