package tk.ardentbot.utils.updaters;

/**
 * Updates needed phrases on a loop
 */
public abstract class PhraseUpdater implements Runnable {
   /* static final String BASE_URL = "https://api.crowdin.com/api/";
    static final String PROJECT_IDENTIFIER = "bot";
    public static String PROJECT_KEY;
    public static String ACCOUNT_KEY;

    private static ArrayList<Phrase> getPhrases(Language language) throws SQLException {
        ArrayList<Phrase> phrases = new ArrayList<>();
        DatabaseAction queryPhrases = new DatabaseAction("SELECT * FROM Translations WHERE Language=?").set(language
                .getIdentifier());
        ResultSet set = queryPhrases.request();
        while (set.next()) {
            phrases.add(new Phrase(set.getString("CommandIdentifier"), set.getString("ID"), set.getString
                    ("translation")));
        }
        queryPhrases.close();
        return phrases;
    }

    @Override
    public void run() {
        try {
            File file = new File("english_phrases.csv");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            ArrayList<Phrase> engPhrases = getPhrases(LangFactory.english);
            StringBuilder pb = new StringBuilder();
            for (Phrase p : engPhrases) {
                pb.append("\"" + p.commandId + "|" + p.id + "\";\"" + p.content + "\";\n");

            }
            IOUtils.write(pb.toString(), new FileOutputStream(file));
            Credentials credentials = new Credentials(BASE_URL, PROJECT_IDENTIFIER, PROJECT_KEY, ACCOUNT_KEY);
            CrowdinApiClient crwdn = new Crwdn();
            CrowdinApiParametersBuilder parameters = new CrowdinApiParametersBuilder();
            parameters.json();
            parameters.type("csv");
            parameters.scheme("identifier,source_phrase,translation");
            parameters.files(file.getAbsolutePath());
            crwdn.updateFile(credentials, parameters);
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }*/
}
