package tk.ardentbot.Utils.Searching;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class SearchResult {
    private String title;
    private String content;
    private String url;

    public static SearchResult fromGoogle(JSONObject googleResult) {
        SearchResult result = new SearchResult();
        result.title = cleanString(googleResult.getString("title"));
        result.content = cleanString(googleResult.getString("snippet"));
        try {
            result.url = URLDecoder.decode(cleanString(googleResult.getString("link")), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String cleanString(String uncleanString) {
        return StringEscapeUtils.unescapeJava(
                StringEscapeUtils.unescapeHtml4(
                        uncleanString
                                .replaceAll("\\s+", " ")
                                .replaceAll("\\<.*?>", "")
                                .replaceAll("\"", "")));
    }

    public String getTitle() {
        return content;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public String getSuggestedReturn() {
        return url + " - *" + title + "*: \"" + content + "\"";
    }
}