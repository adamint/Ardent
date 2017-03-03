package tk.ardentbot.Utils.SQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static tk.ardentbot.Main.Ardent.conn;

public class DatabaseAction {
    private PreparedStatement preparedStatement = null;

    public DatabaseAction(String sql) throws SQLException {
        preparedStatement = conn.prepareStatement(sql);
    }

    public DatabaseAction set(int parameterIndex, Object o) throws SQLException {
        if (o instanceof String) {
            preparedStatement.setString(parameterIndex, (String) o);
        }
        else if (o instanceof Integer) {
            preparedStatement.setInt(parameterIndex, (Integer) o);
        }
        else if (o instanceof Timestamp) {
            preparedStatement.setTimestamp(parameterIndex, (Timestamp) o);
        }
        else if (o instanceof Long) {
            preparedStatement.setLong(parameterIndex, (Long) o);
        }
        else if (o instanceof Double) {
            preparedStatement.setDouble(parameterIndex, (Double) o);
        }
        return this;
    }

    public void update() throws SQLException {
        close();
    }

    public ResultSet request() throws SQLException {
        return preparedStatement.executeQuery();
    }

    public void close() throws SQLException {
        preparedStatement.close();
    }

}
