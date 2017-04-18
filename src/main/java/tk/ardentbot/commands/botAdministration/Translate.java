package tk.ardentbot.commands.botAdministration;

import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.rethink.models.CommandModel;
import tk.ardentbot.rethink.models.SubcommandModel;
import tk.ardentbot.rethink.models.TranslationModel;
import tk.ardentbot.utils.javaAdditions.Pair;
import tk.ardentbot.utils.javaAdditions.Quintet;
import tk.ardentbot.utils.javaAdditions.Triplet;

import java.util.ArrayList;
import java.util.HashMap;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Translate extends Command {
    public String help;

    public Translate(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static ArrayList<String> getTranslationDiscrepancies(Language language) {
        Cursor<HashMap> translations = r.db("data").table("translations").filter(r.hashMap("language", "english")).run(connection);
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

    public static ArrayList<Pair<String, String>> getCommandDiscrepancies(Language language) {
        ArrayList<Pair<String, String>> discrepanciesInEnglish = new ArrayList<>();
        ArrayList<Triplet<String, String, String>> englishTranslations = new ArrayList<>();
        Cursor<HashMap> translations = r.db("data").table("commands").filter(r.hashMap("language", "english")).run(connection);
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

    public static ArrayList<Quintet<String, String, String, String, String>> getSubCommandDiscrepancies(Language language) {
        Cursor<HashMap> translations = r.db("data").table("subcommands").filter(r.hashMap("language", "english")).run(connection);
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
