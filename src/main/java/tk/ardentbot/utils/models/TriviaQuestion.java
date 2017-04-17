package tk.ardentbot.utils.models;

import lombok.Getter;
import lombok.Setter;
import tk.ardentbot.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TriviaQuestion {
    @Getter
    @Setter
    private List<String> answers = new ArrayList<>();
    @Setter
    private String question;
    @Getter
    @Setter
    private String category;

    public String getQuestion() {
        return StringUtils.removeSideBrackets(StringUtils.removeBracketsParentheses(question)).replace("\"", "");
    }

    public TriviaQuestion withAnswer(String s) {
        answers.add(s);
        return this;
    }

}