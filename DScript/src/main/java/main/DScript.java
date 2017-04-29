package main;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.IOUtils;
import tuples.Pair;
import utils.Scope;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class DScript {
    public HashMap<String, Pair<Long, Scope>> users = new HashMap<>();

    public DScript() {
    }

    public static void main(String[] args) throws UnirestException, IOException {
        final int[] i = {0};
        String json = IOUtils.toString(new FileInputStream(new File("C:\\Users\\AMR\\Desktop\\bots.txt")));
        Bot[] b = new Gson().fromJson(json, Bot[].class);
        ArrayList<Bot> bots = new ArrayList<>();
        bots.addAll(Arrays.asList(b));
        Collections.sort(bots, new Comparator<Bot>() {
            @Override
            public int compare(Bot o1, Bot o2) {
                if (o1.getAdditionalProperties().)
            }
        });
    }
    
   /* public Object sendScript(String user, String input) {
        Pair<Long, Scope> u = users.get(user);
        if (u != null) {
            if (u.getLeft() > System.currentTimeMillis() && u.getRight() != Scope.DONATOR) return Error.RATELIMITED;
            users.remove(user)';'
        }
        ((Object) input)
    }*/

}
