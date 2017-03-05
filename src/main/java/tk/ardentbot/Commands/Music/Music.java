package tk.ardentbot.Commands.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Commands.Subcommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Translation.Translation;
import tk.ardentbot.Backend.Translation.TranslationResponse;
import tk.ardentbot.Bot.BotException;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

public class Music extends BotCommand {
    public static HashMap<String, String> textChannels = new HashMap<>();

    public Music(CommandSettings commandSettings) {
        super(commandSettings);
    }

    private static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = ardent.musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(ardent.playerManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            ardent.musicManagers.put(guildId, musicManager);
        }
        return musicManager;
    }

    private static void play(User user, Guild guild, VoiceChannel channel, GuildMusicManager musicManager, AudioTrack
            track) {
        if (guild.getAudioManager().getConnectedChannel() == null) {
            guild.getAudioManager().openAudioConnection(channel);
        }
        musicManager.scheduler.queue(user, track);
    }

    private static void loadAndPlay(User user, BotCommand command, Language language, final TextChannel channel,
                                    String trackUrl, final VoiceChannel voiceChannel, boolean search) {
        Guild guild = channel.getGuild();
        textChannels.put(guild.getId(), channel.getId());
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (trackUrl.contains("watch?v=") && !search) {
            String[] parsed = trackUrl.split("watch\\?v=");
            if (parsed.length == 2) {
                trackUrl = parsed[1];
            }
            else {
                try {
                    command.sendRetrievedTranslation(channel, "music", language, "nosongfound");
                }
                catch (Exception e) {
                    new BotException(e);
                }
            }
        }

        String finalTrackUrl = trackUrl;
        ardent.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                try {
                    command.sendTranslatedMessage(command.getTranslation("music", language, "addingsong")
                                    .getTranslation().replace("{0}", track.getInfo().title) + " " + getDuration(track),
                            channel);
                }
                catch (Exception e) {
                    new BotException(e);
                }
                play(user, guild, voiceChannel, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                try {
                    command.sendTranslatedMessage(command.getTranslation("music", language, "addingsong")
                            .getTranslation().replace("{0}", firstTrack.getInfo().title) + " " + getDuration
                            (firstTrack), channel);
                }
                catch (Exception e) {
                    new BotException(e);
                }
                play(user, guild, voiceChannel, musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                if (!search) {
                    loadAndPlay(user, command, language, channel, "ytsearch: " + finalTrackUrl, voiceChannel, true);
                }
                else {
                    try {
                        command.sendRetrievedTranslation(channel, "music", language, "nosongfound");
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                try {
                    command.sendRetrievedTranslation(channel, "music", language, "notabletoplay");
                    new BotException(exception);
                }
                catch (Exception e) {
                    new BotException(e);
                }
            }
        });
    }

    private static VoiceChannel joinChannel(Guild guild, Member user, Language language, BotCommand cmd, AudioManager
            audioManager, MessageChannel channel) throws Exception {
        GuildVoiceState voiceState = user.getVoiceState();
        if (voiceState.inVoiceChannel()) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            Member bot = guild.getMember(ardent.bot);
            if (bot.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                audioManager.openAudioConnection(voiceChannel);
                cmd.sendTranslatedMessage(cmd.getTranslation("music", language, "connectedto").getTranslation()
                        .replace("{0}", voiceChannel.getName()), channel);
            }
            else {
                cmd.sendRetrievedTranslation(channel, "music", language, "nopermissiontojoin");
            }
            return voiceChannel;
        }
        else {
            cmd.sendRetrievedTranslation(channel, "music", language, "notinvoicechannel");
            return null;
        }
    }

    public static void checkMusicConnections() {
        ardent.executorService.scheduleAtFixedRate(() -> {
            try {
                for (Guild guild : ardent.jda.getGuilds()) {
                    GuildMusicManager manager = getGuildAudioPlayer(guild);
                    GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();
                    if (voiceState.inVoiceChannel()) {
                        TextChannel channel = guild.getTextChannelById(textChannels.get(guild.getId()));
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        Language language = GuildUtils.getLanguage(guild);
                        AudioPlayer player = manager.player;
                        if (voiceState.isGuildMuted()) {
                            ardent.help.sendRetrievedTranslation(channel,
                                    "music", language,
                                    "mutedinchannelpausingnow");
                            player.setPaused(true);
                        }
                        if (voiceChannel.getMembers().size() == 1) {
                            ardent.help.sendTranslatedMessage(ardent.help.getTranslation("music", language, "leftbcnic")
                                    .getTranslation().replace("{0}", voiceChannel.getName()), channel);
                            guild.getAudioManager().closeAudioConnection();
                        }
                    }
                }
            }
            catch (Exception ex) {
                new BotException(ex);
            }
        }, 10, 10, TimeUnit.MINUTES);
    }

    private static String getDuration(AudioTrack track) {
        long length = track.getInfo().length;
        int seconds = (int) (length / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        return "[" + String.format("%02d", (hours % 60)) + ":" + String.format("%02d", (minutes % 60)) + ":" + String
                .format("%02d", (seconds % 60)) + "]";
    }

    private static String getCurrentTime(AudioTrack track) {
        long current = track.getPosition();
        int seconds = (int) (current / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;

        long length = track.getInfo().length;
        int lengthSeconds = (int) (length / 1000);
        int lengthMinutes = lengthSeconds / 60;
        int lengthHours = lengthMinutes / 60;

        return "[" + String.format("%02d", (hours % 60)) + ":" + String.format("%02d", (minutes % 60)) + ":" + String
                .format("%02d", (seconds % 60)) + " / " + String.format("%02d", (lengthHours % 60)) + ":" + String
                .format("%02d", (lengthMinutes % 60)) + ":" + String.format("%02d", (lengthSeconds % 60)) + "]";
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "play") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    AudioManager audioManager = guild.getAudioManager();
                    String url = message.getRawContent().replace(args[0] + " " + args[1] + " ", "");
                    if (!audioManager.isConnected()) {
                        VoiceChannel success = joinChannel(guild, guild.getMember(user), language, Music.this,
                                audioManager, channel);
                        if (success != null) {
                            loadAndPlay(user, Music.this, language, (TextChannel) channel, url, success, false);
                        }
                    }
                    else
                        loadAndPlay(user, Music.this, language, (TextChannel) channel, url, audioManager
                                .getConnectedChannel(), false);
                }
                else sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
            }
        });

        subcommands.add(new Subcommand(this, "queue") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                ArrayList<Translation> translations = new ArrayList<>();
                translations.add(new Translation("music", "songsinqueue"));
                translations.add(new Translation("music", "queuedby"));
                translations.add(new Translation("music", "nosongsinqueue"));
                HashMap<Integer, TranslationResponse> response = getTranslations(language, translations);
                StringBuilder sb = new StringBuilder();
                String queuedBy = response.get(1).getTranslation();
                sb.append("__" + response.get(0).getTranslation() + "__\n");
                BlockingQueue<Pair<String, AudioTrack>> queue = getGuildAudioPlayer(guild).scheduler.getQueue();
                Iterator<Pair<String, AudioTrack>> iterator = queue.iterator();
                int current = 1;
                while (iterator.hasNext()) {
                    Pair<String, AudioTrack> pair = iterator.next();
                    AudioTrack track = pair.getV();
                    sb.append("#" + current + ": " + track.getInfo().title + ": " + track.getInfo().author + " " +
                            getDuration(track) + "\n     *" + queuedBy + " " + ardent.jda.getUserById(pair.getK())
                            .getName()
                            + "*\n");
                    current++;
                }
                if (current == 1) {
                    sb.append(response.get(2).getTranslation());
                }
                sendTranslatedMessage(sb.toString(), channel);
            }
        });

        subcommands.add(new Subcommand(this, "skip") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (audioManager.isConnected()) {
                    GuildMusicManager manager = getGuildAudioPlayer(guild);
                    if (manager.player.getPlayingTrack() != null) {
                        String ownerId = manager.scheduler.ownerOfNowPlaying;
                        if (ownerId == null) ownerId = "";
                        if (GuildUtils.hasManageServerPermission(member) || (user.getId().equalsIgnoreCase(ownerId))) {
                            manager.scheduler.nextTrack();
                            sendRetrievedTranslation(channel, "music", language, "skippedcurrent");
                        }
                        else sendRetrievedTranslation(channel, "music", language, "queuedorhavepermissions");
                    }
                }
                else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
            }
        });

        subcommands.add(new Subcommand(this, "remove") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (audioManager.isConnected()) {
                    try {
                        GuildMusicManager manager = getGuildAudioPlayer(guild);
                        BlockingQueue<Pair<String, AudioTrack>> queue = manager.scheduler.getQueue();
                        int numberToRemove = Integer.parseInt(args[2]) - 1;
                        if (numberToRemove > queue.size() || numberToRemove < 0)
                            sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                        else {
                            Iterator<Pair<String, AudioTrack>> iterator = queue.iterator();
                            int current = 0;
                            while (iterator.hasNext()) {
                                Pair<String, AudioTrack> pair = iterator.next();
                                AudioTrack track = pair.getV();
                                String name = track.getInfo().title;
                                if (current == numberToRemove) {
                                    if (GuildUtils.hasManageServerPermission(member) || pair.getK().equalsIgnoreCase
                                            (user.getId()))
                                    {
                                        queue.remove(pair);
                                        sendTranslatedMessage(getTranslation("music", language, "removedfromqueue")
                                                .getTranslation().replace("{0}", name), channel);
                                    }
                                    else
                                        sendRetrievedTranslation(channel, "music", language, "queuedorhavepermissions");
                                }
                                current++;
                            }
                        }
                    }
                    catch (NumberFormatException ex) {
                        sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                    }
                }
                else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
            }
        });

        subcommands.add(new Subcommand(this, "leave") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (audioManager.isConnected()) {
                        String name = audioManager.getConnectedChannel().getName();
                        audioManager.closeAudioConnection();
                        sendTranslatedMessage(getTranslation("music", language, "disconnected").getTranslation()
                                .replace("{0}", name), channel);
                    }
                    else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });

        subcommands.add(new Subcommand(this, "resume") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (audioManager.isConnected()) {
                        GuildMusicManager manager = getGuildAudioPlayer(guild);
                        if (manager.player.isPaused()) {
                            sendRetrievedTranslation(channel, "music", language, "resumedplayback");
                            manager.player.setPaused(false);
                        }
                        else {
                            sendRetrievedTranslation(channel, "music", language, "notpaused");
                        }
                    }
                    else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });

        subcommands.add(new Subcommand(this, "stop") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (audioManager.isConnected()) {
                        GuildMusicManager manager = getGuildAudioPlayer(guild);
                        if (manager.player.getPlayingTrack() != null) manager.player.stopTrack();
                        manager.scheduler.resetQueue();
                        sendRetrievedTranslation(channel, "music", language, "stoppedandcleared");
                    }
                    else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });


        subcommands.add(new Subcommand(this, "clear") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (audioManager.isConnected()) {
                        GuildMusicManager manager = getGuildAudioPlayer(guild);
                        manager.scheduler.resetQueue();
                        sendRetrievedTranslation(channel, "music", language, "clearedallsongs");
                    }
                    else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });

        subcommands.add(new Subcommand(this, "playing") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                GuildMusicManager manager = getGuildAudioPlayer(guild);
                AudioTrack nowPlaying = manager.player.getPlayingTrack();
                if (nowPlaying != null) {
                    StringBuilder sb = new StringBuilder();
                    String queuedBy = getTranslation("music", language, "queuedby").getTranslation();

                    sb.append(nowPlaying.getInfo().title + ": " + nowPlaying.getInfo().author + " " + getCurrentTime
                            (nowPlaying) +
                            "\n     *" + queuedBy + " " + ardent.jda.getUserById(manager.scheduler.ownerOfNowPlaying)
                            .getName() + "*");
                    sendTranslatedMessage(sb.toString(), channel);
                }
                else sendRetrievedTranslation(channel, "music", language, "notplayingrn");
            }
        });

        subcommands.add(new Subcommand(this, "shuffle") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (audioManager.isConnected()) {
                        GuildMusicManager manager = getGuildAudioPlayer(guild);
                        manager.scheduler.shuffle();
                        sendTranslatedMessage("Shuffled the queue!", channel);
                    }
                    else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });

        subcommands.add(new Subcommand(this, "pause") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    if (audioManager.isConnected()) {
                        GuildMusicManager manager = getGuildAudioPlayer(guild);
                        if (!manager.player.isPaused()) {
                            sendRetrievedTranslation(channel, "music", language, "pausedplayback");
                            manager.player.setPaused(true);
                        }
                        else {
                            sendRetrievedTranslation(channel, "music", language, "alreadypaused");
                        }
                    }
                    else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });

        subcommands.add(new Subcommand(this, "loop") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length == 4) {
                    if (GuildUtils.hasManageServerPermission(guild.getMember(user))) {
                        try {
                            int songsToLoop = Integer.parseInt(args[2]);
                            int amountOfTimes = Integer.parseInt(args[3]);

                            GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild);
                            TrackScheduler trackScheduler = guildMusicManager.scheduler;
                            BlockingQueue<Pair<String, AudioTrack>> queue = trackScheduler.getQueue();
                            int amountOfTracks = queue.size();

                            if (songsToLoop < 0 || songsToLoop > amountOfTracks) {
                                sendRetrievedTranslation(channel, "music", language, "impossibletoloop");
                            }
                            else {
                                if (amountOfTimes < 0 || amountOfTimes > 3) {
                                    sendRetrievedTranslation(channel, "music", language, "onlycanloopsongs");
                                }
                                else {
                                    ArrayList<AudioTrack> tracksToLoop = new ArrayList<>();
                                    Iterator<Pair<String, AudioTrack>> trackIterator = queue.iterator();
                                    for (int i = 0; i < songsToLoop; i++) {
                                        Pair<String, AudioTrack> track = trackIterator.next();
                                        tracksToLoop.add(track.getV());
                                    }
                                    for (int i = 0; i < amountOfTimes; i++) {
                                        tracksToLoop.forEach(track -> {
                                            trackScheduler.queue(user, track.makeClone());
                                        });
                                    }
                                    sendTranslatedMessage(getTranslation("music", language, "addedtheloop")
                                            .getTranslation().replace("{0}", String.valueOf(songsToLoop)).replace
                                                    ("{1}", String.valueOf(amountOfTimes)), channel);
                                }
                            }
                        }
                        catch (NumberFormatException ex) {
                            sendRetrievedTranslation(channel, "prune", language, "notanumber");
                        }
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
                }
                else sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
            }
        });

        subcommands.add(new Subcommand(this, "removeallfrom") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                Member member = guild.getMember(user);
                if (GuildUtils.hasManageServerPermission(member)) {
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if (mentionedUsers.size() == 1) {
                        User deleteFrom = mentionedUsers.get(0);
                        getGuildAudioPlayer(guild).scheduler.removeFrom(deleteFrom);

                        sendTranslatedMessage(getTranslation("music", language, "deletealltracksfrom").getTranslation()
                                .replace("{0}", deleteFrom.getName()), channel);
                    }
                    else sendRetrievedTranslation(channel, "other", language, "mentionuser");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });
        subcommands.add(new Subcommand(this, "restart") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                GuildMusicManager musicManager = getGuildAudioPlayer(guild);
                AudioPlayer player = musicManager.player;
                AudioTrack current = player.getPlayingTrack();
                if (current != null) {
                    if (GuildUtils.hasManageServerPermission(guild.getMember(user)) || user.getId().equalsIgnoreCase
                            (musicManager.scheduler.ownerOfNowPlaying))
                    {
                        current.setPosition(0);
                        sendTranslatedMessage(getTranslation("music", language, "restartedtrack").getTranslation()
                                .replace("{0}", current.getInfo().title), channel);
                    }
                    else sendRetrievedTranslation(channel, "music", language, "queuedorhavepermissions");
                }
                else sendRetrievedTranslation(channel, "music", language, "notplayingrn");
            }
        });
    }
}
