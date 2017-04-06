package tk.ardentbot.Core.Translation;

import com.rethinkdb.net.Cursor;
import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.Models.CommandTranslation;
import tk.ardentbot.Core.Models.PhraseTranslation;
import tk.ardentbot.Core.Models.SubcommandTranslation;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Rethink.Models.CommandModel;
import tk.ardentbot.Rethink.Models.Subcommand;
import tk.ardentbot.Rethink.Models.TranslationModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

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

            Cursor<TranslationModel> translations = r.db("data").table("translations").filter(r.hashMap("language", name)).run(connection);
            translations.forEach(translationModel -> {
                phraseTranslations.add(new PhraseTranslation(translationModel.getCommand_identifier(), translationModel.getId(),
                        translationModel
                        .getTranslation()));
            });

            Cursor<Subcommand> subcommands = r.db("data").table("subcommands").filter(r.hashMap("language", name)).run(connection);
            subcommands.forEach(subcommand -> {
                subcommandTranslations.add(new SubcommandTranslation(subcommand.getCommand_identifier(), subcommand.getIdentifier(),
                        subcommand.getTranslation(),
                        subcommand.getSyntax(), subcommand.getDescription()));
            });

            Cursor<CommandModel> commands = r.db("data").table("commands").filter(r.hashMap("language", name)).run(connection);
            commands.forEach(commandModel -> {
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