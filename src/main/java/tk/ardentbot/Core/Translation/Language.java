package tk.ardentbot.Core.Translation;

import com.rethinkdb.net.Cursor;
import org.json.simple.JSONObject;
import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.Models.CommandTranslation;
import tk.ardentbot.Core.Models.PhraseTranslation;
import tk.ardentbot.Core.Models.SubcommandTranslation;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Rethink.Models.CommandModel;
import tk.ardentbot.Rethink.Models.SubcommandModel;
import tk.ardentbot.Rethink.Models.TranslationModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.globalGson;
import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

/**
 * Holds a language, with automatically updating phrases,
 * commands, and subcommands
 */
public class Language {
    private String name;
    private Status languageStatus;
    private String crowdinLangCode;
    private Queue<PhraseTranslation> phraseTranslations = new ConcurrentLinkedQueue<>();
    private Queue<CommandTranslation> commandTranslations = new ConcurrentLinkedQueue<>();
    private Queue<SubcommandTranslation> subcommandTranslations = new ConcurrentLinkedQueue<>();

    public Language(String name, Status languageStatus, String crowdinLangCode, String folderName) {
        this.name = name;
        this.languageStatus = languageStatus;
        this.crowdinLangCode = crowdinLangCode;
        Ardent.globalExecutorService.scheduleAtFixedRate(() -> {
            phraseTranslations.clear();
            commandTranslations.clear();
            subcommandTranslations.clear();

            Cursor<HashMap> translations = r.db("data").table("translations").filter(row -> row.g("language").eq("english")).optArg
                    ("default", r.error()).run(connection);
            translations.forEach(tm -> {
                TranslationModel translationModel = globalGson.fromJson(JSONObject.toJSONString(tm), TranslationModel.class);
                phraseTranslations.add(new PhraseTranslation(translationModel.getCommand_identifier(), translationModel.getId(),
                        translationModel
                        .getTranslation()));
            });

            Cursor<HashMap> subcommands = r.db("data").table("subcommands").filter(r.hashMap("language", name)).run(connection);
            subcommands.forEach(sc -> {
                SubcommandModel subcommandModel = globalGson.fromJson(JSONObject.toJSONString(sc), SubcommandModel.class);
                subcommandTranslations.add(new SubcommandTranslation(subcommandModel.getCommand_identifier(), subcommandModel
                        .getIdentifier(),
                        subcommandModel.getTranslation(),
                        subcommandModel.getSyntax(), subcommandModel.getDescription()));
            });

            Cursor<HashMap> commands = r.db("data").table("commands").filter(r.hashMap("language", name)).run(connection);
            commands.forEach(cm -> {
                CommandModel commandModel = globalGson.fromJson(JSONObject.toJSONString(cm), CommandModel.class);
                commandTranslations.add(new CommandTranslation(commandModel.getIdentifier(), commandModel.getTranslation(), commandModel
                        .getDescription()));
            });

            translations.close();
            subcommands.close();
            commands.close();

        }, 0, 60, TimeUnit.MINUTES);
    }

    public String getCrowdinLangCode() {
        return crowdinLangCode;
    }

    public String getIdentifier() {
        return name;
    }

    public Status getLanguageStatus() {
        return languageStatus;
    }

    public Queue<PhraseTranslation> getPhraseTranslations() {
        return phraseTranslations;
    }

    public Queue<CommandTranslation> getCommandTranslations() {
        return commandTranslations;
    }

    private Queue<SubcommandTranslation> getSubcommandTranslations() {
        return subcommandTranslations;
    }

    public List<SubcommandTranslation> getSubcommands(BaseCommand baseCommand) {
        ArrayList<SubcommandTranslation> translations = new ArrayList<>();
        getSubcommandTranslations().forEach(subcommandTranslation -> {
            if (subcommandTranslation.getCommandIdentifier().equalsIgnoreCase(baseCommand.getCommandIdentifier()))
                translations.add(subcommandTranslation);
        });
        return translations;
    }

    public enum Status {
        INFANCY,
        DECENT,
        MOST,
        MATURE
    }
}