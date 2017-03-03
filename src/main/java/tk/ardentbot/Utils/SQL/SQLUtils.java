package tk.ardentbot.Utils.SQL;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static tk.ardentbot.Main.Ardent.conn;

public class SQLUtils {
    public static String cleanString(String clean_string) {
        clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
        clean_string = clean_string.replaceAll("\\n", "\\\\n");
        clean_string = clean_string.replaceAll("\\r", "\\\\r");
        clean_string = clean_string.replaceAll("\\t", "\\\\t");
        clean_string = clean_string.replaceAll("\\00", "\\\\0");
        clean_string = clean_string.replaceAll("'", "\\\\'");
        clean_string = clean_string.replaceAll("\\\"", "\\\\\"");
        clean_string = clean_string.replace("$", "");
        return clean_string;
    }

    public static void sendStringUpdate(String preparedSql, String... parameters) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(preparedSql);
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setString((i + 1), parameters[i]);
        }
        preparedStatement.executeUpdate();
    }
}
