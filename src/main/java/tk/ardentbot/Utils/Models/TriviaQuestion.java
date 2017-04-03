package tk.ardentbot.Utils.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import tk.ardentbot.Utils.Discord.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriviaQuestion {

    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("difficulty")
    @Expose
    private String difficulty;
    @SerializedName("question")
    @Expose
    private String question;
    @SerializedName("correct_answer")
    @Expose
    private String correctAnswer;
    @SerializedName("incorrect_answers")
    @Expose
    private List<String> incorrectAnswers = null;

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public String listPossibleAnswers() {
        ArrayList<String> answers = new ArrayList<>();
        answers.addAll(incorrectAnswers);
        answers.add(correctAnswer);
        Collections.shuffle(answers);
        return MessageUtils.listWithCommas(answers);
    }

}