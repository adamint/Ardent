package tk.ardentbot.Rethink;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import tk.ardentbot.Main.Ardent;

public class Database {
    public static final RethinkDB r = RethinkDB.r;
    public static Connection connection;

    public static void setup() {
        // Reference a file in /root/Ardent/rethink
        connection = r.connection().hostname("db.ardentbot.tk").user("ardent", Ardent.dbPassword).db("data").connect();
    }
}
