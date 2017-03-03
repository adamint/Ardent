package tk.ardentbot.Commands.GuildAdministration;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Utils.Emoji;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static tk.ardentbot.Main.Ardent.conn;

public class EmojiPacks extends BotCommand {
    public static final ArrayList<Emoji> emojis = new ArrayList<>();

    public EmojiPacks(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(getHelp(language), channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "list") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                StringBuilder names = new StringBuilder();
                for (String s : getPackNames()) {
                    names.append(", " + s);
                }
                names.replace(0, 2, "");
                sendTranslatedMessage(getTranslation("emojipacks", language, "list").getTranslation().replace("{0}", names.toString()), channel);
            }
        });
        subcommands.add(new Subcommand(this, "viewpack") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                if (args.length == 2) {
                    sendRetrievedTranslation(channel, "emojipacks", language, "addpackname");
                }
                else {
                    String query = args[2];
                    ArrayList<String> packNames = getPackNames();
                    if (packNames.contains(query)) {
                        ArrayList<Emoji> packEmojis = getEmojisForPack(query);
                        StringBuilder sb = new StringBuilder();
                        sb.append(getTranslation("emojipacks", language, "emojisinpack").getTranslation().replace("{0}", query) + ":");
                        for (Emoji emoji : packEmojis) {
                            sb.append("\n\n" + emoji.getImageURL() + " [" + emoji.getName() + "]");
                        }
                        sendTranslatedMessage(sb.toString(), channel);
                    }
                    else {
                        sendRetrievedTranslation(channel, "emojipacks", language, "packnotfounditscasesensitive");
                    }
                }
            }
        });
        subcommands.add(new Subcommand(this, "addpack") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
                Member member = guild.getMember(user);
                if (member.hasPermission(Permission.MANAGE_SERVER)) {
                    Member ardent = guild.getSelfMember();
                    if (ardent.hasPermission(Permission.MANAGE_EMOTES)) {
                        ArrayList<String> packNames = getPackNames();
                        if (args.length > 2 && packNames.contains(args[2])) {
                            sendRetrievedTranslation(channel, "emojipacks", language, "cannotcontinue");
                        }
                        else sendRetrievedTranslation(channel, "emojipacks", language, "needtoincludepack");
                    }
                    else sendRetrievedTranslation(channel, "emojipacks", language, "needmanageemojis");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });
    }

    public static void registerEmojis() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    emojis.clear();
                    Statement statement = conn.createStatement();
                    ResultSet set = statement.executeQuery("SELECT * FROM EmojiPacks");
                    while (set.next()) {
                        emojis.add(new Emoji(set.getString("PackName"), set.getString("EmojiName"), set.getString("ImageURL")));
                    }
                    set.close();
                    statement.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }, 1000, (1000 * 60));
    }

    public ArrayList<String> getPackNames() throws SQLException {
        ArrayList<String> packNames = new ArrayList<>();
        Statement statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM EmojiPacks");
        while (set.next()) {
            String packName = set.getString("PackName");
            if (!packNames.contains(packName)) {
                packNames.add(packName);
            }
        }
        set.close();
        statement.close();
        return packNames;
    }

    public ArrayList<Emoji> getEmojisForPack(String packName) {
        ArrayList<Emoji> emojisInPack = new ArrayList<>();
        for (Emoji emoji : emojis) {
            if (emoji.getPackName().equalsIgnoreCase(packName)) {
                emojisInPack.add(emoji);
            }
        }
        return emojisInPack;
    }
}
