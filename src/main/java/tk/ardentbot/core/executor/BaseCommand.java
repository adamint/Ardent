package tk.ardentbot.core.executor;

import com.google.gson.Gson;
import com.rethinkdb.net.Cursor;
import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.json.simple.JSONObject;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstracted from Command for possible future implementations (WebCommand)
 */
public abstract class BaseCommand {
    @Getter
    private static final Gson staticGson = new Gson();
    public final Gson gson = new Gson();
    Command botCommand;
    boolean privateChannelUsage = true;
    boolean guildUsage = true;
    Category category;
    @Getter
    String description;
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
        return staticGson.fromJson(JSONObject.toJSONString(map), tClass);
    }

    public static <T> ArrayList<T> queryAsArrayList(Class<T> t, Object o) {
        Cursor<HashMap> cursor = (Cursor<HashMap>) o;
        ArrayList<T> tS = new ArrayList<T>();
        cursor.forEach(hashMap -> {
            tS.add(asPojo(hashMap, t));
        });
        return tS;
    }

    public static boolean eic(String s, String... k) {
        for (String kk : k) if (s.equalsIgnoreCase(kk)) return true;
        return false;
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

    public EmbedBuilder chooseFromList(String title, Guild guild, User user, BaseCommand command, String... options)
            throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
        builder.setAuthor(title, Ardent.gameUrl, user.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append("**" + title + "**");
        for (int i = 0; i < options.length; i++) {
            description.append("\n**#" + (i + 1) + "**: " + options[i]);
        }
        description.append("\n\n" + "Select the number of the option you want");
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
                        privateChannel.sendMessage("I don't have permission to type in this channel!").queue();
                    }
                    else {
                        privateChannel.sendMessage("I don't have permission to send embeds in this channel!").queue();
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
                    privateChannel.sendMessage("You're blocked from sending commands in that server!").queue();
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

    public void sendEditedTranslation(String translation, User user, MessageChannel channel, String...
            replacements) {
        for (int i = 0; i < replacements.length; i++) {
            translation = translation.replace("{" + i + "}", replacements[i]);
        }
        sendTranslatedMessage(translation, channel, user);
    }

    public String getName() {
        return aliases[0];
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
        return o instanceof BaseCommand && aliases[0].equals(((BaseCommand) o).aliases[0]);
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
        @Getter
        private String[] aliases;
        @Getter
        private boolean privateChannelUsage;
        @Getter
        private boolean guildUsage;
        @Getter
        private Category category;
        @Getter
        private String description;

        public CommandSettings(boolean privateChannelUsage, boolean guildUsage, Category
                category, String d, String... aliases) {
            this.aliases = aliases;
            this.privateChannelUsage = privateChannelUsage;
            this.guildUsage = guildUsage;
            this.description = d;
            this.category = category;
        }
    }
}
