package tk.ardentbot.Backend.Commands;

import tk.ardentbot.Backend.Translation.Language;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.Statement;

import static tk.ardentbot.Main.Ardent.conn;

public abstract class Subcommand {
    private String identifier;
    private Command command;

    public Subcommand(Command command, String identifier) {
        this.identifier = identifier;
        this.command = command;
    }

    /**
     * Calls the overriden method when the Command has
     * identified the subcommand
     *
     * @param guild The guild of the sent command
     * @param channel Channel of the sent command
     * @param user Command author
     * @param message Command message
     * @param args Message#getContent, split by spaces
     * @param language The current language of the guild
     * @throws Exception
     */
    public abstract void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception;

    /**
     * Gets the identifier of the subcommand
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Retrieves the translated name of the subcommand
     * @param language guild language
     * @return a string representing the translation of the subcommand
     * @throws Exception
     */
    public String getName(Language language) throws Exception {
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" + command.getCommandIdentifier() + "' AND Identifier='" + identifier + "' AND Language='" + language.getIdentifier() + "'");
        if (set.next()) {
            String result = set.getString("Translation");
            set.close();
            statement.close();
            return result;
        }
        else {
            set.close();
            ResultSet engSet = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" + command.getCommandIdentifier() + "' AND Identifier='" + identifier + "' AND Language='english'");
            if (engSet.next()) {
                String result = engSet.getString("Translation");
                engSet.close();
                statement.close();
                return result;
            }
            else {
                engSet.close();
                statement.close();
                return command.getTranslation("other", language, "cannotfindname").getTranslation();
            }
        }
    }

    /**
     * Retrieves the translated syntax of the subcommand
     * @param language guild language
     * @return a string representing the syntax of the subcommand
     * @throws Exception
     */
    String getSyntax(Language language) throws Exception {
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" + command.getCommandIdentifier() + "' AND Identifier='" + identifier + "' AND Language='" + language.getIdentifier() + "'");
        if (set.next()) {
            String result = set.getString("Syntax").replace("{newline}", "\n");
            set.close();
            statement.close();
            return result;
        }
        else {
            set.close();
            ResultSet engSet = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" + command.getCommandIdentifier() + "' AND Identifier='" + identifier + "' AND Language='english'");
            if (engSet.next()) {
                String result = engSet.getString("Syntax");
                engSet.close();
                statement.close();
                return result;
            }
            else {
                engSet.close();
                statement.close();
                return command.getTranslation("other", language, "cannotfindname").getTranslation();
            }
        }
    }

    /**
     * Retrieves the translated description of the subcommand
     * @param language guild language
     * @return a string representing the translated description of the subcommand
     * @throws Exception
     */
    String getDescription(Language language) throws Exception {
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" + command.getCommandIdentifier() + "' AND Identifier='" + identifier + "' AND Language='" + language.getIdentifier() + "'");
        if (set.next()) {
            String result = set.getString("Description");
            set.close();
            statement.close();
            return result;
        }
        else {
            set.close();
            ResultSet engSet = statement.executeQuery("SELECT * FROM Subcommands WHERE CommandIdentifier='" + command.getCommandIdentifier() + "' AND Identifier='" + identifier + "' AND Language='english'");
            if (engSet.next()) {
                String result = engSet.getString("Description");
                engSet.close();
                return result;
            }
            else {
                engSet.close();
                return command.getTranslation("other", language, "cannotfinddescription").getTranslation();
            }
        }
    }
}
