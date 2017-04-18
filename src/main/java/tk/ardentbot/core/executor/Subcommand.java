package tk.ardentbot.core.executor;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.models.SubcommandTranslation;
import tk.ardentbot.core.translation.Language;

import java.util.List;
import java.util.Optional;

import static tk.ardentbot.core.translation.LangFactory.english;

public abstract class Subcommand {
    private String identifier;
    private BaseCommand baseCommand;

    public Subcommand(BaseCommand baseCommand, String identifier) {
        this.identifier = identifier;
        this.baseCommand = baseCommand;
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
    public SubcommandTranslation getSubTranslations(Language language) {
        List<SubcommandTranslation> subcommandTranslations = language.getSubcommands(baseCommand);
        Optional<SubcommandTranslation> currentSubcommand = subcommandTranslations.stream()
                .filter(subcommandTranslation -> subcommandTranslation.getIdentifier()
                        .equals(identifier)).distinct().findFirst();
        return currentSubcommand.orElseGet(() -> (language != english) ? getSubTranslations(english) : null);
    }

    /**
     * Retrieves the translated name of the subcommand
     *
     * @param language guild language
     * @return a string representing the translation of the subcommand
     * @throws Exception
     */
    public String getName(Language language) throws Exception {
        SubcommandTranslation translations = getSubTranslations(language);
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
        SubcommandTranslation translations = getSubTranslations(language);
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
        SubcommandTranslation translations = getSubTranslations(language);
        return (translations != null) ? translations.getDescription() : "invalidsubcommand";
    }
}
