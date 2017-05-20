package tk.ardentbot.core.executor;

import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.events.InteractiveEvent;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.javaAdditions.Triplet;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public abstract class Command extends BaseCommand {
    private static ScheduledExecutorService ex = Executors.newScheduledThreadPool(5);
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
        this.botCommand = this;
    }


    public static void interactiveReaction(MessageChannel channel, Message message, User user, int seconds,
                                           Consumer<MessageReaction> function) {
        Pair<String, Triplet<String, String, Consumer<MessageReaction>>> p = new Pair<>(channel.getId(), new Triplet<>(user.getId(), message
                .getId(), function));
        InteractiveEvent e = GuildUtils.getShard(channel.getJDA()).interactiveEvent;
        e.getReactionInteractivesQueue().add(p);
        ex.schedule(() -> {
            if (e.getReactionInteractivesQueue().contains(p)) {
                e.getReactionInteractivesQueue().remove(p);
                channel.sendMessage("Cancelled your reaction event because you didn't respond in time!").queue();
            }
        }, seconds, TimeUnit.SECONDS);
    }

    public static void longInteractiveOperation(MessageChannel channel, Message message, User user, int seconds,
                                                Consumer<Message> function) {
        if (channel instanceof TextChannel) {
            Ardent.globalExecutorService.execute(() -> dispatchInteractiveEvent(message.getCreationTime(), (TextChannel) channel,
                    message, user, function, seconds * 1000, false));
        }
    }

    public static boolean longInteractiveOperation(MessageChannel channel, Message message, int seconds,
                                                   Consumer<Message> function) {
        final boolean[] succeeded = {false};
        if (channel instanceof TextChannel) {
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
        Pair<String, Triplet<String, String, Consumer<Message>>> p = new Pair<>(channel.getId(), new Triplet<>(user.getId
                (), message.getId(), function));
        InteractiveEvent e = GuildUtils.getShard(channel.getJDA()).interactiveEvent;
        e.getMessageInteractivesQueue().add(p);
        ex.schedule(() -> {
            if (e.getMessageInteractivesQueue().contains(p)) {
                e.getMessageInteractivesQueue().remove(p);
                if (sendMessage) {
                    if (time >= 15) {
                        channel.sendMessage("Cancelled your interactive operation because you didn't respond within " + time + " seconds!")
                                .queue();
                    }
                    else {
                        channel.sendMessage("Cancelled your reaction event because you didn't respond within **" + String.valueOf
                                (time / 1000) + "** seconds").queue();
                    }
                }
            }
            return success[0];
        }, time, TimeUnit.SECONDS);
        return success[0];
    }

    private static boolean dispatchInteractiveEvent(OffsetDateTime creationTime, TextChannel channel, Message message, Consumer<Message>
            function, int time, boolean sendMessage) {
        final boolean[] success = {false};
        Pair<String, Triplet<String, String, Consumer<Message>>> p = new Pair<>(channel.getId(), new Triplet<>(message.getAuthor().getId
                (), message.getId(), function));
        InteractiveEvent e = GuildUtils.getShard(channel.getJDA()).interactiveEvent;
        e.getMessageInteractivesQueue().add(p);
        ex.schedule(() -> {
            if (e.getMessageInteractivesQueue().contains(p)) {
                e.getMessageInteractivesQueue().remove(p);
                if (sendMessage) {
                    if (time >= 15) {
                        channel.sendMessage("Cancelled your interactive operation because you didn't respond within " + time + " seconds!")
                                .queue();
                    }
                    else {
                        channel.sendMessage("Cancelled your reaction event because you didn't respond within **" + String.valueOf
                                (time / 1000) + "** seconds").queue();
                    }
                }
            }
            return success[0];
        }, time, TimeUnit.SECONDS);
        return success[0];
    }


    /**
     * Called when a user runs a command with only one argument
     *
     * @param guild   The guild of the sent command
     * @param channel Channel of the sent command
     * @param user    BaseCommand author
     * @param message BaseCommand message
     * @param args    Message#getContent, split by spaces
     * @throws Exception this shouldn't happen
     */
    public abstract void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
            Exception;

    public abstract void setupSubcommands() throws Exception;

    public void sendHelp(MessageChannel channel, Guild guild, User author, BaseCommand
            baseCommand) throws Exception {
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
            description.append("\n\n**Subcommands**\n");
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


    void onUsage(Guild guild, MessageChannel channel, User user, Message message, String[] args)
            throws Exception {
        if (args.length == 1 || subcommands.size() == 0) noArgs(guild, channel, user, message, args);
        else {
            final boolean[] found = {false};
            subcommands.forEach(subcommand -> {
                if (subcommand.containsAlias(args[1])) try {
                    subcommand.onCall(guild, channel, user, message, args);
                    found[0] = true;
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
            if (message.getRawContent().split(" ").length == 2 && (message.getMentionedUsers().size() > 0)) {
                noArgs(guild, channel, user, message, args);
                return;
            }
            if (!found[0]) {
                sendTranslatedMessage("Sorry, you provided invalid arguments!", channel, user);
            }
        }
    }

    public boolean containsAlias(String query) {
        for (String s : aliases) if (s.equalsIgnoreCase(query)) return true;
        return false;
    }

    public String getDate() {
        return new Date(Instant.now().getEpochSecond() * 1000).toLocaleString();
    }
}