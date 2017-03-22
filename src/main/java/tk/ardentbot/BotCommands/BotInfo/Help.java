package tk.ardentbot.BotCommands.BotInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang.WordUtils;
import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.CommandExecution.Category;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Help extends Command {
    private Subcommand all;

    public Help(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, this);
        embedBuilder.setAuthor(getTranslation("help", language, "bothelp").getTranslation(), "https://ardentbot" +
                ".tk/guild", getShard().bot.getAvatarUrl());
        embedBuilder.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer.png");

        ArrayList<Translation> translations = new ArrayList<>();
        translations.add(new Translation("help", "featuredcommands"));
        translations.add(new Translation("help", "commandcategories"));
        translations.add(new Translation("help", "viewsubcategories"));
        translations.add(new Translation("help", "viewfullhelp"));

        HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

        StringBuilder description = new StringBuilder();
        description.append("**" + responses.get(0).getTranslation() + "**");
        description.append("\n > " + getShard().patreon.getName(language) + ": *" + getShard().patreon.getDescription
                (language) + "*");
        description.append("\n > " + getShard().translateForArdent.getName(language) + ": *" + getShard()
                .translateForArdent.getDescription(language) + "*\n\n");

        for (Category category : Category.values()) {
            description.append("**" + WordUtils.capitalize(category.name().toLowerCase()) + "**\n");
            ArrayList<BaseCommand> commandsInCategory = getCommandsInCategory(category);
            for (BaseCommand baseCommand : commandsInCategory) {
                description.append("`" + baseCommand.getName(language) + "`  ");
            }
            description.append("\n");
        }

        description.append("\n**" + responses.get(1).getTranslation() + "**");
        for (Category category : Category.values()) {
            description.append("\n > *" + Category.getName(category) + "*");
        }

        description.append("\n" + responses.get(2).getTranslation().replace("{0}", GuildUtils.getPrefix(guild) +
                args[0]));
        description.append("\n" + responses.get(3).getTranslation().replace("{0}", GuildUtils.getPrefix(guild) +
                args[0]).replace("{1}", all.getName(language)));
        description.append("\n\n" + getTranslation("help", language, "ifyouneedhelp").getTranslation().replace("{0}",
                "https://ardentbot.tk/guild"));
        embedBuilder.setDescription(description.toString());
        sendEmbed(embedBuilder, channel, user);
    }

    @Override
    public void setupSubcommands() {
        all = new Subcommand(this, "all") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                EmbedBuilder helpEmbed = MessageUtils.getDefaultEmbed(guild, user, Help.this);
                helpEmbed.setAuthor(getTranslation("help", language, "bothelp").getTranslation(), "https://ardentbot" +
                        ".tk/guild", getShard().bot.getAvatarUrl());
                helpEmbed.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer" +
                        ".png");
                StringBuilder description = new StringBuilder();
                description.append(getTranslation("help", language, "commandcategories").getTranslation() + "\n");
                for (Category category : Category.values()) {
                    description.append(" > **" + category.name().toLowerCase() + "**\n");
                }
                description.append("\n" + getTranslation("help", language, "viewsubcategories").getTranslation()
                        .replace("{0}", GuildUtils.getPrefix(guild) + args[0]) + "\n\n");
                description.append(getTranslation("help", language, "ifyouneedhelp").getTranslation().replace("{0}",
                        "https://ardentbot.tk/guild"));
                helpEmbed.setDescription(description.toString());
                sendEmbed(helpEmbed, channel, user);
            }
        };

        subcommands.add(all);

        for (Category category : Category.values()) {
            subcommands.add(new Subcommand(this, Category.getName(category)) {
                @Override
                public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                                   Language language) throws Exception {
                    EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, Help.this);
                    embedBuilder.setAuthor(getTranslation("help", language, "commandsincategory").getTranslation()
                            .replace("{0}", category.name().toLowerCase()), "https://ardentbot.tk/guild", getShard().
                            bot.getAvatarUrl());
                    ArrayList<BaseCommand> commandsInCategory = Help.this.getCommandsInCategory(category);
                    StringBuilder description = new StringBuilder();
                    for (BaseCommand baseCommand : commandsInCategory) {
                        description.append(" > **" + baseCommand.getName(language) + "**: " + baseCommand.getDescription
                                (language) + "\n");
                    }
                    embedBuilder.setDescription(description.toString());
                    sendEmbed(embedBuilder, channel, user);
                }
            });
        }
    }
}
