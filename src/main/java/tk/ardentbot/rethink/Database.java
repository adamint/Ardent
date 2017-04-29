package tk.ardentbot.rethink;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import tk.ardentbot.main.Ardent;

public class Database {
    public static final RethinkDB r = RethinkDB.r;
    public static Connection connection;

    public static void setup() {
        connection = r.connection().hostname("db.ardentbot.tk").user("ardent", Ardent.dbPassword).db("data").connect();
        connection.use("data");
    }
}
