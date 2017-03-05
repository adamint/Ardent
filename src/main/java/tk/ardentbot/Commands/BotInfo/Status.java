package tk.ardentbot.Commands.BotInfo;

import com.sun.management.OperatingSystemMXBean;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.Translation;
import tk.ardentbot.Backend.Translation.TranslationResponse;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static tk.ardentbot.Main.Ardent.ardent;

public class Status extends BotCommand {
    public static ConcurrentHashMap<String, Integer> commandsByGuild = new ConcurrentHashMap<>();

    public Status(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static int getVoiceConnections() {
        int counter = 0;
        for (Guild guild : ardent.jda.getGuilds()) {
            if (guild.getAudioManager().isConnected()) counter++;
        }
        return counter;
    }

    public static int getUserAmount() {
        return ardent.jda.getUsers().size();
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        JDA jda = ardent.jda;
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        double cpuUsage = operatingSystemMXBean.getSystemCpuLoad() + 0.01;
        if (cpuUsage < 0) cpuUsage = 0.01;
        DecimalFormat cpuFormat = new DecimalFormat("#0.00");

        System.gc();

        double totalRAM = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        double usedRAM = totalRAM - Runtime.getRuntime().freeMemory() / 1024 / 1024;

        StringBuilder devUsernames = new StringBuilder();
        for (int i = 0; i < ardent.developers.size(); i++) {
            User current = jda.getUserById(ardent.developers.get(i));
            devUsernames.append(current.getName() + "#" + current.getDiscriminator());
            if (i < (ardent.developers.size() - 1)) devUsernames.append(", ");
        }

        Translation title = new Translation("status", "title");
        Translation botstatus = new Translation("status", "botstatus");
        Translation loadedcommands = new Translation("status", "loadedcommands");
        Translation receivedmessages = new Translation("status", "receivedmessages");
        Translation commandsreceived = new Translation("status", "commandsreceived");
        Translation guilds = new Translation("status", "guilds");
        Translation audioConnections = new Translation("status", "currentaudioconnections");
        Translation cpu = new Translation("status", "cpu");
        Translation ram = new Translation("status", "ram");
        Translation developers = new Translation("status", "developers");
        Translation site = new Translation("status", "site");
        Translation botHelp = new Translation("help", "bothelp");
        ArrayList<Translation> translationQueries = new ArrayList<>();
        translationQueries.add(title);
        translationQueries.add(botstatus);
        translationQueries.add(loadedcommands);
        translationQueries.add(receivedmessages);
        translationQueries.add(commandsreceived);
        translationQueries.add(guilds);
        translationQueries.add(audioConnections);
        translationQueries.add(cpu);
        translationQueries.add(ram);
        translationQueries.add(developers);
        translationQueries.add(site);
        translationQueries.add(botHelp);

        int commandsReceived = (int) ardent.factory.getCommandsReceived();
        DecimalFormat formatter = new DecimalFormat("#,###");
        String cmds = formatter.format(commandsReceived);

        HashMap<Integer, TranslationResponse> translations = getTranslations(language, translationQueries);

        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, this);

        int amtConnections = getVoiceConnections();

        embedBuilder.setAuthor(translations.get(0).getTranslation(), "https://ardentbot.tk", ardent.bot
                .getAvatarUrl());
        embedBuilder.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/computer.png");

        embedBuilder.addField(translations.get(1).getTranslation(), ":thumbsup:", true);
        embedBuilder.addField(translations.get(2).getTranslation(), String.valueOf(ardent.factory
                .getLoadedCommandsAmount()), true);

        embedBuilder.addField(translations.get(3).getTranslation(), String.valueOf(ardent.factory
                .getMessagesReceived()), true);
        embedBuilder.addField(translations.get(4).getTranslation(), cmds, true);

        embedBuilder.addField(translations.get(5).getTranslation(), String.valueOf(jda.getGuilds().size()), true);
        embedBuilder.addField(translations.get(6).getTranslation(), String.valueOf(amtConnections), true);

        embedBuilder.addField(translations.get(7).getTranslation(), cpuFormat.format(cpuUsage) + "%", true);
        embedBuilder.addField(translations.get(8).getTranslation(), usedRAM + " / " + totalRAM + " MB", true);

        embedBuilder.addField(translations.get(9).getTranslation(), devUsernames.toString(), true);
        embedBuilder.addField(translations.get(10).getTranslation(), "https://ardentbot.tk", true);

        embedBuilder.addField(translations.get(11).getTranslation(), "https://ardentbot.tk/guild", true);

        sendEmbed(embedBuilder, channel);
    }

    @Override
    public void setupSubcommands() {}
}
