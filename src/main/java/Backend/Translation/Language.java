package Backend.Translation;

import Backend.Commands.Command;
import Backend.Models.CommandTranslation;
import Backend.Models.PhraseTranslation;
import Backend.Models.SubcommandTranslation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static Main.Ardent.conn;
import static Main.Ardent.executorService;

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

    public enum Status {
        INFANCY,
        DECENT,
        MOST,
        MATURE;
    }

    public Language(String name, Status languageStatus, String crowdinLangCode, String folderName) {
        this.name = name;
        this.languageStatus = languageStatus;
        this.crowdinLangCode = crowdinLangCode;
        executorService.scheduleAtFixedRate(() -> {
            phraseTranslations.clear();
            commandTranslations.clear();
            subcommandTranslations.clear();

            try (Statement statement = conn.createStatement()) {
                ResultSet translations = statement.executeQuery("SELECT * FROM Translations WHERE Language='" + getIdentifier() + "'");
                while (translations.next()) {
                    phraseTranslations.add(new PhraseTranslation(translations.getString("CommandIdentifier"),
                            translations.getString("ID"), translations.getString("Translation")));
                }
                translations.close();

                ResultSet commands = statement.executeQuery("SELECT * FROM Commands WHERE Language='" + getIdentifier() + "'");
                while (commands.next()) {
                    commandTranslations.add(new CommandTranslation(commands.getString("Identifier"),
                            commands.getString("Translation"), commands.getString("Description")));
                }
                commands.close();

                ResultSet subcommands = statement.executeQuery("SELECT * FROM Subcommands WHERE Language='" + getIdentifier() + "'");
                while (subcommands.next()) {
                    subcommandTranslations.add(new SubcommandTranslation(subcommands.getString("CommandIdentifier"),
                            subcommands.getString("Identifier"), subcommands.getString("Translation"), subcommands.getString("Syntax"),
                            subcommands.getString("Description")));
                }
                subcommands.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

        }, 1, 30, TimeUnit.SECONDS);


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

    public List<SubcommandTranslation> getSubcommands(Command command) {
        ArrayList<SubcommandTranslation> translations = new ArrayList<>();
        getSubcommandTranslations().forEach(subcommandTranslation -> {
            if (subcommandTranslation.getCommandIdentifier().equalsIgnoreCase(command.getCommandIdentifier())) translations.add(subcommandTranslation);
        });
        return translations;
    }
}
