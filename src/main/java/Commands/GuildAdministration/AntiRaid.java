package Commands.GuildAdministration;

import Backend.Commands.BotCommand;
import Backend.Commands.Subcommand;
import Backend.Translation.Language;
import Backend.Translation.Translation;
import Backend.Translation.TranslationResponse;
import Main.Ardent;
import Utils.AntiRaidSettings;
import Utils.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import static Main.Ardent.conn;

public class AntiRaid extends BotCommand {
    public AntiRaid(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "config") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("antiraid", "level"));
                translations.add(new Translation("antiraid", "settings"));
                translations.add(new Translation("antiraid", "enabled"));
                translations.add(new Translation("antiraid", "tbb"));

                HashMap<Integer, TranslationResponse> responses = getTranslations(language, translations);

                String title = responses.get(1).getTranslation();

                EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, AntiRaid.this);
                embedBuilder.setTitle(title, Ardent.url);
                embedBuilder.setColor(Color.ORANGE);

                AntiRaidSettings settings = getSettings(guild);

                StringBuilder sb = new StringBuilder();
                sb.append("**" + title + "**");
                sb.append(responses.get(2).getTranslation() + ": *" + settings.isEnabled() + "*\n");
                sb.append(responses.get(3).getTranslation() + ": *" + settings.getMinutesBeforeSpeaking() + "*\n");
                sb.append(responses.get(0).getTranslation() + ": *" + settings.getLevel() + "*\n");

                embedBuilder.setDescription(sb.toString());
                sendEmbed(embedBuilder, channel);
            }
        });
    }

    public AntiRaidSettings getSettings(Guild guild) throws SQLException {
        isInserted(guild);
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM AntiRaidSettings WHERE GuildID='" + guild.getId() + "'");
        if (set.next()) {
            return new AntiRaidSettings(guild.getId(), set.getBoolean("Enabled"), set.getInt("MinutesBeforeSpeaking"), set.getInt("Level"));
        }
        statement.close();
        return null;
    }

    public static void isInserted(Guild guild) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM AntiRaidSettings WHERE GuildID='" + guild.getId() + "'");
        if (!set.next()) {
            statement.executeUpdate("INSERT INTO AntiRaidSettings VALUES ('" + guild.getId() + "','0','2','1')");
        }
        set.close();
        statement.close();
    }
}
