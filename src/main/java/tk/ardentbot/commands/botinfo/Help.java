package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import tk.ardentbot.commands.nsfw.NSFW;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.core.executor.Category;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.ArrayList;

public class Help extends Command {
    private Subcommand all;

    public Help(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(user);
        embedBuilder.setAuthor("Ardent Help", "https://ardentbot.tk/guild", getShard().bot.getAvatarUrl());
        embedBuilder.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer.png");

        StringBuilder description = new StringBuilder();
        for (Category category : Category.values()) {
            if (category == Category.NSFW) {
                if (!NSFW.csn(user, channel, guild)) {
                    continue;
                }
            }
            description.append("**" + WordUtils.capitalize(category.name().toLowerCase()) + "**\n");
            ArrayList<BaseCommand> commandsInCategory = getCommandsInCategory(category);
            for (BaseCommand baseCommand : commandsInCategory) {
                description.append("`" + baseCommand.getName() + "`  ");
            }
            description.append("\n");
        }
        description.append("\nType /help [category] to view a specific category View the full help screen by typing {0}help all".replace
                ("{0}", GuildUtils.getPrefix(guild) + args[0]));
        description.append("\n\nIf you need help, join our support guild @ https://ardentbot.tk/guild");
        embedBuilder.setDescription(description.toString());
        sendEmbed(embedBuilder, channel, user);
    }

    @Override
    public void setupSubcommands() {
        all = new Subcommand("See the traditional help screen", "all", "all") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                EmbedBuilder helpEmbed = MessageUtils.getDefaultEmbed(user);
                helpEmbed.setAuthor("Ardent Help", "https://ardentbot.tk/guild", getShard().bot.getAvatarUrl());
                helpEmbed.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer" +
                        ".png");
                StringBuilder description = new StringBuilder();
                description.append("Command Categories\n");
                for (Category category : Category.values()) {
                    description.append(" > **" + category.name().toLowerCase() + "**\n");
                }
                description.append("\nType /help [category] to view a specific category\n\nIf you need help, join our support guild: " +
                        "https://ardentbot.tk/guild");
                helpEmbed.setDescription(description.toString());
                sendEmbed(helpEmbed, channel, user);
            }
        };

        subcommands.add(all);

        for (Category category : Category.values()) {
            subcommands.add(new Subcommand("how tf did you see this", Category.getName(category), Category.getName(category)) {
                @Override
                public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
                    EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(user);
                    embedBuilder.setAuthor("Commands in Category ".replace("{0}", WordUtils.capitalize(category.name().toLowerCase())),
                            "https://ardentbot.tk/guild", getShard().
                                    bot.getAvatarUrl());
                    ArrayList<BaseCommand> commandsInCategory = Help.this.getCommandsInCategory(category);
                    StringBuilder description = new StringBuilder();
                    for (BaseCommand baseCommand : commandsInCategory) {
                        description.append(" > **" + baseCommand.getName() + "**: " + baseCommand.getDescription() + "\n");
                    }
                    embedBuilder.setDescription(description.toString());
                    sendEmbed(embedBuilder, channel, user);
                }
            });
        }
    }
}
