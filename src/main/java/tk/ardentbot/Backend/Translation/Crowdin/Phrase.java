package tk.ardentbot.Backend.Translation.Crowdin;

/**
 * Wrapper for a phrase translation
 */
class Phrase {
    public String id;
    String commandId;
    String content;

    Phrase(String commandId, String id, String content) {
        this.commandId = commandId;
        this.id = id;
        this.content = content;
    }
}
