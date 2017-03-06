package tk.ardentbot.Backend.Translation;

/**
 * Wrapper for a phrase translation
 */
public class Phrase {
    public String id;
    public String commandId;
    public String content;

    public Phrase(String commandId, String id, String content) {
        this.commandId = commandId;
        this.id = id;
        this.content = content;
    }
}
