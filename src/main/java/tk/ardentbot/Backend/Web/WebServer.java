package tk.ardentbot.Backend.Web;

import tk.ardentbot.Backend.Commands.CommandFactory;
import tk.ardentbot.Backend.Translation.LangFactory;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Web.Models.Command;
import tk.ardentbot.Backend.Web.Models.Status;
import tk.ardentbot.Backend.Web.Models.User;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.Pair;
import tk.ardentbot.Utils.Quintet;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import static tk.ardentbot.Commands.BotAdministration.Translate.*;
import static tk.ardentbot.Main.Ardent.*;
import static tk.ardentbot.Utils.SQLUtils.cleanString;
import static spark.Spark.*;

public class WebServer {
    /**
     * Sets up the web server and the endpoints
     */
    public static void setup() {
        if (Ardent.testingBot) {
            port(668);
        }
        else {
            port(666);
            secure("/root/keystore.jks", "mortimer5", null, null);
        }
        get("/api/commands", (rq, rs) -> {
            CommandFactory factory = Ardent.factory;
            ArrayList<Command> commands = new ArrayList<>();
            factory.getCommands().forEach(command -> {
                try {
                    commands.add(new Command(command.getName(LangFactory.english), command.getCategory(), command.getDescription(LangFactory.english)));
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
            return gson.toJson(commands);
        });

        get("/api/staff", (rq, rs) -> {
            ArrayList<User> developers = new ArrayList<>();
            for (String id : Ardent.developers) {
                net.dv8tion.jda.core.entities.User user = jda.getUserById(id);
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl == null) {
                    int random = new Random().nextInt(4);
                    if (random == 0)
                        avatarUrl = "https://i.gyazo.com/41c854b8f366402cd75a4450becd178a.jpg";
                    else if (random == 1)
                        avatarUrl = "https://i.gyazo.com/5b07238cf478a02c9565d28ed6bb2b1f.jpg";
                    else if (random == 2)
                        avatarUrl = "https://i.gyazo.com/65ab76aa4c70f3b7e85b1cfcc74370df.jpg";
                    else if (random == 3)
                        avatarUrl = "https://i.gyazo.com/249ad1d26af8b388ea3b42fc23f52daa.jpg";
                }
                developers.add(new User(id, user.getName(), user.getDiscriminator(), avatarUrl, "developer"));
            }
            ArrayList<User> translators = new ArrayList<>();
            for (String id : Ardent.translators) {
                net.dv8tion.jda.core.entities.User user = jda.getUserById(id);
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl == null) {
                    int random = new Random().nextInt(4);
                    if (random == 0)
                        avatarUrl = "https://i.gyazo.com/41c854b8f366402cd75a4450becd178a.jpg";
                    else if (random == 1)
                        avatarUrl = "https://i.gyazo.com/5b07238cf478a02c9565d28ed6bb2b1f.jpg";
                    else if (random == 2)
                        avatarUrl = "https://i.gyazo.com/65ab76aa4c70f3b7e85b1cfcc74370df.jpg";
                    else if (random == 3)
                        avatarUrl = "https://i.gyazo.com/249ad1d26af8b388ea3b42fc23f52daa.jpg";
                }
                translators.add(new User(id, user.getName(), user.getDiscriminator(), avatarUrl, "translator"));
            }
            ArrayList<ArrayList<User>> staff = new ArrayList<>();
            staff.add(developers);
            staff.add(translators);
            return gson.toJson(staff);
        });
        get("/api/status", (rq, rs) -> gson.toJson(new Status(factory.getMessagesReceived(), factory.getCommandsReceived(), ManagementFactory.getRuntimeMXBean().getUptime() / 1000,
                factory.getLoadedCommandsAmount(), jda.getGuilds().size(), jda.getUsers().size())));
        get("/api/languages", (rq, rs) -> gson.toJson(LangFactory.languages));
        get("/api/translate/*/*/*", WebServer::translate);
        get("/api/translate/submit", (rq, rs) -> {
            String response = submit(rq);
            if (response.startsWith("Successfully")) {
                String type = rq.queryParams("type");
                String language = rq.queryParams("language");
                if (type != null && language != null) {
                    rs.redirect("https://ardentbot.tk:666/api/translate/172503861477507068/" + type + "/" + language);
                }
            }
            return null;
        });
    }

    /**
     * Submits translations and adds them to the database
     *
     * @param rq request
     * @return status of the translation
     */
    private static String submit(Request rq) {
        try (Statement statement = conn.createStatement()) {
            String type = rq.queryParams("type");
            if (type != null) {
                String languageString = rq.queryParams("language");
                if (languageString != null) {
                    Language language = LangFactory.getLanguage(languageString);
                    if (language != null) {
                        if (type.equalsIgnoreCase("phrases")) {
                            String id = rq.queryParams("identifier");
                            String command = rq.queryParams("commandidentifier");
                            String translation = rq.queryParams("translation");
                            if (translation != null && !translation.isEmpty() && id != null && !id.isEmpty() && command != null && !command.isEmpty()) {
                                statement.executeUpdate("INSERT INTO Translations VALUES ('" + command + "', '" + cleanString(translation) + "'," +
                                        "'" + id + "', '" + language.getIdentifier() + "', '0')");
                                return "Successfully added your translation. Go back and reload the page or use your base URL to enter in another one!";
                            }
                            else return "Invalid translation - null or empty!";
                        }
                        else if (type.equalsIgnoreCase("commands")) {
                            String id = rq.queryParams("identifier");
                            String translationName = rq.queryParams("translationname");
                            String translationDescription = rq.queryParams("translationdescription");
                            if (id != null && translationName != null && translationDescription != null
                                    && !id.isEmpty() && !translationName.isEmpty() &&
                                    !translationDescription.isEmpty())
                            {
                                statement.executeUpdate("INSERT INTO Commands VALUES ('" + id + "', '" + language.getIdentifier() + "', '" +
                                        cleanString(translationName) + "', '" + cleanString(translationDescription) + "')");
                                return "Successfully added your translation. Go back and reload the page or use your base URL to enter in another one!";
                            }
                            else return "Invalid arguments. Make sure you entered in all the fields.";
                        }
                        else if (type.equalsIgnoreCase("subcommands")) {
                            String id = rq.queryParams("identifier");
                            String commandId = rq.queryParams("commandidentifier");
                            String translationName = rq.queryParams("translationname");
                            String translationSyntax = rq.queryParams("translationsyntax");
                            String translationDescription = rq.queryParams("translationdescription");
                            if (id != null && commandId != null && translationName != null && translationSyntax != null && translationDescription != null
                                    && !id.isEmpty() && !commandId.isEmpty() && !translationName.isEmpty() &&
                                    !translationSyntax.isEmpty() && !translationDescription.isEmpty())
                            {
                                statement.executeUpdate("INSERT INTO Subcommands VALUES ('" + commandId + "', '" +
                                        id + "', '" + language.getIdentifier() + "', '" + cleanString(translationName) + "', '" +
                                        cleanString(translationSyntax) + "', '" + cleanString(translationDescription) + "', '1')");
                                return "Successfully added your translation. Go back and reload the page or use your base URL to enter in another one!";
                            }
                            else return "Invalid arguments. Make sure you entered in all the fields.";
                        }
                        else return "Incorrect type specified.... somehow";
                    }
                    else return "Invalid language specified";
                }
                else return "No language specified.";
            }
            else return "wat";
        }
        catch (Exception ex) {
            new BotException(ex);
            return "Database returned an error.";
        }
    }

    /**
     * Returns the translation form
     *
     * @param rq request
     * @param rs response
     * @return translation form
     */
    private static String translate(Request rq, Response rs) {
        String incorrectArgs = "Incorrect args specified: /api/translate/yourid/(phrases OR commands OR subcommands)/languagecode";
        String[] splats = rq.splat();
        if (splats.length == 3) {
            if (Ardent.translators.contains(String.valueOf(Long.valueOf(splats[0]) + 4))) {
                Language language = LangFactory.getLanguage(splats[2]);
                if (language != null) {
                    try (Statement statement = conn.createStatement()) {
                        if (splats[1].equalsIgnoreCase("phrases")) {
                            ArrayList<String> discrepancies = getTranslationDiscrepancies(language);
                            if (discrepancies.size() > 0) {
                                String discrepancy1 = discrepancies.get(0);
                                ResultSet set = statement.executeQuery("SELECT * FROM Translations WHERE Translation='" + cleanString(discrepancy1) + "'");
                                if (set.next()) {
                                    String commandIdentifier = set.getString("CommandIdentifier");
                                    String id = set.getString("ID");
                                    set.close();
                                    return "<!DOCTYPE html>\n" +
                                            "<html>\n" +
                                            "<body>\n" +
                                            "\n" +
                                            "<h2>Translate Phrases for " + LangFactory.getName(language) + " (" + discrepancies.size() + " phrases left to translate)</h2><br>\n" +
                                            "\n" +
                                            "English Text<br>\n" +
                                            "<textarea rows=\"4\" cols=\"100\" name=\"original\" form=\"phrases\" disabled>" + discrepancy1 + "</textarea>\n" +
                                            "<br>Translate Here <br>\n" +
                                            "<textarea rows=\"4\" cols=\"100\" name=\"translation\" form=\"phrases\"></textarea>\n" +
                                            "\n" +
                                            "<form action=\"/api/translate/submit\" id=\"phrases\">\n" +
                                            "  <br>\n" +
                                            "<input type=\"hidden\" name=\"identifier\" value=\"" + id + "\"><input type=\"hidden\" name=\"commandidentifier\" value=\"" + commandIdentifier + "\"><input type=\"hidden\" name=\"language\" value=\"" + LangFactory.getName(language) + "\"><input type=\"hidden\" name=\"type\" value=\"phrases\"><input type=\"submit\" value=\"Add Translation\">\n" +
                                            "</form>\n" +
                                            "<br>\n" +
                                            "</body>\n" +
                                            "</html>\n";
                                }
                                else return "Something went wrong (like really wrong): " + discrepancy1;
                            }
                            else
                                return "No untranslated phrases for this language! Try commands or subcommand translations.";
                        }
                        else if (splats[1].equalsIgnoreCase("commands")) {
                            ArrayList<Pair<String, String>> discrepancies = getCommandDiscrepancies(language);
                            if (discrepancies.size() > 0) {
                                Pair<String, String> discrepancy = discrepancies.get(0);
                                return "<!DOCTYPE html>\n" +
                                        "<html>\n" +
                                        "<body>\n" +
                                        "\n" +
                                        "<h2>Translate tk.ardentbot.Commands for " + LangFactory.getName(language) + " (" + discrepancies.size() + " commands left to translate)</h2><br>\n" +
                                        "\n" +
                                        "English Name<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"originalname\" form=\"commands\" disabled>" + discrepancy.getK() + "</textarea>\n" +
                                        "<br>Translated Name (make sure it's lowercase) <br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"translationname\" form=\"commands\"></textarea>\n" +
                                        "<br>English Description<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"originaldescription\" form=\"commands\" disabled>" + discrepancy.getV() + "</textarea>\n" +
                                        "<br>Translated Description<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"translationdescription\" form=\"commands\"></textarea>\n" +
                                        "\n" +
                                        "<form action=\"/api/translate/submit\" id=\"commands\">\n" +
                                        "  <br>\n" +
                                        "<input type=\"hidden\" name=\"identifier\" value=\"" + discrepancy.getK() + "\">  <input type=\"hidden\" name=\"language\" value=\"" + LangFactory.getName(language) + "\"><input type=\"hidden\" name=\"type\" value=\"commands\"><input type=\"submit\" value=\"Add Translation\">\n" +
                                        "</form>\n" +
                                        "<br>\n" +
                                        "</body>\n" +
                                        "</html>\n";
                            }
                            else
                                return "No untranslated commands for this language! Try phrases or subcommand translations.";
                        }
                        else if (splats[1].equalsIgnoreCase("subcommands")) {
                            ArrayList<Quintet<String, String, String, String, String>> discrepancies = getSubCommandDiscrepancies(language);
                            if (discrepancies.size() > 0) {
                                Quintet<String, String, String, String, String> discrepancy = discrepancies.get(0);
                                String cmdId = discrepancy.getA();
                                String id = discrepancy.getB();
                                return "<!DOCTYPE html>\n" +
                                        "<html>\n" +
                                        "<body>\n" +
                                        "\n" +
                                        "<h2>Translate Subcommands for " + LangFactory.getName(language) + " (" + discrepancies.size() + " subcommands left to translate)</h2><br>\n" +
                                        "\n" +
                                        "Subcommand English Name<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"originalname\" form=\"subcommands\" disabled>" + discrepancy.getB() + "</textarea>\n" +
                                        "<br>Translated Subcommand Name (make sure it's lowercase) <br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"translationname\" form=\"subcommands\"></textarea>\n" +
                                        "<br>English Syntax<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"originalsyntax\" form=\"subcommands\" disabled>" + discrepancy.getD() + "</textarea>\n" +
                                        "<br>Translated Syntax<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"translationsyntax\" form=\"subcommands\"></textarea>\n" +
                                        "<br>English Description<br><textarea rows=\"4\" cols=\"100\" name=\"originaldescription\" form=\"subcommands\" disabled>" + discrepancy.getE() + "</textarea>\n" +
                                        "<br>Translated Description<br>\n" +
                                        "<textarea rows=\"4\" cols=\"100\" name=\"translationdescription\" form=\"subcommands\"></textarea>\n" +
                                        "\n" +
                                        "<form action=\"/api/translate/submit\" id=\"subcommands\">\n" +
                                        "  <br>\n" +
                                        "<input type=\"hidden\" name=\"identifier\" value=\"" + id + "\"><input type=\"hidden\" name=\"commandidentifier\" value=\"" + cmdId + "\"><input type=\"hidden\" name=\"language\" value=\"" + LangFactory.getName(language) + "\"><input type=\"hidden\" name=\"type\" value=\"subcommands\"><input type=\"submit\" value=\"Add Translation\">\n" +
                                        "</form>\n" +
                                        "<br>\n" +
                                        "</body>\n" +
                                        "</html>\n";
                            }
                            else
                                return "No untranslated commands for this language! Try phrases or subcommand translations.";
                        }
                        else return incorrectArgs;
                    }
                    catch (SQLException ex) {
                        return "The database returned status code 304 - please try again";
                    }
                }
                else return "Incorrect language specified";
            }
            else return "You're not a translator!";
        }
        else return incorrectArgs;
    }
}
