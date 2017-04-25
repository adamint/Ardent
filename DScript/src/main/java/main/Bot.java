package main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot {
    public String userId;
    public String inviteUrl;
    public String clientId;
    public String description;
    public String library;
    public String name;
    public List<String> ownerIds = null;
    public String prefix;
    public String website;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
