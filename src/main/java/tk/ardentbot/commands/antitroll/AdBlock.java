package tk.ardentbot.commands.antitroll;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.core.translation.Translation;
import tk.ardentbot.core.translation.TranslationResponse;
import tk.ardentbot.rethink.models.AntiAdvertisingSettings;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.HashMap;

import static tk.ardentbot.main.Ardent.globalGson;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class AdBlock extends Command {
    // TODO: 4/17/2017 look for other forms of server invites
    public AdBlock(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "settings") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("adblock",
                                "adblocksettings"), new Translation("adblock", "allowpostingserverinvites"),
                        new Translation("adblock", "banadvertisersafter2ads"));
                AntiAdvertisingSettings settings = asPojo(r.table("anti_advertising_settings").get(guild.getId()).run(connection)
                        , AntiAdvertisingSettings.class);
                if (settings == null) {
                    settings = new AntiAdvertisingSettings(guild.getId(), true, false);
                    r.table("anti_advertising_settings").insert(r.json(globalGson.toJson(settings))).run(connection);
                }
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, AdBlock.this);
                String adblockSettings = translations.get(0).getTranslation();
                builder.setAuthor(adblockSettings, guild.getIconUrl(), guild.getIconUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + adblockSettings + "**");
                description.append("\n" + translations.get(1).getTranslation() + ": *" + settings.isAllow_discord_server_links() + "*");
                description.append("\n" + translations.get(2).getTranslation() + ": *" + settings.isBan_after_two_infractions() + "*");
                sendEmbed(builder.setDescription(description), channel, user);
            }
        });

        subcommands.add(new Subcommand(this, "serverinvites") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                try {
                    AntiAdvertisingSettings settings = asPojo(r.table("anti_advertising_settings").get(guild.getId()).run(connection)
                            , AntiAdvertisingSettings.class);
                    boolean allow = Boolean.parseBoolean(args[2]);
                    if (allow) {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(globalGson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), true, false)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("allow_discord_server_links", true))
                                .run(connection);
                        sendRetrievedTranslation(channel, "adblock", language, "nowcanpostdiscordserverlinks", user);
                    }
                    else {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(globalGson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), false, false)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("allow_discord_server_links", false))
                                .run(connection);
                        sendRetrievedTranslation(channel, "adblock", language, "nowcannotpostdiscordserverlinks", user);
                    }
                }
                catch (Exception ex) {
                    sendRetrievedTranslation(channel, "other", language, "needspecifytrueorfalse", user);
                }
            }
        });
        subcommands.add(new Subcommand(this, "banafter2ads") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                try {
                    AntiAdvertisingSettings settings = asPojo(r.table("anti_advertising_settings").get(guild.getId()).run(connection)
                            , AntiAdvertisingSettings.class);
                    boolean yes = Boolean.parseBoolean(args[2]);
                    if (yes) {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(globalGson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), false, true)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("ban_after_two_infractions", true))
                                .run(connection);
                        sendRetrievedTranslation(channel, "adblock", language, "willbanafter2ads", user);
                    }
                    else {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(globalGson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), true, false)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("ban_after_two_infractions", false))
                                .run(connection);
                        sendRetrievedTranslation(channel, "adblock", language, "willnotbanusersafter2ads", user);
                    }
                }
                catch (Exception ex) {
                    sendRetrievedTranslation(channel, "other", language, "needspecifytrueorfalse", user);
                }
            }
        });
    }
}
