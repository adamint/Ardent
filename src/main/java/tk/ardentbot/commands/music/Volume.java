package tk.ardentbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.EntityGuild;

import static tk.ardentbot.commands.music.Music.getGuildAudioPlayer;
import static tk.ardentbot.commands.music.Music.sendTo;

public class Volume extends Command {
    public Volume(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild, channel);
            AudioPlayer player = guildMusicManager.player;
            if (args.length == 1) {
                sendTranslatedMessage("The current player volume is " + player.getVolume(), sendTo(channel, guild), user);
            }
            else {
                if (UserUtils.hasTierOnePermissions(user) || EntityGuild.get(guild).isPremium()) {
                    try {
                        int volume = Integer.parseInt(args[1]);
                        player.setVolume(volume);
                        sendTranslatedMessage("Set player volume to " + volume, sendTo(channel, guild), user);
                    }
                    catch (NumberFormatException ex) {
                        sendTranslatedMessage("That's not a number!", channel, user);
                    }
                }
                else sendTranslatedMessage("You must be a patron to do this! To help us out and get this perk, pledge even " +
                        "a dollar a month at https://patreon.com/ardent", channel, user);
            }
        }
        else sendTranslatedMessage("I'm not in a voice channel!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
