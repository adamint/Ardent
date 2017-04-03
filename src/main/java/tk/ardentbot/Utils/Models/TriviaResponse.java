package tk.ardentbot.Utils.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TriviaResponse {
    @SerializedName("response_code")
    @Expose
    private Integer responseCode;
    @SerializedName("triviaQuestions")
    @Expose
    private List<TriviaQuestion> triviaQuestions = null;

    public Integer getResponseCode() {
        return responseCode;
    }

    public List<TriviaQuestion> getTriviaQuestions() {
        return triviaQuestions;
    }
}