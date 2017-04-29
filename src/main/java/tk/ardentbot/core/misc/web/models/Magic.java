package tk.ardentbot.core.misc.web.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds the answer in 8ball responses
 */
public class Magic {
    @SerializedName("question")
    @Expose
    private String question;
    @SerializedName("answer")
    @Expose
    private String answer;
    @SerializedName("type")
    @Expose
    private String type;

    public String getAnswer() {
        return answer;
    }

}