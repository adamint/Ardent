package tk.ardentbot.Backend.Commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Backend.Models.CommandTranslation;
import tk.ardentbot.Backend.Models.PhraseTranslation;
import tk.ardentbot.Backend.Translation.LangFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.Translation;
import tk.ardentbot.Backend.Translation.TranslationResponse;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Main.Config;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static tk.ardentbot.Backend.Translation.LangFactory.english;

/**
 * Abstracted from BotCommand for possible future
 * implementations (WebCommand)
 */
public abstract class Command {
    BotCommand botCommand;
    String commandIdentifier;
    boolean privateChannelUsage = true;
    boolean guildUsage = true;
    Category category;

    /**
     * Handles messages longer than 2000 characters
     *
     * @param translatedString already translated string to send
     * @param channel          channel to send to
     */
    public void sendTranslatedMessage(String translatedString, MessageChannel channel) {
        try {
            if (translatedString.length() <= 2000) {
                channel.sendMessage(translatedString).queue();
            }
            else {
                for (int i = 0; i < translatedString.length(); i += 2000) {
                    if ((i + 2000) <= translatedString.length()) {
                        channel.sendMessage(translatedString.substring(i, i + 2000)).queue();
                    }
                    else {
                        channel.sendMessage(translatedString.substring(i, translatedString.length() - 1)).queue();
                    }
                }
            }
        }
        catch (PermissionException ex) {
            if (channel instanceof TextChannel) {
                TextChannel ch = (TextChannel) channel;
                sendFailed(ch, false);
            }
        }
    }

    /**
     * Sends a translated message for the supplied translation parameter
     *
     * @param channel             channel to send to
     * @param translationCategory the command identifier of the translation
     * @param language            the current language of the guild
     * @param translationId       the identifier of the translation
     * @throws Exception
     */
    public void sendRetrievedTranslation(MessageChannel channel, String translationCategory, Language language,
                                         String translationId) throws Exception {
        TranslationResponse response = getTranslation(translationCategory, language, translationId);
        if (response.isTranslationAvailable()) {
            sendTranslatedMessage(response.getTranslation(), channel);
        }
        else new BotException("There wasn't a translation for " + translationId + " in " + translationCategory);
    }

    public void sendEmbed(EmbedBuilder embedBuilder, MessageChannel channel) {
        try {
            channel.sendMessage(embedBuilder.build()).queue();
        }
        catch (PermissionException ex) {
            if (channel instanceof TextChannel) {
                TextChannel ch = (TextChannel) channel;
                sendFailed(ch, false);
            }
        }
    }

    /**
     * Tell a user that the bot failed to respond to their command
     *
     * @param textChannel channel the command was sent in
     * @param embed       whether the bot attempted to send an embed or not
     */
    private void sendFailed(TextChannel textChannel, boolean embed) {
        textChannel.getHistory().retrievePast(1).queue(messages -> {
            messages.get(0).getAuthor().openPrivateChannel().queue(privateChannel -> {
                try {
                    if (!embed) {
                        privateChannel.sendMessage(getTranslation("other", LangFactory.english,
                                "nopermissionstotype").getTranslation());
                    }
                    else {
                        privateChannel.sendMessage(getTranslation("other", LangFactory.english,
                                "nopermissionstosendembeds").getTranslation());
                    }
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
        });
    }

    /**
     * Retrieves the represented translations for this subcommand
     *
     * @param language specified language
     * @return SubcommandTranslation object containing translations
     * or null
     */
    private CommandTranslation getCmdTranslations(Language language) {
        Queue<CommandTranslation> commandTranslations = language.getCommandTranslations();
        Optional<CommandTranslation> currentSubcommand = commandTranslations.stream()
                .filter(subcommandTranslation -> subcommandTranslation.getIdentifier()
                        .equals(commandIdentifier)).distinct().findFirst();
        if (currentSubcommand.isPresent()) {
            return currentSubcommand.get();
        }
        else {
            return (language != english) ? getCmdTranslations(english) : null;
        }
    }

    /**
     * Retrieves the description of the current command
     * (needs reworking to not query the database each call)
     *
     * @param language the current language of the guild
     * @return the description of the current command
     * @throws Exception
     */
    public String getDescription(Language language) throws Exception {
        CommandTranslation translations = getCmdTranslations(language);
        return (translations != null) ? translations.getDescription() : "invalidcommand";
    }

    /**
     * Retrieves the name of the current command
     *
     * @param language the current language of the guild
     * @return the name of the current command
     * @throws Exception
     */
    public String getName(Language language) throws Exception {
        CommandTranslation translations = getCmdTranslations(language);
        return (translations != null) ? translations.getTranslation() : "invalidcommand";
    }

    /**
     * Returns a translation response representing the given translation
     * parameters. This tries to use the local cache instead of querying
     * the database, but queries the database if requested translations are
     * not found locally.
     *
     * @param cmdName the command identifier of the translation
     * @param lang    the current language of the guild
     * @param id      the identifier of the translation
     * @return the TranslationResponse representing this translation
     * @throws Exception
     */
    public TranslationResponse getTranslation(String cmdName, Language lang, String id) throws Exception {
        Queue<PhraseTranslation> phraseTranslations = lang.getPhraseTranslations();

        for (PhraseTranslation phraseTranslation : phraseTranslations) {
            if (phraseTranslation.getCommandIdentifier().equalsIgnoreCase(cmdName) && phraseTranslation.getId()
                    .equalsIgnoreCase(id))
            {
                return new TranslationResponse(phraseTranslation.getTranslation(), lang, true, false, true);
            }
        }
        return getTranslationDb(cmdName, lang, id);
    }

    /**
     * Returns a translation response representing the given translation
     * parameters. Queries the database.
     *
     * @param cmdName the command identifier of the translation
     * @param lang    the current language of the guild
     * @param id      the identifier of the translation
     * @return the TranslationResponse representing this translation
     * @throws Exception
     */
    private TranslationResponse getTranslationDb(String cmdName, Language lang, String id) throws Exception {
        TranslationResponse response = null;

        DatabaseAction translationRequest = new DatabaseAction("SELECT * FROM Translations WHERE CommandIdentifier=? " +
                "AND Language=? AND ID=?");
        translationRequest.set(cmdName).set(lang.getIdentifier()).set(id);
        ResultSet langTranslations = translationRequest.request();
        if (langTranslations.next()) {
            String translation = langTranslations.getString("Translation").replace("{newline}", "\n");
            response = new TranslationResponse(translation, lang, true, false, true);
        }
        else {
            DatabaseAction englishRequest = new DatabaseAction("SELECT * FROM Translations WHERE CommandIdentifier=? " +
                    "AND Language=? AND ID=?");
            translationRequest.set(cmdName).set("english").set(id);
            ResultSet englishSet = translationRequest.request();
            if (englishSet.next()) {
                String translation = englishSet.getString("Translation").replace("{newline}", "\n");
                response = new TranslationResponse(translation, lang, true, false, true);
            }
            else {
                response = new TranslationResponse(null, lang, false, false, false);
            }
            englishRequest.close();
        }
        translationRequest.close();
        return response;
    }

    /**
     * Returns the translation responses representing the given translations'
     * parameters. This tries to use the local cache instead of querying
     * the database, but queries the database if requested translations are
     * not found locally.
     *
     * @param language     the current language of the guild
     * @param translations a list of translations to request
     * @return the TranslationResponse representing this translation
     * @throws Exception
     */
    public HashMap<Integer, TranslationResponse> getTranslations(Language language, List<Translation> translations)
            throws Exception {
        HashMap<Integer, TranslationResponse> translationResponses = new HashMap<>();
        ConcurrentHashMap<Translation, Integer> originalPlaces = new ConcurrentHashMap<>();

        for (int i = 0; i < translations.size(); i++) {
            Translation translation = translations.get(i);
            originalPlaces.put(translation, i);
        }

        Queue<PhraseTranslation> phraseTranslations = language.getPhraseTranslations();
        Iterator<Translation> translationIterator = translations.iterator();
        while (translationIterator.hasNext()) {
            Translation translation = translationIterator.next();
            for (PhraseTranslation phraseTranslation : phraseTranslations) {
                if (phraseTranslation.getCommandIdentifier().equalsIgnoreCase(translation.getCommandId()) &&
                        phraseTranslation.getId().equalsIgnoreCase(translation.getId()))
                {
                    translationResponses.put(originalPlaces.get(translation), new TranslationResponse
                            (phraseTranslation.getTranslation(), language, true, true, true));
                    translationIterator.remove();

                }
            }
        }

        if (translations.size() > 0) {
            HashMap<Integer, TranslationResponse> dbResponses = getTranslationsDb(language, translations);
            dbResponses.forEach(translationResponses::put);
        }

        return translationResponses;
    }

    /**
     * Returns the translation responses representing the given translations'
     * parameters. This method queries the database.
     *
     * @param language     the current language of the guild
     * @param translations a list of translations to request
     * @return the TranslationResponse representing this translation
     * @throws Exception
     */
    private HashMap<Integer, TranslationResponse> getTranslationsDb(Language language, List<Translation>
            translations) throws Exception {
        ConcurrentHashMap<Translation, Integer> originalPlaces = new ConcurrentHashMap<>();
        for (int i = 0; i < translations.size(); i++) {
            Translation translation = translations.get(i);
            originalPlaces.put(translation, i);
        }
        HashMap<Integer, TranslationResponse> translationResponses = new HashMap<>();

        DatabaseAction langRequest = new DatabaseAction("SELECT * FROM Translations WHERE Language=?").set(language
                .getIdentifier());

        ResultSet langSet = langRequest.request();
        while (langSet.next()) {
            String commandIdentifier = langSet.getString("CommandIdentifier");
            String id = langSet.getString("ID");
            for (int i = 0; i < translations.size(); i++) {
                Translation translation = translations.get(i);
                if (translation.getCommandId().equalsIgnoreCase(commandIdentifier) && translation.getId()
                        .equalsIgnoreCase(id))
                {
                    String returnedTranslation = langSet.getString("Translation").replace("{newline}", "\n");
                    translationResponses.put(originalPlaces.get(translation), new TranslationResponse
                            (returnedTranslation, language, true, true, true));
                    translations.remove(translation);
                }
            }
        }
        if (translations.size() > 0) {
            DatabaseAction englishRequest = new DatabaseAction("SELECT * FROM Translations WHERE Language=?").set
                    ("english");

            ResultSet englishSet = englishRequest.request();
            while (englishSet.next()) {
                String commandIdentifier = englishSet.getString("CommandIdentifier");
                String id = englishSet.getString("ID");
                for (int i = 0; i < translations.size(); i++) {
                    Translation translation = translations.get(i);
                    if (translation.getCommandId().equalsIgnoreCase(commandIdentifier) && translation.getId()
                            .equalsIgnoreCase(id))
                    {
                        String returnedTranslation = englishSet.getString("Translation");
                        translationResponses.put(originalPlaces.get(translation), new TranslationResponse
                                (returnedTranslation, language, true, true, true));
                        translations.remove(translation);
                    }
                }
            }
            for (Translation translation : translations) {
                translationResponses.put(originalPlaces.get(translation), new TranslationResponse(null, language,
                        false, false, false));
            }
            englishRequest.close();
        }
        langRequest.close();
        return translationResponses;
    }

    public String getCommandIdentifier() {
        return commandIdentifier;
    }

    boolean isPrivateChannelUsage() {
        return privateChannelUsage;
    }

    public Category getCategory() {
        return category;
    }

    public BotCommand getBotCommand() {
        return botCommand;
    }

    public ArrayList<Command> getCommandsInCategory(Category category) {
        return Config.factory.getCommands().stream().filter(command -> command.getCategory() == category)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Compare commands based on identifiers
     *
     * @param o the command to compare
     * @return whether the identifiers are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Command) {
            Command c = (Command) o;
            if (this.commandIdentifier.equalsIgnoreCase(c.getCommandIdentifier())) return true;
            else return false;
        }
        else return false;
    }

    /**
     * Holds settings for each command
     */
    public static class CommandSettings {
        private String commandIdentifier;
        private boolean privateChannelUsage;
        private boolean guildUsage;
        private Category category;

        public CommandSettings(String commandIdentifier, boolean privateChannelUsage, boolean guildUsage, Category
                category) {
            this.commandIdentifier = commandIdentifier;
            this.privateChannelUsage = privateChannelUsage;
            this.guildUsage = guildUsage;
            this.category = category;
        }

        String getCommandIdentifier() {
            return commandIdentifier;
        }

        boolean isPrivateChannelUsage() {
            return privateChannelUsage;
        }

        boolean isGuildUsage() {
            return guildUsage;
        }

        Category getCategory() {
            return category;
        }
    }
}
