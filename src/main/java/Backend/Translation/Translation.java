package Backend.Translation;

/**
 * Wrapper for a phrase without its translation
 */
public class Translation {
    private String commandId;
    private String id;

    public Translation(String commandId, String id) {
        this.commandId = commandId;
        this.id = id;
    }

    public String getCommandId() {
        return commandId;
    }

    public String getId() {
        return id;
    }
}
