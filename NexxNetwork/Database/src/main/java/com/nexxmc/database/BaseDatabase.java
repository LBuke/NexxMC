package com.nexxmc.database;

import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * @author Luke Bingham
 */
public final class BaseDatabase extends DatabaseHandler {

    /** Singleton instance for this class */
    private static BaseDatabase instance;

    /** Private constructor as singleton's cannot be instantiated. */
    private BaseDatabase() {}

    /**
     * Get the singleton instance of this class.
     * <p>This allows you to call {@link #getConnection()}.</p>
     *
     * @return The instance of this database.
     */
    public static BaseDatabase getInstance() {
        if (instance == null) {
            instance = new BaseDatabase();
        }

        return instance;
    }

    public static boolean runCustomQuery(String query) {
        try (Connection connection = getInstance().getConnection()) {
            Preconditions.checkNotNull(connection);

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void createTables(Class<?> clazz, String fileName) {
        try (Scanner scanner = new Scanner(clazz.getResourceAsStream("/" + fileName + ".sql"))) {
            scanner.useDelimiter("(;(\r)?\n)|(--\n)");

            try (Connection connection = BaseDatabase.getInstance().getConnection()) {
                Preconditions.checkNotNull(connection);

                Statement statement = connection.createStatement();

                while (scanner.hasNext()) {
                    String line = scanner.next();
                    if (line.startsWith("/*!") && line.endsWith("*/")) {
                        int index = line.indexOf(' ');
                        line = line.substring(index + 1, line.length() - " */".length());
                    }

                    if (line.trim().length() > 0) {
                        statement.execute(line);
                    }
                }

                System.out.println("Database connection test successful.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
