package tk.ardentbot.Utils.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import tk.ardentbot.Utils.StringUtils;

public class TriviaQuestion {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("answer")
    @Expose
    private String answer;
    @SerializedName("question")
    @Expose
    private String question;
    @SerializedName("value")
    @Expose
    private Integer value;
    @SerializedName("airdate")
    @Expose
    private String airdate;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("game_id")
    @Expose
    private Object gameId;
    @SerializedName("invalid_count")
    @Expose
    private Object invalidCount;
    @SerializedName("category")
    @Expose
    private TriviaCategory category;

    public Integer getId() {
        return id;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestion() {
        return StringUtils.removeSideBrackets(StringUtils.removeBracketsParentheses(question)).replace("\"", "");
    }

    public Integer getValue() {
        return value;
    }

    public String getAirdate() {
        return airdate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Object getGameId() {
        return gameId;
    }

    public Object getInvalidCount() {
        return invalidCount;
    }

    public TriviaCategory getCategory() {
        return category;
    }

}