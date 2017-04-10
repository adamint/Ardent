package tk.ardentbot.Rethink.Models;

import lombok.Getter;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;

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
    @Getter
    private String uuid;

    public SubcommandModel(String command_identifier, String description, String identifier, String language, boolean needsDb, String
            syntax,
                           String translation) {
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
