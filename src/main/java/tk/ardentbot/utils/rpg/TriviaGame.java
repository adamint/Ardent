package tk.ardentbot.utils.rpg;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.commands.games.Trivia;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.MapUtils;
import tk.ardentbot.utils.discord.MessageUtils;
import tk.ardentbot.utils.models.TriviaQuestion;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

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
    private ScheduledExecutorService ex;

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
        totalRounds = 9001;
        final int bonus = 250;
        final int perQuestion = 50;
        Trivia.gamesInSession.remove(this);
        Trivia.gamesSettingUp.remove(guildId);
        Guild guild = shard.jda.getGuildById(guildId);
        TextChannel channel = guild.getTextChannelById(textChannelId);
        channel.sendMessage(displayScores(shard, command).build()).queue();
        channel.sendMessage("Thanks for playing! You'll receive **$50** for every correct answer").queue();
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
                channel.sendMessage(user.getAsMention() + ", you won a **$250** bonus for being so smart!").queue();
            }
            current++;
        }
    }

    public EmbedBuilder displayScores(Shard shard, Command command) throws Exception {
        Map<String, Integer> sorted = MapUtils.sortByValue(scores);
        Guild guild = shard.jda.getGuildById(guildId);
        EmbedBuilder builder = MessageUtils.getDefaultEmbed(guild.getSelfMember().getUser());
        String currentScores = "Current Scores";
        if (solo) builder.setAuthor(currentScores + " | Solo Game", shard.url, guild.getIconUrl());
        else builder.setAuthor(currentScores + " | Everyone can play", shard.url, guild.getIconUrl());
        StringBuilder description = new StringBuilder();
        description.append("**" + currentScores + "**");
        Iterator<Map.Entry<String, Integer>> iterator = sorted.entrySet().iterator();
        int currentPlace = 1;
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            description.append("\n#" + currentPlace + ": **" + shard.jda.getUserById(entry.getKey()).getName() + "** " + entry.getValue()
                    + " points");
            currentPlace++;
        }
        if (currentPlace == 1) {
            description.append("\nNo one has gotten a question right so far!");
        }
        description.append("\n\nCurrent Round: " + round);
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
