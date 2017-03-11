package tk.ardentbot.Core.Models;

/**
 * Wraps subcommand translations into a convenient class
 */
public class SubcommandTranslation {
    private String commandIdentifier;
    private String identifier;
    private String translation;
    private String syntax;
    private String description;

    public SubcommandTranslation(String commandIdentifier, String identifier, String translation, String syntax, String description) {
        this.commandIdentifier = commandIdentifier;
        this.identifier = identifier;
        this.translation = translation;
        this.syntax = syntax;
        this.description = description;
    }

    public String getCommandIdentifier() {
        return commandIdentifier;
    }
    public String getIdentifier() {
        return identifier;
    }
    public String getTranslation() {
        return translation;
    }
    public String getSyntax() {
        return syntax;
    }
    public String getDescription() {
        return description;
    }
}
