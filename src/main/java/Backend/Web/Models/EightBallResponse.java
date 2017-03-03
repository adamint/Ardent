package Backend.Web.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Response used in 8ball parsing, holds a Magic object
 */
public class EightBallResponse {
    @SerializedName("magic")
    @Expose
    private Magic magic;

    public Magic getMagic() {
        return magic;
    }
}