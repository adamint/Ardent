package tk.ardentbot.Utils.RPGUtils;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.BotCommands.RPG.Trivia;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Discord.MessageUtils;
import tk.ardentbot.Utils.MapUtils;
import tk.ardentbot.Utils.Models.TriviaQuestion;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;

public class TriviaGame {
    @Getter
    private String guildId;
    @Getter
    private String textChannelId;
    @Getter
    private boolean solo;
    @Getter
    private String creator;
    @Getter
    private int totalRounds;
    @Getter
    private int round = 0;
    @Getter
    @Setter
    private TriviaQuestion currentTriviaQuestion = null;
    @Getter
    @Setter
    private boolean answeredCurrentQuestion = false;
    @Getter
    @Setter
    private Timer timer;

    private HashMap<String, Integer> scores = new HashMap<>();

    public TriviaGame(User user, boolean solo, TextChannel channel, int totalRounds) {
        this.creator = user.getId();
        this.solo = solo;
        guildId = channel.getGuild().getId();
        textChannelId = channel.getId();
        this.totalRounds = totalRounds;
    }

    public Map<String, Integer> getScores() {
        return MapUtils.sortByValue(scores);
    }

    public boolean incrementRounds() {
        if (round == totalRounds) return false;
        round++;
        answeredCurrentQuestion = false;
        return true;
    }

    public void decrementRounds() {
        round--;
        answeredCurrentQuestion = false;
    }

    public void finish(Shard shard, Command command) throws Exception {
        final int bonus = 250;
        final int perQuestion = 50;
        Trivia.gamesInSession.remove(this);
        displayScores(shard, command);
        Guild guild = shard.jda.getGuildById(guildId);
        Language language = GuildUtils.getLanguage(guild);
        TextChannel channel = guild.getTextChannelById(textChannelId);
        command.sendEditedTranslation("trivia", language, "thxforplayingpayout", guild.getSelfMember().getUser(), channel, String.valueOf
                (perQuestion), String.valueOf(bonus));
        Map<String, Integer> sorted = MapUtils.sortByValue(scores);
        Iterator<Map.Entry<String, Integer>> iterator = sorted.entrySet().iterator();
        int current = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            User user = guild.getMemberById(entry.getKey()).getUser();
            Profile profile = Profile.get(user);
            profile.addMoney(perQuestion * entry.getValue());
            if (current == 0 && !isSolo() && scores.size() > 1) {
                profile.addMoney(bonus);
                command.sendEditedTranslation("trivia", language, "youwonbonus", user, channel, user.getName(),
                        String.valueOf(bonus));
            }
            current++;
        }
    }

    public EmbedBuilder displayScores(Shard shard, Command command) throws Exception {
        Map<String, Integer> sorted = MapUtils.sortByValue(scores);
        Guild guild = shard.jda.getGuildById(guildId);
        Language language = GuildUtils.getLanguage(guild);
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild, guild.getSelfMember().getUser(), command);
        HashMap<Integer, TranslationResponse> translations = command.getTranslations(language, new Translation("trivia", "currentscores"),
                new Translation("trivia", "currentround"), new Translation("trivia", "points"), new Translation("trivia",
                        "noscoredyet"), new Translation("trivia", "currentround"));
        String currentScores = translations.get(0).getTranslation();
        builder.setAuthor(currentScores, shard.url, guild.getIconUrl());
        StringBuilder description = new StringBuilder();
        description.append("**" + currentScores + "**");
        Iterator<Map.Entry<String, Integer>> iterator = sorted.entrySet().iterator();
        int currentPlace = 1;
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            description.append("\n#" + currentPlace + ": **" + shard.jda.getUserById(entry.getKey()).getName() + "** " + entry.getValue()
                    + " " + translations.get(2).getTranslation());
            currentPlace++;
        }
        if (currentPlace == 1) {
            description.append("\n" + translations.get(3).getTranslation());
        }
        description.append("\n\n" + translations.get(4).getTranslation() + ": " + round);
        return builder.setDescription(description.toString());
    }

    public void addPoint(User user) {
        String id = user.getId();
        if (scores.containsKey(id)) {
            int oldScore = scores.get(id);
            scores.replace(id, oldScore, oldScore + 1);
        }
        else scores.put(id, 1);
    }
}
