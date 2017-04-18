package tk.ardentbot.core.translation;

import com.rethinkdb.net.Cursor;
import org.json.simple.JSONObject;
import tk.ardentbot.core.executor.BaseCommand;
import tk.ardentbot.core.models.CommandTranslation;
import tk.ardentbot.core.models.PhraseTranslation;
import tk.ardentbot.core.models.SubcommandTranslation;
import tk.ardentbot.main.Ardent;
import tk.ardentbot.rethink.models.CommandModel;
import tk.ardentbot.rethink.models.SubcommandModel;
import tk.ardentbot.rethink.models.TranslationModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.main.Ardent.globalGson;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

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
            Cursor<HashMap> translations = r.db("data").table("translations").filter(row -> row.g("language").eq("english")).optArg
                    ("default", r.error()).run(connection);
            translations.forEach(tm -> {
                TranslationModel translationModel = globalGson.fromJson(JSONObject.toJSONString(tm), TranslationModel.class);
                if (phraseTranslations.stream().filter(p -> p.getCommandIdentifier().equals(translationModel.getCommand_identifier()) &&
                        p.getTranslation().equals(translationModel.getTranslation()) && p.getId().equals(translationModel.getId())).count
                        () == 0)
                {
                    phraseTranslations.add(new PhraseTranslation(translationModel.getCommand_identifier(), translationModel.getId(),
                            translationModel
                                    .getTranslation()));
                }
            });

            Cursor<HashMap> subcommands = r.db("data").table("subcommands").filter(r.hashMap("language", name)).run(connection);
            subcommands.forEach(sc -> {
                SubcommandModel subcommandModel = globalGson.fromJson(JSONObject.toJSONString(sc), SubcommandModel.class);
                if (subcommandTranslations.stream().filter(st -> st.getCommandIdentifier().equals(subcommandModel.getCommand_identifier())
                        && st.getDescription().equals(subcommandModel.getDescription()) && st.getIdentifier().equals(subcommandModel
                        .getIdentifier())
                        && st.getTranslation().equals(subcommandModel.getTranslation())).count() == 0)
                {
                    subcommandTranslations.add(new SubcommandTranslation(subcommandModel.getCommand_identifier(), subcommandModel
                            .getIdentifier(),
                            subcommandModel.getTranslation(),
                            subcommandModel.getSyntax(), subcommandModel.getDescription()));
                }
            });

            Cursor<HashMap> commands = r.db("data").table("commands").filter(r.hashMap("language", name)).run(connection);
            commands.forEach(cm -> {
                CommandModel commandModel = globalGson.fromJson(JSONObject.toJSONString(cm), CommandModel.class);
                if (commandTranslations.stream().filter(ct -> ct.getDescription().equals(commandModel.getDescription()) &&
                        ct.getIdentifier().equals(commandModel.getIdentifier()) && ct.getTranslation().equals(commandModel.getTranslation())
                ).count() == 0)
                {
                    commandTranslations.add(new CommandTranslation(commandModel.getIdentifier(), commandModel.getTranslation(), commandModel
                            .getDescription()));
                }
            });
            translations.close();
            subcommands.close();
            commands.close();

            commandTranslations.forEach(ct -> {
                switch (ct.getIdentifier()) {
                    case "music":
                        ct.with("m", "tunes");
                        break;
                    case "patreon":
                        ct.with("donate");
                        break;
                    case "eval":
                        ct.with("j");
                        break;
                    case "joinmessage":
                        ct.with("join");
                        break;
                    case "random":
                        ct.with("rand");
                        break;
                    case "automessage":
                        ct.with("am");
                        break;
                    case "setnickname":
                        ct.with("sn");
                        break;
                    case "roleinfo":
                        ct.with("ri");
                        break;
                    case "guildinfo":
                        ct.with("serverinfo", "ginfo", "gi");
                        break;
                    case "feet":
                        ct.with("toes", "soles");
                        break;
                    default:
                        break;
                }
            });
        }, 0, 15, TimeUnit.MINUTES);
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