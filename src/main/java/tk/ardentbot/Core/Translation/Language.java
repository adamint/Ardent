package tk.ardentbot.Core.Translation;

import tk.ardentbot.Core.CommandExecution.BaseCommand;
import tk.ardentbot.Core.Models.CommandTranslation;
import tk.ardentbot.Core.Models.PhraseTranslation;
import tk.ardentbot.Core.Models.SubcommandTranslation;
import tk.ardentbot.Utils.SQL.DatabaseAction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

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
        ardent.executorService.scheduleAtFixedRate(() -> {
            phraseTranslations.clear();
            commandTranslations.clear();
            subcommandTranslations.clear();

            try {
                DatabaseAction translationRequest = new DatabaseAction("SELECT * FROM Translations WHERE Language=?")
                        .set(getIdentifier());
                DatabaseAction commandsRequest = new DatabaseAction("SELECT * FROM Commands WHERE Language=?")
                        .set(getIdentifier());
                DatabaseAction subcommandsRequest = new DatabaseAction("SELECT * FROM Subcommands WHERE Language=?")
                        .set(getIdentifier());

                ResultSet translations = translationRequest.request();
                ResultSet commands = commandsRequest.request();
                ResultSet subcommands = subcommandsRequest.request();

                while (translations.next()) {
                    phraseTranslations.add(new PhraseTranslation(translations.getString("CommandIdentifier"),
                            translations.getString("ID"), translations.getString("Translation")));
                }

                while (commands.next()) {
                    commandTranslations.add(new CommandTranslation(commands.getString("Identifier"),
                            commands.getString("Translation"), commands.getString("Description")));
                }

                while (subcommands.next()) {
                    subcommandTranslations.add(new SubcommandTranslation(subcommands.getString("CommandIdentifier"),
                            subcommands.getString("Identifier"), subcommands.getString("Translation"), subcommands
                            .getString("Syntax"),
                            subcommands.getString("Description")));
                }

                translationRequest.close();
                commandsRequest.close();
                subcommandsRequest.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }, 1, 120, TimeUnit.SECONDS);


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
        MATURE;
    }
}