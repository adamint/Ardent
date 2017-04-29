package tk.ardentbot.commands.guildinfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class Roleinfo extends Command {
    public Roleinfo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        List<Role> mentionedRoles = message.getMentionedRoles();
        if (args.length == 1 && mentionedRoles.size() == 0) {
            sendTranslatedMessage("Mention one or more roles or type a role's name to see its info!", channel, user);
            return;
        }
        if (mentionedRoles.size() == 1) {
            sendEmbed(getRoleInformation(mentionedRoles.get(0), guild, user), channel, user);
        } else {
            for (Role r : mentionedRoles) {
                sendEmbed(getRoleInformation(r, guild, user), channel, user);
            }
        }
        String roleString = replaceCommandIdAndPrefix(message.getRawContent());
        List<Role> retrievedRoles = guild.getRolesByName(roleString, true);
        if (retrievedRoles.size() == 0) {
            sendTranslatedMessage("Mention one or more roles or type a role's name to see its info!", channel, user);
        } else if (retrievedRoles.size() > 1) {
            sendTranslatedMessage("Multiple roles were found... Please select the correct one from the below list", channel, user);
            sendEmbed(chooseFromList("Choose a role", guild, user,

                    this, asStringArray(retrievedRoles)), channel, user);
            interactiveOperation(channel, message, selectionMessage -> {
                try {
                    Role role = retrievedRoles.get(Integer.parseInt(selectionMessage.getContent()) - 1);
                    sendEmbed(getRoleInformation(role, guild, user), channel, user);
                } catch (Exception e) {
                    sendTranslatedMessage("Invalid Argument", channel, user);
                }
            });
        } else if (retrievedRoles.size() == 1)
            sendEmbed(getRoleInformation(retrievedRoles.get(0), guild, user), channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }

    private EmbedBuilder getRoleInformation(Role role, Guild guild, User user) throws Exception {
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(user);
        String title = "Role Info | Server Specific";
        builder.setAuthor(title, guild.getIconUrl(), guild.getIconUrl());
        builder.addField("Role name", role.getName(), true);
        builder.addField("# with role", String.valueOf(guild.getMembers().stream().filter(member -> {
            boolean found = false;
            for (Role r : member.getRoles()) {
                if (r.getId().equals(role.getId())) found = true;
            }
            return found;
        }).count()), true);
        builder.addField("Creation time", role.getCreationTime().toLocalDate().toString(), true);
        try {
            builder.addField("Hex color", "#" + Integer.toHexString(role.getColor().getRGB()).substring(2)
                    .toUpperCase(), true);
        } catch (NullPointerException npe) {
            builder.addField("Hex color", "#ffffff", true);
        }
        ArrayList<String> permissions = new ArrayList<>();
        role.getPermissions().forEach(permission -> permissions.add(permission.getName()));
        try {
            builder.addField("Permissions", MessageUtils.listWithCommas(permissions), true);
        } catch (Exception ignored) {
        }
        return builder;
    }

}
