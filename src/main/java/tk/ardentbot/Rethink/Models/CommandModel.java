package tk.ardentbot.Rethink.Models;

import lombok.Getter;

public class CommandModel {
    @Getter
    private String identifier;
    @Getter
    private String language;
    @Getter
    private String translation;
    @Getter
    private String description;
    @Getter
    private String uuid;

    public CommandModel(String identifier, String language, String translation, String description) {
        this.identifier = identifier;
        this.language = language;
        this.translation = translation;
        this.description = description;
    }
}
