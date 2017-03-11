package tk.ardentbot.BotCommands.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.SQL.DatabaseAction;
import tk.ardentbot.Utils.Tuples.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static tk.ardentbot.Main.Ardent.ardent;

public class Music extends Command {
    public Music(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static Pair<Integer, Integer> getMusicStats() {
        int playingGuilds = 0;
        int queueLength = 0;
        for (Guild guild : ardent.jda.getGuilds()) {
            GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild, null);
            ArdentMusicManager manager = guildMusicManager.scheduler.manager;
            if (manager.isTrackCurrentlyPlaying()) {
                playingGuilds++;
                queueLength++;
                queueLength += manager.getQueue().size();
            }
        }
        return new Pair<>(playingGuilds, queueLength);
    }

    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, MessageChannel channel) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = ardent.musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(ardent.playerManager, channel);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            ardent.musicManagers.put(guildId, musicManager);
        }
        else {
            ArdentMusicManager ardentMusicManager = musicManager.scheduler.manager;
            if (ardentMusicManager.getChannel() == null) {
                ardentMusicManager.setChannel(channel);
            }
        }
        return musicManager;
    }

    private static void play(User user, Guild guild, VoiceChannel channel, GuildMusicManager musicManager, AudioTrack
            track, TextChannel textChannel) {
        if (guild.getAudioManager().getConnectedChannel() == null) {
            guild.getAudioManager().openAudioConnection(channel);
        }
        musicManager.scheduler.manager.addToQueue(new ArdentTrack(user.getId(), textChannel, track));
    }

    private static void loadAndPlay(User user, Command command, Language language, final TextChannel channel,
                                    String trackUrl, final VoiceChannel voiceChannel, boolean search) {
        Guild guild = channel.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild(), channel);
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
                play(user, guild, voiceChannel, musicManager, track, channel);
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
                play(user, guild, voiceChannel, musicManager, firstTrack, channel);
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
                }
                catch (Exception e) {
                    new BotException(e);
                }
            }
        });
    }

    private static VoiceChannel joinChannel(Guild guild, Member user, Language language, Command command, AudioManager
            audioManager, MessageChannel channel) throws Exception {
        GuildVoiceState voiceState = user.getVoiceState();
        if (voiceState.inVoiceChannel()) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            Member bot = guild.getMember(ardent.bot);
            if (bot.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                audioManager.openAudioConnection(voiceChannel);
                command.sendTranslatedMessage(command.getTranslation("music", language, "connectedto").getTranslation()
                        .replace("{0}", voiceChannel.getName()), channel);
            }
            else {
                command.sendRetrievedTranslation(channel, "music", language, "nopermissiontojoin");
            }
            return voiceChannel;
        }
        else {
            command.sendRetrievedTranslation(channel, "music", language, "notinvoicechannel");
            return null;
        }
    }

    public static void checkMusicConnections() {
        ardent.executorService.scheduleAtFixedRate(() -> {
            try {
                for (Guild guild : ardent.jda.getGuilds()) {
                    GuildMusicManager manager = getGuildAudioPlayer(guild, null);
                    GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();
                    if (voiceState.inVoiceChannel()) {
                        TextChannel channel = guild.getPublicChannel();
                        if (channel.canTalk()) {
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
                                ardent.help.sendTranslatedMessage(ardent.help.getTranslation("music", language,
                                        "leftbcnic")

                                        .getTranslation().replace("{0}", voiceChannel.getName()), channel);
                                guild.getAudioManager().closeAudioConnection();
                            }
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

    private boolean shouldDeleteMessages(Guild guild) throws SQLException {
        boolean returnValue = false;
        DatabaseAction action = new DatabaseAction("SELECT * FROM MusicSettings WHERE GuildID=?").set(guild.getId());
        ResultSet set = action.request();
        if (set.next()) {
            if (set.getBoolean("RemoveAdditionMessages")) returnValue = true;
        }
        action.close();
        return returnValue;
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendHelp(language, channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "volume") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                AudioManager audioManager = guild.getAudioManager();
                if (audioManager.isConnected()) {
                    GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild, channel);
                    AudioPlayer player = guildMusicManager.player;
                    if (args.length == 2) {
                        sendTranslatedMessage(getTranslation("music", language, "currentplayervolume").getTranslation
                                ().replace("{0}", String.valueOf(player.getVolume())), channel);
                    }
                    else {
                        if (UserUtils.isPatron(user)) {
                            try {
                                int volume = Integer.parseInt(args[2]);
                                player.setVolume(volume);
                                sendTranslatedMessage(getTranslation("music", language, "setplayervolume")
                                        .getTranslation()
                                        .replace("{0}", String.valueOf(volume)), channel);
                            }
                            catch (NumberFormatException ex) {
                                sendRetrievedTranslation(channel, "prune", language, "notanumber");
                            }
                        }
                        else sendRetrievedTranslation(channel, "other", language, "mustbestafforpatron");
                    }
                }
                else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
            }
        });
        subcommands.add(new Subcommand(this, "play") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (args.length > 2) {
                    AudioManager audioManager = guild.getAudioManager();
                    String url = message.getRawContent().replace(args[0] + " " + args[1] + " ", "");
                    boolean shouldDeleteMessage = shouldDeleteMessages(guild);
                    boolean implement = false;
                    if (!audioManager.isConnected()) {
                        VoiceChannel success = joinChannel(guild, guild.getMember(user), language, Music.this,
                                audioManager, channel);
                        if (success != null) {
                            loadAndPlay(user, Music.this, language, (TextChannel) channel, url, success, false);
                            implement = true;
                        }
                    }
                    else {
                        loadAndPlay(user, Music.this, language, (TextChannel) channel, url, audioManager
                                .getConnectedChannel(), false);
                        implement = true;
                    }
                    if (implement) {
                        if (shouldDeleteMessage) {
                            try {
                                message.delete().queue();
                            }
                            catch (PermissionException ex) {
                                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> {
                                    privateChannel.sendMessage("Auto-deleting music play messages is enabled, " +
                                            "but you need to give me the `MANAGE MESSAGES` permission so I can " +
                                            "actually delete the messages.");
                                });
                            }
                        }
                    }
                }
                else sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
            }
        });

        subcommands.add(new Subcommand(this, "config") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                DatabaseAction action = new DatabaseAction("SELECT * FROM MusicSettings WHERE GuildID=?").set(guild
                        .getId());
                ResultSet set = action.request();
                if (set.next()) {
                    sendTranslatedMessage("**Music Settings**\n" + "Delete music play messages: " + set.getBoolean
                            ("RemoveAdditionMessages"), channel);
                }
                else
                    sendTranslatedMessage("Your guild has no set music settings! Type **/manage** to find your portal" +
                            " link", channel);
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
                BlockingQueue<ArdentTrack> queue = getGuildAudioPlayer(guild, channel).scheduler.manager.getQueue();
                Iterator<ArdentTrack> iterator = queue.iterator();
                int current = 1;
                while (iterator.hasNext()) {
                    ArdentTrack ardentTrack = iterator.next();
                    AudioTrack track = ardentTrack.getTrack();
                    sb.append("#" + current + ": " + track.getInfo().title + ": " + track.getInfo().author + " " +
                            getDuration(track) + "\n     *" + queuedBy + " " + ardent.jda.getUserById(ardentTrack
                            .getAuthor())
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
                    GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                    ArdentMusicManager ardentMusicManager = manager.scheduler.manager;
                    ArdentTrack track = ardentMusicManager.getCurrentlyPlaying();
                    if (track != null) {
                        String ownerId = track.getAuthor();
                        if (ownerId == null) ownerId = "";
                        if (GuildUtils.hasManageServerPermission(member) || (user.getId().equalsIgnoreCase(ownerId))) {
                            ardentMusicManager.nextTrack();
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
                if (args.length > 2) {
                    AudioManager audioManager = guild.getAudioManager();
                    Member member = guild.getMember(user);
                    if (audioManager.isConnected()) {
                        try {
                            GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                            BlockingQueue<ArdentTrack> queue = manager.scheduler.manager.getQueue();
                            int numberToRemove = Integer.parseInt(args[2]) - 1;
                            if (numberToRemove > queue.size() || numberToRemove < 0)
                                sendRetrievedTranslation(channel, "tag", language, "invalidarguments");
                            else {
                                Iterator<ArdentTrack> iterator = queue.iterator();
                                int current = 0;
                                while (iterator.hasNext()) {
                                    ArdentTrack ardentTrack = iterator.next();
                                    AudioTrack track = ardentTrack.getTrack();
                                    String name = track.getInfo().title;
                                    if (current == numberToRemove) {
                                        if (GuildUtils.hasManageServerPermission(member) || ardentTrack.getAuthor()
                                                .equalsIgnoreCase(user.getId()))
                                        {
                                            queue.remove(ardentTrack);
                                            sendTranslatedMessage(getTranslation("music", language, "removedfromqueue")
                                                    .getTranslation().replace("{0}", name), channel);
                                        }
                                        else
                                            sendRetrievedTranslation(channel, "music", language,
                                                    "queuedorhavepermissions");

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
                else sendRetrievedTranslation(channel, "prune", language, "notanumber");
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
                        GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
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
                        GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                        if (manager.player.getPlayingTrack() != null) manager.player.stopTrack();
                        manager.scheduler.manager.resetQueue();
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
                        GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                        manager.scheduler.manager.resetQueue();
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
                GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                ArdentMusicManager ardentMusicManager = manager.scheduler.manager;
                ArdentTrack nowPlaying = ardentMusicManager.getCurrentlyPlaying();
                if (nowPlaying != null) {
                    AudioTrack track = nowPlaying.getTrack();
                    AudioTrackInfo info = track.getInfo();

                    StringBuilder sb = new StringBuilder();
                    String queuedBy = getTranslation("music", language, "queuedby").getTranslation();

                    sb.append(info.title + ": " + info.author + " " + getCurrentTime
                            (track) +
                            "\n     *" + queuedBy + " " + ardent.jda.getUserById(nowPlaying.getAuthor())
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
                        GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
                        manager.scheduler.manager.shuffle();
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
                        GuildMusicManager manager = getGuildAudioPlayer(guild, channel);
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

                            GuildMusicManager guildMusicManager = getGuildAudioPlayer(guild, channel);
                            TrackScheduler trackScheduler = guildMusicManager.scheduler;
                            BlockingQueue<ArdentTrack> queue = trackScheduler.manager.getQueue();
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
                                    Iterator<ArdentTrack> trackIterator = queue.iterator();
                                    for (int i = 0; i < songsToLoop; i++) {
                                        ArdentTrack ardentTrack = trackIterator.next();
                                        tracksToLoop.add(ardentTrack.getTrack());
                                    }
                                    for (int i = 0; i < amountOfTimes; i++) {
                                        tracksToLoop.forEach(track -> {
                                            trackScheduler.manager.addToQueue(new ArdentTrack(user.getId(),
                                                    (TextChannel) channel, track.makeClone()));
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
                else sendRetrievedTranslation(channel, "music", language, "loopsyntaxhelp");
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
                        getGuildAudioPlayer(guild, channel).scheduler.manager.removeFrom(deleteFrom);
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
                GuildMusicManager musicManager = getGuildAudioPlayer(guild, channel);
                ArdentMusicManager player = musicManager.scheduler.manager;
                ArdentTrack current = player.getCurrentlyPlaying();
                if (current != null) {
                    if (GuildUtils.hasManageServerPermission(guild.getMember(user)) || user.getId().equalsIgnoreCase
                            (current.getAuthor()))
                    {
                        AudioTrack track = current.getTrack();
                        track.setPosition(0);
                        sendTranslatedMessage(getTranslation("music", language, "restartedtrack").getTranslation()
                                .replace("{0}", track.getInfo().title), channel);
                    }
                    else sendRetrievedTranslation(channel, "music", language, "queuedorhavepermissions");
                }
                else sendRetrievedTranslation(channel, "music", language, "notplayingrn");
            }
        });
        subcommands.add(new Subcommand(this, "resetplayer") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args,
                               Language language) throws Exception {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    AudioManager audioManager = guild.getAudioManager();
                    Member member = guild.getMember(user);
                    if (GuildUtils.hasManageServerPermission(member)) {
                        if (audioManager.isConnected()) {
                            audioManager.closeAudioConnection();
                            sendRetrievedTranslation(channel, "music", language, "resetplayer");
                        }
                        else sendRetrievedTranslation(channel, "music", language, "notinmusicchannel");
                    }
                    else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        });
    }
}
