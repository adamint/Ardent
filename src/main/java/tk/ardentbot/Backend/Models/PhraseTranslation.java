package tk.ardentbot.Backend.Models;

/**
 * Wraps phrase translations into a convenient class
 */
public class PhraseTranslation {
    private String commandIdentifier;
    private String id;
    private String translation;

    public PhraseTranslation(String commandIdentifier, String id, String translation) {
        this.commandIdentifier = commandIdentifier;
        this.id = id;
        this.translation = translation.replace("{newline}", "\n");
    }

    public String getCommandIdentifier() {
        return commandIdentifier;
    }
    public String getId() {
        return id;
    }
    public String getTranslation() {
        return translation;
    }
}
