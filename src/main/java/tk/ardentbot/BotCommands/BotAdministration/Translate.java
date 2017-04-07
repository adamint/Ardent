package tk.ardentbot.BotCommands.BotAdministration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Rethink.Models.CommandModel;
import tk.ardentbot.Rethink.Models.SubcommandModel;
import tk.ardentbot.Rethink.Models.TranslationModel;
import tk.ardentbot.Utils.JLAdditions.Pair;
import tk.ardentbot.Utils.JLAdditions.Quintet;
import tk.ardentbot.Utils.JLAdditions.Triplet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class Translate extends Command {
    public String help;

    public Translate(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static ArrayList<String> getTranslationDiscrepancies(Language language) throws SQLException {
        Cursor<HashMap> translations = r.db("data").table("translations").run(connection);
        ArrayList<String> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Triplet<String, String, String>> englishTranslations = new ArrayList<>();
        while (translations.hasNext()) {
            TranslationModel translationModel = asPojo(translations.next(), TranslationModel.class);
            englishTranslations.add(new Triplet<>(translationModel.getCommand_identifier(), translationModel.getId(), translationModel
                    .getTranslation()));
        }
        for (Triplet<String, String, String> translation : englishTranslations) {
            Cursor<HashMap> exists = r.db("data").table("translations").filter(row -> row.g("command_identifier").eq(translation.getA())
                    .and(row.g("id").eq(translation.getB()).and(row.g("language").eq(language.getIdentifier())))).run(connection);
            if (!exists.hasNext()) {
                discrepanciesInEnglish.add(translation.getC());
            }
            exists.close();
        }
        translations.close();
        return discrepanciesInEnglish;
    }

    public static ArrayList<Pair<String, String>> getCommandDiscrepancies(Language language) throws SQLException {
        ArrayList<Pair<String, String>> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Triplet<String, String, String>> englishTranslations = new ArrayList<>();
        Cursor<HashMap> translations = r.db("data").table("commands").run(connection);
        while (translations.hasNext()) {
            CommandModel commandModel = asPojo(translations.next(), CommandModel.class);
            englishTranslations.add(new Triplet<>(commandModel.getIdentifier(), commandModel.getLanguage(), commandModel.getDescription()));
        }
        for (Triplet<String, String, String> translation : englishTranslations) {
            Cursor<HashMap> exists = r.db("data").table("commands").filter(row -> row.g("identifier").eq(translation.getA())
                    .and(row.g("language").eq(language.getIdentifier()))).run(connection);
            if (!exists.hasNext()) {
                discrepanciesInEnglish.add(new Pair<>(translation.getA(), translation.getC()));
            }
            exists.close();
        }
        translations.close();
        return discrepanciesInEnglish;
    }

    public static ArrayList<Quintet<String, String, String, String, String>> getSubCommandDiscrepancies(Language language) throws
            SQLException {
        Cursor<HashMap> translations = r.db("data").table("subcommands").run(connection);
        ArrayList<Quintet<String, String, String, String, String>> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Quintet<String, String, String, String, String>> englishTranslations = new ArrayList<>();
        while (translations.hasNext()) {
            SubcommandModel subcommandModel = asPojo(translations.next(), SubcommandModel.class);
            if (subcommandModel.isNeedsDb()) {
                englishTranslations.add(new Quintet<>(subcommandModel.getCommand_identifier(), subcommandModel.getIdentifier(),
                        subcommandModel.getTranslation(),
                        subcommandModel.getSyntax(), subcommandModel.getDescription()));
            }
        }
        for (Quintet<String, String, String, String, String> translation : englishTranslations) {
            Cursor<HashMap> exists = r.db("data").table("commands").filter(row -> row.g("command_identifier").eq(translation.getA())
                    .and(row.g("identifier").eq(translation.getB()).and(row.g("language").eq(language.getIdentifier())))).run(connection);
            if (!exists.hasNext()) {
                discrepanciesInEnglish.add(translation);
            }
            exists.close();
        }
        translations.close();
        return discrepanciesInEnglish;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(help, channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
