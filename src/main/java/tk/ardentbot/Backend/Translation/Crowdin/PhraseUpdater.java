package tk.ardentbot.Backend.Translation.Crowdin;

import tk.ardentbot.Backend.Translation.LangFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import com.crowdin.Credentials;
import com.crowdin.Crwdn;
import com.crowdin.client.CrowdinApiClient;
import com.crowdin.parameters.CrowdinApiParametersBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static tk.ardentbot.Main.Ardent.conn;

/**
 * Updates needed phrases on a loop
 */
public class PhraseUpdater implements Runnable {
    static final String BASE_URL = "https://api.crowdin.com/api/";
    static final String PROJECT_IDENTIFIER = "ardent";
    public static String PROJECT_KEY;
    public static String ACCOUNT_KEY;

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
    }
    private static ArrayList<Phrase> getPhrases(Language language) throws SQLException {
        ArrayList<Phrase> phrases = new ArrayList<>();
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Translations WHERE Language='" + language.getIdentifier() + "'");
        while (set.next()) {
            phrases.add(new Phrase(set.getString("CommandIdentifier"), set.getString("ID"), set.getString("Translation")));
        }
        set.close();
        statement.close();
        return phrases;
    }
}
