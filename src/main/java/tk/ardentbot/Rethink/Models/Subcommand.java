package tk.ardentbot.Rethink.Models;

import lombok.Getter;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;

public class Subcommand {
    @Getter
    private String command_identifier;
    @Getter
    private String identifier;
    private String language;
    @Getter
    private String translation;
    @Getter
    private String syntax;
    @Getter
    private String description;
    @Getter
    private boolean needsDb;
    @Getter
    private String uuid;

    public Language getLanguage() {
        return LangFactory.getLanguage(language);
    }
}
