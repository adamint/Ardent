package tk.ardentbot.rethink.models;

import lombok.Getter;
import tk.ardentbot.Core.translate.LangFactory;
import tk.ardentbot.Core.translate.Language;

public class SubcommandModel {
    @Getter
    private String command_identifier;
    @Getter
    private String description;
    @Getter
    private String identifier;
    private String language;
    @Getter
    private boolean needsDb;
    @Getter
    private String syntax;
    @Getter
    private String translation;

    public SubcommandModel(String command_identifier, String description, String identifier, String language, boolean needsDb, String
            syntax, String translation) {
        this.command_identifier = command_identifier;
        this.description = description;
        this.identifier = identifier;
        this.language = language;
        this.needsDb = needsDb;
        this.syntax = syntax;
        this.translation = translation;
    }

    public Language getLanguage() {
        return LangFactory.getLanguage(language);
    }
}
