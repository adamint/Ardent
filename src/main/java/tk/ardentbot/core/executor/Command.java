package tk.ardentbot.core.executor;

import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import tk.ardentbot.core.events.ReactionEvent;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.models.SubcommandTranslation;
import tk.ardentbot.core.translate.LangFactory;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static tk.ardentbot.core.events.InteractiveOnMessage.lastMessages;
import static tk.ardentbot.core.events.InteractiveOnMessage.queuedInteractives;

public abstract class Command extends BaseCommand {
    public int usages = 0;
    public ArrayList<Subcommand> subcommands = new ArrayList<>();
    @Getter
    private String[] aliases;

    /**
     * Instantiates a new Command
     *
     * @param commandSettings settings to instantiate the command with
     */
    public Command(@NonNull CommandSettings commandSettings) {
        this.privateChannelUsage = commandSettings.isPrivateChannelUsage();
        this.guildUsage = commandSettings.isGuildUsage();
        this.category = commandSettings.getCategory();
        this.description = commandSettings.getDescription();
        this.aliases = commandSettings.getAliases();
    }

    public static void interactiveReaction(MessageChannel channel, Message message, User user, int seconds,
                                           Consumer<MessageReaction> function) {
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        final int interval = 50;
        final int[] ranFor = {0};
        ex.scheduleAtFixedRate(() -> {
            if (ranFor[0] > 10000) {
                channel.sendMessage("Cancelled your reaction event because you didn't respond in time!").queue();
                ex.shutdown();
            }
            else {
                ranFor[0] += interval;
                for (Map.Entry<String, MessageReactionAddEvent> current : ReactionEvent.reactionEvents.entrySet()) {
                    String channelId = current.getKey();
                    MessageReactionAddEvent event = current.getValue();
                    if (channelId.equals(channel.getId())) {
                        if (event.getMessageId().equals(message.getId()) && event.getUser().getId().equals(user.getId())) {
                            function.accept(event.getReaction());
                            ex.shutdown();
                        }
                    }

                }
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
    }

    public static void longInteractiveOperation(MessageChannel channel, Message message, User user, int seconds,
                                                Consumer<Message> function) {
        if (channel instanceof TextChannel) {
            queuedInteractives.put(message.getId(), user.getId());
            Ardent.globalExecutorService.execute(() -> dispatchInteractiveEvent(message.getCreationTime(), (TextChannel) channel,
                    message, user, function, seconds * 1000, false));
        }
    }

    public static boolean longInteractiveOperation(MessageChannel channel, Message message, int seconds,
                                                   Consumer<Message> function) {
        final boolean[] succeeded = {false};
        if (channel instanceof TextChannel) {
            queuedInteractives.put(message.getId(), message.getAuthor().getId());
            Ardent.globalExecutorService.execute(() -> succeeded[0] = dispatchInteractiveEvent(message.getCreationTime(), (TextChannel)
                            channel,
                    message, function, seconds * 1000, true));
            return succeeded[0];
        }
        return false;
    }

    public static boolean interactiveOperation(MessageChannel channel, Message message, Consumer<Message> function) {
        final boolean[] succeeded = {false};
        if (channel instanceof TextChannel) {
            queuedInteractives.put(message.getId(), message.getAuthor().getId());
            Ardent.globalExecutorService.execute(() -> {
                succeeded[0] = dispatchInteractiveEvent(message.getCreationTime(), (TextChannel) channel,
                        message, function, 10000, true);
            });
            return succeeded[0];
        }
        return false;
    }

    private static boolean dispatchInteractiveEvent(OffsetDateTime creationTime, TextChannel channel, Message message, User user,
                                                    Consumer<Message> function, int time, boolean sendMessage) {
        final boolean[] success = {false};
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        final int interval = 50;
        final int[] ranFor = {0};
        ex.scheduleAtFixedRate(() -> {
            if (ranFor[0] >= time) {
                try {
                    if (sendMessage) {
                        if (time >= 15000) {
                            channel.sendMessage("Cancelled your interactive operation because you didn't respond within 15 seconds!")
                                    .queue();
                        }
                        else {
                            channel.sendMessage("Cancelled your reaction event because you didn't respond within **" + String.valueOf
                                    (time / 1000) + "** seconds").queue();
                        }
                    }
                }
                catch (Exception e) {
                    new BotException(e);
                }
                ex.shutdown();
                return;
            }
            Iterator<Message> iterator = lastMessages.keySet().iterator();
            while (iterator.hasNext()) {
                Message m = iterator.next();
                if (m.getCreationTime().isAfter(creationTime)) {
                    if (m.getAuthor().getId().equalsIgnoreCase(user.getId()) &&
                            m.getChannel().getId().equalsIgnoreCase(channel.getId()))
                    {
                        success[0] = true;
                        function.accept(m);
                        iterator.remove();
                        ex.shutdown();
                    }
                }
            }
            ranFor[0] += interval;
        }, interval, interval, TimeUnit.MILLISECONDS);
        return success[0];
    }

    private static boolean dispatchInteractiveEvent(OffsetDateTime creationTime, TextChannel channel, Message message, Consumer<Message>
            function, int time, boolean sendMessage) {
        final boolean[] success = {false};
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        final int interval = 50;
        final int[] ranFor = {0};
        ex.scheduleAtFixedRate(() -> {
            if (ranFor[0] >= time) {
                try {
                    if (sendMessage) {
                        if (time >= 15000) {
                            channel.sendMessage("Cancelled your interactive operation because you didn't respond within 15 seconds!")
                                    .queue();
                        }
                        else {
                            channel.sendMessage("Cancelled your reaction event because you didn't respond within **" + String.valueOf
                                    (time / 1000) + "** seconds").queue();
                        }
                    }
                }
                catch (Exception e) {
                    new BotException(e);
                }
                ex.shutdown();
                return;
            }
            Iterator<Message> iterator = lastMessages.keySet().iterator();
            while (iterator.hasNext()) {
                Message m = iterator.next();
                if (m.getCreationTime().isAfter(creationTime)) {
                    if (m.getAuthor().getId().equalsIgnoreCase(message.getAuthor().getId()) &&
                            m.getChannel().getId().equalsIgnoreCase(channel.getId()))
                    {
                        success[0] = true;
                        function.accept(m);
                        iterator.remove();
                        ex.shutdown();
                    }
                }
            }
            ranFor[0] += interval;
        }, interval, interval, TimeUnit.MILLISECONDS);
        return success[0];
    }


    /**
     * Called when a user runs a command with only one argument
     *
     * @param guild    The guild of the sent command
     * @param channel  Channel of the sent command
     * @param user     BaseCommand author
     * @param message  BaseCommand message
     * @param args     Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception this shouldn't happen
     */
    public abstract void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
            Exception;

    public abstract void setupSubcommands() throws Exception;

    public void sendHelp(MessageChannel channel, Guild guild, User author, BaseCommand
            baseCommand) throws
            Exception {
        sendEmbed(getHelp(guild, author), channel, author);
    }

    private EmbedBuilder getHelp(Guild guild, User author) throws
            Exception {
        Shard shard = GuildUtils.getShard(guild);
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(author);
        String name = getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        embedBuilder.setAuthor(name, shard.url, shard.bot.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append(getDescription());

        if (subcommands.size() > 0) {
            description.append("\n\n**Subcommands**");
            for (Subcommand subcommand : subcommands) {
                description.append(subcommand.getSyntax() + ": *" + subcommand.getDescription
                        () + "*\n");
            }
            description.append("\n**Example**:");
            description.append("\n/" + name.toLowerCase() + " " + subcommands.get(0).getSyntax());
        }

        if (aliases != null && aliases.length > 0) {
            description.append("\n**Aliases**:\n");
            description.append(MessageUtils.listWithCommas(Arrays.asList(aliases)));
        }
        embedBuilder.setDescription(description.toString());
        return embedBuilder;
    }

    /**
     * Called when the CommandFactory identifies the queried
     * command. Will either call a subcommand from the list
     * or Command#noArgs
     *
     * @param guild    The guild of the sent command
     * @param channel  Channel of the sent command
     * @param user     BaseCommand author
     * @param message  BaseCommand message
     * @param args     Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception
     */
    void onUsage(Guild guild, MessageChannel channel, User user, Message message, String[] args)
            throws Exception {
        if (args.length == 1 || subcommands.size() == 0) noArgs(guild, channel, user, message, args);
        else {

            List<SubcommandTranslation> subcommandTranslations = language.getSubcommands(this);
            final boolean[] found = {false};
            subcommandTranslations.forEach(subcommandTranslation -> {
                if (subcommandTranslation.getTranslation().equalsIgnoreCase(args[1])) {
                    found[0] = true;
                    subcommands.forEach(subcommand -> {
                        if (subcommand.getIdentifier().equalsIgnoreCase(subcommandTranslation.getIdentifier())) {
                            try {
                                if (oldLang == null) {
                                    subcommand.onCall(guild, channel, user, message, args, language);
                                }
                                else {
                                    subcommand.onCall(guild, channel, user, message, args, oldLang);
                                }
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        }
                    });
                }
            });
            if (message.getRawContent().split(" ").length == 2 && (message.getMentionedUsers().size() > 0)) {
                noArgs(guild, channel, user, message, args, language);
                return;
            }
            if (!found[0] && language == LangFactory.english) {
                sendRetrievedTranslation(channel, "other", language, "subcommandnotfound", user);
            }
            else if (!found[0]) onUsage(guild, channel, user, message, args, LangFactory.english, language);
        }
    }

    public String getDate() {
        return new Date(Instant.now().getEpochSecond() * 1000).toLocaleString();
    }
}