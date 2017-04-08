package tk.ardentbot.BotCommands.RPG;

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
import tk.ardentbot.Utils.RPGUtils.TriviaGame;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static tk.ardentbot.Main.Ardent.triviaSheet;

public class Trivia extends Command {
    public static final CopyOnWriteArrayList<TriviaGame> gamesInSession = new CopyOnWriteArrayList<>();

    public Trivia(CommandSettings commandSettings) {
        super(commandSettings);
    }

    public static void dispatchRound(Guild guild, TextChannel channel, User creator, TriviaGame currentGame, Timer timer) {
        try {
            Language language = GuildUtils.getLanguage(guild);
            Shard shard = GuildUtils.getShard(guild);
            currentGame.incrementRounds();
            channel.sendMessage(currentGame.displayScores(shard, shard.help).build()).queue();
            List<List<Object>> values = triviaSheet.getValues();
            List<Object> row = values.get(new SecureRandom().nextInt(values.size()));
            String category = (String) row.get(0);
            String q = (String) row.get(1);
            String answerUnparsed = (String) row.get(2);
            TriviaQuestion triviaQuestion = new TriviaQuestion();
            triviaQuestion.setQuestion(q);
            triviaQuestion.setCategory(category);
            for (String answer : answerUnparsed.split("~")) {
                triviaQuestion.withAnswer(answer);
            }
            currentGame.setCurrentTriviaQuestion(triviaQuestion);
            HashMap<Integer, TranslationResponse> translations = shard.help.getTranslations(language, new Translation
                    ("trivia", "trueorfalse"), new Translation("trivia", "multiplechoice"), new Translation("trivia",
                    "yourchoices"));

            StringBuilder question = new StringBuilder();
            question.append("**" + triviaQuestion.getCategory() + "**: " + triviaQuestion.getQuestion());
            shard.help.sendTranslatedMessage(question.toString(), channel, creator);

            int currentRound = currentGame.getRound();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!currentGame.isAnsweredCurrentQuestion() && currentRound == currentGame.getRound()) {
                        try {
                            shard.help.sendEditedTranslation("trivia", language, "failed", creator, channel, triviaQuestion
                                    .getAnswers().get(0));
                            if (currentGame.getRound() + 1 < currentGame.getTotalRounds())
                                dispatchRound(guild, channel, creator, currentGame, timer);
                            else currentGame.finish(shard, shard.help);
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    }
                }
            }, 17000);
        }
        catch (Exception e) {
            new BotException(e);
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
