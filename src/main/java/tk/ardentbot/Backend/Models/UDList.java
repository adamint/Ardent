package tk.ardentbot.Backend.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UDList {

    @SerializedName("definition")
    @Expose
    private String definition;
    @SerializedName("permalink")
    @Expose
    private String permalink;
    @SerializedName("thumbs_up")
    @Expose
    private int thumbsUp;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("word")
    @Expose
    private String word;
    @SerializedName("defid")
    @Expose
    private int defid;
    @SerializedName("current_vote")
    @Expose
    private String currentVote;
    @SerializedName("example")
    @Expose
    private String example;
    @SerializedName("thumbs_down")
    @Expose
    private int thumbsDown;

    public UDList() {}

    public UDList(String definition, String permalink, int thumbsUp, String author, String word, int defid, String currentVote, String example, int thumbsDown) {
        this.definition = definition;
        this.permalink = permalink;
        this.thumbsUp = thumbsUp;
        this.author = author;
        this.word = word;
        this.defid = defid;
        this.currentVote = currentVote;
        this.example = example;
        this.thumbsDown = thumbsDown;
    }

    public String getDefinition() {
        return definition;
    }
    public String getPermalink() {
        return permalink;
    }
    public int getThumbsUp() {
        return thumbsUp;
    }
    public String getAuthor() {
        return author;
    }
    public String getExample() {
        return example;
    }
    public int getThumbsDown() {
        return thumbsDown;
    }
}
