package tk.ardentbot.core.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UrbanDictionary {
    @SerializedName("tags")
    @Expose
    private java.util.List<String> tags = new ArrayList<String>();
    @SerializedName("result_type")
    @Expose
    private String resultType;
    @SerializedName("list")
    @Expose
    private java.util.List<UDList> list = new ArrayList<UDList>();
    @SerializedName("sounds")
    @Expose
    private java.util.List<String> sounds = new ArrayList<String>();

    public UrbanDictionary() {}

    public UrbanDictionary(java.util.List<String> tags, String resultType, java.util.List<UDList> list, java.util.List<String> sounds) {
        this.tags = tags;
        this.resultType = resultType;
        this.list = list;
        this.sounds = sounds;
    }

    public java.util.List<UDList> getList() {
        return list;
    }

}
