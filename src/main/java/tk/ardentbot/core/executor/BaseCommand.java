package tk.ardentbot.core.executor;

import com.rethinkdb.net.Cursor;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.json.simple.JSONObject;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.core.models.CommandTranslation;
import tk.ardentbot.core.models.PhraseTranslation;
import tk.ardentbot.core.translation.LangFactory;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.core.translation.Translation;
import tk.ardentbot.core.translation.TranslationResponse;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.rethink.models.TranslationModel;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static tk.ardentbot.core.translation.LangFactory.english;
import static tk.ardentbot.main.Ardent.globalGson;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

/**
 * Abstracted from Command for possible future implementations (WebCommand)
 */
public abstract class BaseCommand {
    Command botCommand;
    String commandIdentifier;
    boolean privateChannelUsage = true;
    boolean guildUsage = true;
    Category category;
    private String[] aliases;
    private Shard shard;


    /**
     * Convert a HashMap into a POJO via GSON and Java's JSON library - use only with RethinkDB
     *
     * @param map    Map returned from a rethink query
     * @param tClass The POJO class
     * @param <T>    an object created from tClass
     * @return an instance of tClass
     */
    public static <T> T asPojo(HashMap map, Class<T> tClass) {
        return globalGson.fromJson(JSONObject.toJSONString(map), tClass);
    }

    public static <T> ArrayList<T> queryAsArrayList(Class<T> t, Object o) {
        Cursor<HashMap> cursor = (Cursor<HashMap>) o;
        ArrayList<T> tS = new ArrayList<T>();
        cursor.forEach(hashMap -> {
            tS.add(asPojo(hashMap, t));
        });
        return tS;
    }


    /**
     * Handles messages longer than 2000 characters
     *
     * @param translatedString already translated string to send
     * @param channel          channel to send to
     */
    public void sendTranslatedMessage(String translatedString, MessageChannel channel, User user) {
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
                sendFailed(user, false);
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
                                         String translationId, User user) {
        TranslationResponse response = null;
        try {
            response = getTranslation(translationCategory, language, translationId);
        }
        catch (Exception e) {
            new BotException(e);
        }
        if (response.isTranslationAvailable()) {
            sendTranslatedMessage(response.getTranslation(), channel, user);
        }
        else new BotException("There wasn't a translation for " + translationId + " in " + translationCategory);
    }

    public String replaceCommandIdAndPrefix(String message) {
        return message.replace(message.split(" ")[0] + " ", "");
    }

    public String[] asStringArray(List<Role> roles) {
        String[] strings = new String[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            strings[i] = roles.get(i).getName();
        }
        return strings;
    }

    public EmbedBuilder chooseFromList(String title, Guild guild, Language language, User user, BaseCommand command, String... options)
            throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, command);
        builder.setAuthor(title, Ardent.gameUrl, user.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append("**" + title + "**");
        for (int i = 0; i < options.length; i++) {
            description.append("\n#" + (i + 1) + " " + options[i]);
        }
        description.append("\n\n" + command.getTranslation("other", language, "selectoption").getTranslation());
        return builder.setDescription(description.toString());
    }

    public Message sendEmbed(EmbedBuilder embedBuilder, MessageChannel channel, User user, String... reactions) {
        try {
            Message message = channel.sendMessage(embedBuilder.build()).complete();
            for (String reaction : reactions) {
                message.addReaction(EmojiParser.parseToUnicode(reaction)).queue();
            }
            return message;
        }
        catch (PermissionException ex) {
            sendFailed(user, true);
        }
        return null;
    }

    /**
     * Tell a user that the bot failed to respond to their command
     *
     * @param user  user who sent the command
     * @param embed whether the bot attempted to send an embed or not
     */
    protected void sendFailed(User user, boolean embed) {
        if (user != null) {
            user.openPrivateChannel().queue(privateChannel -> {
                try {
                    if (!embed) {
                        privateChannel.sendMessage(getTranslation("other", LangFactory.english,
                                "nopermissionstotype").getTranslation()).queue();
                    }
                    else {
                        privateChannel.sendMessage(getTranslation("other", LangFactory.english,
                                "nopermissionstosendembeds").getTranslation()).queue();
                    }
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
        }
    }

    void sendRestricted(User user) {
        if (user != null) {
            user.openPrivateChannel().queue(privateChannel -> {
                try {
                    privateChannel.sendMessage(getTranslation("restrict", LangFactory.english,
                            "youareblocked").getTranslation()).queue();
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
        }
    }

    /**
     * Removes the amount of arguments supplied, because Adam was an idiot when he
     * designed the CommandFactory
     *
     * @param content      original message raw content
     * @param amountOfArgs args to remove, starting at 1
     * @return the edited message
     */
    public String replace(String content, int amountOfArgs) {
        String[] arrayed = content.split(" ");
        StringBuilder toReplace = new StringBuilder();
        for (int start = 0; start < amountOfArgs; start++) {
            toReplace.append(arrayed[start] + " ");
        }
        return content.replace(toReplace.toString(), "");
    }

    /**
     * Replace {0}, {1}, etc.. easily in a translation
     *
     * @param category     Command category the translation is from
     * @param language     The guild's language
     * @param identifier   The unique identifier of the translation
     * @param user         The user who sent the command
     * @param replacements An array of replacements to make
     * @throws Exception SQLException when retrieving translation
     */
    public void sendEditedTranslation(String category, Language language, String identifier, User user, MessageChannel channel, String...
            replacements) {
        String translation = null;
        try {
            translation = getTranslation(category, language, identifier).getTranslation();
        }
        catch (Exception e) {
            new BotException(e);
        }
        for (int i = 0; i < replacements.length; i++) {
            translation = translation.replace("{" + i + "}", replacements[i]);
        }
        sendTranslatedMessage(translation, channel, user);
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
        return currentSubcommand.orElseGet(() -> (language != english) ? getCmdTranslations(english) : null);
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
        TranslationResponse response;
        Cursor<HashMap> translations = r.db("data").table("translations").filter(r.hashMap("language", lang.getIdentifier())
                .with("command_identifier",
                        cmdName).with("id", id)).run(connection);
        if (!translations.hasNext()) {
            Cursor<HashMap> english = r.db("data").table("translations").filter(r.hashMap("language", "english").with
                    ("command_identifier",
                            cmdName).with("id", id)).run(connection);
            if (english.hasNext()) {
                TranslationModel translationModel = globalGson.fromJson(JSONObject.toJSONString(english.next()), TranslationModel.class);
                response = new TranslationResponse(translationModel.getTranslation(), lang, false, true, true);
            }
            else response = new TranslationResponse(null, lang, false, false, false);
            english.close();
        }
        else {
            TranslationModel translationModel = globalGson.fromJson(JSONObject.toJSONString(translations.next()), TranslationModel.class);
            response = new TranslationResponse(translationModel.getTranslation(), lang, true, true, true);
        }
        translations.close();
        return response;
    }

    /**
     * Get translations, via an array as compared to a list
     *
     * @param language
     * @param translations
     * @return
     * @throws Exception
     */
    public HashMap<Integer, TranslationResponse> getTranslations(Language language, Translation... translations) throws Exception {
        ArrayList<Translation> translationArrayList = new ArrayList<>();
        translationArrayList.addAll(Arrays.asList(translations));
        return getTranslations(language, translationArrayList);
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

    public Command getBotCommand() {
        return botCommand;
    }

    public ArrayList<BaseCommand> getCommandsInCategory(Category category) {
        return shard.factory.getBaseCommands().stream().filter(command -> command.getCategory() == category)
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
        if (o instanceof BaseCommand) {
            BaseCommand c = (BaseCommand) o;
            return this.commandIdentifier.equalsIgnoreCase(c.getCommandIdentifier());
        }
        else return false;
    }

    public Shard getShard() {
        return this.shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
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
