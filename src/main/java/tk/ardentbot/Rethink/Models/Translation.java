package tk.ardentbot.Rethink.Models;

import lombok.Getter;
import tk.ardentbot.Core.Translation.LangFactory;
import tk.ardentbot.Core.Translation.Language;

public class Translation {
    @Getter
    private String command_identifier;
    @Getter
    private String translation;
    @Getter
    private String id;
    private String language;
    @Getter
    private boolean verified;


    public Language getLanguage() {
        return LangFactory.getLanguage(language);
    }
}
