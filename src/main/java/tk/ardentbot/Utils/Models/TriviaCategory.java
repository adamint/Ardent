package tk.ardentbot.Utils.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.WordUtils;

public class TriviaCategory {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("clues_count")
    @Expose
    private Integer cluesCount;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return WordUtils.capitalizeFully(title);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Integer getCluesCount() {
        return cluesCount;
    }

}