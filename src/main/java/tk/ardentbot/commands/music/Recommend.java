package tk.ardentbot.commands.music;

import com.wrapper.spotify.methods.RecommendationsRequest;
import com.wrapper.spotify.methods.TrackSearchRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.Track;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static tk.ardentbot.commands.music.Music.*;
import static tk.ardentbot.main.Ardent.spotifyApi;

public class Recommend extends Command {
    public Recommend(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length > 1) {
            AudioManager audioManager = guild.getAudioManager();
            if (audioManager.isConnected()) {
                VoiceChannel connected = audioManager.getConnectedChannel();
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount <= 0 || amount > 10) {
                        sendTranslatedMessage("You can only get 10 recommended songs at a time!", channel, user);
                        return;
                    }
                    GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                    ArdentTrack ardentTrack = manager.scheduler.manager.getCurrentlyPlaying();
                    if (ardentTrack == null) {
                        sendTranslatedMessage("I'm not playing anything right now!", channel, user);
                        return;
                    }
                    String[] nameArgs = StringUtils.removeBracketsParentheses(ardentTrack.getTrack().getInfo
                            ().title).split(" ");
                    StringBuilder name = new StringBuilder();
                    for (String arg : nameArgs) {
                        if (!arg.contains(".") && !arg.contains("+") && !arg.contains(":") && !arg.contains
                                ("//"))
                        {
                            name.append(arg);
                        }
                        name.append(" ");
                    }
                    TrackSearchRequest trackSearchRequest = spotifyApi.searchTracks(name.toString()).build();
                    try {
                        Page<Track> tracks = trackSearchRequest.get();
                        String id = tracks.getItems().get(0).getId();
                        ArrayList<String> ids = new ArrayList<>();
                        ids.add(id);
                        RecommendationsRequest recommendationsRequest = spotifyApi.getRecommendations()
                                .tracks(ids)
                                .build();
                        List<Track> recommendations = recommendationsRequest.get();
                        for (int i = 0; i < amount; i++) {
                            loadAndPlay(message, user, this, (TextChannel) sendTo(channel, guild), recommendations
                                    .get(i).getName(), connected, false, true);
                        }
                    }
                    catch (Exception e) {
                        channel.sendMessage("There were no recommendations available, sorry!").queue();
                    }
                }
                catch (NumberFormatException e) {
                    sendTranslatedMessage("That's not a number!", channel, user);
                }
            }
            else {
                sendTranslatedMessage("I'm not in a voice channel", channel, user);
            }
        }
        else sendTranslatedMessage("Invalid arguments", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
