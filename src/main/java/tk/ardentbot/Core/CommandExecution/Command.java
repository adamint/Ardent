package tk.ardentbot.Core.CommandExecution;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Models.SubcommandTranslation;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static tk.ardentbot.Core.Events.InteractiveOnMessage.lastMessages;
import static tk.ardentbot.Core.Events.InteractiveOnMessage.queuedInteractives;

public abstract class Command extends BaseCommand {
    public int usages = 0;
    public ArrayList<Subcommand> subcommands = new ArrayList<>();

    /**
     * Instantiates a new Command
     *
     * @param commandSettings settings to instantiate the command with
     */
    public Command(CommandSettings commandSettings) {
        this.commandIdentifier = commandSettings.getCommandIdentifier();
        this.privateChannelUsage = commandSettings.isPrivateChannelUsage();
        this.guildUsage = commandSettings.isGuildUsage();
        this.category = commandSettings.getCategory();
        this.botCommand = this;
    }

    public static void nextMessageByUser(Language language, MessageChannel channel, Message message, Consumer<Message> function) {
        if (channel instanceof TextChannel) {
            queuedInteractives.put(message.getId(), message.getAuthor().getId());
            dispatchInteractiveEvent(message.getCreationTime(), (TextChannel) channel, message, function, language);
        }
    }

    private static void dispatchInteractiveEvent(OffsetDateTime creationTime, TextChannel channel, Message message, Consumer<Message>
            function, Language language) {
        final int interval = 50;

        final int[] ranFor = {0};
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ranFor[0] >= 10000) {
                    try {
                        GuildUtils.getShard(channel.getGuild()).help.sendRetrievedTranslation(channel, "other", language,
                                "cancelledinteractiveevent", message.getAuthor());
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                    this.cancel();
                    return;
                }
                Iterator<Message> iterator = lastMessages.keySet().iterator();
                while (iterator.hasNext()) {
                    Message m = iterator.next();
                    if (m.getCreationTime().isAfter(creationTime)) {
                        if (m.getAuthor().getId().equalsIgnoreCase(message.getAuthor().getId()) &&
                                m.getChannel().getId().equalsIgnoreCase(channel.getId()))
                        {
                            function.accept(m);
                            iterator.remove();
                            this.cancel();
                        }
                    }
                }
                ranFor[0] += interval;
            }
        }, interval, interval);
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
     * @throws Exception
     */
    public abstract void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
            Exception;

    public abstract void setupSubcommands() throws Exception;

    public void sendHelp(Language language, MessageChannel channel, Guild guild, User author, BaseCommand
            baseCommand) throws
            Exception {
        sendEmbed(getHelp(language, guild, author, baseCommand), channel, author);
    }

    private EmbedBuilder getHelp(Language language, Guild guild, User author, BaseCommand baseCommand) throws
            Exception {
        Shard shard = GuildUtils.getShard(guild);
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, author, baseCommand);
        embedBuilder.setColor(Color.ORANGE);
        String name = getName(language);
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        embedBuilder.setAuthor(name, shard.url, shard.bot.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append("*" + getDescription(language) + "*");

        if (subcommands.size() > 0) {
            description.append("\n\n**" + getTranslation("other", language, "subcommands").getTranslation() + "**\n");
            for (Subcommand subcommand : subcommands) {
                description.append("- " + subcommand.getSyntax(language) + ": *" + subcommand.getDescription
                        (language) + "*\n");
            }
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
    void onUsage(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language,
                 Language oldLang)
            throws Exception {
        if (args.length == 1 || subcommands.size() == 0) noArgs(guild, channel, user, message, args, language);
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
}