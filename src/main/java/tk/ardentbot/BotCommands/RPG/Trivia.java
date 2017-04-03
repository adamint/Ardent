package tk.ardentbot.BotCommands.RPG;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.CommandExecution.Subcommand;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.Translation.Translation;
import tk.ardentbot.Core.Translation.TranslationResponse;
import tk.ardentbot.Main.Shard;
import tk.ardentbot.Utils.Discord.GuildUtils;
import tk.ardentbot.Utils.Models.TriviaQuestion;
import tk.ardentbot.Utils.Models.TriviaResponse;
import tk.ardentbot.Utils.RPGUtils.TriviaGame;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static tk.ardentbot.Main.Ardent.shard0;

public class Trivia extends Command {
    public static final ArrayList<TriviaGame> gamesInSession = new ArrayList<>();

    public Trivia(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void dispatchRound(Guild guild, TextChannel channel, User creator, TriviaGame currentGame, Timer timer) {
        currentGame.incrementRounds();
        String url = null;
        switch (new SecureRandom().nextInt(4)) {
            case 0:
                url = "https://opentdb.com/api.php?amount=1&category=22";
                break; // Geography category
            case 1:
                url = "https://opentdb.com/api.php?amount=1&category=24";
                break; // Politics category
            case 2:
                url = "https://opentdb.com/api.php?amount=1";
                break; // Random category
            case 3:
                url = "https://opentdb.com/api.php?amount=1&category=17";
                break; // Science & Nature category
        }
        if (url != null) {
            try {
                Language language = GuildUtils.getLanguage(guild);
                Shard shard = GuildUtils.getShard(guild);
                currentGame.displayScores(shard, shard.help);

                String json = Unirest.get(url).asString().getBody();
                TriviaResponse triviaResponse = shard0.gson.fromJson(json, TriviaResponse.class);
                TriviaQuestion triviaQuestion = triviaResponse.getTriviaQuestions().get(0);
                currentGame.setCurrentTriviaQuestion(triviaQuestion);
                HashMap<Integer, TranslationResponse> translations = shard.help.getTranslations(language, new Translation
                        ("trivia", "trueorfalse"), new Translation("trivia", "multiplechoice"), new Translation("trivia",
                        "yourchoices"));

                StringBuilder question = new StringBuilder();
                if (triviaQuestion.getCategory().equalsIgnoreCase("boolean")) {
                    question.append("**" + translations.get(0).getTranslation() + "**");
                }
                else {
                    question.append("**" + translations.get(1).getTranslation() + "**");
                }
                question.append(": ").append(triviaQuestion.getQuestion());
                question.append("\n\n" + translations.get(2).getTranslation() + ": [" + triviaQuestion.listPossibleAnswers() + "]");
                shard.help.sendTranslatedMessage(question.toString(), channel, creator);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!currentGame.isAnsweredCurrentQuestion()) {
                            try {
                                shard.help.sendEditedTranslation("trivia", language, "failed", creator, channel, triviaQuestion
                                        .getCorrectAnswer());
                                shard.help.sendEmbed(currentGame.displayScores(shard, shard.help), channel, creator);
                                if (currentGame.getRound() + 1 < currentGame.getTotalRounds())
                                    dispatchRound(guild, channel, creator, currentGame, timer);
                                else currentGame.finish(shard, shard.help);
                            }
                            catch (Exception e) {
                                new BotException(e);
                            }
                        }
                    }
                }, 10000);
            }
            catch (Exception e) {
                new BotException(e);
                currentGame.decrementRounds();
                dispatchRound(guild, channel, creator, currentGame, timer);
            }
        }
        else {
            currentGame.decrementRounds();
            dispatchRound(guild, channel, creator, currentGame, timer);
        }
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendHelp(language, channel, guild, user, this);
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
                sendRetrievedTranslation(channel, "trivia", language, "soloornot", user);
                nextMessageByUser(language, channel, message, (soloMessage) -> {
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
                        return;
                    }
                    TriviaGame currentGame = new TriviaGame(user, solo, (TextChannel) channel, 15);
                    gamesInSession.add(currentGame);
                    sendRetrievedTranslation(channel, "trivia", language, "writeyouranswersthischannel", user);
                    commenceRounds(guild, (TextChannel) channel, user, currentGame);
                });
            }
        });
    }

    private void commenceRounds(Guild guild, TextChannel channel, User creator, TriviaGame currentGame) {
        Timer gameTimer = new Timer();
        currentGame.setTimer(gameTimer);
        dispatchRound(guild, channel, creator, currentGame, gameTimer);
    }
}
