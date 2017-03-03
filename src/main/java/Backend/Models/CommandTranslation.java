package Backend.Models;

/**
 * Wraps command translations into a convenient class
 */
public class CommandTranslation {
    private String identifier;
    private String translation;
    private String description;

    public CommandTranslation(String identifier, String translation, String description) {
        this.identifier = identifier;
        this.translation = translation;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }
    public String getTranslation() {
        return translation;
    }
    public String getDescription() {
        return description;
    }
}
