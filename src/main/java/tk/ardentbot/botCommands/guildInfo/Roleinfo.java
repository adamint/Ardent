package tk.ardentbot.botCommands.guildInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.core.translation.Translation;
import tk.ardentbot.core.translation.TranslationResponse;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.HashMap;
import java.util.List;

public class Roleinfo extends Command {
    public Roleinfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        List<Role> mentionedRoles = message.getMentionedRoles();
        if (args.length == 1 || mentionedRoles.size() == 0) {
            sendRetrievedTranslation(channel, "roleinfo", language, "mentionuserortyperolename", user);
            return;
        }
        if (mentionedRoles.size() == 1) {
            sendEmbed(getRoleInformation(mentionedRoles.get(0), guild, language, user), channel, user);
        }
        else {
            for (Role r : mentionedRoles) {
                sendEmbed(getRoleInformation(r, guild, language, user), channel, user);
            }
        }
        String roleString = replaceCommandIdAndPrefix(message.getRawContent());
        List<Role> retrievedRoles = guild.getRolesByName(roleString, true);
        if (retrievedRoles.size() == 0) {
            sendRetrievedTranslation(channel, "roleinfo", language, "mentionuserortyperolename", user);
        }

        else if (retrievedRoles.size() > 1) {
            sendRetrievedTranslation(channel, "roleinfo", language, "multiplerolesfound", user);
            sendEmbed(chooseFromList(getTranslation("roleinfo", language, "choosefromlist").getTranslation(), guild, language, user,
                    this, asStringArray(retrievedRoles)), channel, user);
            interactiveOperation(language, channel, message, selectionMessage -> {
                try {
                    Role role = retrievedRoles.get(Integer.parseInt(selectionMessage.getContent()) - 1);
                    sendEmbed(getRoleInformation(role, guild, language, user), channel, user);
                }
                catch (Exception e) {
                    sendRetrievedTranslation(channel, "tags", language, "invalidarguments", user);
                }
            });
        }
        else if (retrievedRoles.size() == 1) sendEmbed(getRoleInformation(retrievedRoles.get(0), guild, language, user), channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }

    private EmbedBuilder getRoleInformation(Role role, Guild guild, Language language, User user) throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, user, this);
        HashMap<Integer, TranslationResponse> translations = getTranslations(language, new Translation("roleinfo", "title"),
                new Translation("roleinfo", "rolename"), new Translation("roleinfo", "memberswith"),
                new Translation("roleinfo", "creationtime"), new Translation("roleinfo", "color"));
        String title = translations.get(0).getTranslation();
        builder.setAuthor(title, guild.getIconUrl(), guild.getIconUrl());
        builder.addField(translations.get(1).getTranslation(), role.getName(), true);
        builder.addField(translations.get(2).getTranslation(), String.valueOf(guild.getMembers().stream().filter(member -> {
            boolean found = false;
            for (Role r : member.getRoles()) {
                if (r.getId().equals(role.getId())) found = true;
            }
            return found;
        }).count()), true);
        builder.addField(translations.get(3).getTranslation(), role.getCreationTime().toString(), true);
        builder.addField(translations.get(4).getTranslation(), "#" + Integer.toHexString(role.getColor().getRGB()).substring(2)
                .toUpperCase(), true);
        return builder;
    }

}
