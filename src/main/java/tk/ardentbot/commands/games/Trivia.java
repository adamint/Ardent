package tk.ardentbot.commands.games;

import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.executor.Subcommand;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.core.translate.Language;
import tk.ardentbot.core.translate.Translation;
import tk.ardentbot.core.translate.TranslationResponse;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;
import tk.ardentbot.utils.models.TriviaQuestion;
import tk.ardentbot.utils.rpg.TriviaGame;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Trivia extends Command {
    public static final CopyOnWriteArrayList<TriviaGame> gamesInSession = new CopyOnWriteArrayList<>();
    public static final CopyOnWriteArrayList<String> gamesSettingUp = new CopyOnWriteArrayList<>();
    public static ArrayList<TriviaQuestion> triviaQuestions = new ArrayList<>();

    public Trivia(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void dispatchRound(Guild guild, TextChannel channel, User creator, TriviaGame currentGame, ScheduledExecutorService ex) {
        try {
            Shard shard = GuildUtils.getShard(guild);
            currentGame.incrementRounds();
            if (currentGame.getRound() >= currentGame.getTotalRounds()) {
                currentGame.finish(shard, shard.help);
                return;
            }
            if (!gamesInSession.contains(currentGame)) return;
            channel.sendMessage(currentGame.displayScores(shard, shard.help).build()).queue();
            TriviaQuestion triviaQuestion = triviaQuestions.get(new SecureRandom().nextInt(triviaQuestions.size()));
            currentGame.setCurrentTriviaQuestion(triviaQuestion);
            HashMap<Integer, TranslationResponse> translations = shard.help.getTranslations(language, new Translation
                    ("trivia", "trueorfalse"), new Translation("trivia", "multiplechoice"), new Translation("trivia",
                    "yourchoices"));

            StringBuilder question = new StringBuilder();
            question.append("**" + triviaQuestion.getCategory() + "**: " + triviaQuestion.getQuestion());
            shard.help.sendTranslatedMessage(question.toString(), channel, creator);

            int currentRound = currentGame.getRound();
            ex.schedule(() -> {
                if (!currentGame.isAnsweredCurrentQuestion() && currentRound == currentGame.getRound() && !(currentRound ==
                        currentGame.getTotalRounds()))
                {
                    try {
                        shard.help.sendEditedTranslation("trivia", language, "failed", creator, channel, triviaQuestion
                                .getAnswers().get(0));
                        if (currentGame.getRound() + 1 < currentGame.getTotalRounds())
                            dispatchRound(guild, channel, creator, currentGame, ex);
                        else currentGame.finish(shard, shard.help);
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                }
            }, 17, TimeUnit.SECONDS);
        }
        catch (Exception ignored) {
        }
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(channel, guild, user, this);
    }

    @Override
    public void setupSubcommands() throws Exception {
        subcommands.add(new Subcommand(this, "start") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                for (TriviaGame triviaGame : gamesInSession) {
                    if (triviaGame.getGuildId().equalsIgnoreCase(guild.getId())) {
                        sendRetrievedTranslation(channel, "trivia", language, "gameinsession", user);
                        return;
                    }
                }
                if (gamesSettingUp.contains(guild.getId())) {
                    sendRetrievedTranslation(channel, "trivia", language, "gameinsession", user);
                    return;
                }
                gamesSettingUp.add(guild.getId());
                sendRetrievedTranslation(channel, "trivia", language, "soloornot", user);
                interactiveOperation(language, channel, message, (soloMessage) -> {
                    String content = soloMessage.getContent();
                    boolean solo;
                    if (content.equalsIgnoreCase("yes")) {
                        solo = true;
                    }
                    else if (content.equalsIgnoreCase("no")) {
                        solo = false;
                    }
                    else {
                        sendRetrievedTranslation(channel, "trivia", language, "invalidyesorno", user);
                        gamesSettingUp.remove(guild.getId());
                        return;
                    }
                    TriviaGame currentGame = new TriviaGame(user, solo, (TextChannel) channel, 15);
                    gamesInSession.add(currentGame);
                    sendRetrievedTranslation(channel, "trivia", language, "writeyouranswersthischannel", user);
                    commenceRounds(guild, (TextChannel) channel, user, currentGame);
                    gamesSettingUp.remove(guild.getId());
                });
            }
        });

        subcommands.add(new Subcommand(this, "stop") {
            @Override
            public void onCall(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws
                    Exception {
                if (gamesInSession.stream().filter(game -> game.getGuildId().equals(guild.getId())).count() > 0 || gamesSettingUp
                        .contains(guild.getId()))
                {
                    gamesSettingUp.remove(guild.getId());
                    gamesInSession.removeIf(g -> g.getGuildId().equals(guild.getId()));
                    sendRetrievedTranslation(channel, "trivia", language, "stoppedtrivia", user);
                }
                else {
                    sendRetrievedTranslation(channel, "trivia", language, "notriviacurrently", user);
                }
            }
        });
    }

    private void commenceRounds(Guild guild, TextChannel channel, User creator, TriviaGame currentGame) {
        currentGame.setEx(Executors.newSingleThreadScheduledExecutor());
        dispatchRound(guild, channel, creator, currentGame, currentGame.getEx());
    }
}
