package tk.ardentbot.Commands.BotAdministration;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Main.Ardent;
import tk.ardentbot.Utils.GuildUtils;
import tk.ardentbot.Utils.MessageUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static tk.ardentbot.Main.Ardent.conn;
import static tk.ardentbot.Utils.SQL.SQLUtils.cleanString;

public class Todo extends BotCommand {
    public Todo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        ArrayList<String> todos = getTodos();
        EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, this);
        String upcoming = getTranslation("todo", language, "upcoming").getTranslation();
        embedBuilder.setAuthor(upcoming, "https://ardentbot.tk", Ardent.ardent.getAvatarUrl());
        StringBuilder description = new StringBuilder();
        description.append("**" + upcoming + "**");
        for (int i = 0; i < todos.size(); i++) {
            description.append("\n" + (i + 1) + ": " + todos.get(i));
        }
        embedBuilder.setDescription(description.toString());
        sendEmbed(embedBuilder, channel);
    }

    @Override
    public void setupSubcommands() {
        subcommands.add(new Subcommand(this, "add") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    String todo = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " " + args[1] + " ", "");
                    Statement statement = conn.createStatement();
                    statement.executeUpdate("INSERT INTO Todo VALUES ('" + cleanString(todo) + "')");
                    statement.close();
                    sendTranslatedMessage("Added todo successfully", channel);
                }
                else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission");
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (Ardent.developers.contains(user.getId())) {
                    if (args.length == 3) {
                        try {
                            ArrayList<String> todos = getTodos();
                            int place = Integer.parseInt(args[2]) - 1;
                            if (place > -1 && place < todos.size()) {
                                Statement statement = conn.createStatement();
                                statement.executeUpdate("DELETE FROM Todo WHERE Text='" + cleanString(todos.get(place)) + "'");
                                statement.close();
                                sendTranslatedMessage("Removed todo successfully", channel);
                            }
                            else sendTranslatedMessage("Incorrect place specified.", channel);
                        }
                        catch (NumberFormatException ex) {
                            sendTranslatedMessage("That wasn't a number lol", channel);
                        }
                    }
                    else sendTranslatedMessage("Invalid arguments.", channel);
                }
                else sendRetrievedTranslation(channel, "other", language, "needdeveloperpermission");
            }
        });
    }

    public ArrayList<String> getTodos() throws SQLException {
        ArrayList<String> todos = new ArrayList<>();
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM Todo");
        while (set.next()) {
            todos.add(set.getString("Text"));
        }
        set.close();
        statement.close();
        return todos;
    }
}
