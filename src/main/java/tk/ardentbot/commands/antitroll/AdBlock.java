package tk.ardentbot.commands.antitroll;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.AntiAdvertisingSettings;
import tk.ardentbot.utils.discord.MessageUtils;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class AdBlock extends Command {
    // TODO: 4/17/2017 look for other forms of server invites
    public AdBlock(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("View the Adblock settings for this server", "settings", "settings") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                AntiAdvertisingSettings settings = asPojo(r.table("anti_advertising_settings").get(guild.getId()).run(connection)
                        , AntiAdvertisingSettings.class);
                if (settings == null) {
                    settings = new AntiAdvertisingSettings(guild.getId(), true, false);
                    r.table("anti_advertising_settings").insert(r.json(gson.toJson(settings))).run(connection);
                }
                EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
                String adblockSettings = "Adblock Settings";
                builder.setAuthor(adblockSettings, guild.getIconUrl(), guild.getIconUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + adblockSettings + "**");
                description.append("\nAllow users to post server invites: *" + settings.isAllow_discord_server_links() + "*");
                description.append("\nBan users after they advertise twice: *" + settings.isBan_after_two_infractions() + "*");
                sendEmbed(builder.setDescription(description), channel, user);
            }
        });

        subcommands.add(new Subcommand("Allow or block users from sending server invites", "serverinvites [true/false]", "serverinvites") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                try {
                    AntiAdvertisingSettings settings = asPojo(r.table("anti_advertising_settings").get(guild.getId()).run(connection)
                            , AntiAdvertisingSettings.class);
                    boolean allow = Boolean.parseBoolean(args[2]);
                    if (allow) {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(gson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), true, false)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("allow_discord_server_links", true))
                                .run(connection);
                        sendTranslatedMessage("People can now post Discord server invite links", channel, user);
                    }
                    else {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(gson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), false, false)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("allow_discord_server_links", false))
                                .run(connection);
                        sendTranslatedMessage("People now **cannot** post Discord server invite links", channel, user);
                    }
                }
                catch (Exception ex) {
                    sendTranslatedMessage("You need to specify true or false!", channel, user);
                }
            }
        });
        subcommands.add(new Subcommand("Ban users who advertise more than twice", "banafter2ads [true/false]", "banafter2ads") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                try {
                    AntiAdvertisingSettings settings = asPojo(r.table("anti_advertising_settings").get(guild.getId()).run(connection)
                            , AntiAdvertisingSettings.class);
                    boolean yes = Boolean.parseBoolean(args[2]);
                    if (yes) {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(gson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), false, true)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("ban_after_two_infractions", true))
                                .run(connection);
                        sendTranslatedMessage("I will now ban users if they advertise more than twice", channel, user);
                    }
                    else {
                        if (settings == null)
                            r.table("anti_advertising_settings").insert(r.json(gson.toJson(new AntiAdvertisingSettings(guild.getId
                                    (), true, false)))).run(connection);
                        else r.table("anti_advertising_settings").get(guild.getId()).update(r.hashMap("ban_after_two_infractions", false))
                                .run(connection);
                        sendTranslatedMessage("I won't ban members after 2 advertising infractions", channel, user);
                    }
                }
                catch (Exception ex) {
                    sendTranslatedMessage("You need to specify true or false!", channel, user);
                }
            }
        });
    }
}
