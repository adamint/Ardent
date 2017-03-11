package tk.ardentbot.Core.Translation;

/**
 * Response from getTranslation or getTranslations
 */
public class TranslationResponse {
    private String translation;
    private Language language;
    private boolean existsInLang;
    private boolean availableEnglishSubstitute = false;
    private boolean translationAvailable;

    public TranslationResponse(String translation, Language language, boolean existsInLang, boolean availableEnglishSubstitute, boolean translationAvailable) {
        this.translation = translation;
        this.language = language;
        this.existsInLang = existsInLang;
        this.availableEnglishSubstitute = availableEnglishSubstitute;
        this.translationAvailable = translationAvailable;
    }

    public String getTranslation() {
        return translation;
    }

    public Language getLanguage() {
        return language;
    }

    public boolean translationExists() {
        return existsInLang;
    }

    public boolean availableEnglishSubstitute() {
        return availableEnglishSubstitute;
    }

    public boolean isTranslationAvailable() {
        return translationAvailable;
    }

}
