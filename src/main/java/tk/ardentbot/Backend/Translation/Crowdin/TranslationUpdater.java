package tk.ardentbot.Backend.Translation.Crowdin;

import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Bot.BotException;
import com.crowdin.Credentials;
import com.crowdin.Crwdn;
import com.crowdin.client.CrowdinApiClient;
import com.crowdin.parameters.CrowdinApiParametersBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.ZipInputStream;

import static tk.ardentbot.Backend.Translation.Crowdin.PhraseUpdater.*;
import static tk.ardentbot.Main.Ardent.*;
import static tk.ardentbot.Utils.SQLUtils.cleanString;

/**
 * Downloads and inserts phrase translations on a loop
 */
public class TranslationUpdater implements Runnable {
    private Statement statement = conn.createStatement();
    private Credentials credentials = new Credentials(BASE_URL, PROJECT_IDENTIFIER, PROJECT_KEY, ACCOUNT_KEY);
    private CrowdinApiClient crwdn = new Crwdn();

    public TranslationUpdater() throws SQLException {}

    @Override
    public void run() {
        try {
            for (Language l : crowdinLanguages) {
                CrowdinApiParametersBuilder parameters = new CrowdinApiParametersBuilder();

                File temp = new File("null" + l.getCrowdinLangCode() + ".zip");
                if (temp.exists()) temp.delete();

                parameters.downloadPackage(l.getCrowdinLangCode());
                crwdn.downloadTranslations(credentials, parameters);
                File downloaded = new File("null" + l.getCrowdinLangCode() + ".zip");
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(downloaded));
                zipInputStream.getNextEntry();

                CSVParser parser = new CSVParser(new StringReader(IOUtils.toString(zipInputStream)), CSVFormat.DEFAULT.withDelimiter(';'));

                parser.forEach(record -> {
                    String context = record.get(0);
                    String[] split = context.split("\\|");

                    String commandId = split[0];
                    String translationId = split[1];

                    String original = record.get(1);
                    String translation = record.get(2);

                    if (!original.equalsIgnoreCase(translation)) {
                        try {
                            ResultSet set = statement.executeQuery("SELECT * FROM Translations WHERE CommandIdentifier='" + commandId + "' AND " +
                                    "ID='" + translationId + "' AND Language='" + l.getIdentifier() + "'");
                            if (!set.next()) {
                                statement.executeUpdate("INSERT INTO Translations VALUES ('" + commandId + "','" + cleanString(translation) + "','" +
                                        translationId + "','" + l.getIdentifier() + "','0')");
                                p("INSERTED VALUES: Language: " + l.getIdentifier() + " | Command ID: " + commandId + " | Translation ID: " + translationId + " | Translation: " + translation);
                            }
                            set.close();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
        catch (Exception ex) {
            new BotException(ex);
        }
    }

    private void p(String s) {
        botLogs.sendMessage(s).queue();
    }
}
