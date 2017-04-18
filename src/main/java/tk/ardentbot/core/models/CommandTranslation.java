package tk.ardentbot.core.models;

/**
 * Wraps command translations into a convenient class
 */
public class CommandTranslation {
    private String identifier;
    private String translation;
    private String description;
    private String[] aliases = null;
    public CommandTranslation(String identifier, String translation, String description) {
        this.identifier = identifier;
        this.translation = translation;
        this.description = description;
    }

    public void with(String... aliases) {
        this.aliases = aliases;
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

    /**
     * Check whether the queried command name includes an alias
     *
     * @param query the first argument of the message
     * @return whether this command includes that alias
     */
    public boolean containsAlias(String query) {
        if (aliases == null) return false;
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(query)) return true;
        }
        return false;
    }
}
