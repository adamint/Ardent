package tk.ardentbot.Utils.SQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static tk.ardentbot.Main.Ardent.conn;

public class DatabaseAction {
    private PreparedStatement preparedStatement = null;
    private int currentPlace = 1;

    public DatabaseAction(String sql) throws SQLException {
        preparedStatement = conn.prepareStatement(sql);
    }

    public DatabaseAction set(Object o) throws SQLException {
        if (o instanceof String) {
            preparedStatement.setString(currentPlace, (String) o);
        }
        else if (o instanceof Integer) {
            preparedStatement.setInt(currentPlace, (Integer) o);
        }
        else if (o instanceof Timestamp) {
            preparedStatement.setTimestamp(currentPlace, (Timestamp) o);
        }
        else if (o instanceof Long) {
            preparedStatement.setLong(currentPlace, (Long) o);
        }
        else if (o instanceof Double) {
            preparedStatement.setDouble(currentPlace, (Double) o);
        }
        currentPlace++;
        return this;
    }

    public void update() throws SQLException {
        preparedStatement.executeUpdate();
        close();
    }

    public ResultSet request() throws SQLException {
        return preparedStatement.executeQuery();
    }

    public void close() throws SQLException {
        preparedStatement.close();
    }

}
