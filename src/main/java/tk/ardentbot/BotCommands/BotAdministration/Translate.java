package tk.ardentbot.BotCommands.BotAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.JLAdditions.Pair;
import tk.ardentbot.Utils.JLAdditions.Quintet;
import tk.ardentbot.Utils.JLAdditions.Triplet;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static tk.ardentbot.Utils.SQL.SQLUtils.cleanString;

public class Translate extends Command {
    public String help;

    public Translate(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static ArrayList<String> getTranslationDiscrepancies(Language language) throws SQLException {
        DatabaseAction getTranslations = new DatabaseAction("SELECT * FROM Translations WHERE Language=?")
                .set("english");
        ArrayList<String> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Triplet<String, String, String>> englishTranslations = new ArrayList<>();
        ResultSet translations = getTranslations.request();
        while (translations.next()) {
            englishTranslations.add(new Triplet<>(translations.getString("CommandIdentifier"), translations.getString
                    ("ID"), translations.getString("Translation")));
        }
        translations.close();
        for (Triplet<String, String, String> translation : englishTranslations) {
            DatabaseAction getCommandTranslations = new DatabaseAction("SELECT * FROM Translations WHERE " +
                    "CommandIdentifier=?" +
                    " AND ID=? AND Language=?").set(translation.getA()).set(translation.getB()).set(language
                    .getIdentifier());
            ResultSet exists = getCommandTranslations.request();
            if (!exists.next()) {
                discrepanciesInEnglish.add(translation.getC());
            }
            getCommandTranslations.close();
        }
        getTranslations.close();
        return discrepanciesInEnglish;
    }

    public static ArrayList<Pair<String, String>> getCommandDiscrepancies(Language language) throws SQLException {
        ArrayList<Pair<String, String>> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Triplet<String, String, String>> englishTranslations = new ArrayList<>();
        DatabaseAction getTranslations = new DatabaseAction("SELECT * FROM Commands WHERE Language=?")
                .set("english");
        ResultSet translations = getTranslations.request();
        while (translations.next()) {
            englishTranslations.add(new Triplet<>(translations.getString("Identifier"), translations.getString
                    ("Language"), translations.getString("Description")));
        }
        getTranslations.close();
        for (Triplet<String, String, String> translation : englishTranslations) {
            DatabaseAction getLang = new DatabaseAction("SELECT * FROM Commands WHERE Identifier=? AND " +
                    "Language=?")
                    .set(translation.getA()).set(language.getIdentifier());
            ResultSet exists = getLang.request();
            if (!exists.next()) {
                discrepanciesInEnglish.add(new Pair<>(translation.getA(), translation.getC()));
            }
            getLang.close();
        }
        return discrepanciesInEnglish;
    }

    public static ArrayList<Quintet<String, String, String, String, String>> getSubCommandDiscrepancies(Language
                                                                                                                language) throws SQLException {
        Statement statement = Ardent.conn.createStatement();
        ArrayList<Quintet<String, String, String, String, String>> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Quintet<String, String, String, String, String>> englishTranslations = new ArrayList<>();
        ResultSet translations = statement.executeQuery("SELECT * FROM Subcommands WHERE Language='english'");
        while (translations.next()) {
            if (translations.getBoolean("NeedsDb")) {
                englishTranslations.add(new Quintet<>(translations.getString("CommandIdentifier"), translations
                        .getString("Identifier"), translations.getString("Language"), translations.getString
                        ("Syntax"), translations.getString("Description")));
            }
        }
        translations.close();
        for (Quintet<String, String, String, String, String> translation : englishTranslations) {
            ResultSet exists = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" +
                    translation.getA() + "' AND " +
                    "Identifier='" + translation.getB() + "' AND Language='" + language.getIdentifier() + "'");
            if (!exists.next()) {
                discrepanciesInEnglish.add(translation);
            }
        }
        statement.close();
        return discrepanciesInEnglish;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(help, channel, user);
    }

    @Override
    public void setupSubcommands() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Translation**\nUse /translate (language) to view simple translations for that language.\nUse " +
                "/translate (language)cmds to view available command translations\nUse /translate (language)" +
                "subcommands " +
                "to view available subcommand translations\n" +
                "Translation adding syntax varies by subcategory (basic, commands, subcommands) so the syntax will be on each page\n" +
                "Example: /translate basic french 1 this is the translation for 1 in /translate french \nAvailable languages: ");
        for (Language language : LangFactory.languages) sb.append("**" + language.getIdentifier() + "** ");
        help = sb.toString();

        subcommands.add(new Subcommand(this, "basic") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (Ardent.translators.contains(user.getId())) {
                    if (args.length >= 5) {
                        Language translateTo = LangFactory.getLanguage(args[2]);
                        if (translateTo != null) {
                            ArrayList<String> discrepanciesInTableTranslations = getTranslationDiscrepancies(translateTo);
                            try {
                                int place = Integer.parseInt(args[3]);
                                if (place > 0 && place <= discrepanciesInTableTranslations.size()) {
                                    String translationOf = discrepanciesInTableTranslations.get((place - 1));
                                    String translation = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " +
                                            args[1] + " " + args[2] + " " + args[3] + " ", "");
                                    ResultSet set = Ardent.conn.prepareStatement("SELECT * FROM Translations WHERE " +
                                            "Language='english' AND " +
                                            "Translation='" + cleanString(translationOf) + "'").executeQuery();
                                    if (set.next()) {
                                        String commandID = set.getString("CommandIdentifier");
                                        String id = set.getString("ID");
                                        Ardent.conn.prepareStatement("INSERT INTO Translations VALUES ('" + commandID
                                                + "', '" + cleanString(translation) + "'," +
                                                "'" + id + "', '" + translateTo.getIdentifier() + "', '0')").executeUpdate();
                                        sendTranslatedMessage("Good job and thanks! Please do /translate " + translateTo.getIdentifier() + " again " +
                                                "because the place values have shifted!", channel, user);
                                    }
                                    else sendTranslatedMessage("Something went wrong... Uh oh!", channel, user);
                                }
                                else
                                    sendTranslatedMessage("Incorrect place value specified. Please review the " +
                                            "translation table", channel, user);
                            }
                            catch (NumberFormatException ex) {
                                sendTranslatedMessage("You need to specify a place. Use /translate " + translateTo.getIdentifier() + " for the syntax", channel, user);
                            }
                        }
                        else sendTranslatedMessage("Incorrect language specified", channel, user);
                    }
                    else
                        sendTranslatedMessage("There aren't enough arguments. Make sure that you're entering in this " +
                                "correctly", channel, user);
                }
                else sendTranslatedMessage("You must be a **translator** to use this command!", channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "cmds") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (Ardent.translators.contains(user.getId())) {
                    if (args.length >= 5) {
                        Language translateTo = LangFactory.getLanguage(args[2]);
                        if (translateTo != null) {
                            ArrayList<Pair<String, String>> discrepanciesInCommands = getCommandDiscrepancies(translateTo);
                            try {
                                int place = Integer.parseInt(args[3]);
                                if (place > 0 && place <= discrepanciesInCommands.size()) {
                                    Pair<String, String> translationOf = discrepanciesInCommands.get((place - 1));
                                    String unformattedTranslation = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " ", "");
                                    String[] parts = unformattedTranslation.split("//");
                                    if (parts.length == 2) {

                                        ResultSet set = new DatabaseAction("SELECT * FROM Commands WHERE Language = ?" +
                                                " AND Translation = ? AND Description = ?")
                                                .set("english").set(translationOf.getK())
                                                .set(translationOf.getV()).request();

                                        if (set.next()) {
                                            String commandID = set.getString("Identifier");

                                            new DatabaseAction("INSERT INTO Commands VALUES(?, ?, ?, ?)")
                                                    .set(commandID).set(translateTo.getIdentifier())
                                                    .set(parts[0]).set(parts[1]).update();
                                            
                                            sendTranslatedMessage("Good job and thanks! Please do /translate " +
                                                    translateTo.getIdentifier() + "cmds again " +
                                                    "because the place values have shifted!", channel, user);
                                        }
                                        else sendTranslatedMessage("Something went wrong... Uh oh!", channel, user);
                                    }
                                    else
                                        sendTranslatedMessage("You didn't use the correct syntax. Please do /translate " + translateTo.getIdentifier() + "cmds to get the syntax", channel, user);
                                }
                                else
                                    sendTranslatedMessage("Incorrect place value specified. Please review the " +
                                            "translation table", channel, user);
                            }
                            catch (NumberFormatException ex) {
                                sendTranslatedMessage("You need to specify a place. Use /translate " + translateTo
                                        .getIdentifier() + " for the syntax", channel, user);
                            }
                        }
                        else sendTranslatedMessage("Incorrect language specified", channel, user);
                    }
                    else
                        sendTranslatedMessage("There aren't enough arguments. Make sure that you're entering in this correctly", channel, user);
                }
                else sendTranslatedMessage("You must be a **translator** to use this command!", channel, user);
            }
        });

        for (Language language : LangFactory.languages) {
            subcommands.add(new Subcommand(this, language.getIdentifier()) {
                @Override
                public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                                   Language language) throws Exception {
                    if (Ardent.translators.contains(user.getId())) {
                        Language translateTo = LangFactory.getLanguage(args[1]);
                        if (translateTo != null) {
                            ArrayList<String> discrepanciesInTableTranslations = getTranslationDiscrepancies(translateTo);
                            StringBuilder list = new StringBuilder();
                            list.append("Translations needed for **" + translateTo.getIdentifier() + "**");
                            for (int i = 0; i < discrepanciesInTableTranslations.size(); i++) {
                                list.append("\n" + (i + 1) + ": " + discrepanciesInTableTranslations.get(i));
                            }
                            list.append("\n\nTranslate these by doing /translate basic " + translateTo.getIdentifier
                                    () + " (place) translation goes here");
                            sendTranslatedMessage(list.toString(), channel, user);
                        }
                        else sendTranslatedMessage("Incorrect language specified", channel, user);
                    }
                    else sendTranslatedMessage("You must be a **translator** to use this command!", channel, user);
                }
            });
            subcommands.add(new Subcommand(this, language.getIdentifier() + "cmds") {
                @Override
                public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                    if (Ardent.translators.contains(user.getId())) {
                        Language translateTo = LangFactory.getLanguage(args[1].replace("cmds", ""));
                        if (translateTo != null) {
                            ArrayList<Pair<String, String>> discrepanciesInCommandTranslations = getCommandDiscrepancies(translateTo);
                            StringBuilder list = new StringBuilder();
                            list.append("Translations needed for **" + translateTo.getIdentifier() + "**");
                            for (int i = 0; i < discrepanciesInCommandTranslations.size(); i++) {
                                Pair<String, String> current = discrepanciesInCommandTranslations.get(i);
                                list.append("\n" + (i + 1) + ": **Name: **" + current.getK() + " **|** **Description:** " + current.getV());
                            }
                            list.append("\n\nTranslate these by doing /translate cmds " + translateTo.getIdentifier()
                                    + " (place) name//description goes here\n**There should be no spaces before or " +
                                    "after the | seperator for the name and description!**");
                            sendTranslatedMessage(list.toString(), channel, user);
                        }
                        else sendTranslatedMessage("Incorrect language specified", channel, user);
                    }
                    else sendTranslatedMessage("You must be a **translator** to use this command!", channel, user);
                }
            });


        }
    }
}
