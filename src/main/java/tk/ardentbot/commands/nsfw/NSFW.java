package tk.ardentbot.commands.nsfw;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.rethink.models.NSFWSettings;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.discord.MessageUtils;

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

    static boolean canSendNSFW(User user, MessageChannel mc, Guild guild, Command command) {
        TextChannel channel = (TextChannel) mc;
        NSFWSettings settings = getSettings(guild);
        if (!settings.isGlobal()) {
            if (!settings.getNsfwChannels().contains(channel.getId())) {
                channel.sendMessage("You can't use NSFW commands in this channel").queue();
                return false;
            }
        }
        if (settings.isNeedNsfwRole()) {
            final boolean[] hasRole = {false};
            guild.getMember(user).getRoles().forEach(role -> {
                if (role.getName().equalsIgnoreCase("nsfw")) hasRole[0] = true;
            });
            if (!hasRole[0]) {
                channel.sendMessage("In order to send NSFW commands, you need the NSFW role!").queue();
                return false;
            }
        }
        return true;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand("Set whether people can send NSFW channels in all channels", "alloweverywhere [true/false]",
                "alloweverywhere") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 3) {
                        try {
                            boolean allowEverywhere = Boolean.parseBoolean(args[2]);
                            getSettings(guild);
                            if (allowEverywhere) sendTranslatedMessage("You can now use NSFW commands in all channels", channel, user);
                            else sendTranslatedMessage("You can now use NSFW commands only in specified channels", channel, user);
                            r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("global", allowEverywhere)).run(connection);
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("You need to specify true or false", channel, user);
                        }
                    }
                    else sendTranslatedMessage("Invalid arguments ¯\\_(ツ)_/¯", channel, user);
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });
        subcommands.add(new Subcommand("Set whether you need a role called NSFW to send NSFW commands", "neednsfwrole [true/false]",
                "neednsfwrole") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    if (args.length == 3) {
                        try {
                            boolean needs = Boolean.parseBoolean(args[2]);
                            getSettings(guild);
                            if (needs) sendTranslatedMessage("You now need the NSFW role to send NSFW commands", channel, user);
                            else sendTranslatedMessage("Anyone can now send NSFW commands", channel, user);
                            r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("needNsfwRole", needs)).run(connection);
                        }
                        catch (Exception e) {
                            sendTranslatedMessage("You need to specify true or false", channel, user);
                        }
                    }
                    else sendTranslatedMessage("Invalid arguments ¯\\_(ツ)_/¯", channel, user);
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("Add the channel you're in to the list of allowed channels for NSFW", "addchannel", "addchannel") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                MessageChannel toAdd = message.getMentionedChannels().size() > 0 ? message.getMentionedChannels().get(0) : channel;
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    NSFWSettings settings = getSettings(guild);
                    if (settings.getNsfwChannels().contains(channel.getId())) {
                        sendTranslatedMessage("This channel has already been added to the NSFW list", channel, user);
                    }
                    else {
                        settings.getNsfwChannels().add(toAdd.getId());
                        r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("nsfwChannels", settings.getNsfwChannels())).run
                                (connection);
                        sendTranslatedMessage("You can now send NSFW commands in this channel", channel, user);
                    }
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("Remove the channel you're in from the NSFW list", "removechannel", "removechannel") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    NSFWSettings settings = getSettings(guild);
                    if (!settings.getNsfwChannels().contains(channel.getId())) {
                        sendTranslatedMessage("This channel isn't on the NSFW list", channel, user);
                    }
                    else {
                        settings.getNsfwChannels().remove(channel.getId());
                        r.table("nsfw_settings").get(guild.getId()).update(r.hashMap("nsfwChannels", settings.getNsfwChannels())).run
                                (connection);
                        sendTranslatedMessage("You now **cannot** send NSFW commands in this channel", channel, user);
                    }
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        });

        subcommands.add(new Subcommand("View the current NSFW settings for your server", "settings", "settings") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws
                    Exception {
                String nsfwSettings = "NSFW Settings";
                NSFWSettings settings = getSettings(guild);

                EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbed(user);
                embedBuilder.setAuthor(nsfwSettings, getShard().url, getShard().bot.getAvatarUrl());
                StringBuilder description = new StringBuilder();
                description.append("**" + nsfwSettings + "**");
                description.append("\n\nAble to use NSFW commands in all channels: " + settings.isGlobal());
                description.append("\n\nNeed the NSFW role: " + settings.isNeedNsfwRole());
                if (!settings.isGlobal()) {
                    description.append("\n\nChannels where you can use NSFW commands: " + MessageUtils.listWithCommas(GuildUtils
                            .getChannelNames(settings.getNsfwChannels(), guild)));
                }
                embedBuilder.setDescription(description.toString());
                sendEmbed(embedBuilder, channel, user);
            }
        });
    }
}
