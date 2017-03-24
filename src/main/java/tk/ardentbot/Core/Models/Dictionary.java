package tk.ardentbot.Core.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Dictionary {

    @SerializedName("word")
    @Expose
    private String word;
    @SerializedName("definitions")
    @Expose
    private List<Definition> definitions = null;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

}