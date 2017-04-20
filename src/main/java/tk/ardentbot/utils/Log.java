package tk.ardentbot.utils;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Log {
    private String title;
    private ArrayList<String> features;
    private Timestamp timestamp;

    public Log(String title, ArrayList<String> features, Timestamp timestamp) {
        this.title = title;
        this.features = features;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }
    public ArrayList<String> getFeatures() {
        return features;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
}

