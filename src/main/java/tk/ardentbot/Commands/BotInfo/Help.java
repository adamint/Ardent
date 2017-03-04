package tk.ardentbot.Commands.BotInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Category;
import tk.ardentbot.Backend.Commands.Command;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.Translation;
import tk.ardentbot.Backend.Translation.TranslationResponse;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.UsageUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.Main.Ardent.*;

public class Help extends BotCommand {
    private Subcommand all;

    public Help(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, this);
        embedBuilder.setAuthor(getTranslation("help", language, "bothelp").getTranslation(), "https://ardentbot.tk/guild", Ardent.ardent.getAvatarUrl());
        embedBuilder.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer.png");

        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("help", "featuredcommands"));
        translations.add(new Translation("help", "commandcategories"));
        translations.add(new Translation("help", "viewsubcategories"));
        translations.add(new Translation("help", "viewfullhelp"));

        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

        StringBuilder description = new StringBuilder();
        description.append("**" + responses.get(0).getTranslation() + "**");
        description.append("\n > " + patreon.getName(language) + ": *" + patreon.getDescription(language) + "*");
        description.append("\n > " + translateForArdent.getName(language) + ": *" + translateForArdent.getDescription
                (language) + "*");
        description.append("\n > " + getDevHelp.getName(language) + ": *" + getDevHelp.getDescription(language) + "*");

        ArrayList<Command> byUsage = UsageUtils.orderByUsageDesc();
        for (int i = 0; i < 3; i++) {
            BotCommand botCommand = byUsage.get(i).getBotCommand();
            description.append("\n > " + botCommand.getName(language) + ": *" + botCommand.getDescription(language) + "*");
        }
        description.append("\n\n**" + responses.get(1).getTranslation() + "**");
        for (Category category : Category.values()) {
            description.append("\n > *" + Category.getName(category) + "*");
        }

        description.append("\n\n" + responses.get(2).getTranslation().replace("{0}", GuildUtils.getPrefix(guild) +
                args[0]));
        description.append("\n" + responses.get(3).getTranslation().replace("{0}", GuildUtils.getPrefix(guild) +
                args[0]).replace("{1}", all.getName(language)));
        description.append("\n\n" + getTranslation("help", language, "ifyouneedhelp").getTranslation().replace("{0}",
                "https://ardentbot.tk/guild"));
        embedBuilder.setDescription(description.toString());
        sendEmbed(embedBuilder, channel);
    }

    @Override
    public void setupSubcommands() {
        all = new Subcommand(this, "all") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                EmbedBuilder helpEmbed = MessageUtils.getDefaultEmbed(guild, user, Help.this);
                helpEmbed.setAuthor(getTranslation("help", language, "bothelp").getTranslation(), "https://ardentbot.tk/guild", Ardent.ardent.getAvatarUrl());
                helpEmbed.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer.png");
                StringBuilder description = new StringBuilder();
                description.append(getTranslation("help", language, "commandcategories").getTranslation() + "\n");
                for (Category category : Category.values()) {
                    description.append(" > **" + category.name().toLowerCase() + "**\n");
                }
                description.append("\n" + getTranslation("help", language, "viewsubcategories").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]) + "\n\n");
                description.append(getTranslation("help", language, "ifyouneedhelp").getTranslation().replace("{0}", "https://ardentbot.tk/guild"));
                helpEmbed.setDescription(description.toString());
                sendEmbed(helpEmbed, channel);
            }
        };

        subcommands.add(all);

        for (Category category : Category.values()) {
            subcommands.add(new Subcommand(this, Category.getName(category)) {
                @Override
                public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                    EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, Help.this);
                    embedBuilder.setAuthor(getTranslation("help", language, "commandsincategory").getTranslation().replace("{0}", category.name().toLowerCase()), "https://ardentbot.tk/guild", Ardent.ardent.getAvatarUrl());
                    ArrayList<Command> commandsInCategory = Help.this.getCommandsInCategory(category);
                    StringBuilder description = new StringBuilder();
                    for (Command command : commandsInCategory) {
                        description.append(" > **" + command.getName(language) + "**: " + command.getDescription(language) + "\n");
                    }
                    embedBuilder.setDescription(description.toString());
                    sendEmbed(embedBuilder, channel);
                }
            });
        }
    }
}
