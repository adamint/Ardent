package tk.ardentbot.commands.nsfw;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.core.translate.Translation;
import tk.ardentbot.core.translate.TranslationResponse;
import tk.ardentbot.rethink.models.NSFWSettings;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.HashMap;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class NSFW extends Command {
    public NSFW(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static NSFWSettings getSettings(Guild guild) {
        NSFWSettings settings = asPojo(r.table("nsfw_settings").get(guild.getId()).run(connection), NSFWSettings.class);
        if (settings == null) {
            settings = new NSFWSettings(guild.getId());
            r.table("nsfw_settings").insert(r.json(getStaticGson().toJson(settings))).run(connection);
        }
        return settings;
    }

    public static boolean canSendNSFW(User user, MessageChannel mc, Guild guild, Language language, Command command) {
        TextChannel channel = (TextChannel) mc;
        NSFWSettings settings = getSettings(guild);
        if (!settings.isGlobal()) {
            if (!settings.getNsfwChannels().contains(channel.getId())) {
                command.sendRetrievedTranslation(channel, "nsfw", language, "cantsendnsfwinthischannel", user);
                return false;
            }
        }
        if (settings.isNeedNsfwRole()) {
            final boolean[] hasRole = {false};
            guild.getMember(user).getRoles().forEach(role -> {
                if (role.getName().equalsIgnoreCase("nsfw")) hasRole[0] = true;
            });
            if (!hasRole[0]) {
                command.sendRetrievedTranslation(channel, "nsfw", language, "youneedthensfwrole", user);
                return false;
            }
        }
        return true;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "alloweverywhere") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 3) {
                        try {
                            boolean allowEverywhere = Boolean.parseBoolean(args[2]);
                            getSettings(guild);
                            if (allowEverywhere) sendRetrievedTranslation(channel, "nsfw", language, "nowcanuseeverywhere", user);
                            else sendRetrievedTranslation(channel, "nsfw", language, "onlyuseinallowedchannels", user);
                            r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("global", allowEverywhere)).run(connection);
                        }
                        catch (Exception e) {
                            sendRetrievedTranslation(channel, "other", language, "needspecifytrueorfalse", user);
                        }
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });
        subcommands.add(new Subcommand(this, "neednsfwrole") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 3) {
                        try {
                            boolean needs = Boolean.parseBoolean(args[2]);
                            getSettings(guild);
                            if (needs) sendRetrievedTranslation(channel, "nsfw", language, "nowneednsfwrole", user);
                            else sendRetrievedTranslation(channel, "nsfw", language, "nolongerneednsfwrole", user);
                            r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("needNsfwRole", needs)).run(connection);
                        }
                        catch (Exception e) {
                            sendRetrievedTranslation(channel, "other", language, "needspecifytrueorfalse", user);
                        }
                    }
                    else sendRetrievedTranslation(channel, "tag", language, "invalidarguments", user);
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "addchannel") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                MessageChannel toAdd = message.getMentionedChannels().size() > 0 ? message.getMentionedChannels().get(0) : channel;
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    NSFWSettings settings = getSettings(guild);
                    if (settings.getNsfwChannels().contains(channel.getId())) {
                        sendRetrievedTranslation(channel, "nsfw", language, "channelalreadyadded", user);
                    }
                    else {
                        settings.getNsfwChannels().add(toAdd.getId());
                        r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("nsfwChannels", settings.getNsfwChannels())).run
                                (connection);
                        sendRetrievedTranslation(channel, "nsfw", language, "addedchannel", user);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "removechannel") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    NSFWSettings settings = getSettings(guild);
                    if (!settings.getNsfwChannels().contains(channel.getId())) {
                        sendRetrievedTranslation(channel, "nsfw", language, "channelnotadded", user);
                    }
                    else {
                        settings.getNsfwChannels().remove(channel.getId());
                        r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("nsfwChannels", settings.getNsfwChannels())).run
                                (connection);
                        sendRetrievedTranslation(channel, "nsfw", language, "removedchannel", user);
                    }
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        });

        subcommands.add(new Subcommand(this, "settings") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("nsfw", "nsfwsettings"),
                        new Translation("nsfw", "canenterglobally"), new Translation("nsfw", "needsnsfwrole"),
                        new Translation("nsfw", "allowedchannels"));
                String nsfwSettings = translations.get(0).getTranslation();
                NSFWSettings settings = getSettings(guild);

                EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(guild, user, NSFW.this);
                embedBuilder.setAuthor(nsfwSettings, getShard().url, getShard().bot.getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + nsfwSettings + "**");
                description.append("\n\n" + translations.get(1).getTranslation() + ": " + settings.isGlobal());
                description.append("\n\n" + translations.get(2).getTranslation() + ": " + settings.isNeedNsfwRole());
                if (!settings.isGlobal()) {
                    description.append("\n\n" + translations.get(3).getTranslation() + ": " + MessageUtils.listWithCommas(GuildUtils
                            .getChannelNames(settings.getNsfwChannels(), guild)));
                }
                embedBuilder.setDescription(description.toString());
                sendEmbed(embedBuilder, channel, user);
            }
        });
    }
}
